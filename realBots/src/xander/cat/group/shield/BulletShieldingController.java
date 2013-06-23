package xander.cat.group.shield;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import xander.core.Resources;
import xander.core.event.MyWaveListener;
import xander.core.event.OpponentWaveListener;
import xander.core.event.RoundBeginListener;
import xander.core.event.TurnListener;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.track.Snapshot;
import xander.core.track.Wave;
import xander.core.track.XBulletWave;

/**
 * Controller class for bullet shielding against opponents.  This class is designed to accept requests from the
 * bullet shielding gun and issue orders to the bullet shielding drive.
 * 
 * @author Scott Arnold
 */
public class BulletShieldingController implements RoundBeginListener, TurnListener, OpponentWaveListener, MyWaveListener {

	private static final int REQUIRED_LEAD_TIME = 4;
	
	private static class WavePair {
		private XBulletWave myShieldingWave;
		private Wave opponentWave;
		private double distanceBetween;
		public WavePair(XBulletWave myShieldingWave, Wave opponentWave) {
			this.myShieldingWave = myShieldingWave;
			this.opponentWave = opponentWave;
			this.distanceBetween = RCMath.getDistanceBetweenPoints(myShieldingWave.getOrigin(), opponentWave.getOrigin());
		}
	}
	
	private List<Wave> waveQueue = new ArrayList<Wave>();
	private Wave nextQueueWave = null;  // next wave to pull from queue when requested
	private Wave waveToShieldAgainst = null;  // holds wave after it has been requested and removed from queue
	private Wave justAddedToQueue = null;
	private boolean opponentTooClose;
	private BulletShieldingDrive bsDrive;
	private List<WavePair> wavePairs = new ArrayList<WavePair>();
	private int bulletShieldingShots;
	private int bulletShieldingHits;
	private int rollingHits;
	private int rollingShots;
	private boolean[] rollingHitLog = new boolean[32];
	private int rollingIndex;
	private int bulletShieldingConsecutiveMisses;
	private List<BulletShieldingListener> bulletShieldingListeners = new ArrayList<BulletShieldingListener>();
	private double missDamage;   // TODO: Should we not add miss damage if the shot doesn't actually hit us?
	private String fireCheckString;
	
	public BulletShieldingController(BulletShieldingDrive bsDrive) {
		Resources.getRobotEvents().addRoundBeginListener(this);
		Resources.getRobotEvents().addTurnListener(this);
		Resources.getWaveHistory().addOpponentWaveListener(this);
		Resources.getWaveHistory().addMyWaveListener(this);
		this.bsDrive = bsDrive;
	}
	
	public String getFireCheckString() {
		return fireCheckString;
	}

	public void setFireCheckString(String fireCheckString) {
		this.fireCheckString = fireCheckString;
	}

	public void addBulletShieldingListener(BulletShieldingListener listener) {
		this.bulletShieldingListeners.add(listener);
	}
	
	/**
	 * Reset system to initial state.  
	 */
	public void reset() {
		bsDrive.reset();
	}
	
	public String getDstate() {
		return bsDrive.getDstate();
	}
	
	public double getMissDamage() {
		return missDamage;
	}
	
	public double getMissDamagePerShieldingShot() {
		return (bulletShieldingShots == 0)? 0d : missDamage / (double)bulletShieldingShots;
	}
	
	public int getRequiredLeadTimeForOpponentWaves() {
		return REQUIRED_LEAD_TIME;
	}
	
	public int getBulletShieldingMisses() {
		return bulletShieldingShots - bulletShieldingHits;
	}
	
	public int getBulletShieldingShots() {
		return bulletShieldingShots;
	}
	
	public int getBulletShieldingConsecutiveMisses() {
		return bulletShieldingConsecutiveMisses;
	}
	
	public double getBulletShieldingRatio() {
		return (bulletShieldingHits == 0)? 0d : (double)bulletShieldingHits / (double)bulletShieldingShots;
	}
	
	public double getRollingBulletShieldingRatio() {
		return (rollingHits == 0)? 0d : (double)rollingHits / (double)rollingShots;
	}
	
	public Wave requestWaveToShieldAgainst() {
		if (!bsDrive.isAtStandingPosition()) {
			if (bsDrive.isAtFiringPosition()) { // bug workaround; this happens on rare occasions for some reason
				bsDrive.requestMoveToStandingPosition();
			}
			return null;
		}
		// this method initiates the firing sequence
		waveToShieldAgainst = nextQueueWave;
		if (waveToShieldAgainst != null) {
			bsDrive.requestMoveToFiringPosition(waveToShieldAgainst);
			waveQueue.remove(waveToShieldAgainst);
		}
		nextQueueWave = null;  // will get set at start of next turn
		return waveToShieldAgainst;
	}
	
	public boolean requestAuthorizationToFire() {
		return bsDrive.isAtFiringPosition();
	}
	
	@Override
	public void onRoundBegin() {
		waveQueue.clear();
		wavePairs.clear();
		nextQueueWave = null;
		waveToShieldAgainst = null;
		justAddedToQueue = null;
		opponentTooClose = false;
		bulletShieldingConsecutiveMisses = 0; // forgive consecutive misses from previous round
	}

