package xander.cat.group.shield;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.event.MyWaveListener;
import xander.core.event.OpponentWaveListener;
import xander.core.gun.AbstractGun;
import xander.core.gun.Aim;
import xander.core.math.Linear;
import xander.core.math.LinearIntercept;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.track.Snapshot;
import xander.core.track.Wave;
import xander.core.track.XBulletWave;

/**
 * Gun for bullet shielding.
 * 
 * @author Scott Arnold
 */
public class BulletShieldingGun extends AbstractGun implements MyWaveListener, OpponentWaveListener, BulletShieldingListener {

	public static final String NAME = "Bullet Shielding Gun";
	
//	private static final Log log = Logger.getLog(BulletShieldingGun.class);
	
	private BulletTargeter[] bulletTargeters;
	private int[] bulletTargeterMatches;
	private Map<Wave, double[]> waveAims = new HashMap<Wave, double[]>();
	private Map<Wave, Integer> selectedAims = new HashMap<Wave, Integer>();
	private RobotProxy robotProxy;
	private BulletShieldingController controller;
	private Wave targetedWave;
	private Snapshot targetedSnapshot;
	private double maxFirePower = RCPhysics.MIN_FIRE_POWER;
	private int firePowerAdjustedCount;
	private int totalCount;
	private int lastMissIdx = -1;
	private boolean allowFinishingShot;
	private int fireCheck;
	private String fireCheckString;
	
	public BulletShieldingGun(BulletShieldingController controller, BulletTargeter... bulletTargeters) {
		if (bulletTargeters == null) {
			throw new IllegalArgumentException("There must be at least 1 bullet targeter provided.");
		}
		this.controller = controller;
		controller.addBulletShieldingListener(this);
		this.bulletTargeters = bulletTargeters;
		this.bulletTargeterMatches = new int[bulletTargeters.length];
		this.robotProxy = Resources.getRobotProxy();
		Resources.getWaveHistory().addMyWaveListener(this);
		Resources.getWaveHistory().addOpponentWaveListener(this);
	}
	
	public BulletShieldingGun(BulletShieldingController controller, double maxAdjustedFirePower, BulletTargeter... bulletTargeters) {
		this(controller, bulletTargeters);
		this.maxFirePower = maxAdjustedFirePower;
	}
	
