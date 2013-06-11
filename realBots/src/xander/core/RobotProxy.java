package xander.core;

import java.awt.geom.Rectangle2D;
import java.io.File;

import robocode.Condition;
import xander.core.math.RCMath;
import xander.paint.Paintable;
import xander.paint.Paintables;

/**
 * Proxy class for the main robot that exposes only the main robot getter methods
 * and methods to add listeners.  An instance of this class is stored in the
 * StaticResourceManager as a resource for any interested component to utilize.
 * 
 * @author Scott Arnold
 */
public class RobotProxy implements Paintable {
	
	AbstractXanderRobot robot;
	private Double battleFieldDiagonal;
	
	public RobotProxy() {
		Paintables.addPaintable(this);
	}
	
	/**
	 * Sets the main robot class.  This should only be called by the framework.
	 * This method should not be exposed to classes in other packages.
	 * 
	 * @param robot
	 */
	void setRobot(AbstractXanderRobot robot) {
		this.robot = robot;
	}

	public String getName() {
		return robot.getName();
	}
	
	public long getTime() {
		return robot.getTime();
	}
	
	public int getRoundNum() {
		return robot.getRoundNum();
	}
	
	public int getOthers() {
		return robot.getOthers();
	}
	
	public double getX() {
		return robot.getX();
	}
	
	public double getY() {
		return robot.getY();
	}
	
	public double getVelocity() {
		return this.robot.getVelocity();
	}
	
	public double getHeadingDegrees() {
		return this.robot.getHeading();
	}
	
	public double getHeadingRadians() {
		return this.robot.getHeadingRadians();
	}
	
	/**
	 * Returns heading of robot, taking back-as-front into account.
	 * 
	 * @return           heading of robot, taking back-as-front into account
	 */
	public double getBackAsFrontHeadingDegrees() {
		double heading = robot.getHeading();
		if (robot.getVelocity() < 0) {
			heading = RCMath.normalizeDegrees(heading + 180);
		}
		return heading;
	}
	
	public String getActiveRadarName() {
		return robot.getActiveRadarName();
	}
	
	public String getActiveDriveName() {
		return robot.getActiveDriveName();
	}
	
	public String getActiveGunName() {
		return robot.getActiveGunName();
	}
	
	public double getBattleFieldHeight() {
		return robot.getBattleFieldHeight();
	}

	public double getBattleFieldWidth() {
		return robot.getBattleFieldWidth();
	}

	public Rectangle2D.Double getBattleFieldSize() {
		return robot.getBattleFieldSize();
	}
	
	/**
	 * Returns the length of the battlefield from one corner to the opposite corner.
	 * 
	 * @return    corner-to-corner length of the battlefield
	 */
	public double getBattleFieldDiagonal() {
		if (battleFieldDiagonal == null) {
			battleFieldDiagonal = Double.valueOf(Math.sqrt(
					robot.getBattleFieldHeight()*robot.getBattleFieldHeight() 
					+ robot.getBattleFieldWidth()*robot.getBattleFieldWidth()));
		}
		return battleFieldDiagonal.doubleValue();
	}
	
	public double getEnergy() {
		return robot.getEnergy();
	}

	public double getGunCoolingRate() {
		return robot.getGunCoolingRate();
	}

	public double getGunHeadingDegrees() {
		return robot.getGunHeading();
	}

	public double getGunHeadingRadians() {
		return robot.getGunHeadingRadians();
	}

	public double getGunHeat() {
		return robot.getGunHeat();
	}

	public double getGunTurnRemainingDegrees() {
		return robot.getGunTurnRemaining();
	}

	public double getGunTurnRemainingRadians() {
		return robot.getGunTurnRemainingRadians();
	}

	public int getNumRounds() {
		return robot.getNumRounds();
	}

	public double getRadarHeadingDegrees() {
		return robot.getRadarHeading();
	}

	public double getRadarHeadingRadians() {
		return robot.getRadarHeadingRadians();
	}

	public double getRadarTurnRemainingDegrees() {
		return robot.getRadarTurnRemaining();
	}

	public double getRadarTurnRemainingRadians() {
		return robot.getRadarTurnRemainingRadians();
	}

	public double getTurnRemainingDegrees() {
		return robot.getTurnRemaining();
	}

	public double getTurnRemainingRadians() {
		return robot.getTurnRemainingRadians();
	}
	
	public File getDataFile(String filename) {
		return robot.getDataFile(filename);
	}
	
	public boolean isAdjustGunForRobotTurn() {
		return robot.isAdjustGunForRobotTurn();
	}
	
	public boolean isAdjustRadarForGunTurn() {
		return robot.isAdjustRadarForGunTurn();
	}
	
	public boolean isAdjustRadarForRobotTurn() {
		return robot.isAdjustRadarForRobotTurn();
	}

	public void addCustomEvent(Condition condition) {
		robot.addCustomEvent(condition);
	}
	
	public void removeCustomEvent(Condition condition) {
		robot.removeCustomEvent(condition);
	}
	
	@Override
	public String getPainterName() {
		return null;  // no specific Painter
	}	
}
