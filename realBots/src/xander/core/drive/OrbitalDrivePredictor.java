package xander.core.drive;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.log.Log;
import xander.core.log.Logger;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.math.VelocityVector;
import xander.core.track.Snapshot;
import xander.core.track.Wave;

public class OrbitalDrivePredictor {
	
	private static final Log log = Logger.getLog(OrbitalDrivePredictor.class);
	
	private RobotProxy robotProxy;
	private double wallStick = 180d;
	private Path2D.Double driveBounds;
	
	public OrbitalDrivePredictor() {
		this.robotProxy = Resources.getRobotProxy();
		this.driveBounds = DriveBoundsFactory.getRectangularBounds(robotProxy.getBattleFieldSize());
	}
	
	public OrbitalDrivePredictor(Path2D.Double driveBounds) {
		this.robotProxy = Resources.getRobotProxy();
		this.driveBounds = driveBounds;
	}
	
	public void setWallStick(double wallStick) {
		this.wallStick = wallStick;
	}
	
	/**
	 * Returns wall-smoothed orbit angle without any center-point reference.
	 * This results in the robot driving around the edges of the battlefield.
	 * 
	 * @param targetSpeed   target speed (always positive)
	 * 
	 * @return              wall smoothed orbit angle, continued from previous smoothing
	 */
	public VelocityVector getSmoothedOrbitAngle(double targetSpeed) {
		double heading = robotProxy.getHeadingDegrees();
		if (robotProxy.getVelocity() < 0) {
			heading = RCMath.normalizeDegrees(heading + 180); // back-as-front
		}
		return getSmoothedOrbitAngle(heading, targetSpeed);
	}
	
	/**
	 * Returns wall-smoothed orbit angle without any center-point reference
	 * and in the preferred direction.  This results in the robot driving 
	 * in the preferred direction smoothing around the edges of the battlefield.
	 * 
	 * @param targetAngle   preferred direction to drive
	 * @param targetSpeed   target speed (always positive)
	 * 
	 * @return              wall smoothed orbit angle, continued from previous smoothing
	 */
	public VelocityVector getSmoothedOrbitAngle(double targetAngle, double targetSpeed) {
		// first, find the general orbital direction we are headed in
		double angleToCenter = RCMath.getRobocodeAngleToCenter(
				robotProxy.getX(), robotProxy.getY(), robotProxy.getBattleFieldSize());

		double offsetFromCenter = RCMath.getTurnAngle(angleToCenter, targetAngle);
		Direction direction = (offsetFromCenter >= 0)? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
		// now that we have direction, pick a center such that we would just drive straight
		// this lets the wall stick do all the driving
		double innerAngle = RCMath.normalizeDegrees(targetAngle + direction.getDirectionUnit() * 90);
		Point2D.Double innerCenter = RCMath.getLocation(
				robotProxy.getX(), robotProxy.getY(), 1000, innerAngle);
		return getSmoothedOrbitAngle(innerCenter, 100, direction, DistancingEquation.NO_ADJUST, targetSpeed);
	}
	
	/**
	 * Returns current orbital direction around the given point.
	 * 
	 * @param x     orbit center x-coordinate
	 * @param y     orbit center y-coordinate
	 * 
	 * @return      current orbital direction
	 */
	public Direction getOribitalDirection(double x, double y) {
		double angleToCenter = RCMath.getRobocodeAngle(
				robotProxy.getX(), robotProxy.getY(), x, y);
		double heading = robotProxy.getHeadingDegrees();
		if (robotProxy.getVelocity() < 0) {
			heading = RCMath.normalizeDegrees(heading + 180); // back-as-front
		}
		double offsetFromCenter = RCMath.getTurnAngle(angleToCenter, heading);
		return (offsetFromCenter >= 0)? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;	
	}
	
	/**
	 * Returns current orbital direction around the given point.
	 * 
	 * @param x          orbit center x-coordinate
	 * @param y          orbit center y-coordinate
	 * @param rx         robot x-coordinate
	 * @param ry         robot y-coordinate
	 * @param velocity   robot velocity
	 * @param heading    robot heading in degrees
	 * 
	 * @return           current orbital direction
	 */
	public static Direction getOribitalDirection(double x, double y, double rx, double ry, double velocity, double heading) {
		double angleToCenter = RCMath.getRobocodeAngle(
				rx, ry, x, y);
		if (velocity < 0) {
			heading = RCMath.normalizeDegrees(heading + 180); // back-as-front
		}
		double offsetFromCenter = RCMath.getTurnAngle(angleToCenter, heading);
		return (offsetFromCenter >= 0)? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;	
	}
	
	/**
	 * Returns wall-smoothed orbit angle from current robot position.
	 * 
	 * @param center          center of orbit
	 * @param distance        distance from opponent
	 * @param direction       direction of orbit
	 * @param distancingEquation  distancing equation
	 * @param targetSpeed     target speed (always positive)
	 * 
	 * @return                wall smoothed orbit angle and speed
	 */
	public VelocityVector getSmoothedOrbitAngle(Point2D.Double center, double distance, Direction direction, DistancingEquation distancingEquation, double targetSpeed) {
		Point2D.Double position = new Point2D.Double(robotProxy.getX(), robotProxy.getY());
		return getSmoothedOrbitAngle(center, distance, position, direction, distancingEquation, targetSpeed);
	}
	
