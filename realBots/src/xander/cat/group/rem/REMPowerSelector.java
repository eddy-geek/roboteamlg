package xander.cat.group.rem;

import java.util.HashMap;
import java.util.Map;

import xander.core.gun.power.PowerSelector;
import xander.core.track.Snapshot;

public class REMPowerSelector implements PowerSelector {

	private Map<String, PowerSelector> altPowerSelectors = new HashMap<String, PowerSelector>();
	private PowerSelector basePowerSelector;
	private double minPower;
	private double maxPower;
	private PowerSelector lastPowerSelector;
	
	public REMPowerSelector(PowerSelector basePowerSelector) {
		this.basePowerSelector = basePowerSelector;
		this.minPower = basePowerSelector.getMinimumPower();
		this.maxPower = basePowerSelector.getMaximumPower();
	}
	
	public void useAlternate(PowerSelector altPowerSelector, String name) {
		altPowerSelectors.put(name, altPowerSelector);
		this.minPower = Math.min(minPower, altPowerSelector.getMinimumPower());
		this.maxPower = Math.max(maxPower, altPowerSelector.getMaximumPower());
	}
	
	@Override
	public double getFirePower(Snapshot target) {
		PowerSelector powerSelector = altPowerSelectors.get(target.getName());
		if (powerSelector == null) {
			powerSelector = basePowerSelector;
		}
		this.lastPowerSelector = powerSelector;
		return powerSelector.getFirePower(target);
	}

	@Override
	public double getMinimumPower() {
		return minPower;
	}

	@Override
	public double getMaximumPower() {
		return maxPower;
	}

	@Override
	public boolean isAutoAdjustAllowed() {
		if (lastPowerSelector != null) {
			return lastPowerSelector.isAutoAdjustAllowed();
		}
		return false;
	}
}
