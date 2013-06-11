package xander.core.gun.power;

import xander.core.track.Snapshot;

/**
 * Power selector that always returns the same fixed fire power.
 * 
 * @author Scott Arnold
 */
public class FixedPowerSelector implements PowerSelector {

	private double firePower;
	private boolean allowAutoAdjust = true;
	
	public FixedPowerSelector(double firePower) {
		this.firePower = firePower;
	}
	
	@Override
	public double getFirePower(Snapshot target) {
		return firePower;
	}

	@Override
	public double getMinimumPower() {
		return firePower;
	}

	@Override
	public double getMaximumPower() {
		return firePower;
	}

	public void setAllowAutoAdjust(boolean allowAutoAdjust) {
		this.allowAutoAdjust = allowAutoAdjust;
	}
	
	@Override
	public boolean isAutoAdjustAllowed() {
		return allowAutoAdjust;
	}
}
