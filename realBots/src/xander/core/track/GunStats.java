package xander.core.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import robocode.BattleEndedEvent;
import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import robocode.RoundEndedEvent;

import xander.core.Configuration;
import xander.core.RobotEvents;
import xander.core.RobotProxy;
import xander.core.event.MyVirtualWaveListener;
import xander.core.event.MyWaveListener;
import xander.core.event.OpponentWaveListener;
import xander.core.event.RoundListener;
import xander.core.log.Log;
import xander.core.log.Logger;
import xander.paint.Paintable;
import xander.paint.Paintables;

public class GunStats implements MyWaveListener, MyVirtualWaveListener, OpponentWaveListener, RoundListener, Paintable {
	
	private static final Log log = Logger.getLog(GunStats.class);
	
	private int[] cumulativeHitRatio = new int[3];
	private int[] oppCumulativeHitRatio = new int[3];
	private Map<String, int[]> hitRatios = new HashMap<String, int[]>();
	private Map<String, List<int[]>> rollingRatios = new HashMap<String, List<int[]>>();
	private int rollingVirtualHitRatioDepth;
	private double cumulativeOppPower;
	private int cumulativeOppShots;
	private int lastOppPowerIndex;
	private double[] lastOppPowers = new double[5]; // keep power on last 5 shots
	private long lastOppFireTime;
	private Map<String, int[]> oppHitRatios = new HashMap<String, int[]>(); // hit ratios against different drives
	private RobotProxy robotProxy;
	
	public GunStats(RobotProxy robotProxy, WaveHistory waveHistory, RobotEvents robotEvents, Configuration configuration) {
		this.robotProxy = robotProxy;
		this.rollingVirtualHitRatioDepth = configuration.getRollingVirtualHitRatioDepth();
		waveHistory.addMyVirtualWaveListener(this);
		waveHistory.addMyWaveListener(this);
		waveHistory.addOpponentWaveListener(this);
		robotEvents.addRoundListener(this);
		Paintables.addPaintable(this);
	}
	
	@Override
	public String getPainterName() {
		return null;  // no specific painter for instance of this class
	}

	/**
	 * Returns a Set of all gun names.
	 * 
	 * @return         set of all gun names
	 */
	public Set<String> getGunNames() {
		return hitRatios.keySet();
	}
	
	/**
	 * Returns a Set of all drive names that have been used.
	 * 
	 * @return         drive names that have been used
	 */
	public Set<String> getDriveNames() {
		return oppHitRatios.keySet();
	}
	
	/**
	 * Returns the hit ratio (from 0 to 1) for the gun of given name.  Interference
	 * shots are not considered when calculating the ratio.
	 * 
	 * @param gunName   name of gun
	 * 
	 * @return          hit ratio for gun
	 */
	public double getHitRatio(String gunName) {
		int[] hitRatio = hitRatios.get(gunName);
		if (hitRatio == null) {
			return 0;
		}
		int actionedBullets = hitRatio[2]-hitRatio[1];
		if (actionedBullets == 0) {
			return 0;
		}
		return (double) hitRatio[0] / (double) actionedBullets;
	}
	
	/**
	 * Returns the virtual hit ratio (from 0 to 1) for the gun of given name.  
	 * Interference shots are not considered when calculating the ratio.
	 * Actual bullets fired, in addition to virtual bullets fired, are 
	 * included in the hit ratio.
	 * 
	 * @param gunName   name of gun
	 * 
	 * @return          virtual hit ratio for gun
	 */	
	public double getVirtualHitRatio(String gunName) {
		int[] hitRatio = hitRatios.get(gunName);
		if (hitRatio == null) {
			return 0;
		}
		int actionedBullets = hitRatio[5]-hitRatio[4];
		if (actionedBullets == 0) {
			return 0;
		}
		return (double) hitRatio[3] / (double) actionedBullets;		
	}
	
	public double getRollingHitRatio(String gunName) {
		return getRollingVirtualHitRatio(gunName);
	}
	
	/**
	 * Returns the rolling virtual hit ratio (from 0 to 1) for the gun of given name.  
	 * Interference shots are not considered when calculating the ratio.
	 * Actual bullets fired, in addition to virtual bullets fired, are 
	 * included in the hit ratio.
	 * 
	 * @param gunName   name of gun
	 * 
	 * @return          virtual hit ratio for gun
	 */	
	public double getRollingVirtualHitRatio(String gunName) {
		int[] hitRatio = hitRatios.get(gunName);
		if (hitRatio == null) {
			return 0;
		}
		int actionedBullets = hitRatio[8]-hitRatio[7];
		if (actionedBullets == 0) {
			return 0;
		}
		return (double) hitRatio[6] / (double) actionedBullets;		
	}
	
