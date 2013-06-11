package xander.core.radar;

import xander.core.AbstractXanderRobot;

/**
 * Delegate for radar control that exposes only the methods
 * and objects needed for radar operation.
 * 
 * @author Scott Arnold
 */
public class RadarController {
	
	private AbstractXanderRobot robot;
	
	public void setRobot(AbstractXanderRobot robot) {
		this.robot = robot;
	}

	public void setTurnRadarLeftDegrees(double degrees) {
		robot.setTurnRadarLeft(degrees);
	}
	
	public void setTurnRadarLeftRadians(double radians) {
		robot.setTurnRadarLeftRadians(radians);
	}
	
	public void setTurnRadarRightDegrees(double degrees) {
		robot.setTurnRadarRight(degrees);
	}
	
	public void setTurnRadarRightRadians(double radians) {
		robot.setTurnRadarRightRadians(radians);
	}
	
	public double getRadarHeadingDegrees() {
		return robot.getRadarHeading();
	}
}
