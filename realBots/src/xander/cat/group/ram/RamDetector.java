package xander.cat.group.ram;

import xander.core.Resources;
import xander.core.log.Logger;
import xander.core.math.LinearEquation;
import xander.core.track.DriveStats;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;

public class RamDetector {
	
	private double engageDistance;
	private double disengageDistance;
	private boolean engaged;
	private LinearEquation growthEq;
	private String ramEscapeDriveName;
	private SnapshotHistory snapshotHistory;
	private DriveStats driveStats;
	
	public RamDetector(String ramEscapeDriveName, double engageDistance, double disengageDistance) {
		this.ramEscapeDriveName = ramEscapeDriveName;
		this.snapshotHistory = Resources.getSnapshotHistory();
		this.engageDistance = engageDistance;
		this.disengageDistance = disengageDistance;
		this.growthEq = new LinearEquation(0,1,1,3); // 3x distance when 100 percent utilization;
		this.driveStats = Resources.getDriveStats();
	}
	
	/**
	 * Sets a multiplier used for the engage and disengage distance based on how
	 * much the ram escape drive is being used.  Multiplier provided is multiplier
	 * to use if drive usage is 100%, and scales linearly down to 1 at a drive 
	 * usage of 0%.  Set a value of 1 to use same engage and disengage distance
	 * regardless of drive utilization.
	 * 
	 * @param multiplierAtFullUtilization     100% drive utilization multiplier for engage/disengage distances
	 */
	public void setUtilizationMultiplier(double multiplierAtFullUtilization) {
		this.growthEq = new LinearEquation(0,1,1,multiplierAtFullUtilization);
	}
	
	public boolean isOpponentRamming() {
		Snapshot opponentSnapshot = snapshotHistory.getLastOpponentScanned();
		if (opponentSnapshot == null) {
			engaged = false;
		} else {
			double driveUtilization = driveStats.getDriveUsagePercent(ramEscapeDriveName);
			double mult = growthEq.getY(driveUtilization);
			if (!engaged && opponentSnapshot.getDistance() <= (engageDistance*mult)) {
				//System.out.println("Engaging Ram Escape: mult=" + Logger.format(mult,2) + ";engageDistance=" + Logger.format(engageDistance) + ";opponentDistance=" + Logger.format(opponentSnapshot.getDistance()));
				engaged = true;
			} else if (engaged && opponentSnapshot.getDistance() >= (disengageDistance*mult)) {
				engaged = false;
			}
		}
		return engaged;
	}
}