	public void setAllowFinishingShot(boolean allowFinishingShot) {
		this.allowFinishingShot = allowFinishingShot;
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void onRoundBegin() {
		super.onRoundBegin();
		if (fireCheckString != null) {
			controller.setFireCheckString(fireCheckString);
		}
		this.waveAims.clear();
		this.selectedAims.clear();
		reset();
		fireCheck =0;
	}

	public double getFirePowerAdjustedRatio() {
		return (firePowerAdjustedCount == 0)? 0d : (double)firePowerAdjustedCount / (double)totalCount; 
	}
	
	public void reset() {
		this.targetedSnapshot = null;
		this.targetedWave = null;
	}
	
	@Override
	public Aim getAim(Snapshot target, Snapshot myself) {
		if (targetedWave == null) {
			targetedWave = controller.requestWaveToShieldAgainst();
			if (targetedWave != null) {
				// find current best matching targeter
				int targeterIdx = 0;
				int mostMatches = bulletTargeterMatches[0];
				boolean tie = false;
				for (int i=1; i<bulletTargeters.length; i++) {
					if (bulletTargeterMatches[i] > mostMatches) {
						tie = false;
						targeterIdx = i;
						mostMatches = bulletTargeterMatches[i];
					} else if (bulletTargeterMatches[i] == mostMatches) {
						tie = true;
					}
				}
				// check for most matches tie and pick a different targeter if the selected on missed it's last attempt
				if (tie && lastMissIdx == targeterIdx) {
					for (int i=bulletTargeters.length-1; i>=0; i--) {
						if (i != lastMissIdx && bulletTargeterMatches[i] == mostMatches) {
							targeterIdx = i;
						}
					}
				}
				// calculate aims for this wave
				double[] aims = waveAims.get(targetedWave);
				if (aims == null) {
					aims = new double[bulletTargeters.length];
					for (int i=0; i<bulletTargeters.length; i++) {
						aims[i] = bulletTargeters[i].getAim(targetedWave);
					}
					waveAims.put(targetedWave, aims);
				}
				// get aim for best targeter and create mock snapshot to be used in targeting
				selectedAims.put(targetedWave, Integer.valueOf(targeterIdx));
				targetedSnapshot = new Snapshot("Opponent Bullet", targetedWave.getOriginX(), targetedWave.getOriginY(), 
						aims[targeterIdx], targetedWave.getBulletVelocity(), 
						targetedWave.getInitialDefenderSnapshot().getDistance(), 100, targetedWave.getOriginTime()-1);
			} else if (allowFinishingShot) {
				Snapshot los = Resources.getSnapshotHistory().getLastOpponentScanned();
				if (los != null && los.getEnergy() <= 0 && Resources.getWaveHistory().getOpponentWaveCount() == 0) {
					// opponent is out of energy and there are no waves in the air; finish her off with head-on shot
					double aimHeading = RCMath.getRobocodeAngle(robotProxy.getX(), robotProxy.getY(), los.getX(), los.getY());
					return new Aim(aimHeading, RCPhysics.MIN_FIRE_POWER);
				} 
			}
		}
		if (targetedSnapshot != null && controller.requestAuthorizationToFire()) {
			// start with calculating intercept with minimum fire power bullet
			LinearIntercept intercept = Linear.calculateTrajectory(targetedSnapshot, 
					robotProxy.getX(), robotProxy.getY(), RCPhysics.MAX_BULLET_VELOCITY, 
					robotProxy.getBattleFieldSize(), Resources.getTime());
			double firePower = RCPhysics.MIN_FIRE_POWER;
			if (intercept != null) {
				double mfp = Math.min(maxFirePower, targetedWave.getBulletPower());
				if (mfp > firePower && robotProxy.getEnergy() >= target.getEnergy()) {
					// calculate better intercept time (note that we can't make the time lower; original calculation was done with max bullet velocity)
					double improvedTimeToIntercept = Math.floor(intercept.getTimeToIntercept()) + 0.5;
					if (improvedTimeToIntercept < intercept.getTimeToIntercept()) {
						improvedTimeToIntercept++;
					}
					//System.out.println("Time to intercept: " + Logger.format(intercept.getTimeToIntercept(), 2) + "; Improved time: " + Logger.format(improvedTimeToIntercept, 2));
					// calculate approximate intercept position using better intercept time
					double totalTime = Resources.getTime() - targetedSnapshot.getTime() + improvedTimeToIntercept;
					double travelDistance = targetedSnapshot.getVelocity() * totalTime;
					Point2D.Double improvedInterceptPos = RCMath.getLocation(
							targetedSnapshot.getX(), targetedSnapshot.getY(), 
							travelDistance, targetedSnapshot.getHeadingRoboDegrees());
					// calculate improved fire power and bullet speed
					double myShieldingShotTravelDistance = RCMath.getDistanceBetweenPoints(
							robotProxy.getX(), robotProxy.getY(), improvedInterceptPos.x, improvedInterceptPos.y);
					//System.out.println("Intercept dist: " + Logger.format(intercept.getDistanceToIntercept(), 2) + "; Improved dist: " + Logger.format(myShieldingShotTravelDistance, 2));
					double improvedBulletSpeed = myShieldingShotTravelDistance / improvedTimeToIntercept;
					double improvedFirePower = RCPhysics.getBulletPower(improvedBulletSpeed);
					if (improvedFirePower <= mfp) {
						// better power allowable, calculate new intercept
						LinearIntercept improvedIntercept = Linear.calculateTrajectory(targetedSnapshot, 
								robotProxy.getX(), robotProxy.getY(), improvedBulletSpeed, 
								robotProxy.getBattleFieldSize(), Resources.getTime());		
						if (improvedIntercept != null) {
							intercept = improvedIntercept;
							firePower = improvedFirePower;
							firePowerAdjustedCount++;
						}
					}
				}
				totalCount++;
				// return the aim
				//System.out.println("Ideal fire power is set to " + Logger.format(improvedFirePower, 2));
				return new Aim(intercept.getVelocityVector().getRoboAngle(), firePower);
			} else {
				// can't compute intercept; throw away the wave in order to move onto the next
				reset();
			}
		} 
		if (fireCheckString == null && fireCheck > 3) {
			String twave = (targetedWave == null)? "null" : targetedWave.toString();
			fireCheckString = "dstate:" + controller.getDstate().toString() + ";twave:" + twave;
		}
		return null;
	}

	private void updateMatches(Wave wave, Bullet bullet) {
		double[] aims = waveAims.get(wave);
		if (aims != null) {
			double actualAim = bullet.getHeadingRadians();
//			log.info("actual aim: " + Logger.format(actualAim, 4) + "; aims: " + Logger.format(aims, 4));
//			double[] diffs = new double[aims.length];
//			for (int i=0; i<aims.length; i++) {
//				diffs[i] = Math.abs(aims[i] - actualAim);
//			}
//			log.info("Differences: " + Logger.format(diffs, 4));
			for (int i=0; i<aims.length; i++) {
				if (Math.abs(actualAim - aims[i]) < 0.003) {
					bulletTargeterMatches[i]++;
				}
			}
			waveAims.remove(wave);
			selectedAims.remove(wave);
		}
	}
	
	@Override
	public boolean canFireAt(Snapshot target) {
		return true;
	}
	
	@Override
	public void myWaveCreated(XBulletWave wave) {
		reset();
		fireCheck--;
	}

	@Override
	public void myWaveHitBullet(XBulletWave wave, Bullet myBullet) {
		// no action required
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

	@Override
	public void oppWaveCreated(Wave wave) {
		fireCheck++;
		// no action required
	}

	@Override
	public void oppWaveHitBullet(Wave wave, Bullet oppBullet) {
		updateMatches(wave, oppBullet);
	}

	@Override
	public void oppWaveHit(Wave wave) {
		// no action required
	}

	@Override
	public void oppNextWaveToHit(Wave wave) {
		// no action required
	}

	@Override
	public void oppBulletHit(Wave wave, HitByBulletEvent hitByBulletEvent) {
		updateMatches(wave, hitByBulletEvent.getBullet());
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
		waveAims.remove(wave);
		selectedAims.remove(wave);
	}

	@Override
	public void shieldingShotHit(XBulletWave myWave, Wave opponentWave) {
		// no action required
	}

	@Override
	public void shieldingShotMissed(XBulletWave myWave, Wave opponentWave) {
		Integer aimIdx = selectedAims.get(opponentWave);
		if (aimIdx != null) {
			lastMissIdx = aimIdx.intValue();
		}
	}
}