	@Override
	public void onTurnBegin() {
		// clear out any waves that are already too close to shield against
		long closestTUH = Long.MAX_VALUE;
		for (Iterator<Wave> iter = waveQueue.iterator(); iter.hasNext();) {
			Wave wave = iter.next();
			Snapshot snap = wave.getInitialDefenderSnapshot();
			long tuh = wave.getTimeUntilHit(snap.getX(), snap.getY(), Resources.getTime());
			if ((tuh - RCPhysics.getTimeUntilGunCool()) < REQUIRED_LEAD_TIME + bsDrive.getMoveTimeNeededForWave(wave)) {
				iter.remove();
				if (nextQueueWave == wave) {
					nextQueueWave = null;
				}
				if (justAddedToQueue == wave) {
					opponentTooClose = true;
				}
			} else if (tuh < closestTUH) {
				nextQueueWave = wave;
				closestTUH = tuh;
			}
		}
	}

	public boolean isOpponentTooClose() {
		return opponentTooClose;
	}
	
	private void updateRollingHits(boolean hit) {
		if (rollingShots < rollingHitLog.length) {
			rollingShots++;
			if (hit) {
				rollingHits++;
			}
		} else if (rollingHitLog[rollingIndex] && !hit) {
			rollingHits--;
		} else if (!rollingHitLog[rollingIndex] && hit) {
			rollingHits++;
		}
		rollingHitLog[rollingIndex] = hit;
		rollingIndex++;
		if (rollingIndex >= rollingHitLog.length) {
			rollingIndex = 0;
		}
	}
	
	@Override
	public void onTurnEnd() {
		long time = Resources.getTime();
		for (Iterator<WavePair> iter = wavePairs.iterator(); iter.hasNext();) {
			// check for missed shielding shots
			WavePair wavePair = iter.next();
			double myDist = wavePair.myShieldingWave.getBulletTravelDistance(time);
			double oppDist = wavePair.opponentWave.getBulletTravelDistance(time);
			if (myDist + oppDist - RCPhysics.MAX_BULLET_VELOCITY*1.5 > wavePair.distanceBetween) { // bullet velocity in there as a fudge factor
				// shot missed
				bulletShieldingShots++;
				bulletShieldingConsecutiveMisses++;
				updateRollingHits(false);
				missDamage += RCPhysics.getBulletDamage(wavePair.opponentWave.getBulletPower());
				iter.remove();
				fireShieldingShotMissed(wavePair);
			}			
		}
	}

	@Override
	public void oppWaveCreated(Wave wave) {
		opponentTooClose = false;
		if (Math.abs(wave.getInitialDefenderSnapshot().getVelocity()) < 0.01) {  // can only use waves where we were not moving
			waveQueue.add(wave);
			justAddedToQueue = wave;
		}
	}

	@Override
	public void oppWaveHitBullet(Wave wave, Bullet oppBullet) {
		// no action required (handled by myWaveHitBullet)
	}

	@Override
	public void oppWaveHit(Wave wave) {
		waveQueue.remove(wave);
	}

	@Override
	public void oppNextWaveToHit(Wave wave) {
		// no action required
	}

	@Override
	public void oppBulletHit(Wave wave, HitByBulletEvent hitByBulletEvent) {
		// no action required
	}

	@Override
	public void oppWavePassing(Wave wave) {
		// no action required
	}

	@Override
	public void oppWavePassed(Wave wave) {
		// no action required
	}

	@Override
	public void oppWaveUpdated(Wave wave) {
		// no action required
	}

	@Override
	public void oppWaveDestroyed(Wave wave) {
		// no action required
	}

	@Override
	public void myWaveCreated(XBulletWave wave) {
		if (waveToShieldAgainst != null) {
			wavePairs.add(new WavePair(wave, waveToShieldAgainst));
			waveToShieldAgainst = null;
			bsDrive.requestMoveToStandingPosition();
		}
	}

	@Override
	public void myWaveHitBullet(XBulletWave wave, Bullet myBullet) {
		for (Iterator<WavePair> iter = wavePairs.iterator(); iter.hasNext();) {
			WavePair wavePair = iter.next();
			if (wavePair.myShieldingWave == wave) {
				iter.remove();
				bulletShieldingShots++;
				bulletShieldingHits++;
				updateRollingHits(true);
				bulletShieldingConsecutiveMisses = 0;
				fireShieldingShotHit(wavePair);
			}
		}	
	}

	private void fireShieldingShotHit(WavePair wavePair) {
		for (BulletShieldingListener listener : bulletShieldingListeners) {
			listener.shieldingShotHit(wavePair.myShieldingWave, wavePair.opponentWave);
		}
	}
	
	private void fireShieldingShotMissed(WavePair wavePair) {
		for (BulletShieldingListener listener : bulletShieldingListeners) {
			listener.shieldingShotMissed(wavePair.myShieldingWave, wavePair.opponentWave);
		}		
	}
	
	@Override
	public void myWaveHit(XBulletWave wave, Snapshot opponentSnapshot) {
		// no action required
	}

	@Override
	public void myBulletHit(XBulletWave wave, BulletHitEvent bulletHitEvent) {
		// no action required
	}

	@Override
	public void myWavePassing(XBulletWave wave, Snapshot opponentSnapshot) {
		// no action required
	}

	@Override
	public void myWavePassed(XBulletWave wave, Snapshot opponentSnapshot) {
		// no action required
	}

	@Override
	public void myWaveDestroyed(XBulletWave wave) {
		// no action required
	}

}
