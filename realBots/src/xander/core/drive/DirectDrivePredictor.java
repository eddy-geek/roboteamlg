package xander.core.drive;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.track.Wave;

public class DirectDrivePredictor {

	private Rectangle2D.Double battlefieldBounds;
	private Path2D.Double driveBounds;
	private double lastHeading = 370, cosPheta = 0, sinPheta = 0;  // used to cache trig values on advanceDriveState 
	
	public DirectDrivePredictor(Rectangle2D.Double battlefieldBounds, Path2D.Double driveBounds) {
		if (battlefieldBounds == null) {
			throw new IllegalArgumentException("Battlefield Bounds cannot be null");
		}
		if (driveBounds == null) {
			throw new IllegalArgumentException("Drive Bounds cannot be null");
		}
		this.battlefieldBounds = battlefieldBounds;
		this.driveBounds = driveBounds;
	}
	
	public Path2D.Double getDriveBounds() {
		return driveBounds;
	}
	
	/**
	 * Predict path towards target heading and speed until a bullet wave hits or
	 * robot has to stop to avoid hitting wall. 
	 * 
	 * @param wave             bullet wave
	 * @param driveState       initial drive state
	 * @param targetHeading    desired heading
	 * @param targetSpeed      desired max speed
	 * @param currentTime      current time
	 * 
	 * @return                 drive prediction
	 */
	public DrivePrediction predictPathUntilWaveHits(
			Wave wave, DriveState driveState, 
			double targetHeading, double targetSpeed, long currentTime) {
		driveState = new DriveState(driveState);  // use a copy to avoid possible shared reference issues
		long time = currentTime;
		double adjustedTargetSpeed = targetSpeed;
		long tup = wave.getTimeUntilHit(driveState.getPosition().x, driveState.getPosition().y, time);
		List<Point2D.Double> drivePath = new ArrayList<Point2D.Double>();
		drivePath.add(new Point2D.Double(driveState.position.x, driveState.position.y));
		while (tup > 0 && (adjustedTargetSpeed > 0 || Math.abs(driveState.velocity) > 0)) {
			if (shouldStop(driveState, targetHeading, targetSpeed)) {
				adjustedTargetSpeed = 0;
			} else {
				adjustedTargetSpeed = targetSpeed;
			}
			advanceDriveState(driveState, targetHeading, adjustedTargetSpeed);
			drivePath.add(new Point2D.Double(driveState.position.x, driveState.position.y));
			time++;
			tup = wave.getTimeUntilHit(driveState.position.x, driveState.position.y, time);
		}
		return new DrivePrediction(driveState, drivePath);
	}
	
	/**
	 * Predict drive state towards target heading and speed until a bullet wave hits or
	 * robot has to stop to avoid hitting wall. 
	 * 
	 * @param wave             bullet wave
	 * @param driveState       initial drive state
	 * @param targetHeading    desired heading
	 * @param targetSpeed      desired max speed
	 * @param currentTime      current time
	 * 
	 * @return                 drive prediction
	 */
	public DriveState predictDriveStateUntilWaveHits(
			Wave wave, DriveState driveState, 
			double targetHeading, double targetSpeed, long currentTime) {
		driveState = new DriveState(driveState);  // use a copy to avoid possible shared reference issues
		long time = currentTime;
		double adjustedTargetSpeed = targetSpeed;
		long tup = wave.getTimeUntilHit(driveState.getPosition().x, driveState.getPosition().y, time);
		while (tup > 0 && (adjustedTargetSpeed > 0 || Math.abs(driveState.velocity) > 0)) {
			if (shouldStop(driveState, targetHeading, targetSpeed)) {
				adjustedTargetSpeed = 0;
			} else {
				adjustedTargetSpeed = targetSpeed;
			}
			advanceDriveState(driveState, targetHeading, adjustedTargetSpeed);
			time++;
			tup = wave.getTimeUntilHit(driveState.position.x, driveState.position.y, time);
		}
		return driveState;
	}
	
