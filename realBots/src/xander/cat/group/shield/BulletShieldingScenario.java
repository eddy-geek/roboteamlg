package xander.cat.group.shield;

import xander.cat.group.rem.REMFactory;
import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.Scenario;
import xander.core.event.RoundBeginListener;
import xander.core.math.RCPhysics;
import xander.core.track.GunStats;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;
import xander.core.track.Wave;
import xander.core.track.XBulletWave;

/**
 * Scenario for activing bullet shielding.
 * 
 * @author Scott Arnold
 */
public class BulletShieldingScenario implements Scenario, RoundBeginListener, BulletShieldingListener {

//	private static final Log log = Logger.getLog(BulletShieldingScenario.class);
	
	private BulletShieldingController controller;
	private RobotProxy robotProxy;
	private GunStats gunStats;
	private int activationTime;  // allows some other scenario to improve positioning before this scenario can kick in
	private int requiredLeadTimeForOpponentWaves;
	private double disengageDistance;
	private double reengageDistance;
	private boolean previouslyEngaged = true;
	private SnapshotHistory snapshotHistory;
	private boolean disableOnMiss;  // for now, only apply this on first round
	private long reactivateTime;
	private BulletShieldingGun gun;
//	private boolean[] conditions = new boolean[9];
//	private int[] conditionFailCounts = new int[9];
//	private int appliedRounds;
//	private int ticksThisRound;
//	private int appliedTicksThisRound;
	private int opponentNotFiringFirstCount;  // keeps track and helps determine if opponent is determined to not fire first
	private boolean opponentNotFiringFirst;
	private boolean requiredDistanceIncreased;
	private boolean x5Checked;
	private boolean isX5;
	
	public BulletShieldingScenario(BulletShieldingController controller, BulletShieldingGun gun, int activationTime, boolean disableOnMiss) {
		this.controller = controller;
		this.gun = gun;
		this.robotProxy = Resources.getRobotProxy();
		this.activationTime = activationTime;
		this.disableOnMiss = disableOnMiss;
		this.gunStats = Resources.getGunStats();
		this.requiredLeadTimeForOpponentWaves = controller.getRequiredLeadTimeForOpponentWaves();
		this.disengageDistance = RCPhysics.ROBOT_HALF_WIDTH + (requiredLeadTimeForOpponentWaves + 1) * RCPhysics.getBulletVelocity(1.5);
		this.reengageDistance = disengageDistance + 200;
		this.snapshotHistory = Resources.getSnapshotHistory();
		Resources.getRobotEvents().addRoundBeginListener(this);
		controller.addBulletShieldingListener(this);
	}
	
	public void setDisableOnX5(boolean disableOnX5) {
		if (disableOnX5) {
			x5Checked = false;
		} else {
			x5Checked = true;
			isX5 = false;
		}
	}
	
//	public int getAppliedRounds() {
//		return appliedRounds;
//	}
	
//	public int[] getConditionFailCounts() {
//		return conditionFailCounts;
//	}
	
	@Override
	public void onRoundBegin() {
//		this.appliedTicksThisRound = 0;
		this.reactivateTime = 0;
		this.opponentNotFiringFirstCount = 0;  // reset this counter for each round
		this.requiredDistanceIncreased = false;
		if (robotProxy.getRoundNum() > 0) {
			// adjust the required opponent distance based on average opponent bullet power
			double avgBulletPower = gunStats.getAverageOpponentBulletPower();
			this.disengageDistance = RCPhysics.ROBOT_HALF_WIDTH 
				+ (requiredLeadTimeForOpponentWaves + 1) * RCPhysics.getBulletVelocity(avgBulletPower);
			this.reengageDistance = disengageDistance + 200;
		}
	}

//	private static final String[] conditionDesc = new String[] {
//		"Controller deactivated",
//		"Waiting on initial positioning",
//		"Waiting for missed wave to pass",
//		"Opponent is not firing",
//		"Opponent is too close to shield against",
//		"Damage per shielding shot is too high",
//		"Opponent bullet power is too low",
//		"Misses too high or overall shielding ratio too low"
//	};
	
