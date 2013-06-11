package xander.cat.group.ram;

import xander.cat.group.rem.REMFactory;
import xander.core.ComponentChain;
import xander.core.math.RCPhysics;
import xander.core.gun.Gun;
import xander.core.gun.XanderGun;
import xander.core.gun.power.FixedPowerSelector;
import xander.core.gun.power.PowerSelector;
import xander.core.gun.targeter.LinearTargeter;
import xander.core.gun.targeter.Targeter;

/**
 * Factory for creating ramming-related components.
 * 
 * @author Scott Arnold
 */
public class RamFactory {

	public static final double DEFAULT_ENGAGE_DISTANCE = 70;
	public static final double DEFAULT_DISENGAGE_DISTANCE = 120;
	
	public static void addAntiRamComponents(ComponentChain chain) {
		RamEscapeDrive ramEscapeDrive = new RamEscapeDrive();
		PowerSelector ramEscapePowerSelector = new FixedPowerSelector(RCPhysics.MAX_FIRE_POWER);
		ramEscapePowerSelector = REMFactory.getX5PowerSelector(ramEscapePowerSelector);
		Targeter ramEscapeTargeter = new LinearTargeter();
		Gun ramEscapeGun = new XanderGun(ramEscapeTargeter, ramEscapePowerSelector);
		AntiRamScenario antiRamScenario = new AntiRamScenario(ramEscapeDrive.getName(), 
				DEFAULT_ENGAGE_DISTANCE, DEFAULT_DISENGAGE_DISTANCE);
		chain.addComponents(antiRamScenario, ramEscapeDrive, ramEscapeGun);
	}
	
	public static void addRamComponents(ComponentChain chain, double opponentEnergy, double fallbackOpponentEnergy, int ticksSinceNoWaves) {
		RamLowEnergyScenario scenario = new RamLowEnergyScenario(opponentEnergy, ticksSinceNoWaves);
		scenario.setFallbackOpponentEnergy(fallbackOpponentEnergy);
		RamDrive ramDrive = new RamDrive();
		RamGun ramGun = new RamGun();
		chain.addComponents(scenario, ramDrive, ramGun);
	}
}