	/**
	 * Returns whether or not a robot should stop (or decelerate, to be more precise) to reach 
	 * the target factor angle.  This does NOT check to ensure robot stays within bounds.
	 * 
	 * @param wave                bullet wave
	 * @param targetFactorAngle   target factor angle
	 * @param driveState          initial drive state
	 * @param targetHeading       target heading
	 * @param targetSpeed         target speed
	 * @param currentTime         current time
	 * 
	 * @return       whether or not a robot should stop to reach the target factor angle before a wave hits.   
	 */
	public boolean shouldStop(Wave wave, double targetFactorAngle, 
			DriveState driveState, double targetHeading, double targetSpeed, long currentTime) {

		// compare stopping now with stopping after next tick and see which gets us closer
		
		// try stopping now
		DriveState testDriveState = new DriveState(driveState);  // use a copy to avoid possible shared reference issues
		long time = currentTime;
		double speed = Math.abs(testDriveState.velocity);
		long tup = wave.getTimeUntilHit(testDriveState.getPosition().x, testDriveState.getPosition().y, time);
		while (speed > 0 && tup > 0) {
			advanceDriveState(testDriveState, targetHeading, 0);
			time++;
			speed = Math.abs(testDriveState.velocity);
			tup = wave.getTimeUntilHit(testDriveState.getPosition().x, testDriveState.getPosition().y, time);
		}
		double defenderBearing = RCMath.getRobocodeAngle(wave.getOrigin(), testDriveState.position); 
		double stopNowFactorAngle = RCMath.getTurnAngle(wave.getInitialDefenderBearing(), defenderBearing);
		
		//try stopping after next tick
		testDriveState = new DriveState(driveState);
		time = currentTime;
		tup = wave.getTimeUntilHit(testDriveState.getPosition().x, testDriveState.getPosition().y, time);
		if (tup > 0) {
			advanceDriveState(testDriveState, targetHeading, targetSpeed);
			time++;
		}
		speed = Math.abs(testDriveState.velocity);
		tup = wave.getTimeUntilHit(testDriveState.getPosition().x, testDriveState.getPosition().y, time);
		while (speed > 0 && tup > 0) {
			advanceDriveState(testDriveState, targetHeading, 0);
			time++;
			speed = Math.abs(testDriveState.velocity);
			tup = wave.getTimeUntilHit(testDriveState.getPosition().x, testDriveState.getPosition().y, time);
		}		
		defenderBearing = RCMath.getRobocodeAngle(wave.getOrigin(), testDriveState.position); 
		double stopNextTickFactorAngle = RCMath.getTurnAngle(wave.getInitialDefenderBearing(), defenderBearing);
		
		// see which got us closer to the target factor angle
		double stopNowFactorAngleDiff = RCMath.getTurnAngle(targetFactorAngle, stopNowFactorAngle);
		double stopNextTickFactorAngleDiff = RCMath.getTurnAngle(targetFactorAngle, stopNextTickFactorAngle);
		return Math.abs(stopNowFactorAngleDiff) < Math.abs(stopNextTickFactorAngleDiff);		
	}
	
	private boolean beyondWallLimits(Point2D.Double position) {
		double hardLimit = (RCPhysics.ROBOT_HALF_WIDTH)-3;
		return ((position.x < battlefieldBounds.getMinX() + hardLimit)
				|| (position.x > battlefieldBounds.getMaxX() - hardLimit)
				|| (position.y < battlefieldBounds.getMinY() + hardLimit) 
				|| (position.y > battlefieldBounds.getMaxY() - hardLimit));
	}
	
	/**
	 * Returns whether or not robot should stop to avoid going outside a given drive bounds.
	 * Robot is allowed to proceed outside of drive bounds so long as it's course will take
	 * it back in bounds without hitting a wall.
	 * 
	 * @param driveState      initial drive state
	 * @param targetHeading   target heading
	 * @param targetSpeed     target speed
	 * 
	 * @return    whether or not robot should stop to avoid going outside a given drive bounds.
	 */
	public boolean shouldStop(DriveState driveState, double targetHeading, double targetSpeed) {
		driveState = new DriveState(driveState);  // use a copy to avoid possible shared reference issues
		double speed = Math.abs(driveState.velocity);
		boolean initialInBounds = driveBounds.contains(driveState.position);
		if (initialInBounds) {
			// have robot continue for 1 tick before stopping; this will ensure we stop in time 
			advanceDriveState(driveState, targetHeading, targetSpeed);
			// now continue until stopped and check if out of bounds
			while (speed > 0) {
				advanceDriveState(driveState, targetHeading, 0);	
				speed = Math.abs(driveState.velocity);
			}
			boolean stoppedInBounds = driveBounds.contains(driveState.position);
			if (stoppedInBounds) {
				return false;
			} 
		}
		// robot either started out of bounds or drifted out of bounds while stopping
		// check if continuing in target heading would take us back in bounds
		// only actually check every 4 ticks to reduce the overhead of bounds checking
		for (int i=1; i<13; i++) {  // arbitrarily trying 12 ticks
			advanceDriveState(driveState, targetHeading, RCPhysics.MAX_SPEED);
			if (i % 4 == 0) {
				if (beyondWallLimits(driveState.position)) {
					return true;
				}
				if (driveBounds.contains(driveState.position)) {
					return false;
				}
			}
		}
		// continuing in target heading does not help, so robot should definitely stop
		return true;
	}
	
	/**
	 * Advance the given drive state 1 tick into the future with the given target
	 * heading and speed.
	 * 
	 * @param driveState         drive state to be advanced
	 * @param targetHeading      target heading
	 * @param targetSpeed        target speed
	 */
	public void advanceDriveState(DriveState driveState, double targetHeading, double targetSpeed) {
		double targetVelocity = targetSpeed;
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
		
		if (driveState.heading != lastHeading) {
			double pheta = Math.toRadians(RCMath.convertDegrees(driveState.heading));
			cosPheta = Math.cos(pheta);
			sinPheta = Math.sin(pheta);
			lastHeading = driveState.heading;
		}
		driveState.position.x += driveState.velocity * cosPheta;
		driveState.position.y += driveState.velocity * sinPheta;
		driveState.time += 1;
	}
}
