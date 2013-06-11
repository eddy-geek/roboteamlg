package xander.cat.scenario;

import xander.core.Resources;
import xander.core.Scenario;
import xander.core.track.WaveHistory;

public class NoOpponentWavesScenario implements Scenario {

	private WaveHistory waveHistory;
	
	public NoOpponentWavesScenario() {
		this.waveHistory = Resources.getWaveHistory();
	}
	
	@Override
	public boolean applies() {
		return waveHistory.getOpponentActiveWaveCount() == 0;
	}
}
