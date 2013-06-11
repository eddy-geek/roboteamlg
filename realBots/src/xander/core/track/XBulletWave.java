package xander.core.track;

import java.awt.geom.Point2D;

import xander.core.math.RCMath;

public class XBulletWave extends Wave {

	XBullet xbullet;
	String gunName;
	
	public XBulletWave(Snapshot defenderSnapshot, Snapshot attackerSnapshot,
			XBullet bullet, String gunName, long bulletFiredTime) {
		super(defenderSnapshot, attackerSnapshot, bullet.getPower(), bulletFiredTime);
		this.xbullet = bullet;
		this.gunName = gunName;
	}

	XBulletWave(Snapshot defenderSnapshot, Snapshot attackerSnapshot,
			double bulletPower, long bulletFiredTime) {
		super(defenderSnapshot, attackerSnapshot, bulletPower, bulletFiredTime);
	}
	
	public XBullet getXBullet() {
		return xbullet;
	}

	public String getGunName() {
		return gunName;
	}
	
	public Point2D.Double getBulletLocation(long time) {
		return RCMath.getLocation(getOriginX(), getOriginY(), 
				getBulletTravelDistance(time), xbullet.getAim());
	}
}
