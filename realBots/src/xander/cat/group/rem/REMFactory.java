package xander.cat.group.rem;

import xander.core.gun.power.FixedPowerSelector;
import xander.core.gun.power.PowerSelector;

public class REMFactory {

	public static PowerSelector getX5PowerSelector(PowerSelector basePowerSelector) {
		REMPowerSelector remPowerSelector = new REMPowerSelector(basePowerSelector);
		FixedPowerSelector x5PowerSelector = new FixedPowerSelector(1.95);
		x5PowerSelector.setAllowAutoAdjust(false);
		for (String name : REMHitSets.getX5Set()) {
			remPowerSelector.useAlternate(x5PowerSelector, name);
		}
		return remPowerSelector;
	}
	
	public static boolean isX5(String robotName) {
		for (String name: REMHitSets.getX5Set()) {
			if (name.equals(robotName)) {
				return true;
			}
		}
		return false;
	}
}
