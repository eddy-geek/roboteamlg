package xander.core.gun.detect;

import xander.core.gun.targeter.Targeter;
import xander.core.track.Wave;

public class TargeterTargetingDetector extends TargetingDetector {

	private Targeter targeter;
	private double sloppyAimTolerance;
	
	public TargeterTargetingDetector(boolean offensive, Targeter targeter, double sloppyAimTolerance) {
		super(targeter.getTargetingType() + " Targeting Detector", offensive);
		this.targeter = targeter;
		this.sloppyAimTolerance = sloppyAimTolerance;
	}

	@Override
	public double getDetectionAngle(Wave wave) {
		if (targeter.canAimAt(wave.getInitialDefenderSnapshot())) {
			return targeter.getAim(wave.getInitialDefenderSnapshot(), wave.getInitialAttackerSnapshot(), wave);
		} else {
			return -1;
		}
	}

	@Override
	protected double getSloppyAimTolerance() {
		return sloppyAimTolerance;
	}
}
