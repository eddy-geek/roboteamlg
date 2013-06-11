package xander.cat.group.ram;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.Scenario;
import xander.core.event.RoundBeginListener;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;
import xander.core.track.WaveHistory;

public class RamLowEnergyScenario implements Scenario, RoundBeginListener {

	private RobotProxy robotProxy;
	private SnapshotHistory snapshotHistory;
	private WaveHistory waveHistory;
	private double opponentEnergy;
	private int ticksSinceNoWaves;
	private int noWavesTickCounter;
	private double fallbackOpponentEnergy;
	
	public RamLowEnergyScenario(double opponentEnergy, int ticksSinceNoWaves) {
		this.robotProxy = Resources.getRobotProxy();
		this.snapshotHistory = Resources.getSnapshotHistory();
		this.waveHistory = Resources.getWaveHistory();
		this.opponentEnergy = opponentEnergy;
		this.fallbackOpponentEnergy = opponentEnergy;
		this.ticksSinceNoWaves = ticksSinceNoWaves;
		Resources.getRobotEvents().addRoundBeginListener(this);
	}
	
	/**
	 * Opponent energy limit will "fall back" to the level set here if the opponent who had stopped
	 * firing then fires again at close range.
	 * 
	 * @param fallbackOpponentEnergy   should be set to a value less than the opponent energy parameter
	 */
	public void setFallbackOpponentEnergy(double fallbackOpponentEnergy) {
		this.fallbackOpponentEnergy = fallbackOpponentEnergy;
	}
	
	@Override
	public boolean applies() {
		Snapshot snapshot = snapshotHistory.getLastOpponentScanned();
		if (snapshot != null                                  // can't apply without opponent info 
				&& snapshot.getEnergy() <= opponentEnergy     // opponent energy must be below the set threshold
				&& robotProxy.getEnergy() > 5) {              // our own energy needs to be not too low
			if (ticksSinceNoWaves > 0) {
				if (waveHistory.getOpponentWaveCount() == 0) {
					if (noWavesTickCounter > ticksSinceNoWaves) {
						return true;
					}
					noWavesTickCounter++;
				} else {
					if (noWavesTickCounter > ticksSinceNoWaves) {
						// uh oh, opponent started firing again (presumably because we closed in on them)
						// revert to fallback opponent energy settings
						opponentEnergy = fallbackOpponentEnergy;
					}
					noWavesTickCounter = 0;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onRoundBegin() {
		noWavesTickCounter = 0;
	}
}
