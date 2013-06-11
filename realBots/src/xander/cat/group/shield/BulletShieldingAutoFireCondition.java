package xander.cat.group.shield;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.gun.AutoFireCondition;
import xander.core.track.Snapshot;

/**
 * Auto-fire on disabled opponents condition to delay auto-fire until all active opponent waves
 * have been shielded.
 * 
 * @author Scott Arnold
 */
public class BulletShieldingAutoFireCondition implements AutoFireCondition {

	private RobotProxy robotProxy;
	
	public BulletShieldingAutoFireCondition() {
		this.robotProxy = Resources.getRobotProxy();
	}
	
	@Override
	public boolean isSatisfied(Snapshot target) {
		if (BulletShieldingGun.NAME.equals(robotProxy.getActiveGunName())) {
			return Resources.getWaveHistory().getOpponentActiveWaveCount() == 0;
		} else {
			return true;
		}
	}
}
