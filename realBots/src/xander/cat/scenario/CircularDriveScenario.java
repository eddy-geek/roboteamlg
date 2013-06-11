package xander.cat.scenario;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.Scenario;
import xander.core.event.TurnListener;
import xander.core.gun.Aim;
import xander.core.gun.XanderGun;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;

public class CircularDriveScenario implements Scenario, TurnListener {

	private XanderGun xanderGun;
	private RobotProxy robotProxy;
	private SnapshotHistory snapshotHistory;
	private Rectangle2D.Double bounds;
	private int referenceCount;
	private double referenceTurnLow;
	private double referenceTurnHigh;
	private double lastHeading;
	private int applyCount;
	private int notApplyCount;
	
	public CircularDriveScenario(XanderGun xanderGun) {
		this.xanderGun = xanderGun;
		this.robotProxy = Resources.getRobotProxy();
		this.snapshotHistory = Resources.getSnapshotHistory();
		this.bounds = RCMath.shrink(Resources.getRobotProxy().getBattleFieldSize(), RCPhysics.ROBOT_HALF_WIDTH);
		Resources.getRobotEvents().addTurnListener(this);
	}
	
	@Override
	public void onTurnBegin() {
		Snapshot opponent = snapshotHistory.getLastOpponentScanned();
		if (opponent != null) {
			double referenceTurn = RCMath.getTurnAngle(lastHeading, opponent.getHeadingRoboDegrees());
			if (referenceCount < 100 
					&& referenceTurn != 0 
					&& referenceTurn <= referenceTurnHigh 
					&& referenceTurn >= referenceTurnLow) {
				referenceCount++;
			} else {
				if (referenceCount > 2 && robotProxy.getOthers() > 0) {
					referenceCount -= 2;
				}
				referenceTurnLow = referenceTurn - 0.1;
				referenceTurnHigh = referenceTurn + 0.1;
			}
			lastHeading = opponent.getHeadingRoboDegrees();
		}
	}

	public double getAppliesPercentage() {
		double total = applyCount + notApplyCount;
		return (total > 0)? (double)applyCount / total : 0;
	}
	
	@Override
	public void onTurnEnd() {
		// no action required
	}

	@Override
	public boolean applies() {
		if (referenceCount >= 60) {
			// good detection rate, but only apply if opponent will not hit wall before bullet gets there
			Snapshot opponent = snapshotHistory.getLastOpponentScanned();
			Snapshot pOpponent = null;
			Snapshot myself = null;
			Aim aim = null;
			if (opponent != null) {
				pOpponent = snapshotHistory.getSnapshot(opponent.getName(), opponent.getTime()-1, false);
				myself = snapshotHistory.getMySnapshot();
				aim = xanderGun.getAim(opponent, myself);
			}
			if (aim != null && pOpponent != null) {
				Point2D.Double oppP = new Point2D.Double(opponent.getX(), opponent.getY());
				double oppV = opponent.getVelocity();
				double oppH = opponent.getHeadingRoboDegrees();
				double oppTR = RCMath.getTurnAngle(pOpponent.getHeadingRoboDegrees(), opponent.getHeadingRoboDegrees());
				double bulletDistance = 0;
				double oppDistance = opponent.getDistance();
				while (bulletDistance < oppDistance) {
					oppH += oppTR;
					oppP = RCMath.getLocation(oppP.x, oppP.y, oppV, oppH);
					if (!bounds.contains(oppP)) {
						notApplyCount++;
						return false;
					}
					bulletDistance += aim.getBulletVelocity();
					oppDistance = RCMath.getDistanceBetweenPoints(myself.getLocation(), oppP);
				}
				applyCount++;
				return true;
			}
		}
		notApplyCount++;
		return false;
	}
}
