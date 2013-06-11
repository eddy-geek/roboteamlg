package xander.core.drive;

import java.awt.geom.Point2D;

import xander.core.RobotProxy;
import xander.core.track.Snapshot;

/**
 * Stores a robot's driving information, including position, heading, and velocity.
 * 
 * @author Scott Arnold
 */
public class DriveState {

	Point2D.Double position;
	double heading;
	double velocity;
	long time;
	
	public DriveState() {
	}
	public DriveState(RobotProxy robotProxy) {
		position = new Point2D.Double(robotProxy.getX(), robotProxy.getY());
		velocity = robotProxy.getVelocity();
		heading = robotProxy.getHeadingDegrees();
		time = robotProxy.getTime();
	}
	public DriveState(Snapshot rs) {
		position = new Point2D.Double(rs.getX(), rs.getY());
		heading = rs.getHeadingRoboDegrees();
		velocity = rs.getVelocity();
		time = rs.getTime();
	}
	public DriveState(DriveState ds) {
		position = new Point2D.Double(ds.position.x, ds.position.y);
		heading = ds.heading;
		velocity = ds.velocity;
		time = ds.time;
	}
	public void setState(DriveState ds) {
		position = new Point2D.Double(ds.position.x, ds.position.y);
		heading = ds.heading;
		velocity = ds.velocity;
		time = ds.time;
	}
	public void setState(Snapshot rs) {
		position = new Point2D.Double(rs.getX(), rs.getY());
		heading = rs.getHeadingRoboDegrees();
		velocity = rs.getVelocity();
		time = rs.getTime();
	}
	public Point2D.Double getPosition() {
		return position;
	}
	public double getX() {
		return position.x;
	}
	public double getY() {
		return position.y;
	}
	public double getHeading() {
		return heading;
	}
	public double getVelocity() {
		return velocity;
	}
	public long getTime() {
		return time;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(heading);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
		temp = Double.doubleToLongBits(velocity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DriveState other = (DriveState) obj;
		if (Double.doubleToLongBits(heading) != Double
				.doubleToLongBits(other.heading))
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		if (Double.doubleToLongBits(velocity) != Double
				.doubleToLongBits(other.velocity))
			return false;
		return true;
	}
}
