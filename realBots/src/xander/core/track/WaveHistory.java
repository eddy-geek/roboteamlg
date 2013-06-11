package xander.core.track;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;

import xander.core.Configuration;
import xander.core.Resources;
import xander.core.RobotEvents;
import xander.core.RobotProxy;
import xander.core.drive.DriveOptions;
import xander.core.drive.DriveState;
import xander.core.event.BulletHitListener;
import xander.core.event.GunFiredEvent;
import xander.core.event.GunListener;
import xander.core.event.MyVirtualWaveListener;
import xander.core.event.MyWaveListener;
import xander.core.event.OpponentGunFiredEvent;
import xander.core.event.OpponentGunListener;
import xander.core.event.OpponentWaveListener;
import xander.core.event.RoundBeginListener;
import xander.core.event.TurnListener;
import xander.core.gun.GunController;
import xander.core.log.Log;
import xander.core.log.Logger;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.math.RelativeAngleRange;
import xander.paint.Paintable;
import xander.paint.Paintables;

/**
 * Maintains information on all bullet waves.
 * 
 * @author Scott Arnold
 */
public class WaveHistory implements RoundBeginListener, GunListener, OpponentGunListener, TurnListener, BulletHitListener, Paintable {

	private static final Log log = Logger.getLog(WaveHistory.class);
	
	private static class WaveParams {
		Snapshot attacker;  // really just need wave origin, but using snapshot of attacker works out easier
		Snapshot defender;
		double bulletPower;
		public WaveParams(Snapshot attacker, Snapshot defender, double bulletPower) {
			this.attacker = attacker;
			this.defender = defender;
			this.bulletPower = bulletPower;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((attacker == null) ? 0 : attacker.hashCode());
			long temp;
			temp = Double.doubleToLongBits(bulletPower);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result
					+ ((defender == null) ? 0 : defender.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WaveParams other = (WaveParams) obj;
			if (attacker == null) {
				if (other.attacker != null)
					return false;
			} else if (!attacker.equals(other.attacker))
				return false;
			if (Double.doubleToLongBits(bulletPower) != Double
					.doubleToLongBits(other.bulletPower))
				return false;
			if (defender == null) {
				if (other.defender != null)
					return false;
			} else if (!defender.equals(other.defender))
				return false;
			return true;
		}
	}
	
	private List<Wave> opponentWaves = new ArrayList<Wave>();
	private List<XBulletWave> myWaves = new ArrayList<XBulletWave>();
	private List<XBulletWave> myVirtualWaves = new ArrayList<XBulletWave>();
	private List<MyWaveListener> myWaveListeners = new ArrayList<MyWaveListener>();
	private List<MyVirtualWaveListener> myVirtualWaveListeners = new ArrayList<MyVirtualWaveListener>();
	private List<OpponentWaveListener> oppWaveListeners = new ArrayList<OpponentWaveListener>();
	private Map<WaveParams,RelativeAngleRange> meaCache = new HashMap<WaveParams,RelativeAngleRange>();
	private Wave oppNextWaveToHit;
	private SnapshotHistory snapshotHistory;
	private double maxWaveSaveDistance;
	private Rectangle2D.Double battleFieldBounds;
	private DriveOptions myDriveOptions;
	private DriveOptions opponentDriveOptions;
	
	public WaveHistory(GunController gunController, 
			OpponentGunWatcher opponentGunWatcher, 
			RobotEvents robotEvents, 
			RobotProxy robotProxy, 
			SnapshotHistory snapshotHistory,
			Configuration configuration) {
		this.snapshotHistory = snapshotHistory;
		if (configuration.isUsePreciseMEAForOpponentWaves()) {
			this.myDriveOptions = new DriveOptions(30, 
					robotProxy.getBattleFieldSize(), 
					configuration.getMyPreciseMEADriveBounds());
		}
		if (configuration.isUsePreciseMEAForMyWaves()) {
			this.opponentDriveOptions = new DriveOptions(30,
					robotProxy.getBattleFieldSize(),
					configuration.getOpponentPreciseMEADriveBounds());
		}
		gunController.addGunListener(this);
		opponentGunWatcher.addOpponentGunListener(this);
		robotEvents.addRoundBeginListener(this);
		robotEvents.addTurnListener(this);
		robotEvents.addBulletHitListener(this);
		this.maxWaveSaveDistance = robotProxy.getBattleFieldDiagonal();
		this.battleFieldBounds = robotProxy.getBattleFieldSize();
		Paintables.addPaintable(this);
	}

