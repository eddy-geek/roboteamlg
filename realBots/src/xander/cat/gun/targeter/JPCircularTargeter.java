package xander.cat.gun.targeter;

import java.awt.geom.Point2D;

import xander.core.Resources;
import xander.core.gun.targeter.Targeter;
import xander.core.math.RCMath;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;
import xander.core.track.Wave;

/**
 * Circular targeter that targets opponents moving in a circular path.  Unlike the
 * Xander core circular targeter that determines path based on finding a center
 * point, this targeter just assumes the target will continue turning at the rate
 * determined by subtracting a previous scan heading from the current heading.
 * 
 * @author Scott Arnold
 */
public class JPCircularTargeter implements Targeter {

	private SnapshotHistory snapshotHistory;
	private int tMinus;
	
	public JPCircularTargeter(int tMinus) {
		this.snapshotHistory = Resources.getSnapshotHistory();
		this.tMinus = tMinus;
	}
	
	@Override
	public String getTargetingType() {
		return "JP Circular";
	}

	@Override
	public boolean canAimAt(Snapshot target) {
		return true;
	}

	@Override
	public double getAim(Snapshot target, Snapshot myself, Wave wave) {
		Snapshot targetTM1 = snapshotHistory.getSnapshot(target.getName(), target.getTime()-tMinus, false);
		if (targetTM1 == null) {
			targetTM1 = target;
		}
		double distance = 0;
		double angleRadians = targetTM1.getHeadingRoboRadians();
		double radianTurn = target.getHeadingRoboRadians() - targetTM1.getHeadingRoboRadians();
		double x = target.getX() - myself.getX();
		double y = target.getY() - myself.getY();
		do {
			distance += wave.getBulletVelocity();
			angleRadians += radianTurn;
			x += target.getVelocity()*Math.sin(angleRadians);
			y += target.getVelocity()*Math.cos(angleRadians);
		} while (distance < Point2D.distance(0, 0, x, y));
		double aim = RCMath.normalizeDegrees(Math.toDegrees(Math.atan2(x, y)));
		//System.out.println("AIM: " + Logger.format(aim) + "; tm0h=" + Logger.format(target.getHeadingRoboDegrees()) + "; tm1h=" + Logger.format(targetTM1.getHeadingRoboDegrees()) + "; time=" + target.getTime() + "; lastTime=" + targetTM1.getTime());
		return aim;
	}
}