	/**
	 * Returns the opponent hit ratio against the drive of given name.
	 * 
	 * @param driveName    name of drive to get hit ratio for
	 * 
	 * @return     opponent hit ratio against drive
	 */
	public double getOpponentHitRatioAgainstDrive(String driveName) {
		int[] ratio = oppHitRatios.get(driveName);
		if (ratio == null) {
			return 0;
		}
		return (double) ratio[0] / (double) ratio[1];
	}
	
	/**
	 * Returns overall hit ratio for all guns.
	 * 
	 * @return    overall hit ratio for all guns
	 */
	public double getOverallHitRatio() {
		int actionedBullets = cumulativeHitRatio[2]-cumulativeHitRatio[1];
		if (actionedBullets == 0) {
			return 0;
		}
		return (double) cumulativeHitRatio[0] / (double) actionedBullets;
	}
	
	public double getOverallInterferenceRatio() {
		if (cumulativeHitRatio[2] == 0) {
			return 0;
		} else {
			return (double)cumulativeHitRatio[1] / (double)cumulativeHitRatio[2];
		}
	}
	
	/**
	 * Returns overall hit ratio for the opponent.
	 * 
	 * @return    overall hit ratio for opponent
	 */
	public double getOverallOpponentHitRatio() {
		int actionedBullets = oppCumulativeHitRatio[2]-oppCumulativeHitRatio[1];
		if (actionedBullets == 0) {
			return 0;
		}
		return (double) oppCumulativeHitRatio[0] / (double) actionedBullets;
	}
	
	/**
	 * Returns the number of bullets fired by self.
	 * 
	 * @return    number of bullets fired by self
	 */
	public int getBulletsFired() {
		return cumulativeHitRatio[2];
	}
	
	/**
	 * Returns the number of bullets fired by opponent(s).
	 * 
	 * @return    number of bullets fired by opponent(s)
	 */
	public int getOpponentBulletsFired() {
		return oppCumulativeHitRatio[2];
	}
	
	/**
	 * Returns the number of bullets fired by the given gun that either
	 * hit or missed.  Interference bullets are not included (bullets that neither
	 * hit nor missed due to some external factor like the round ending).
	 * 
	 * @param gunName    name of gun
	 * 
	 * @return           bullets fired by gun
	 */
	public int getActionedBulletsFired(String gunName) {
		int[] hitRatio = hitRatios.get(gunName);
		if (hitRatio == null) {
			return 0;
		}
		return hitRatio[2]-hitRatio[1];
	}
	
	/**
	 * Returns the number of virtual bullets fired by the given gun that either
	 * hit or missed.  Interference bullets are not included (bullets that neither
	 * hit nor missed due to some external factor like the round ending).
	 * Actual bullets ARE included in the total count.
	 * 
	 * @param gunName    name of gun
	 * 
	 * @return           bullets fired by gun
	 */	
	public int getVirtualBulletsFired(String gunName) {
		int[] hitRatio = hitRatios.get(gunName);
		if (hitRatio == null) {
			return 0;
		}
		return hitRatio[5]-hitRatio[4];
	}
	
	/**
	 * Returns the rolling number of virtual bullets fired by the given gun that
	 * either hit or missed.  Interference bullets are not included (bullets that 
	 * neither hit nor missed due to some external factor like the round ending).
	 * Actual bullets ARE included in the total count.
	 * 
	 * @param gunName    name of gun
	 * 
	 * @return           bullets fired by gun
	 */	
	public int getRollingVirtualBulletsFired(String gunName) {
		int[] hitRatio = hitRatios.get(gunName);
		if (hitRatio == null) {
			return 0;
		}
		return hitRatio[8]-hitRatio[7];
	}
	
