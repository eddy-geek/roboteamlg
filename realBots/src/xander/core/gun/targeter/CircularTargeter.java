package xander.core.gun.targeter;

import xander.core.Resources;
import xander.core.math.Circular;
import xander.core.math.RCMath;
import xander.core.math.VelocityVector;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;
import xander.core.track.Wave;

/**
 * Gun for shooting at targets traveling in a circular arc.
 * 
 * When using this gun, canAimAt(...) should ALWAYS be called before fireAt(...).
 * 
 * @author Scott Arnold
 *
 */
public class CircularTargeter implements Targeter {

	private double[] centerPoint;
	private double aim;
	private SnapshotHistory snapshotHistory;
	private Snapshot firstSnapshot;
	private Snapshot secondSnapshot;
	private int minTimeAgo = 2;
	private double minHeadingChange = 4;  // second snapshot must have heading change greater than this value in degrees
	private int maxTimeAgo = 30;

	public CircularTargeter() {
		this.snapshotHistory = Resources.getSnapshotHistory();
	}
	
	@Override
	public String getTargetingType() {
		return "Circular";
	}

	@Override
	public boolean canAimAt(Snapshot target) {
		centerPoint = null;
		if (target != null) {
			Snapshot olderSnapshot = null;
			long checkTime = target.getTime() - minTimeAgo;
			while (checkTime > target.getTime() - maxTimeAgo) {
				olderSnapshot = snapshotHistory.getSnapshot(target.getName(), checkTime, false);
				if (olderSnapshot != null) {
					double headingChange = RCMath.getTurnAngle(olderSnapshot.getHeadingRoboDegrees(), target.getHeadingRoboDegrees());
					if (Math.abs(headingChange) >= minHeadingChange) {
						break;
					}
				}
				checkTime--;
			}
			if (olderSnapshot != null) {
				centerPoint = Circular.getCenterPoint(target, olderSnapshot);
			} 
			this.secondSnapshot = olderSnapshot;
		}
		this.firstSnapshot = target;
		return centerPoint != null;
	}

	@Override
	public double getAim(Snapshot target, Snapshot myself,
			Wave wave) {
		if (centerPoint == null) {
			return -1;
		}
		VelocityVector targetVector = Circular.calculateTrajectory(
				target, 
				myself.getX(), 
				myself.getY(), 
				centerPoint, 
				wave.getBulletVelocity(), 
				target.getTime());
		this.aim = (targetVector == null)? -1 : targetVector.getRoboAngle();
		return aim;
	}

//	@Override
//	public RobotSnapshot getFirstSnapshot() {
//		return firstSnapshot;
//	}
//
//	@Override
//	public RobotSnapshot getSecondSnapshot() {
//		return secondSnapshot;
//	}
//
//	@Override
//	public double[] getCircleCenter() {
//		return centerPoint;
//	}
//
//	@Override
//	public double getAim() {
//		return aim;
//	}
}