	@Override
	public String getPainterName() {
		return null;  // no specific painter for instances of this class
	}

	@Override
	public void onRoundBegin() {
		opponentWaves.clear();
		myWaves.clear();
		myVirtualWaves.clear();
	}

	public int getOpponentWaveCount() {
		return opponentWaves.size();
	}
	
	/**
	 * Return count of opponent waves that have not passed us yet.
	 * 
	 * @return    count of opponent waves that have not passed us yet
	 */
	public int getOpponentActiveWaveCount() {
		int count = 0;
		for (Wave wave : opponentWaves) {
			if (!wave.isPassed()) {
				count++;
			}
		}
		return count;
	}
	
	public void addMyWaveListener(MyWaveListener listener) {
		myWaveListeners.add(listener);
	}
	
	public void addMyVirtualWaveListener(MyVirtualWaveListener listener) {
		myVirtualWaveListeners.add(listener);
	}
	
	public void addOpponentWaveListener(OpponentWaveListener listener) {
		oppWaveListeners.add(listener);
	}
	
	public List<Wave> getOpponentWaves() {
		return opponentWaves;
	}
	
	public List<XBulletWave> getMyWaves() {
		return myWaves;
	}
	
	public List<XBulletWave> getMyVirtualWaves() {
		return myVirtualWaves;
	}
	
	private RelativeAngleRange getMEA(Wave wave, Snapshot defenderSnapshot, long bulletFiredTime, boolean opponentWave) {
		if (!opponentWave && opponentDriveOptions != null) {
			DriveState defenderDriveState = new DriveState(defenderSnapshot);
			opponentDriveOptions.computeDriveOptions(wave, defenderDriveState, bulletFiredTime);
			return opponentDriveOptions.getMEA();
		} else if (opponentWave && myDriveOptions != null) {
			DriveState defenderDriveState = new DriveState(defenderSnapshot);
			myDriveOptions.computeDriveOptions(wave, defenderDriveState, bulletFiredTime);
			return myDriveOptions.getMEA();			
		} else {
			double simpleMEA = RCMath.getMaximumEscapeAngle(wave.getBulletVelocity());
			return new RelativeAngleRange(-simpleMEA, simpleMEA, "WaveHistory.getMEA");
		}
	}
	
	public Wave createWave(Snapshot defenderSnapshot, Snapshot attackerSnapshot, double bulletPower, long bulletFiredTime, boolean opponentWave) {
		WaveParams wp = new WaveParams(attackerSnapshot, defenderSnapshot, bulletPower);
		Wave wave = null;
		if (opponentWave) {
			wave = new Wave(defenderSnapshot, attackerSnapshot, bulletPower, bulletFiredTime);				
		} else {
			wave = new XBulletWave(defenderSnapshot, attackerSnapshot, bulletPower, bulletFiredTime);
		}
		RelativeAngleRange mea = meaCache.get(wp);
		if (mea == null) {
			mea = getMEA(wave, defenderSnapshot, bulletFiredTime, opponentWave);
			meaCache.put(wp, mea);
		}
		wave.initialMEA = mea;
		return wave;
	}
	
	public XBulletWave createXBulletWave(Snapshot defenderSnapshot, Snapshot attackerSnapshot,
			XBullet bullet, String gunName, long bulletFiredTime, boolean opponentWave) {
		XBulletWave wave = (XBulletWave) createWave(defenderSnapshot, attackerSnapshot, bullet.getPower(), bulletFiredTime, opponentWave);
		wave.gunName = gunName;
		wave.xbullet = bullet;
		return wave;		
	}
	
	public Wave getOpponentWaveAfter(Wave wave, double myX, double myY) {
		long time = Resources.getTime();
		long timeToHit = wave.getTimeUntilHit(myX, myY, time);
		long closestTimeToHitAfter = Long.MAX_VALUE;
		Wave closestWaveAfter = null;
		for (Wave waveAfter : opponentWaves) {
			if (wave != waveAfter && waveAfter.isLeading()) {
				long timeToHitAfter = waveAfter.getTimeUntilHit(myX, myY, time);
				if (timeToHitAfter >= timeToHit && timeToHitAfter < closestTimeToHitAfter) {
					closestTimeToHitAfter = timeToHitAfter;
					closestWaveAfter = waveAfter;
				}
			}
		}
		return closestWaveAfter;
	}
	
