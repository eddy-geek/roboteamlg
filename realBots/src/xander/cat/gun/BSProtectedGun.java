package xander.cat.gun;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.gun.Aim;
import xander.core.gun.Gun;
import xander.core.gun.GunController;
import xander.core.log.Log;
import xander.core.log.Logger;
import xander.core.track.GunStats;
import xander.core.track.Snapshot;

public class BSProtectedGun implements Gun {

	private static final Log log = Logger.getLog(BSProtectedGun.class);
	
	private Gun gun;
	private RobotProxy robotProxy;
	private GunStats gunStats;
	private boolean moxie;
	
	public BSProtectedGun(Gun gun) {
		this.gun = gun;
		this.robotProxy = Resources.getRobotProxy();
		this.gunStats = Resources.getGunStats();
	}
	
	@Override
	public String getName() {
		return gun.getName();  // masquerade as the wrapped gun
	}

	@Override
	public void onRoundBegin() {
		if (Resources.getRobotProxy().getRoundNum() > 1
				&& gunStats.getAverageOpponentBulletPower() < 1  // a shielder isn't going to fire high power bullets
				&& (gunStats.getOverallInterferenceRatio() > 0.2 
						|| (gunStats.getOverallInterferenceRatio() > 0.1
								&& gunStats.getOverallInterferenceRatio() > gunStats.getOverallHitRatio()))) {
			moxie = true;
			log.info("Bullet Shielding protection activated!");
		}
	}

	@Override
	public boolean fireAt(Snapshot target, Snapshot myself,
			GunController gunController) {
		return gun.fireAt(target, myself, gunController);
	}

	@Override
	public Aim getAim(Snapshot target, Snapshot myself) {
		return gun.getAim(target, myself);
	}

	@Override
	public boolean canFireAt(Snapshot target) {
		if (moxie && target.getEnergy() < robotProxy.getEnergy()) {
			// we are currently ahead in energy; don't fire unless they fire first
			long timeSinceLastFire = robotProxy.getTime() - gunStats.getLastOpponentFireTime();
			return timeSinceLastFire < 4;
		}
		return gun.canFireAt(target);
	}

}
