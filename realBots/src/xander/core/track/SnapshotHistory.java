package xander.core.track;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import robocode.DeathEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;

import xander.core.Configuration;
import xander.core.RobotEvents;
import xander.core.RobotProxy;
import xander.core.event.RoundBeginListener;
import xander.core.event.ScannedRobotListener;
import xander.core.event.SurvivalListener;
import xander.core.event.TurnListener;
import xander.core.math.RCMath;

public class SnapshotHistory implements TurnListener, RoundBeginListener, ScannedRobotListener, SurvivalListener {

	private Map<String, List<Snapshot>> snapshots = new HashMap<String, List<Snapshot>>();
	private Map<String, Snapshot> lastScanMap = new HashMap<String, Snapshot>();
	private Map<String, Boolean> aliveMap = new HashMap<String, Boolean>();
	private Snapshot lastOpponentScanned;
	private int historySize;
	private String myRobotName;
	private RobotProxy robotProxy;
	
	public SnapshotHistory(String myRobotName, RobotProxy robotProxy, Configuration configuration, RobotEvents robotEvents) {
		this.myRobotName = myRobotName;
		this.robotProxy = robotProxy;
		this.historySize = configuration.getSnapshotHistorySize();
		robotEvents.addTurnListener(this);
		robotEvents.addScannedRobotListener(this);
		robotEvents.addRoundBeginListener(this);
		robotEvents.addSurvivalListener(this);
	}
	
	public int getHistorySize() {
		return historySize;
	}
	
	/**
	 * Returns the number of opponents that are known to exist.
	 * 
	 * @return     number of opponents that are known to exist
	 */
	public int getOpponentCount() {
		return aliveMap.size()-1;
	}
	
	public Snapshot getMySnapshot(long time, boolean allowGuess) {
		return getSnapshot(myRobotName, time, allowGuess);
	}
	
	/**
	 * Returns the latest available snapshot for self.
	 * 
	 * @return    most recent available snapshot for self
	 */
	public Snapshot getMySnapshot() {
		return getSnapshot(myRobotName);
	}
	
	/**
	 * Returns an iterator of the snapshots for the given robot starting at the provided snapshot.
	 * If the provided snapshot is null, the iterator will start at the first snapshot.
	 * If there are no snapshots for the robot or the starting snapshot is not null but cannot
	 * be found, null is returned.
	 * 
	 * @param robotName          name of robot to get iterator for
	 * @param startingSnapshot   where iterator starts
	 * 
	 * @return                   iterator of snapshots for robot starting from given snapshot
	 */
	public Iterator<Snapshot> getSnapshotIterator(String robotName, Snapshot startingSnapshot) {
		List<Snapshot> snapsForRobot = snapshots.get(robotName);
		Iterator<Snapshot> iter = null;
		if (snapsForRobot != null && snapsForRobot.size() > 0) {
			int idx = 0;
			if (startingSnapshot != null) {
				idx = snapsForRobot.indexOf(startingSnapshot);
				if (idx < 0) {
					return null;
				}
			}
			if (idx >= 0) {
				iter = snapsForRobot.iterator();
				for (int i=0; i<idx; i++) {
					iter.next();
				}
			}
		}
		return iter;
	}
	
	/**
	 * Returns the latest available snapshot for the robot of given name.  If
	 * no snapshot can be found, null is returned.
	 * 
	 * @param robotName      name of robot to get snapshot for
	 * 
	 * @return               most recent available snapshot of robot
	 */
	public Snapshot getSnapshot(String robotName) {
		Snapshot snapshot = null;
		List<Snapshot> snapsForRobot = snapshots.get(robotName);
		if (snapsForRobot != null && snapsForRobot.size() > 0) {
			snapshot = snapsForRobot.get(snapsForRobot.size()-1);
		}
		return snapshot;
	}
	
	/**
	 * Returns the earliest available snapshot for the robot of given name.
	 * 
	 * @param robotName    name of robot to get snapshot for
	 * 
	 * @return             earliest available snapshot for robot
	 */
	public Snapshot getEarliestSnapshot(String robotName) {
		Snapshot snapshot = null;
		List<Snapshot> snapsForRobot = snapshots.get(robotName);
		if (snapsForRobot != null && snapsForRobot.size() > 0) {
			snapshot = snapsForRobot.get(0);
		}
		return snapshot;
	}
	
	/**
	 * Returns the next earliest available snapshot for the robot prior to the given snapshot for that robot.
	 * 
	 * @param snapshot    snapshot to get previous snapshot for
	 * 
	 * @return            next earliest available snapshot before the provided snapshot for the same robot
	 */
	public Snapshot getPreviousSnapshot(Snapshot snapshot) {
		Snapshot previous = null;
		List<Snapshot> snapsForRobot = snapshots.get(snapshot.getName());
		if (snapsForRobot != null && snapsForRobot.size() > 0) {
			int idx = snapsForRobot.indexOf(snapshot);
			if (idx > 0) {
				previous = snapsForRobot.get(--idx);
			}
		}
		return previous;
	}
	