	/**
	 * Returns wall-smoothed orbit angle from arbitrary position.
	 * 
	 * @param center          center of orbit
	 * @param distance        distance from opponent (used for distancing)
	 * @param position        arbitrary robot position
	 * @param direction       direction of orbit
	 * @param distancingEquation equation used to alter the orbit path to distance ourselves from enemy
	 * @param targetSpeed     target speed (always positive)
	 * 
	 * @return                wall smoothed orbit angle
	 */
	public VelocityVector getSmoothedOrbitAngle(Point2D.Double center, double distance, Point2D.Double position, Direction direction, DistancingEquation distancingEquation, double targetSpeed) {
		double facingAngle = RCMath.getRobocodeAngle(
				position.x, position.y, center.x, center.y);
		double tangentAngle = RCMath.normalizeDegrees(facingAngle - 90 * direction.getDirectionUnit());
		//double distanceToCenter = RCMath.getDistanceBetweenPoints(position.x, position.y, center.x, center.y);
		double retreatAngle = RCMath.normalizeDegrees(tangentAngle - distancingEquation.getAdjustAngle(distance, null) * direction.getDirectionUnit());
		if (Math.abs(RCMath.getTurnAngle(tangentAngle, retreatAngle)) > 90) {
			log.warn("Distancing equation causing drive angle out of proper range.  Unmodified tangent heading is " + Logger.format(tangentAngle) + "; Distancing heading is " + Logger.format(retreatAngle) + "; Distancing angle should not be more than 90 degrees offset from Tangent.");
		}
		return getWallStickSmoothedOrbitVector(position, direction, retreatAngle, targetSpeed);
	}
	
	public VelocityVector getWallStickSmoothedOrbitVector(Point2D.Double position, Direction direction, double desiredHeading, double desiredSpeed) {
		double smoothedAngle = desiredHeading;
		Point2D.Double advancedPosition = RCMath.getLocation(position.x, position.y, wallStick, desiredHeading);
		boolean inBounds = driveBounds.contains(advancedPosition);
		//boolean inBounds = BoundingBox.inBounds(advancedPosition.x, advancedPosition.y, driveBounds);
		// note: this can result in infinite loop if wall stick is made absurdly large
		while (!inBounds) {
			smoothedAngle = RCMath.normalizeDegrees(smoothedAngle + direction.getDirectionUnit()*2);   // adjust by 2 degrees
			advancedPosition = RCMath.getLocation(position.x, position.y, wallStick, smoothedAngle);
			inBounds = driveBounds.contains(advancedPosition);
			//inBounds = BoundingBox.inBounds(advancedPosition.x, advancedPosition.y, driveBounds);
		}
		return new VelocityVector(smoothedAngle, (float)desiredSpeed);		
	}
	
	/**
	 * Returns whether or not robot will overshoot it's target orbital position if
	 * it immediately attempts to stop.  Note that this will not indicate if the
	 * robot would stop too soon; only if it would stop too late.
	 * 
	 * @param center           orbit center
	 * @param distance         distance from opponent
	 * @param direction        drive direction
	 * @param targetHeadingFromCenterDegrees   target orbital position or factor
	 * @param wave             bullet wave
	 * @param distancingEquation distancing equation
	 * 
	 * @return    whether or not robot will overshoot it's target position.
	 */
	public boolean isOvershoot(Point2D.Double center, double distance, 
			Direction direction, double targetHeadingFromCenterDegrees, 
			Wave wave, DistancingEquation distancingEquation) {		
		DriveState driveState = new DriveState(robotProxy);
		long time = robotProxy.getTime();
		long tup = wave.getTimeUntilHit(
				driveState.position.x, driveState.position.y, time);
		while (tup > 0 && driveState.velocity != 0) {
			advanceOrbitalDriveState(driveState, center, distance, direction, 1, 0, distancingEquation);
			time++;
			tup = wave.getTimeUntilHit(driveState.position.x, driveState.position.y, time);
		}
		double headingFromCenter = RCMath.getRobocodeAngle(
				center.x, center.y, driveState.position.x, driveState.position.y);
		double offset = RCMath.getTurnAngle(
				targetHeadingFromCenterDegrees, headingFromCenter);
		if (direction == Direction.CLOCKWISE) {
			return offset > 0;
		} else {
			return offset < 0;
		}
	}
	
