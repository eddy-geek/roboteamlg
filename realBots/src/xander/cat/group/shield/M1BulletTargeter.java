package xander.cat.group.shield;

import xander.core.Resources;
import xander.core.math.RCMath;
import xander.core.track.Snapshot;
import xander.core.track.Wave;

public class M1BulletTargeter implements BulletTargeter {

	@Override
	public double getAim(Wave wave) {
		Snapshot snap = Resources.getSnapshotHistory().getSnapshot(
				wave.getInitialAttackerSnapshot().getName(), 
				wave.getInitialAttackerSnapshot().getTime()-1, true);
		double headingRoboRadians = Math.toRadians(
				RCMath.getRobocodeAngle(snap.getLocation(), 
						wave.getInitialDefenderSnapshot().getLocation()));
		return headingRoboRadians;
	}

}