	/**
	 * Returns the number of shots against the drive of given name.
	 * 
	 * @param driveName   name of drive
	 * 
	 * @return    number of shots against the drive of given name
	 */
	public int getShotsAgainstDrive(String driveName) {
		int[] ratio = oppHitRatios.get(driveName);
		if (ratio == null) {
			return 0;
		}
		return ratio[1];
	}
	/**
	 * Returns the hit ratio values for the gun of given name.  The returned array
	 * is of length 6.  The first integer represents the number of hits on intended
	 * target, the second integer represents the number of hits on robots or bullets
	 * that were not the intended target, and the third integer represents the 
	 * number of bullets fired.  The final three are the same, but for virtual
	 * bullets.
	 * 
	 * @param gunName    name of the gun
	 * 
	 * @return           hit ratio values for gun
	 */
	public int[] getHitRatioValues(String gunName) {
		return hitRatios.get(gunName);
	}
	
	/**
	 * Returns the average opponent bullet power over the entire battle.  A 
	 * value of 0 is returned if opponent has never fired a shot.
	 * 
	 * @return          average opponent bullet power
	 */
	public double getAverageOpponentBulletPower() {
		if (cumulativeOppShots == 0) {
			return 0;
		} else {
			return cumulativeOppPower / (double)cumulativeOppShots;
		}
	}
	
	/**
	 * Returns the average bullet power used by the opponent on the last few
	 * shots.  Values roll over from one round to the next.  A value of 0 
	 * is returned if opponent has never fired any shots.  
	 * 
	 * @return     average bullet power used by opponent on the last few shots
	 */
	public double getRollingAverageOpponentBulletPower() {
		int values = 0;
		double sum = 0;
		for (int i=0; i<lastOppPowers.length; i++) {
			sum += lastOppPowers[i];
			if (lastOppPowers[i] > 0) {
				values++;
			}
		}
		return (values == 0)? 0 : sum / (double)values;
	}
	
	/**
	 * Return the bullet power of the last opponent bullet fired.
	 * If no opponent bullets have been fired in the battle, a value
	 * of 0 is returned.  For first shot of new round, the power of the last shot
	 * of the previous round is returned.
	 * 
	 * @return       power of last opponent bullet fired
	 */
	public double getLastOpponentBulletPower() {
		return lastOppPowers[lastOppPowerIndex];
	}
	
	/**
	 * Returns the last time the opponent fired.
	 * 
	 * @return    last time opponent fired
	 */
	public long getLastOpponentFireTime() {
		return lastOppFireTime;
	}
	
	private void updateRatio(String gunName, int hits, int interferences, int fires) {
		int[] ratio = hitRatios.get(gunName);
		if (ratio == null) {
			ratio = new int[9];
			hitRatios.put(gunName, ratio);
		}
		cumulativeHitRatio[0] += hits;
		cumulativeHitRatio[1] += interferences;
		cumulativeHitRatio[2] += fires;
		ratio[0] += hits;
		ratio[1] += interferences;
		ratio[2] += fires;
		ratio[3] += hits;
		ratio[4] += interferences;
		ratio[5] += fires;
		ratio[6] += hits;
		ratio[7] += interferences;
		ratio[8] += fires;
		List<int[]> gunRollingRatios = rollingRatios.get(gunName);
		if (gunRollingRatios == null) {
			gunRollingRatios = new ArrayList<int[]>();
			rollingRatios.put(gunName, gunRollingRatios);
		}
		gunRollingRatios.add(new int[] {hits, interferences, fires});
		if (gunRollingRatios.size() > rollingVirtualHitRatioDepth) {
			int[] oldCount = gunRollingRatios.remove(0);
			ratio[6] -= oldCount[0];
			ratio[7] -= oldCount[1];
			ratio[8] -= oldCount[2];
		}
	}
	
	private void updateOpponentRatio(int hits, int interferences, int fires) {
		oppCumulativeHitRatio[0] += hits;
		oppCumulativeHitRatio[1] += interferences;
		oppCumulativeHitRatio[2] += fires;
	}
	
	private void updateOpponentRatio(String driveName, int hits, int shots) {
		int[] ratio = oppHitRatios.get(driveName);
		if (ratio == null) {
			ratio = new int[2];
			oppHitRatios.put(driveName, ratio);
		}
		ratio[0] += hits;
		ratio[1] += shots;
	}
	
