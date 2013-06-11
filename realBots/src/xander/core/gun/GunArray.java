package xander.core.gun;

import xander.core.track.Snapshot;

public class GunArray implements Gun {

	private Gun[] guns;
	private int activeGunIndex;
	private boolean fireVirtualBullets = true;
	private GunSelector gunSelector;
	
	/**
	 * Constructs a new GunArray using the given gun selector and guns.
	 * 
	 * @param gunSelector   selects which gun in the array to use
	 * @param guns          guns in the array
	 */
	public GunArray(GunSelector gunSelector, Gun... guns) {
		this.gunSelector = gunSelector;
		this.guns = guns;
	}

	/**
	 * Constructs a new GunArray that selects a gun based on which has
	 * the best virtual hit ratio.
	 * 
	 * @param guns    guns in the array
	 */
	public GunArray(Gun... guns) {
		this(new VirtualHitRatioGunSelector(), guns);
	}
	
	/**
	 * Constructs a new GunArray that selects a gun based on which has
	 * the best virtual hit ratio, adding the given biases for the guns
	 * hit ratios.  
	 * 
	 * @param guns         guns in the array
	 * @param gunBiases    baises for guns 
	 */
	public GunArray(Gun[] guns, double[] gunBiases) {
		this.gunSelector = new VirtualHitRatioGunSelector();
		this.guns = guns;
		for (int i=0; i<guns.length; i++) {
			((VirtualHitRatioGunSelector)gunSelector).addBias(guns[i], gunBiases[i]);
		}
	}
	
	public boolean isFireVirtualBullets() {
		return fireVirtualBullets;
	}

	public void setFireVirtualBullets(boolean fireVirtualBullets) {
		this.fireVirtualBullets = fireVirtualBullets;
	}

	@Override
	public String getName() {
		return guns[activeGunIndex].getName();
	}

	@Override
	public void onRoundBegin() {
		for (Gun gun : guns) {
			gun.onRoundBegin();
		}
	}

	@Override
	public boolean fireAt(Snapshot target, Snapshot myself,
			GunController gunController) {
		activeGunIndex = gunSelector.selectGun(guns, target);
		boolean bulletFired = guns[activeGunIndex].fireAt(target, myself, gunController);	
		if (bulletFired && fireVirtualBullets) {
			for (int i=0; i<guns.length; i++) {
				if (i != activeGunIndex && guns[i].canFireAt(target)) {
					Aim aim = guns[i].getAim(target, myself);
					if (aim != null) {
						gunController.setFireVirtualBullet(
								guns[i], aim.getHeading(), aim.getFirePower(), 
								myself, target);
					}
				}
			}
		}
		return bulletFired;
	}

	@Override
	public Aim getAim(Snapshot target, Snapshot myself) {
		return guns[activeGunIndex].getAim(target, myself);
	}

	@Override
	public boolean canFireAt(Snapshot target) {
		return guns[activeGunIndex].canFireAt(target);
	}
}
