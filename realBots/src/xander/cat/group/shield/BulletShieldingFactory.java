package xander.cat.group.shield;

import xander.core.ComponentChain;
import xander.core.math.RCPhysics;

/**
 * Factory for building bullet.
 * 
 * @author Scott Arnold
 */
public class BulletShieldingFactory {

	public static BulletTargeter[] getCommonBulletTargeters() {
		return new BulletTargeter[] {
			new M1BulletTargeter(),
			new M1TBulletTargeter()
		};
	}
	
	public static BulletShieldingController addBulletShieldingComponents(ComponentChain chain, int roundActivationTime, double maxAdjustedFirePower, boolean temporarilyDisableOnShieldingMiss, boolean allowFinishingShot) {
		BulletShieldingDrive bulletShieldingDrive = new BulletShieldingDrive();
		BulletShieldingController bulletShieldingController = new BulletShieldingController(bulletShieldingDrive);
		BulletShieldingGun bulletShieldingGun = null;
		if (maxAdjustedFirePower < RCPhysics.MIN_FIRE_POWER) {
			bulletShieldingGun = new BulletShieldingGun(bulletShieldingController, getCommonBulletTargeters());
		} else {
			bulletShieldingGun = new BulletShieldingGun(bulletShieldingController, maxAdjustedFirePower, getCommonBulletTargeters());
		}
		bulletShieldingGun.setAllowFinishingShot(allowFinishingShot);
		BulletShieldingScenario bulletShieldingScenario = new BulletShieldingScenario(
				bulletShieldingController, 
				bulletShieldingGun, 
				roundActivationTime, temporarilyDisableOnShieldingMiss);
		chain.addComponents(bulletShieldingScenario, bulletShieldingDrive, bulletShieldingGun);
		return bulletShieldingController;
	}

	public static void addBulletShieldingComponentsAsDefault(ComponentChain chain, double maxAdjustedFirePower, boolean allowFinishingShot) {
		BulletShieldingDrive bulletShieldingDrive = new BulletShieldingDrive();
		BulletShieldingController bulletShieldingController = new BulletShieldingController(bulletShieldingDrive);
		BulletShieldingGun bulletShieldingGun = null;
		if (maxAdjustedFirePower < RCPhysics.MIN_FIRE_POWER) {
			bulletShieldingGun = new BulletShieldingGun(bulletShieldingController, getCommonBulletTargeters());
		} else {
			bulletShieldingGun = new BulletShieldingGun(bulletShieldingController, maxAdjustedFirePower, getCommonBulletTargeters());
		}
		bulletShieldingGun.setAllowFinishingShot(allowFinishingShot);
		chain.addDefaultComponents(bulletShieldingDrive, bulletShieldingGun);
	}
}
