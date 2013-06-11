package xander.core.gun.targeter;

import xander.core.math.RCMath;
import xander.core.track.Snapshot;
import xander.core.track.Wave;

public class HeadOnTargeter implements Targeter {

	@Override
	public String getTargetingType() {
		return "Head On";
	}

	@Override
	public boolean canAimAt(Snapshot target) {
		return true;
	}

	@Override
	public double getAim(Snapshot target, Snapshot myself,
			Wave wave) {
		double targetDirection = RCMath.getRobocodeAngle(myself.getLocation(), target.getLocation());
		return targetDirection;
	}
}
