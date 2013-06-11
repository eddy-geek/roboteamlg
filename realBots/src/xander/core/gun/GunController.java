package xander.core.gun;

import java.util.ArrayList;
import java.util.List;

import robocode.Bullet;

import xander.core.AbstractXanderRobot;
import xander.core.event.GunFiredEvent;
import xander.core.event.GunListener;
import xander.core.track.Snapshot;

public class GunController {
	
	private AbstractXanderRobot robot;
	private List<GunListener> gunListeners = new ArrayList<GunListener>();
	
	public void setRobot(AbstractXanderRobot robot) {
		this.robot = robot;
	}
	
	public void addGunListener(GunListener gunListener) {
		this.gunListeners.add(gunListener);
	}
	
	public Bullet setFireBullet(Gun gun, Snapshot mySnapshot, Snapshot opponentSnapshot, double power) {
		if (robot.getEnergy() > 0 && robot.getGunHeat() <= 0) {
			Bullet bullet = robot.setFireBullet(power);
			GunFiredEvent event = new GunFiredEvent(gun, robot.getGunHeading(), power, mySnapshot, opponentSnapshot);
			for (GunListener gunListener : gunListeners) {
				gunListener.gunFired(event);
			}
			return bullet;
		} else {
			return null;
		}
	}

	public void setFireVirtualBullet(Gun gun, double aim, double power, Snapshot mySnapshot, Snapshot opponentSnapshot) {
		GunFiredEvent event = new GunFiredEvent(gun, aim, power, mySnapshot, opponentSnapshot);
		for (GunListener gunListener : gunListeners) {
			gunListener.virtualGunFired(event);
		}
	}
	
	/**
	 * Returns true if gun heat is dissipated and gun turn remaining is negligible.
	 * 
	 * @return    whether or not the gun is ready to fire
	 */
	public boolean isGunReadyToFire() {
		return robot.getGunHeat() <= 0 && Math.abs(robot.getGunTurnRemaining()) < 0.05;
	}
	
	public double getGunCoolingRate() {
		return robot.getGunCoolingRate();
	}
	
	public double getGunHeadingDegrees() {
		return robot.getGunHeading();
	}
	
	public double getGunHeadingRadians() {
		return robot.getGunHeadingRadians();
	}
	
	public double getPreciseTimeUntilReadyToFire() {
		return robot.getGunHeat()/robot.getGunCoolingRate();
	}
	
	public void setTurnGunLeftDegrees(double degrees) {
		robot.setTurnGunLeft(degrees);
	}
	
	public void setTurnGunLeftRadians(double radians) {
		robot.setTurnGunLeftRadians(radians);
	}
	
	public void setTurnGunRightDegrees(double degrees) {
		robot.setTurnGunRight(degrees);
	}
	
	public void setTurnGunRightRadians(double radians) {
		robot.setTurnGunRightRadians(radians);
	}
}