	private void addBulletShadow(XBulletWave myWave, Wave opponentWave, long time) {
		if (myWave.getState() == WaveState.LEADING && opponentWave.getState() == WaveState.LEADING) {
			
			// find time at which my bullet will intersect opponent wave
			time--;
			double myBulletToOppWaveOriginDistance = 0;
			double oppWaveDistance = 0;
			Point2D.Double myBulletPosition = null;
			do {
				time++;
				myBulletPosition = RCMath.getLocation(
						myWave.getOriginX(), myWave.getOriginY(), 
						myWave.getBulletTravelDistance(time), myWave.getXBullet().getAim());
				myBulletToOppWaveOriginDistance = RCMath.getDistanceBetweenPoints(
						myBulletPosition, opponentWave.getOrigin());
				oppWaveDistance = opponentWave.getBulletTravelDistance(time);
			} while (myBulletToOppWaveOriginDistance > oppWaveDistance && battleFieldBounds.contains(myBulletPosition));
			
			if (!battleFieldBounds.contains(myBulletPosition)) {
				// my bullet leaves battlefield before intersection occurs, no shadow to add
				return;
			}
			
			// calculate bullet collision end points and intersect point
			double trailPointDistance = myWave.getBulletTravelDistance(time-1);
			double leadPointDistance = myWave.getBulletTravelDistance(time);
			Point2D.Double leadPoint = RCMath.getLocation(
					myWave.getOriginX(), myWave.getOriginY(), 
					leadPointDistance, myWave.getXBullet().getAim());
			Point2D.Double trailPoint = RCMath.getLocation(
					myWave.getOriginX(), myWave.getOriginY(), 
					trailPointDistance, myWave.getXBullet().getAim());
			
			double oppLeadPointDistance = opponentWave.getBulletTravelDistance(time);
			if (oppLeadPointDistance < RCMath.getDistanceBetweenPoints(opponentWave.getOrigin(), trailPoint)) {
				// need to calculate where opponent wave intersects bullet line
				// this intersection will be stored in trailPoint
				Point2D.Double[] intersections = RCMath.getCircleToLineIntersections(
						opponentWave.getOrigin(), oppLeadPointDistance, trailPoint, leadPoint);
				// figure out which point to use
				if (intersections == null) {
					log.error("No intersections found!"); // this shouldn't happen
					return;
				}
				if (intersections.length == 1) {
					// segment is tangent, only 1 point (not likely, but technically possible)
					trailPoint = intersections[0];
				} else {
					// line has 2 intersections with circle, find which one is on original segment
					if (leadPoint.x == trailPoint.x) {
						if (RCMath.between(intersections[0].y, leadPoint.y, trailPoint.y)) {
							trailPoint = intersections[0];
						} else if (RCMath.between(intersections[1].y, leadPoint.y, trailPoint.y)) {
							trailPoint = intersections[1];
						} else {
							log.error("Vertical segment does not contain either of the calculated intersection points!");
						}
					} else if (leadPoint.y == trailPoint.y) { 
						if (RCMath.between(intersections[0].x, leadPoint.x, trailPoint.x)) {
							trailPoint = intersections[0];
						} else if (RCMath.between(intersections[1].x, leadPoint.x, trailPoint.x)) {
							trailPoint = intersections[1];
						} else {
							log.error("Horizontal segment does not contain either of the calculated intersection points!");
						}					
					} else {
						double x = Math.min(leadPoint.x, trailPoint.x);
						double y = Math.min(leadPoint.y, trailPoint.y);
						double w = Math.abs(leadPoint.x - trailPoint.x);
						double h = Math.abs(leadPoint.y - trailPoint.y);
						Rectangle2D.Double segmentBounds = new Rectangle2D.Double(x, y, w, h);
						if (segmentBounds.contains(intersections[0])) {
							trailPoint = intersections[0];
						} else if (segmentBounds.contains(intersections[1])) {
							trailPoint = intersections[1];
						} else {
							// this should never happen
							log.error("Segment bounds does not contain either of the calculated intersection points!");
						}
					}
				}
			} 
			
			// calculate shadow angles and apply shadow to opponent wave
			double angle1 = RCMath.getRobocodeAngle(opponentWave.getOrigin(), leadPoint);
			double angle2 = RCMath.getRobocodeAngle(opponentWave.getOrigin(), trailPoint);
			double turnAngle = RCMath.getTurnAngle(angle1, angle2);
			if (turnAngle < 0) {
				opponentWave.addBulletShadow(new BulletShadow(angle2, angle1));
			} else {
				opponentWave.addBulletShadow(new BulletShadow(angle1, angle2));
			}
		}
	}
	
