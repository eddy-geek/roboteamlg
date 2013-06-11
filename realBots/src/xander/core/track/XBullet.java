package xander.core.track;

import java.awt.geom.Point2D;

import xander.core.math.RCPhysics;

public class XBullet {

	private Point2D.Double origin;
	private double aim;
	private double power;
	private double speed;
	
	public XBullet(Point2D.Double origin, double aim, double power, double speed) {
		this.origin = origin;
		this.aim = aim;
		this.power = power;
		this.speed = speed;
	}

	public XBullet(Point2D.Double origin, double aim, double power) {
		this(origin, aim, power, RCPhysics.getBulletVelocity(power));
	}
	
	public Point2D.Double getOrigin() {
		return origin;
	}

	public double getAim() {
		return aim;
	}

	public double getPower() {
		return power;
	}

	public double getSpeed() {
		return speed;
	}
}
