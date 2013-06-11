package xander.cat.gun.power;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.gun.power.PowerSelector;
import xander.core.math.RCPhysics;
import xander.core.track.GunStats;
import xander.core.track.Snapshot;

public class SteppedHitRatioPowerSelector implements PowerSelector {

	private double[] stepHitRatios;
	private double[] stepFirePowers;
	private double minPower = RCPhysics.MAX_FIRE_POWER;
	private double maxPower = RCPhysics.MIN_FIRE_POWER;
	private GunStats gunStats;
	private RobotProxy robotProxy;
	private double dropPowerHitRatio = 1d;
	private double dropPowerEnergyLead;
	private double dropPowerBy;
	private int dropPowerCount;
	
	public SteppedHitRatioPowerSelector(double[] stepHitRatios, double[] stepFirePowers) {
		for (int i=0; i<stepHitRatios.length-1; i++) {
			if (stepHitRatios[i+1] < stepHitRatios[i]) {
				throw new IllegalArgumentException("stepHitRatios must be in ascending order.");
			}
		}
		if (stepFirePowers.length != stepHitRatios.length + 1) {
			throw new IllegalArgumentException("stepFirePowers must contain one more element than stepHitRatios");
		}
		for (int i=0; i<stepFirePowers.length; i++) {
			minPower = Math.min(minPower, stepFirePowers[i]);
			maxPower = Math.max(maxPower, stepFirePowers[i]);
		}
		this.stepFirePowers = stepFirePowers;
		this.stepHitRatios = stepHitRatios;
	}
	
	public void setPowerDrop(double dropPowerHitRatio, double dropPowerEnergyLead, double dropPowerBy) {
		this.dropPowerHitRatio = dropPowerHitRatio;
		this.dropPowerEnergyLead = dropPowerEnergyLead;
		this.dropPowerBy = dropPowerBy;
	}
	
	@Override
	public double getFirePower(Snapshot target) {
		if (gunStats == null) {
			this.robotProxy = Resources.getRobotProxy();
			this.gunStats = Resources.getGunStats();
		}
		double hitRatio = gunStats.getOverallHitRatio();
		int i=0;
		while (i < stepHitRatios.length && hitRatio > stepHitRatios[i]) {
			i++;
		}
		double firePower = stepFirePowers[i];
		if (hitRatio <= dropPowerHitRatio) {
			double energyLead = robotProxy.getEnergy() - target.getEnergy();
			if (energyLead >= dropPowerEnergyLead && firePower > gunStats.getRollingAverageOpponentBulletPower()) {
				this.dropPowerCount++;
				if (dropPowerBy == 0) {
					firePower = gunStats.getRollingAverageOpponentBulletPower();
				} else {
					firePower -= dropPowerBy;
				}
			}
		}
		return firePower;
	}
	
	public int getDropPowerCount() {
		return dropPowerCount;
	}
	
	@Override
	public double getMinimumPower() {
		return minPower;
	}
	
	@Override
	public double getMaximumPower() {
		return maxPower;
	}
	
	@Override
	public boolean isAutoAdjustAllowed() {
		return true;
	}
	
	
}