	/**
	 * Predict a future drive state from a given drive state for a given number
	 * of ticks into the future for an orbital drive strategy of the given
	 * parameters.  This will update the provided drive state; if you need to
	 * keep the original drive state, make a copy of it before calling this
	 * method.
	 * 
	 * @param driveState        drive state to advance
	 * @param center            orbit center
	 * @param direction         orbit direction
	 * @param ticks             ticks into the future
	 * @param targetSpeed       target speed not to exceed
	 * @param retreatEquation   retreat equation
	 */
	public void advanceOrbitalDriveState(DriveState driveState,
			Point2D.Double center, double distance, Direction direction, 
			int ticks, double targetSpeed, DistancingEquation distancingEquation) {
		for (int i=0; i<ticks; i++) {
			// calculate desired heading
			VelocityVector smoothedVector = getSmoothedOrbitAngle(
					center, distance, driveState.position, 
					direction, distancingEquation, targetSpeed);
			double targetHeading = smoothedVector.getRoboAngle();
			double targetVelocity = smoothedVector.getMagnitude();
			double targetTurnAngle = RCMath.getTurnAngle(driveState.heading, targetHeading);
			if (Math.abs(targetTurnAngle) > 90) {
				targetHeading = RCMath.normalizeDegrees(targetHeading+180);
				targetVelocity = -targetSpeed;
				targetTurnAngle = RCMath.getTurnAngle(driveState.heading, targetHeading);
			}
			// calculate possible change for this tick
			// based on order of action described in the Robowiki, it should be
			// proper to do turn rate calc before velocity change.
			double turnThisTick = targetTurnAngle;
			double maxTurnRate = RCPhysics.getMaxTurnRate(driveState.getVelocity());
			if (targetTurnAngle > maxTurnRate) {
				turnThisTick = maxTurnRate;
			} else if (targetTurnAngle < -maxTurnRate) {
				turnThisTick = -maxTurnRate;
			}
			double velocityChangeThisTick = targetVelocity - driveState.velocity;
			double maxChangeRate = RCPhysics.ACCELERATION_RATE;
			if (driveState.velocity > 0 && targetVelocity < driveState.velocity 
					|| driveState.velocity < 0 && targetVelocity > driveState.velocity) {
				maxChangeRate = -RCPhysics.DECELERATION_RATE;
			}
			if (Math.abs(velocityChangeThisTick) > maxChangeRate) {
				velocityChangeThisTick = (velocityChangeThisTick > 0)? maxChangeRate : -maxChangeRate;
			}
			// update heading, velocity, and position
			driveState.heading += turnThisTick;
			driveState.velocity += velocityChangeThisTick;
			driveState.position = RCMath.getLocation(
					driveState.position.x, driveState.position.y, 
					driveState.velocity, driveState.heading);
			driveState.time += 1;
		}
	}
	
	/**
	 * Predict how far a robot can get in the given direction before the given bullet wave 
	 * will hit them.
	 * 
	 * @param robotSnapshot              robot to make prediction for
	 * @param direction                  direction of robot orbit
	 * @param bulletOriginX              x-coordinate origin of bullet
	 * @param bulletOriginY              y-coordinate origin of bullet
	 * @param currentBulletFlightTime    time bullet has been in the air
	 * @param bulletVelocity             velocity of bullet
	 * @param distance                   distance robot is from opponent (or from point used for distancing)
	 * @param distancingEquation         equation used for distancing
	 * 
	 * @return                           drive prediction for robot
	 */
	public DrivePrediction predictMaxPathBeforeBulletHit(
			Snapshot robotSnapshot, Direction direction, 
			double bulletOriginX, double bulletOriginY, long currentBulletFlightTime, 
			double bulletVelocity, double distance, DistancingEquation distancingEquation,
			double[] velocityConstraints) {
		List<Point2D.Double> drivePath = new ArrayList<Point2D.Double>();
		DriveState driveState = new DriveState(robotSnapshot);
		drivePath.add(new Point2D.Double(driveState.getPosition().x, driveState.getPosition().y));
		Point2D.Double origin = new Point2D.Double(bulletOriginX, bulletOriginY);
		double bulletTravelDistance = bulletVelocity * currentBulletFlightTime;
		double distanceToRobot = RCMath.getDistanceBetweenPoints(
				origin.getX(), origin.getY(), 
				driveState.getPosition().x, driveState.getPosition().y) - RCPhysics.ROBOT_HALF_WIDTH;
		int vcidx = 0;
		while (distanceToRobot > bulletTravelDistance) {
			bulletTravelDistance += bulletVelocity;
			double cspeed = RCPhysics.MAX_SPEED;
			Direction cdirection = direction; 
			if (velocityConstraints != null && vcidx < velocityConstraints.length) {
				cspeed = Math.abs(velocityConstraints[vcidx]);
				if (velocityConstraints[vcidx] < 0) {
					cdirection = cdirection.reverse();
				}
				vcidx++;
			}
			advanceOrbitalDriveState(driveState, origin, distance, 
					cdirection, 1, cspeed, distancingEquation);
			drivePath.add(new Point2D.Double(driveState.getPosition().x, driveState.getPosition().y));
			distanceToRobot = RCMath.getDistanceBetweenPoints(
					origin.getX(), origin.getY(), 
					driveState.getPosition().x, driveState.getPosition().y) - RCPhysics.ROBOT_HALF_WIDTH;
		}
		return new DrivePrediction(driveState, drivePath);
	}
}
