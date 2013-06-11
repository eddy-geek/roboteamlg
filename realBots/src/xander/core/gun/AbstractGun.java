package xander.core.gun;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.math.RCMath;
import xander.core.track.Snapshot;

/**
 * Abstract gun that takes care of handling actually firing the bullet, leaving the aim
 * to be handled by the subclass.
 * 
 * @author Scott Arnold
 */
public abstract class AbstractGun implements Gun {
	
	private long nextFireTick;
	private double nextFirePower;
	
	protected RobotProxy robotProxy;
	
	public AbstractGun() {
		this.robotProxy = Resources.getRobotProxy();
	}
	
	@Override
	public void onRoundBegin() {
		nextFireTick = 0;	
	}
	
	@Override
	public boolean fireAt(Snapshot target, Snapshot myself,
			GunController gunController) {
		boolean bulletFired = false;
		if (gunController.isGunReadyToFire() && robotProxy.getTime() == nextFireTick) {
			// let the snapshots be current, but we need to use the next fire power
			gunController.setFireBullet(this, myself, target, nextFirePower);
			bulletFired = true;
		}
		if (gunController.getPreciseTimeUntilReadyToFire() < 2) {
			Aim aim = getAim(target, myself);
			if (aim != null) {
				this.nextFireTick = robotProxy.getTime() + 1;
				this.nextFirePower = aim.getFirePower();	
				double turn = RCMath.getTurnAngle(robotProxy.getGunHeadingDegrees(), aim.getHeading());	
				if (turn != 0) {
					gunController.setTurnGunRightDegrees(turn);
				}
			}
		} else {
			// just point the gun in the opponent's general direction
			double oppHeading = RCMath.getRobocodeAngle(myself.getLocation(), target.getLocation());
			double turn = RCMath.getTurnAngle(robotProxy.getGunHeadingDegrees(), oppHeading);
			gunController.setTurnGunRightDegrees(turn);
		}
		return bulletFired;
	}
}