	/**
	 * Returns the snapshot at given time for the robot of given name.
	 * If no snapshot can be found for the given robot at the given
	 * time, null is returned.
	 * 
	 * @param robotName    name of robot
	 * @param time         time of snapshot
	 * @param allowGuess   if exact snapshot not available: if true, make best guess; if false, return null
	 * 
	 * @return             snapshot for robot at specific time
	 */
	public Snapshot getSnapshot(String robotName, long time, boolean allowGuess) {
		List<Snapshot> snapsForRobot = snapshots.get(robotName);
		if (snapsForRobot != null) {
			// generally will not want a very old snapshot, so search from latest to earliest
			for (int i = snapsForRobot.size()-1; i >= 0; i--) {
				Snapshot snapshot = snapsForRobot.get(i);
				if (snapshot.getTime() == time) {
					return snapshot;
				} else if (snapshot.getTime() < time) {
					if (allowGuess) {
						long ticksToAdvance = time - snapshot.getTime();
						for (long t = 0; t < ticksToAdvance; t++) {
							snapshot = snapshot.advance();
						}
						return snapshot;
					} else {
						return null;
					}
				}
			}
		}
		return allowGuess? new Snapshot(robotName, 400, 300, 0, 0, 100, 100, time) : null;
	}

	/**
	 * Returns whether or not the given robot is still alive.  
	 * A value of null is returned if the status of the given robot is unknown.
	 * 
	 * @param robotName    name of robot
	 * 
	 * @return             whether or not robot is alive
	 */
	public Boolean isAlive(String robotName) {
		return aliveMap.get(robotName);
	}
	
	/**
	 * Returns the snapshot of the last opponent scanned.
	 * 
	 * @return             snapshot of last opponent scanned
	 */
	public Snapshot getLastOpponentScanned() {
		return lastOpponentScanned;
	}
	
	/**
	 * Returns collection of last scan for all robots. 
	 * The returned collection is unmodifiable.
	 * 
	 * @return    list of last scan for all robots
	 */
	public Collection<Snapshot> getLastOpponentsScanned() {
		return Collections.unmodifiableCollection(lastScanMap.values());
	}
	
	private void addSnapshot(Snapshot snapshot) {
		List<Snapshot> snaps = snapshots.get(snapshot.getName());
		if (snaps == null) {
			snaps = new ArrayList<Snapshot>();
			snapshots.put(snapshot.getName(), snaps);
		}
		snaps.add(snapshot);
		if (snaps.size() > historySize) {
			snaps.remove(0);
		}
		if (!aliveMap.containsKey(snapshot.getName())) {
			aliveMap.put(snapshot.getName(), Boolean.TRUE);
		}
		lastScanMap.put(snapshot.getName(), snapshot);
	}
	
	/**
	 * For melee combat, radar's may optionally add group snapshots to the 
	 * snapshot history.  
	 * 
	 * Note:  The argument uses the Snapshot class rather than the GroupSnapshot 
	 * class as a work-around for a bug in the Robocode robot packager.
	 * 
	 * @param snapshot    group snapshot
	 */
	public void addGroupSnapshot(Snapshot snapshot) {
		addSnapshot(snapshot);
	}
	
	@Override
	public void onTurnBegin() {
		Snapshot mySnapshot = new Snapshot(robotProxy, 0);
		addSnapshot(mySnapshot);
	}

	@Override
	public void onTurnEnd() {
		// no action required
	}

	@Override
	public void onRoundBegin() {
		lastOpponentScanned = null;
		for (List<Snapshot> snaps : snapshots.values()) {
			snaps.clear();
		}
		lastScanMap.clear();
		for (String robotName : aliveMap.keySet()) {
			// all robots we know about should be alive at the beginning of the round
			aliveMap.put(robotName, Boolean.TRUE);
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		Point2D.Double pos = RCMath.getRobotPosition(event.getBearingRadians(), event.getDistance(), 
				robotProxy.getX(), robotProxy.getY(), robotProxy.getHeadingRadians());
		Snapshot snapshot = new Snapshot(event.getName(), pos.x, pos.y, 
				event.getHeadingRadians(), event.getVelocity(), 
				event.getDistance(), event.getEnergy(), event.getTime());
		addSnapshot(snapshot);
		this.lastOpponentScanned = snapshot;
	}

	@Override
	public void onWin(WinEvent event) {
		// no action required
	}

	@Override
	public void onDeath(DeathEvent event) {
		// no action required
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		if (aliveMap.containsKey(event.getName())) {
			aliveMap.put(event.getName(), Boolean.FALSE);
		}
	}
	
	
}