	@Override
	public void gunFired(GunFiredEvent event) {
		long adjustedFireTime = Resources.getTime();
		XBullet xbullet = new XBullet(event.getMySnapshot().getLocation(), event.getAim(), event.getPower());
		XBulletWave wave = createXBulletWave(event.getOpponentSnapshot(), event.getMySnapshot(), xbullet, event.getGun().getName(), adjustedFireTime, false);
		myWaves.add(wave);
		for (Wave opponentWave : opponentWaves) {
			addBulletShadow(wave, opponentWave, Resources.getTime());
			for (OpponentWaveListener listener : oppWaveListeners) {
				listener.oppWaveUpdated(opponentWave);
			}
		}
		for (MyWaveListener listener : myWaveListeners) {
			listener.myWaveCreated(wave);
		}
	}

	@Override
	public void virtualGunFired(GunFiredEvent event) {
		long adjustedFireTime = Resources.getTime();
		XBullet xbullet = new XBullet(event.getMySnapshot().getLocation(), event.getAim(), event.getPower());
		XBulletWave wave = createXBulletWave(event.getOpponentSnapshot(), event.getMySnapshot(), xbullet, event.getGun().getName(), adjustedFireTime, false);
		myVirtualWaves.add(wave);
		for (MyVirtualWaveListener listener : myVirtualWaveListeners) {
			listener.myVirtualWaveCreated(wave);
		}
	}

	@Override
	public void opponentGunFired(OpponentGunFiredEvent event) {
		long adjustedFireTime = event.getTime()-1;
		double adjustedFirePower = Math.max(RCPhysics.MIN_FIRE_POWER, event.getPower()); //TODO: Monitor this; probable bug in Robocode 1.7.3.x and may change
		Wave wave = createWave(event.getMySnapshot(), event.getOpponentSnapshot(), adjustedFirePower, adjustedFireTime, true);
		opponentWaves.add(wave);
		for (XBulletWave myWave : myWaves) {
			addBulletShadow(myWave, wave, Resources.getTime());
		}
		for (OpponentWaveListener listener : oppWaveListeners) {
			listener.oppWaveCreated(wave);
		}
	}

	private void updateMyWaves(long time) {
		Snapshot oppSnapshot = null;
		for (Iterator<XBulletWave> iter = myWaves.iterator(); iter.hasNext();) {
			XBulletWave wave = iter.next();
			if (oppSnapshot == null || !oppSnapshot.getName().equals(wave.getInitialDefenderSnapshot().getName())) {
				oppSnapshot = snapshotHistory.getSnapshot(wave.getInitialDefenderSnapshot().getName());
			}
			double waveDistance = wave.getBulletTravelDistance(time);
			double oppDistance = RCMath.getDistanceBetweenPoints(wave.getOrigin(), oppSnapshot.getLocation());
			if (wave.getState() == WaveState.LEADING && waveDistance >= oppDistance - RCPhysics.ROBOT_HALF_WIDTH) {
				wave.state = WaveState.HIT;
				for (MyWaveListener listener : myWaveListeners) {
					listener.myWaveHit(wave, oppSnapshot);
				}
			}
			if (wave.getState() == WaveState.HIT && waveDistance >= oppDistance) {
				wave.state = WaveState.PASSING;
				for (MyWaveListener listener : myWaveListeners) {
					listener.myWavePassing(wave, oppSnapshot);
				}
			}
			if (wave.getState() == WaveState.PASSING && waveDistance >= oppDistance + RCPhysics.ROBOT_HALF_WIDTH) {
				wave.state = WaveState.PASSED;
				for (MyWaveListener listener : myWaveListeners) {
					listener.myWavePassed(wave, oppSnapshot);
				}				
			}
			if (wave.getState() == WaveState.PASSED && waveDistance > maxWaveSaveDistance) {
				for (MyWaveListener listener : myWaveListeners) {
					listener.myWaveDestroyed(wave);
				}
				iter.remove();
			}
		}		
	}
	
