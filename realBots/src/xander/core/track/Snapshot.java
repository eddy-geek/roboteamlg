package xander.core.track;

import java.awt.geom.Point2D;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.log.Logger;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;

/**
 * Snapshot of a robot at a given point in time.
 * 
 * @author Scott Arnold
 */
public class Snapshot {

	private String name;                // name of robot
	private Point2D.Double location;	// location of robot
	private long time;                  // time of snapshot
	private double headingRoboRadians;  // heading in robo-radians
	private double velocity;            // velocity of robot
	private double distance;            // distance of robot from self
	private double energy;              // target's remaining energy
	
	/**
	 * Create a snapshot of a robot with the given parameters.
	 * 
	 * @param name                 name of robot
	 * @param x                    robot x-coordinate
	 * @param y                    robot y-coordinate
	 * @param headingRoboRadians   robot heading in robo-radians
	 * @param velocity             robot velocity
	 * @param distance             distance from robot to self
	 * @param time                 time of snapshot
	 */
	public Snapshot(String name, double x, double y, double headingRoboRadians, double velocity, double distance, double energy, long time) {
		this.name = name;
		this.location = new Point2D.Double(x, y);
		this.time = time;
		this.headingRoboRadians = headingRoboRadians;
		this.velocity = velocity;
		this.distance = distance;
		this.energy = energy;
	}
	
	/**
	 * Create a snapshot of the given advanced robot.
	 * 
	 * @param robot    robot to take snapshot for
	 */
	public Snapshot(RobotProxy robot, double distance) {
		this.name = robot.getName();
		this.location = new Point2D.Double(robot.getX(), robot.getY());
		this.time = robot.getTime();
		this.headingRoboRadians = robot.getHeadingRadians();
		this.velocity = robot.getVelocity();
		this.distance = distance;
		this.energy = robot.getEnergy();
	}

	public String getName() {
		return name;
	}

	public Point2D.Double getLocation() {
		return location;
	}
	
	public double getX() {
		return location.x;
	}

	public double getY() {
		return location.y;
	}

	/**
	 * Returns the amount by which the robots X and Y values will change
	 * on the next turn, based on it's velocity and heading.
	 * 
	 * @return
	 */
	public double[] getXYShift() {
		Snapshot previous = Resources.getSnapshotHistory().getSnapshot(name, time-1, false);
		double[] xyShift = new double[2];
		double magnitude = velocity;
		double fheading = headingRoboRadians;
		if (previous != null) {
			// if previous snapshot is available, use it to refine future position prediction
			double deltaV = velocity - previous.velocity;
			if (deltaV < 0) {
				magnitude = Math.max(velocity + deltaV, -RCPhysics.MAX_SPEED);
			} else {
				magnitude = Math.min(velocity + deltaV, RCPhysics.MAX_SPEED);
			}
			double deltaH = headingRoboRadians - previous.headingRoboRadians;
			fheading = RCMath.normalizeRadians(headingRoboRadians + deltaH);
		}
		if (magnitude < 0) {
			// flip direction
			fheading = RCMath.normalizeRadians(headingRoboRadians + Math.PI);
			magnitude = -magnitude;
		}
		double conventionalHeading = RCMath.convertRadiansRobocodeToNormal(fheading);
		xyShift[0] = magnitude * Math.cos(conventionalHeading);
		xyShift[1] = magnitude * Math.sin(conventionalHeading);
		return xyShift;
	}
	
	public double[] getNextXY() {
		double[] nextXY = getXYShift();
		nextXY[0] += location.x;
		nextXY[1] += location.y;
		return nextXY;
	}
	
	/**
	 * Returns a snapshot based on this snapshot predicted 1 tick into 
	 * the future.  Heading and velocity is assumed to remain constant.
	 * Opponent location is used to calculate distance attribute.
	 * 
	 * @param oppX   opponent x-coordinate 1 tick into the future
	 * @param oppY   opponent y-coordinate 1 tick into the future
	 * 
	 * @return       snapshot based on this snapshot but predicted 1 tick into the future
	 */
	public Snapshot advance(double oppX, double oppY) {
		double nextXY[] = getNextXY();
		double dist = RCMath.getDistanceBetweenPoints(nextXY[0], nextXY[1], oppX, oppY);
		return new Snapshot(name, nextXY[0], nextXY[1], headingRoboRadians, velocity, dist, energy, time+1);
	}

	/**
	 * Returns a snapshot based on this snapshot predicted 1 tick into 
	 * the future.  Heading and velocity is assumed to remain constant.
	 * Distance attribute will be the same as in this snapshot.
	 * 
	 * @return       snapshot based on this snapshot but predicted 1 tick into the future
	 */
	public Snapshot advance() {
		double nextXY[] = getNextXY();
		return new Snapshot(name, nextXY[0], nextXY[1], headingRoboRadians, velocity, distance, energy, time+1);		
	}
	
	public long getTime() {
		return time;
	}

	public double getHeadingRoboRadians() {
		return headingRoboRadians;
	}

	public double getHeadingRoboDegrees() {
		return Math.toDegrees(headingRoboRadians);
	}
	
	public double getVelocity() {
		return velocity;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public double getEnergy() {
		return energy;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Snapshot[");
		sb.append("name=").append(name);
		sb.append(";location=").append(location.toString());
		sb.append(";time=").append(time);
		sb.append(";headingRoboRadians=").append(Logger.format(headingRoboRadians,4));
		sb.append(";velocity=").append(Logger.format(velocity, 3));
		sb.append(";distance=").append(Logger.format(distance));
		sb.append(";energy=").append(Logger.format(energy, 3));
		sb.append("]");
		return sb.toString();
	}
}
