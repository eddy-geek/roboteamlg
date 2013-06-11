package xander.core.track;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import robocode.BattleEndedEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RoundEndedEvent;

import xander.core.Component;
import xander.core.ComponentChain;
import xander.core.Configuration;
import xander.core.RegisteredComponentListener;
import xander.core.Resources;
import xander.core.RobotEvents;
import xander.core.RobotProxy;
import xander.core.drive.Drive;
import xander.core.event.CollisionListener;
import xander.core.event.RoundListener;
import xander.core.event.TurnListener;
import xander.core.log.Log;
import xander.core.log.Logger;
import xander.core.math.RCPhysics;

public class DriveStats implements TurnListener, RoundListener, CollisionListener, RegisteredComponentListener {

	private static final String NO_DRIVE = "No Drive";
	private static final Log log = Logger.getLog(DriveStats.class);
	
	private RobotProxy robotProxy;
	private Map<String, Long> driveTimes = new HashMap<String, Long>();
	private String activeDrive = NO_DRIVE;
	private long activeDriveStartTime = 0;
	private boolean logDriveTimes;
	private int wallHits;
	private int robotHits;
	private double wallHitDmgAccum;
	
	public DriveStats(RobotProxy robotProxy, RobotEvents robotEvents, Configuration configuration, ComponentChain chain) {
		this.robotProxy = robotProxy;
		this.logDriveTimes = configuration.isLogDriveTimes();
		robotEvents.addTurnListener(this);
		robotEvents.addRoundListener(this);
		robotEvents.addCollisionListener(this);
		chain.addRegisteredComponentListener(this);
		
	}

	@Override
	public void componentRegistered(Component component) {
		if (component instanceof Drive) {
			// this only happens at the beginning, so we don't need to check if it's already there
			driveTimes.put(component.getName(), Long.valueOf(0));
		}
	}

	@Override
	public void onTurnBegin() {
		// no action required
	}

	@Override
	public void onTurnEnd() {
		String currentActiveDrive = robotProxy.getActiveDriveName();
		if (currentActiveDrive == null) {
			currentActiveDrive = NO_DRIVE;
		}
		if (!currentActiveDrive.equals(activeDrive)) {
			long cumulativeTime = Resources.getCumulativeTime();
			long timeForActiveDrive = cumulativeTime - activeDriveStartTime;
			if (timeForActiveDrive > 0) {
				Long prevTime = driveTimes.get(activeDrive);
				if (prevTime == null) {
					driveTimes.put(activeDrive, Long.valueOf(timeForActiveDrive));
				} else {
					driveTimes.put(activeDrive, Long.valueOf(timeForActiveDrive + prevTime.longValue()));
				}
			}
			activeDrive = currentActiveDrive;
			activeDriveStartTime = cumulativeTime;
		}
	}
	
	public long getDriveTime(String driveName) {
		Long driveTime = driveTimes.get(driveName);
		if (driveTime == null) {
			return 0;
		} else {
			return driveTime.longValue();
		}
	}
	
	public int getWallHits() {
		return wallHits;
	}
	
	public double getAverageWallHitDamage() {
		return (wallHits > 0)? wallHitDmgAccum / (double)wallHits : 0;
	}
	public int getRobotHits() {
		return robotHits;
	}
	
	public Set<String> getDriveNames() {
		return driveTimes.keySet();
	}
	
	/**
	 * Returns the percent of time a particular drive has been used (in range 0-1).
	 * 
	 * @param driveName    name of drive to get usage for
	 * 
	 * @return    percent of time a particular drive has been used (in range 0-1).
	 */
	public double getDriveUsagePercent(String driveName) {
		long driveTime = getDriveTime(driveName);
		return (double)driveTime / (double)Resources.getCumulativeTime();
	}
	
	private void logDriveTimes() {
		for (Map.Entry<String, Long> entry : driveTimes.entrySet()) {
			log.stat(entry.getKey() + ": " + entry.getValue().toString() + " ticks");
		}
	}

	@Override
	public void onBattleEnded(BattleEndedEvent event) {
		// no action required
	}

	@Override
	public void onRoundEnded(RoundEndedEvent event) {
		if (logDriveTimes) {
			logDriveTimes();
		}
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		robotHits++;
	}

	@Override
	public void onHitWall(HitWallEvent event) {
		wallHits++;
		Snapshot snap = Resources.getSnapshotHistory().getMySnapshot(event.getTime()-1, true);
		wallHitDmgAccum += RCPhysics.getWallHitDamage(snap.getVelocity());
	}
}