	private void updateMyVirtualWaves(long time) {
		Snapshot oppSnapshot = null;
		for (Iterator<XBulletWave> iter = myVirtualWaves.iterator(); iter.hasNext();) {
			XBulletWave wave = iter.next();
			if (oppSnapshot == null || !oppSnapshot.getName().equals(wave.getInitialDefenderSnapshot().getName())) {
				oppSnapshot = snapshotHistory.getSnapshot(wave.getInitialDefenderSnapshot().getName());
			}
			double waveDistance = wave.getBulletTravelDistance(time);
			double oppDistance = RCMath.getDistanceBetweenPoints(wave.getOrigin(), oppSnapshot.getLocation());
			if (wave.getState() == WaveState.LEADING && waveDistance >= oppDistance - RCPhysics.ROBOT_HALF_WIDTH) {
				wave.state = WaveState.HIT;
				for (MyVirtualWaveListener listener : myVirtualWaveListeners) {
					listener.myVirtualWaveHit(wave);
				}
				double waveDistanceToOpponent = RCMath.getDistanceBetweenPoints(wave.getOrigin(), oppSnapshot.getLocation());
				Point2D.Double expectedRobotPosition = RCMath.getLocation(
						wave.getOriginX(), wave.getOriginY(), 
						waveDistanceToOpponent, wave.getXBullet().getAim());
				double diff = RCMath.getDistanceBetweenPoints(oppSnapshot.getLocation(), expectedRobotPosition);
				if (diff <= RCPhysics.ROBOT_HALF_WIDTH) {
					for (MyVirtualWaveListener listener : myVirtualWaveListeners) {
						listener.myVirtualBulletHit(wave);
					}
				}
			}
			if (wave.getState() == WaveState.HIT && waveDistance >= oppDistance) {
				wave.state = WaveState.PASSING;
				for (MyVirtualWaveListener listener : myVirtualWaveListeners) {
					listener.myVirtualWavePassing(wave);
				}
			}
			if (wave.getState() == WaveState.PASSING && waveDistance >= oppDistance + RCPhysics.ROBOT_HALF_WIDTH) {
				wave.state = WaveState.PASSED;
				for (MyVirtualWaveListener listener : myVirtualWaveListeners) {
					listener.myVirtualWavePassed(wave);
				}				
			}
			if (wave.getState() == WaveState.PASSED && waveDistance > maxWaveSaveDistance) {
				for (MyVirtualWaveListener listener : myVirtualWaveListeners) {
					listener.myVirtualWaveDestroyed(wave);
				}
				iter.remove();
			}
		}		
	}	
	
