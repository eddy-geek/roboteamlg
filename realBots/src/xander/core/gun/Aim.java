package xander.core.gun;

import xander.core.math.RCPhysics;

public class Aim {

	private double heading;         // heading bullet should be fired in -- Robocode degrees
	private double firePower;       // fire power to use for the given aim
	private double bulletVelocity;  // bullet velocity given the fire power
	
	public Aim(double heading, double firePower) {
		this.heading = heading;
		this.firePower = firePower;
		this.bulletVelocity = RCPhysics.getBulletVelocity(firePower);
	}

	public double getHeading() {
		return heading;
	}

	public double getFirePower() {
		return firePower;
	}

	public double getBulletVelocity() {
		return bulletVelocity;
	}
}
