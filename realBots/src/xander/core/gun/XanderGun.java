package xander.core.gun;

import xander.core.Resources;
import xander.core.gun.power.PowerSelector;
import xander.core.gun.targeter.Targeter;
import xander.core.log.Log;
import xander.core.log.Logger;
import xander.core.math.RCPhysics;
import xander.core.track.Snapshot;
import xander.core.track.Wave;
import xander.core.track.WaveHistory;

/**
 * Primary Xander framework gun that relies on using a Targeter and a PowerSelector.
 * 
 * @author Scott Arnold
 */
public class XanderGun extends AbstractGun {

	private static final Log log = Logger.getLog(XanderGun.class);
	
	private Targeter targeter;
	private PowerSelector powerSelector;
	private WaveHistory waveHistory;
	private String gunName;
	private double minEnergyToFire = 0.5d;
	private double lowEnergyConservationRate = 1d;
	
	public XanderGun(String gunName, Targeter targeter, PowerSelector powerSelector) {
		this.gunName = gunName;
		this.targeter = targeter;
		this.powerSelector = powerSelector;
		this.waveHistory = Resources.getWaveHistory();
	}
	
	public XanderGun(Targeter targeter, PowerSelector powerSelector) {
		this(null, targeter, powerSelector);
	}
	
	public double getMinEnergyToFire() {
		return minEnergyToFire;
	}

	public void setMinEnergyToFire(double minEnergyToFire) {
		this.minEnergyToFire = minEnergyToFire;
	}

	public double getLowEnergyConservationRate() {
		return lowEnergyConservationRate;
	}

	public void setLowEnergyConservationRate(double lowEnergyConservationRate) {
		this.lowEnergyConservationRate = lowEnergyConservationRate;
	}

	@Override
	public String getName() {
		return (gunName == null)? targeter.getTargetingType() + " Xander Gun" : gunName;
	}

	@Override
	public boolean canFireAt(Snapshot target) {
		return targeter.canAimAt(target);
	}

	@Override
	public Aim getAim(Snapshot target, Snapshot myself) {
		Aim aim = null;
		if (targeter.canAimAt(target)) {
			if (robotProxy.getTime() != target.getTime()) {
				log.warn(getName() + " aiming with old target data (from " + (robotProxy.getTime()-target.getTime()) + " ticks ago)");
			}
			double[] myNextXY = myself.getNextXY();  // will keep distance at 0 for self
			Snapshot myselfP1 = myself.advance(myNextXY[0], myNextXY[1]);
			Snapshot targetP1 = target.advance(myself.getX(), myself.getY());
			double firePower = powerSelector.getFirePower(targetP1);
			if (powerSelector.isAutoAdjustAllowed()) {
				double availableFiringEnergy = robotProxy.getEnergy()-minEnergyToFire;
				firePower = Math.min(firePower, availableFiringEnergy * lowEnergyConservationRate);
				firePower = Math.min(firePower, RCPhysics.getFirePowerToKill(targetP1.getEnergy()));
			}
			if (firePower > 0.09 && firePower < robotProxy.getEnergy()) { 
				Wave wave = waveHistory.createWave(target, myself, firePower, Resources.getTime(), false);
				double aimHeading = targeter.getAim(targetP1, myselfP1, wave);
				if (aimHeading >= 0) {
					aim = new Aim(aimHeading, firePower);
				}
			}	
		}		
		return aim;
	}
}