	private void updateOpponentWaves(long time) {
		Snapshot mySnapshot = snapshotHistory.getMySnapshot(time, true);	
		Wave nextWaveToHit = null;
		long minToHitTime = Long.MAX_VALUE;
		for (Iterator<Wave> iter = opponentWaves.iterator(); iter.hasNext();) {
			Wave wave = iter.next();
			double waveDistance = wave.getBulletTravelDistance(time);
			double myDistance = RCMath.getDistanceBetweenPoints(wave.getOrigin(), mySnapshot.getLocation());
			if (wave.getState() == WaveState.LEADING && waveDistance >= myDistance - RCPhysics.ROBOT_HALF_WIDTH) {
				wave.state = WaveState.HIT;
				for (OpponentWaveListener listener : oppWaveListeners) {
					listener.oppWaveHit(wave);
				}
			}
			if (wave.getState() == WaveState.HIT && waveDistance >= myDistance) {
				wave.state = WaveState.PASSING;
				for (OpponentWaveListener listener : oppWaveListeners) {
					listener.oppWavePassing(wave);
				}
			}
			if (wave.getState() == WaveState.PASSING && waveDistance >= myDistance + RCPhysics.ROBOT_HALF_WIDTH) {
				wave.state = WaveState.PASSED;
				for (OpponentWaveListener listener : oppWaveListeners) {
					listener.oppWavePassed(wave);
				}				
			}
			if (wave.getState() == WaveState.PASSED && waveDistance > maxWaveSaveDistance) {
				for (OpponentWaveListener listener : oppWaveListeners) {
					listener.oppWaveDestroyed(wave);
				}
				iter.remove();
			}
			if (wave.getState() == WaveState.LEADING) {
				long timeToHit = wave.getTimeUntilHit(mySnapshot.getX(), mySnapshot.getY(), mySnapshot.getTime());
				if (timeToHit < minToHitTime) {
					minToHitTime = timeToHit;
					nextWaveToHit = wave;
				}
			}
		}
		if (nextWaveToHit != this.oppNextWaveToHit) {
			for (OpponentWaveListener listener : oppWaveListeners) {
				listener.oppNextWaveToHit(nextWaveToHit);
			}
			this.oppNextWaveToHit = nextWaveToHit;
		}
	}
	
	@Override
	public void onTurnBegin() {
		// update wave states
		long time = Resources.getTime();
		meaCache.clear();
		updateMyWaves(time);
		updateMyVirtualWaves(time);
		updateOpponentWaves(time);
	}

	@Override
	public void onTurnEnd() {
		// no action required	
	}

	@Override
	public void onBulletHit(BulletHitEvent event) {
		XBulletWave wave = (XBulletWave)getMatchingWave(myWaves, event.getBullet(), event.getTime());
		if (wave != null) {
			for (MyWaveListener listener : myWaveListeners) {
				listener.myBulletHit(wave, event);
			}
		}
	}

	private Wave getMatchingWave(List<? extends Wave> waves, Bullet bullet, long time) {
		double closestWaveDist = Double.POSITIVE_INFINITY;
		Wave closestWave = null;
		for (Wave wave : waves) {
			if (RCMath.differenceLessThan(bullet.getVelocity(), wave.getBulletVelocity(), 0.05)) {
				double waveDistance = wave.getBulletTravelDistance(time);
				double bulletAngle = RCMath.getRobocodeAngle(
						wave.getOriginX(), wave.getOriginY(), bullet.getX(), bullet.getY());
				Point2D.Double wavePoint = RCMath.getLocation(
						wave.getOriginX(), wave.getOriginY(), waveDistance, bulletAngle);
				double positionDifference = RCMath.getDistanceBetweenPoints(
						wavePoint.x, wavePoint.y, bullet.getX(), bullet.getY());
				if (positionDifference < closestWaveDist) {
					closestWaveDist = positionDifference;
					closestWave = wave;
				}
			}
		}
		if (closestWaveDist <= bullet.getVelocity()*2 + 0.1) {  // allow a little tolerance in the matching
			return closestWave;
		}
		log.warn("Unable to find matching wave for bullet.");
		log.warn("Bullet: owner=" + bullet.getName() + "; velocity=" + Logger.format(bullet.getVelocity()) + "; closest wave dist=" + closestWaveDist);
		return null;
	}
	
	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		// remove the waves for the bullets
		Bullet myBullet = event.getBullet();
		Bullet oppBullet = event.getHitBullet();
		long time = event.getTime();
		XBulletWave myWave = (XBulletWave)getMatchingWave(myWaves, myBullet, time);
		if (myWave != null) {
			for (MyWaveListener listener : myWaveListeners) {
				listener.myWaveHitBullet(myWave, myBullet);
			}
			myWaves.remove(myWave);
		}
		Wave oppWave = getMatchingWave(opponentWaves, oppBullet, time);
		if (oppWave != null) {
			for (OpponentWaveListener listener : oppWaveListeners) {
				listener.oppWaveHitBullet(oppWave, oppBullet);
			}
			opponentWaves.remove(oppWave);
		}
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		// no action required
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		Wave wave = getMatchingWave(opponentWaves, event.getBullet(), event.getTime());
		if (wave != null) {
			for (OpponentWaveListener listener : oppWaveListeners) {
				listener.oppBulletHit(wave, event);
			}
		}
	}
}