	@Override
	public boolean applies() {
		//System.out.println(Logger.format(controller.getBulletShieldingRatio(), 3) + " : " + Logger.format(controller.getRollingBulletShieldingRatio(), 3));
		Snapshot scannedOpponent = snapshotHistory.getLastOpponentScanned();

		if (!x5Checked && scannedOpponent != null) {
			x5Checked = true;
			isX5 = REMFactory.isX5(scannedOpponent.getName());
		}
		
		long timeSinceOpponentFired = Resources.getTime()-gunStats.getLastOpponentFireTime();
		if (timeSinceOpponentFired == 250) {
			opponentNotFiringFirstCount++;
			if (opponentNotFiringFirstCount > 3) {
				opponentNotFiringFirst = true;
			}
		}
		
		if (scannedOpponent != null && scannedOpponent.getEnergy() <= 8 && !requiredDistanceIncreased) {
			disengageDistance += 100; // add safety margin as opponent will likely start firing low power shots at the end
			reengageDistance += 100;
			requiredDistanceIncreased = true;
		}
		
		double requiredDistance = previouslyEngaged? disengageDistance : reengageDistance;
		
		boolean applies = !isX5
			&& Resources.getTime() > activationTime
			&& Resources.getTime() > reactivateTime
			&& !opponentNotFiringFirst
			&& (Resources.getCumulativeTime() < 200 || timeSinceOpponentFired < 250)  
			&& (!controller.isOpponentTooClose() && (scannedOpponent == null || scannedOpponent.getDistance() >= requiredDistance))
			&& (robotProxy.getRoundNum() < 3 || (controller.getMissDamagePerShieldingShot() < 0.2) || (robotProxy.getRoundNum() < 6 && controller.getMissDamagePerShieldingShot() < 0.3))
			&& (gunStats.getOpponentBulletsFired() < 3 || Resources.getTime() < 36 || gunStats.getRollingAverageOpponentBulletPower() > 0.2)  // resources.getTime < 36 to handle when last shots from previous round were all low power
			&& (controller.getBulletShieldingMisses() < 3 || (controller.getBulletShieldingConsecutiveMisses() < 5 && (controller.getBulletShieldingRatio() > 0.925 || (controller.getBulletShieldingRatio() > 0.79 && controller.getBulletShieldingShots() < 20))));

//		conditions[0] = controller.isActive(); 
//		conditions[1] = Resources.getTime() > activationTime;
//		conditions[2] = Resources.getTime() > reactivateTime;
//		conditions[3] = !opponentNotFiringFirst;
//		conditions[4] = (Resources.getCumulativeTime() < 200 || timeSinceOpponentFired < 250);  // opp not firing
//		conditions[5] = (!controller.isOpponentTooClose() && (scannedOpponent == null || scannedOpponent.getDistance() >= requiredDistance));  // isOpponentTooClose is after bullet already fired; distance check can apply before bullet is fired
//		conditions[6] = (robotProxy.getRoundNum() < 3 || (controller.getMissDamagePerShieldingShot() < 0.2) || (robotProxy.getRoundNum() < 6 && controller.getMissDamagePerShieldingShot() < 0.3));
//		conditions[7] = (gunStats.getOpponentBulletsFired() < 3 || Resources.getTime() < 36 || gunStats.getRollingAverageOpponentBulletPower() > 0.2);  // resources.getTime < 36 to handle when last shots from previous round were all low power
//		conditions[8] = (controller.getBulletShieldingMisses() < 3 || (controller.getBulletShieldingConsecutiveMisses() < 5 && (controller.getBulletShieldingRatio() > 0.925 || (controller.getBulletShieldingRatio() > 0.79 && controller.getBulletShieldingShots() < 20))));
//		int i=0;
//		boolean applies = true;
//		while (applies && i<conditions.length) {
//			applies = conditions[i];
//			i++;
//		}
//		for (i=0; i<conditions.length; i++) {
//			if (!conditions[i]) {
//				conditionFailCounts[i]++;
//			}
//		}
//		ticksThisRound++;
		previouslyEngaged = applies;
		if (!applies) {
			controller.reset();
			gun.reset();
		} else {
//			appliedTicksThisRound++;
		}
		return applies;
	}

//	private String getInfo() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("nwc:" + targeter.getNoWaveCount());
//		sb.append(";nac:" + targeter.getNoAuthCount());
//		sb.append(";dstate:" + controller.getDstate());
//		return sb.toString();
//	}
	
	@Override
	public void shieldingShotHit(XBulletWave myWave, Wave opponentWave) {
		// no action required
	}

	@Override
	public void shieldingShotMissed(XBulletWave myWave, Wave opponentWave) {
		if (robotProxy.getRoundNum() == 0 && disableOnMiss) {
			long deactivateDuration = Math.min(12L, opponentWave.getTimeUntilHit(
					robotProxy.getX(), robotProxy.getY(), Resources.getTime()));
			this.reactivateTime = Resources.getTime() + deactivateDuration;
		}
	}

//	@Override
//	public void onBattleEnded(BattleEndedEvent event) {
//		
//	}
//
//	@Override
//	public void onRoundEnded(RoundEndedEvent event) {
//		if ((double)appliedTicksThisRound / (double)ticksThisRound > 0.6) {
//			appliedRounds++;
//		}
//		log.stat(Logger.format(controller.getBulletShieldingRatio(), 3) 
//				+ " : " + Logger.format(controller.getRollingBulletShieldingRatio(), 3) 
//				+ " : CM " + controller.getBulletShieldingConsecutiveMisses() 
//				+ " : MDpSS " + Logger.format(controller.getMissDamagePerShieldingShot(), 5)
//				+ " : FPA Ratio " + Logger.format(gun.getFirePowerAdjustedRatio(), 3));		
//	}
}
