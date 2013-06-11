package xander.core.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import xander.core.Configuration;
import xander.core.Resources;
import xander.core.RobotEvents;
import xander.core.event.BulletHitListener;
import xander.core.event.CollisionListener;
import xander.core.event.OpponentGunFiredEvent;
import xander.core.event.OpponentGunListener;
import xander.core.event.RoundBeginListener;
import xander.core.event.ScannedRobotListener;
import xander.core.log.Log;
import xander.core.log.Logger;
import xander.core.math.RCPhysics;

/**
 * Watches for energy drops in scanned opponents that appear to be bullets being
 * fired by the opponent.  When it appears opponent has fired a bullet, any
 * BulletFiredListeners are notified.
 * 
 * @author Scott Arnold
 */
public class OpponentGunWatcher implements RoundBeginListener, ScannedRobotListener, BulletHitListener, CollisionListener {

	private static final Log log = Logger.getLog(OpponentGunWatcher.class);
	
	private Map<String, Double> opponentEnergyMap = new HashMap<String, Double>();
	private String lastScannedOpponent = null;
	private List<OpponentGunListener> opponentGunListeners = new ArrayList<OpponentGunListener>();
	private SnapshotHistory snapshotHistory;
	private boolean logEnemyBulletFiredStats;
	private double previousAssumedGunEnergyDrop;
	private long previousAssumedGunEnergyDropDetectedTime;
	private ScannedRobotEvent previousEvent;
	private boolean collisionOnLastAssumedGunFire;
	
	public OpponentGunWatcher(SnapshotHistory snapshotHistory, Configuration configuration) {
		this.snapshotHistory = snapshotHistory;
		RobotEvents robotEvents = Resources.getRobotEvents();
		robotEvents.addCollisionListener(this);
		robotEvents.addScannedRobotListener(this);
		robotEvents.addBulletHitListener(this);
		robotEvents.addRoundBeginListener(this);
		this.logEnemyBulletFiredStats = configuration.isLogEnemyBulletFiredStats();
	}
	
	public void addOpponentGunListener(OpponentGunListener listener) {
		this.opponentGunListeners.add(listener);
	}
	
	@Override
	public void onRoundBegin() {
		opponentEnergyMap.clear();
		previousEvent = null;
		previousAssumedGunEnergyDrop = 0;
		previousAssumedGunEnergyDropDetectedTime = 0;
	}

	public void onScannedRobot(ScannedRobotEvent event) {
		String opponentName = event.getName();
		Double opponentEnergy = opponentEnergyMap.get(opponentName);
		if (opponentEnergy == null || !opponentName.equals(lastScannedOpponent)) {
			// if not scanned before or recently, just update the energy value
			opponentEnergyMap.put(opponentName, Double.valueOf(event.getEnergy()));
		} else {
			// we only look closer when same opponent is repeatedly scanned
			double energyDrop = opponentEnergy.doubleValue() - event.getEnergy();
			if (energyDrop != 0) {
				opponentEnergyMap.put(opponentName, event.getEnergy());
				long tslf = 0;
				int tfc = 0; 
				if (previousAssumedGunEnergyDrop > 0) {
					tslf = event.getTime() - previousAssumedGunEnergyDropDetectedTime; 
					tfc = RCPhysics.getTimeUntilGunCool(previousAssumedGunEnergyDrop);					
				}
				double decelRate = 0;
				if (previousEvent != null) {
					decelRate = Math.abs(event.getVelocity() - previousEvent.getVelocity()) / (event.getTime() - previousEvent.getTime());
				}
				boolean collision = decelRate > -RCPhysics.DECELERATION_RATE;
				if ((tslf >= tfc || collisionOnLastAssumedGunFire) 
						&& energyDrop > 0 && (energyDrop <= RCPhysics.MAX_FIRE_POWER || collision)
						&& (energyDrop > 0.15 || (tslf < 200 && tslf > 2))) {
					// only assume bullet fired if energy drop is in proper firepower range
					// also try to avoid recording a no-fire energy drain as a bullet being fired
					// try to obtain snapshots from (time-1) as scan is likely one tick before bullet was fired
					
					if (collision && previousAssumedGunEnergyDrop > 0) {
						// No way to know for sure what collision damage was; WAG that bullet power was same as last shot
						energyDrop = previousAssumedGunEnergyDrop;
					}
					
					// if collision occurred, we don't know if bullet was really fired; remember this so we can ignore the next gun heat check
					collisionOnLastAssumedGunFire = collision;
					
					// now get the snapshots
					Snapshot snapshot = snapshotHistory.getSnapshot(opponentName, event.getTime()-1, false);
					if (snapshot == null) {
						snapshot = snapshotHistory.getSnapshot(opponentName);
						log.warn("Desired enemy snapshot not available; using alternate snapshot from time " + snapshot.getTime());
						//log.warn("History times available: " + robotHistory.getHistoryTimesAvailable(opponentName));
					}
					// my data should come from 2 ticks ago (most likely)
					Snapshot mySnapshot = snapshotHistory.getMySnapshot(event.getTime()-2, false);
					if (mySnapshot == null) {
						mySnapshot = snapshotHistory.getMySnapshot();
						log.warn("My desired snapshot not available; using alternate snapshot from time " + mySnapshot.getTime());
						//log.warn("My history times available: " + robotHistory.getMyHistoryTimesAvailable());
					}
					OpponentGunFiredEvent ogfEvent = new OpponentGunFiredEvent(energyDrop, mySnapshot, snapshot, event.getTime());
					for (OpponentGunListener listener : opponentGunListeners) {
						listener.opponentGunFired(ogfEvent);
					}
					if (logEnemyBulletFiredStats) {
						log.stat("Enemy fired bullet at time " + snapshot.getTime() + " from position " + Logger.formatPosition(snapshot.getX(), snapshot.getY()));
						log.stat("Enemy presumed to have scanned me at time " + mySnapshot.getTime());
					}
					previousAssumedGunEnergyDrop = energyDrop;
					previousAssumedGunEnergyDropDetectedTime = event.getTime();
				}
			}
		}
		previousEvent = event;
		lastScannedOpponent = opponentName;
	}

	public void onBulletHit(BulletHitEvent event) {
		String opponentName = event.getName();
		Double oppEnergy = opponentEnergyMap.get(opponentName);
		if (oppEnergy != null) {  
			// subtract bullet damage only -- there may also be an opponent bullet fired
			double bulletDamage = RCPhysics.getBulletDamage(event.getBullet().getPower());
			opponentEnergyMap.put(opponentName, Double.valueOf(oppEnergy.doubleValue()-bulletDamage));
		} else {
			opponentEnergyMap.put(opponentName, Double.valueOf(event.getEnergy()));
		}
	}

	public void onBulletHitBullet(BulletHitBulletEvent event) {
		// no action required
	}

	public void onBulletMissed(BulletMissedEvent event) {
		// no action required
	}

	public void onHitByBullet(HitByBulletEvent event) {
		String opponentName = event.getName();
		Double opponentEnergy = opponentEnergyMap.get(opponentName);
		if (opponentEnergy != null) {
			double updatedEnergy = opponentEnergy.doubleValue() + RCPhysics.getEnergyRegained(event.getPower());
			opponentEnergyMap.put(opponentName, Double.valueOf(updatedEnergy));
		}
	}

	public void onHitRobot(HitRobotEvent event) {
		// account for energy drop due to collision
		opponentEnergyMap.put(event.getName(), Double.valueOf(event.getEnergy()));
	}

	public void onHitWall(HitWallEvent event) {
		// no action required
	}
}