	private void updateVirtualRatio(String gunName, int hits, int interferences, int fires) {
		int[] ratio = hitRatios.get(gunName);
		if (ratio == null) {
			ratio = new int[9];
			hitRatios.put(gunName, ratio);
		}		
		ratio[3] += hits;
		ratio[4] += interferences;
		ratio[5] += fires;
		ratio[6] += hits;
		ratio[7] += interferences;
		ratio[8] += fires;
		List<int[]> gunRollingRatios = rollingRatios.get(gunName);
		if (gunRollingRatios == null) {
			gunRollingRatios = new ArrayList<int[]>();
			rollingRatios.put(gunName, gunRollingRatios);
		}
		gunRollingRatios.add(new int[] {hits, interferences, fires});
		if (gunRollingRatios.size() > rollingVirtualHitRatioDepth) {
			int[] oldCount = gunRollingRatios.remove(0);
			ratio[6] -= oldCount[0];
			ratio[7] -= oldCount[1];
			ratio[8] -= oldCount[2];
		}
	}

	private void logGunHitRatios() {
		for (Map.Entry<String, int[]> entry : hitRatios.entrySet()) {
			double denom = entry.getValue()[2] - entry.getValue()[1];
			String percent = (denom > 0)? Logger.format((double) entry.getValue()[0] * 100d / denom) : "?";
			log.stat("Hit ratio for " + entry.getKey() + ": " + percent + "% (" + entry.getValue()[0] + "," + entry.getValue()[1] + "," + entry.getValue()[2] + ")");
		}
		log.stat("My overall hit ratio:       " + Logger.format(100*getOverallHitRatio()) + "%");
		log.stat("Opponent overall hit ratio: " + Logger.format(100*getOverallOpponentHitRatio()) + "%");
	}
	
	@Override
	public void oppWaveCreated(Wave wave) {
		lastOppFireTime = wave.getOriginTime();
		cumulativeOppPower += wave.getBulletPower();
		cumulativeOppShots++;
		lastOppPowerIndex++;
		if (lastOppPowerIndex >= lastOppPowers.length) {
			lastOppPowerIndex = 0;
		}
		lastOppPowers[lastOppPowerIndex] = wave.getBulletPower();
	}

	@Override
	public void oppWaveHitBullet(Wave wave, Bullet bullet) {
		updateOpponentRatio(0, 1, 1);
	}

	@Override
	public void oppWaveHit(Wave wave) {
		updateOpponentRatio(0, 0, 1);
		if (robotProxy.getTime()-wave.getOriginTime() > 5) { // don't penalize drives for short range shots
			updateOpponentRatio(robotProxy.getActiveDriveName(), 0, 1);
		}
	}

	@Override
	public void oppNextWaveToHit(Wave wave) {
		// no action required
	}

	@Override
	public void oppBulletHit(Wave wave, HitByBulletEvent hitByBulletEvent) {
		updateOpponentRatio(1, 0, 0);	
		if (robotProxy.getTime()-wave.getOriginTime() > 5) { // don't penalize drives for short range shots
			updateOpponentRatio(robotProxy.getActiveDriveName(), 1, 0);
		}
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
	public void myVirtualWaveCreated(XBulletWave wave) {
		// no action required	
	}

	@Override
	public void myVirtualWaveHit(XBulletWave wave) {
		updateVirtualRatio(wave.getGunName(), 0, 0, 1);	
	}

	@Override
	public void myVirtualBulletHit(XBulletWave wave) {
		updateVirtualRatio(wave.getGunName(), 1, 0, 0);		
	}

	@Override
	public void myVirtualWavePassing(XBulletWave wave) {
		// no action required		
	}

	@Override
	public void myVirtualWavePassed(XBulletWave wave) {
		// no action required		
	}

	@Override
	public void myVirtualWaveDestroyed(XBulletWave wave) {
		// no action required
	}

	@Override
	public void myWaveCreated(XBulletWave wave) {
		// no action required	
	}

	@Override
	public void myWaveHitBullet(XBulletWave wave, Bullet bullet) {
		updateRatio(wave.getGunName(), 0, 1, 1);	
	}

	@Override
	public void myWaveHit(XBulletWave wave, Snapshot opponentSnapshot) {
		updateRatio(wave.getGunName(), 0, 0, 1);		
	}

	@Override
	public void myBulletHit(XBulletWave wave, BulletHitEvent bulletHitEvent) {
		updateRatio(wave.getGunName(), 1, 0, 0);		
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
	public void onBattleEnded(BattleEndedEvent event) {
		// no action required
	}

	@Override
	public void onRoundEnded(RoundEndedEvent event) {
		logGunHitRatios();
		lastOppFireTime = 0;
	}
}
