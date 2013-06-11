package xander.core.track;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import xander.core.Resources;
import xander.core.drive.Direction;
import xander.core.drive.OrbitalDrivePredictor;
import xander.core.log.Logger;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.math.RelativeAngleRange;

/**
 * Keeps information on a bullet wave fired by a robot.  Origin time should be set
 * 1 tick behind to be logically accurate.  Robocode moves the bullet on the very
 * first tick, which it shouldn't.
 * 
 * @author Scott Arnold
 */
public class Wave {

	private Point2D.Double origin;       // location bullet fired from
	private long originTime;             // time bullet was fired
	private double originDistance;       // our original distance from the origin of fire
	private double bulletPower;          // power of fired bullet
	private double bulletVelocity;       // velocity of fired bullet
	private double initialDefenderBearing;  // angle starting from origin and pointing towards defender at time bullet was fired
	private Direction surfDirection;     // defender surf direction at time wave was fired
	RelativeAngleRange initialMEA;
	private Snapshot initialAttackerSnapshot;
	private Snapshot initialDefenderSnapshot;
	WaveState state = WaveState.LEADING;
	private List<BulletShadow> bulletShadows = new ArrayList<BulletShadow>();
	private boolean opponentWave;
	
	public Wave(Snapshot defenderSnapshot, Snapshot attackerSnapshot, double bulletPower, long bulletFiredTime) {
		this.initialAttackerSnapshot = attackerSnapshot;
		this.initialDefenderSnapshot = defenderSnapshot;
		this.opponentWave = defenderSnapshot.getName().equals(Resources.getRobotProxy().getName());
		this.origin = new Point2D.Double(attackerSnapshot.getX(), attackerSnapshot.getY());
		this.originTime = bulletFiredTime;
		this.originDistance = RCMath.getDistanceBetweenPoints(attackerSnapshot.getLocation(), defenderSnapshot.getLocation());
		this.bulletPower = bulletPower;
		this.bulletVelocity = RCPhysics.getBulletVelocity(bulletPower);
		this.initialDefenderBearing = RCMath.getRobocodeAngle(
				attackerSnapshot.getX(), attackerSnapshot.getY(), 
				defenderSnapshot.getX(), defenderSnapshot.getY());
		this.surfDirection = OrbitalDrivePredictor.getOribitalDirection(
				attackerSnapshot.getX(), attackerSnapshot.getY(), 
				defenderSnapshot.getX(), defenderSnapshot.getY(), 
				defenderSnapshot.getVelocity(), defenderSnapshot.getHeadingRoboDegrees());		
	}

	public Snapshot getInitialAttackerSnapshot() {
		return initialAttackerSnapshot;
	}

	public Snapshot getInitialDefenderSnapshot() {
		return initialDefenderSnapshot;
	}

	public boolean isOpponentWave() {
		return opponentWave;
	}
	
	
	/**
	 * Returns the max escape angle at time wave was created.  
	 * 
	 * @return    max escape angle at time wave was created.
	 */
	public RelativeAngleRange getInitialMEA() {
		return initialMEA;
	}

	public void addBulletShadow(BulletShadow bulletShadow) {
		this.bulletShadows.add(bulletShadow);
	}
	
	public List<BulletShadow> getBulletShadows() {
		return bulletShadows;
	}
	
	public Point2D.Double getOrigin() {
		return origin;
	}
	
	public double getOriginX() {
		return origin.x;
	}

	public double getOriginY() {
		return origin.y;
	}

	public long getOriginTime() {
		return originTime;
	}

	public WaveState getState() {
		return state;
	}
	
	public boolean isLeading() {
		return state == WaveState.LEADING;
	}
	
	public boolean isPassed() {
		return state == WaveState.PASSED;
	}
	
	public double getOriginDistance() {
		return originDistance;
	}

	public double getBulletPower() {
		return bulletPower;
	}
	
	public double getBulletVelocity() {
		return bulletVelocity;
	}
	
	/**
	 * Returns angle starting from origin and pointing towards defender at time bullet was fired.
	 * 
	 * @return    angle starting from origin and pointing towards defender at time bullet was fired.
	 */
	public double getInitialDefenderBearing() {
		return initialDefenderBearing;
	}
	
	public Direction getSurfDirection() {
		return surfDirection;
	}

	/**
	 * Get time until bullet wave reaches a fixed point.  This does NOT take
	 * robot width into account, and happens when the wave hits the exact
	 * point provided.
	 * 
	 * @param x              robot x-coordinate
	 * @param y              robot y-coordinate
	 * @param currentTime    current time
	 * 
	 * @return               time until wave hits
	 */
	public long getTimeUntilMatched(double x, double y, long currentTime) {
		double distanceBetween = RCMath.getDistanceBetweenPoints(origin.x, origin.y, x, y);
		double distanceTravelled = (double) (currentTime - originTime) * bulletVelocity;
		return Math.round((distanceBetween - distanceTravelled) / bulletVelocity);		
	}
	
	/**
	 * Get time until bullet wave hits a robot at given position.  This takes
	 * robot width into account, and happens when the wave hits the leading
	 * edge of the robot.
	 * 
	 * @param x              robot x-coordinate
	 * @param y              robot y-coordinate
	 * @param currentTime    current time
	 * 
	 * @return               time until wave hits
	 */
	public long getTimeUntilHit(double x, double y, long currentTime) {
		double distanceBetween = RCMath.getDistanceBetweenPoints(origin.x, origin.y, x, y);
		// take robot width into account!
		distanceBetween -= RCPhysics.ROBOT_HALF_WIDTH;
		double distanceTravelled = (double) (currentTime - originTime) * bulletVelocity;
		return Math.round((distanceBetween - distanceTravelled) / bulletVelocity);
	}
	
	/**
	 * Get time until bullet wave is completely passed a robot at given position.
	 * This takes robot width into account, and happens when the wave passes the
	 * trailing edge of the robot.
	 * 
	 * @param x              robot x-coordinate
	 * @param y              robot y-coordinate
	 * @param currentTime    current time
	 * 
	 * @return               time until wave is passed
	 */
	public long getTimeUntilPassed(double x, double y, long currentTime) {
		double distanceBetween = RCMath.getDistanceBetweenPoints(origin.x, origin.y, x, y);
		// take robot width into account!
		distanceBetween += RCPhysics.ROBOT_HALF_WIDTH;
		double distanceTravelled = (double) (currentTime - originTime) * bulletVelocity;
		return Math.round((distanceBetween - distanceTravelled) / bulletVelocity);
	}
	
	/**
	 * Returns the distance the bullet of the wave has traveled at the given point in time.
	 * 
	 * @param time           time to check
	 * 
	 * @return               distance bullet has traveled
	 */
	public double getBulletTravelDistance(long time) {
		return (time - originTime) * bulletVelocity;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Wave[");
		sb.append("originTime=").append(originTime);
		sb.append(";origin=").append(origin.toString());
		sb.append(";bulletPower=").append(Logger.format(bulletPower, 3));
		sb.append(";bulletVelocity=").append(Logger.format(bulletVelocity, 3));
		sb.append(";state=").append(state.toString());
		sb.append(";opponentWave=").append(opponentWave);
		sb.append("]");
		return sb.toString();
	}
}
