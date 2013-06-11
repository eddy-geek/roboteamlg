package xander.core.drive;

import xander.core.AbstractXanderRobot;
import xander.core.math.RCMath;

/**
 * Delegate for drive control that exposes only methods
 * and objects needed for movement.
 * 
 * @author Scott Arnold
 */
public class DriveController {
	
	private AbstractXanderRobot robot;
	
	public void setRobot(AbstractXanderRobot robot) {
		this.robot = robot;
	}
	
	public void setMaxVelocity(double velocity) {
		this.robot.setMaxVelocity(velocity);
	}
	
	public void setTurnLeftDegrees(double degrees) {
		this.robot.setTurnLeft(degrees);
	}
	
	public void setTurnLeftRadians(double radians) {
		this.robot.setTurnLeftRadians(radians);
	}
	
	public void setTurnRightDegrees(double degrees) {
		this.robot.setTurnRight(degrees);
	}
	
	public void setTurnRightRadians(double radians) {
		this.robot.setTurnRightRadians(radians);
	}
	
	public void setAhead(double distance) {
		this.robot.setAhead(distance);
	}
	
	public void setBack(double distance) {
		this.robot.setBack(distance);
	}
	
	public double getDistanceRemaining() {
		return this.robot.getDistanceRemaining();
	}
	
	/**
	 * Drive in the given direction at given speed.  Speed value should
	 * always be positive.  Robot may be driven forwards or backwards
	 * depending on which direction requires less turning.
	 * 
	 * @param roboDegrees    direction to drive in Robocode degrees
	 * @param speed          speed to drive (speed should always be positive)
	 */
	public void drive(double roboDegrees, double speed) { 
		double heading = this.robot.getHeading();
		double velocity = speed;
		double turnAngle = RCMath.getTurnAngle(heading, roboDegrees);
		if (Math.abs(turnAngle) > 90) {
			heading = RCMath.normalizeDegrees(heading+180);
			velocity = -speed;
			turnAngle = RCMath.getTurnAngle(heading, roboDegrees);
		}
		setTurnRightDegrees(turnAngle);
		setMaxVelocity(speed);
		if (speed == 0) {
			setAhead(0);
		} else if (velocity < 0) {
			setBack(300);
		} else {
			setAhead(300);
		}
	}
}
