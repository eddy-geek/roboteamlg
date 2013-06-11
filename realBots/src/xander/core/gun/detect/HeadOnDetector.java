package xander.core.gun.detect;

import xander.core.track.Wave;

public class HeadOnDetector extends TargetingDetector {

	public HeadOnDetector(boolean offensive) {
		super("Head-On Detector", offensive);
	}

	@Override
	public double getDetectionAngle(Wave wave) {
		return wave.getInitialDefenderBearing();
	}

	@Override
	protected double getSloppyAimTolerance() {
		return 0;
	}
}
