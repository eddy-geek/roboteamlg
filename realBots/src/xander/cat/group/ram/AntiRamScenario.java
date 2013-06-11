package xander.cat.group.ram;

import xander.core.Scenario;

public class AntiRamScenario implements Scenario {

	private RamDetector ramDetector;
	
	public AntiRamScenario(String ramEscapeDriveName, double engageDistance, double disengageDistance) {
		this.ramDetector = new RamDetector(ramEscapeDriveName, engageDistance, disengageDistance);
	}
	
	@Override
	public boolean applies() {
		return ramDetector.isOpponentRamming();
	}
}
