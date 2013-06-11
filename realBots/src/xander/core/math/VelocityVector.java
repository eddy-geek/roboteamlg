package xander.core.math;

import java.awt.geom.Point2D;
import java.text.NumberFormat;

/**
 * A velocity vector, including x and y components, angular heading, and methods
 * for manipulation.
 * 
 * @author Scott Arnold
 */
public class VelocityVector implements Cloneable {

	private double x;
	private double y;
	private Double roboAngle = null;
	
	/**
	 * Create a new velocity vector with given x and y components.  If both
	 * x and y components are 0, the zeroDirection is used; otherwise, it 
	 * is ignored.
	 * 
	 * @param x				x-component of vector
	 * @param y				y-component of vector
	 * @param zeroDirection	Robocode angle in degrees (needed when x and y are 0)
	 */
	public VelocityVector(double x, double y, double zeroDirection) {
		this.x = x;
		this.y = y;
		if (x == 0 && y == 0) {
			roboAngle = new Double(zeroDirection);
		}
	}
	
	/**
	 * Crate a new velocity vector from given heading and velocity.  A negative
	 * magnitude will reverse the heading.
	 * 
	 * @param heading		direction of vector, in Robocode degrees
	 * @param magnitude		magnitude of vector
	 */
	public VelocityVector(double heading, double magnitude) {
		if (magnitude < 0) {
			if (heading >= 180) {
				heading -= 180;
			} else {
				heading += 180;
			}
			magnitude = -magnitude;
		}
		roboAngle = new Double(heading);
		double nonDumbAngleInRadians = (90d - heading) * (Math.PI / 180d);
		x = magnitude * Math.cos(nonDumbAngleInRadians);
		y = magnitude * Math.sin(nonDumbAngleInRadians);
	}
	
	/**
	 * Get the x-component of this velocity vector.
	 * 
	 * @return				x-component
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * Get the y-component of this velocity vector.
	 * 
	 * @return				y-component
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Reverse this velocity vector's direction.
	 */
	public void reverse() {
		this.x = -this.x;
		this.y = -this.y;
		if (roboAngle != null) {
			roboAngle = new Double(RCMath.normalizeDegrees(roboAngle.doubleValue()+180));
		}
	}
	
	/**
	 * Add another velocity vector to this one.
	 * 
	 * @param vv			velocity vector to add to this one
	 */
	public void add(VelocityVector vv) {
		this.x += vv.getX();
		this.y += vv.getY();
		if (x != 0 || y != 0) {
			roboAngle = null;
		}
	}
	
	/**
	 * Add the given heading and velocity to this velocity vector.
	 * 
	 * @param heading
	 * @param velocity
	 */
	public void add(double heading, double velocity) {
		Point2D.Double p = RCMath.getLocation(0, 0, velocity, heading);
		this.x += p.x;
		this.y += p.y;
		if (x != 0 || y != 0) {
			roboAngle = null;
		}
	}
	
	/**
	 * Turn this vector by the given degrees (negative values for left, positive for right).
	 * 
	 * @param degrees
	 */
	public void turn(double degrees) {
		double newAngle = getRoboAngle() + degrees;
		double mag = getMagnitude();
		double nonDumbAngleInRadians = (90d - newAngle) * (Math.PI / 180d);
		x = mag * Math.cos(nonDumbAngleInRadians);
		y = mag * Math.sin(nonDumbAngleInRadians);	
		if (x == 0 && y == 0) {
			roboAngle = new Double(RCMath.normalizeDegrees(roboAngle.doubleValue()+degrees));
		} else {
			roboAngle = null;
		}
	}
	
	/**
	 * Get the magnitude of this vector.
	 * 
	 * @return					magnitude of vector
	 */
	public double getMagnitude() {
		return Math.sqrt(x*x + y*y);
	}
	
	/**
	 * Set the magnitude of this vector without changing it's direction 
	 * (however, direction can be reversed if a negative magnitude is used).
	 * 
	 * @param magnitude			new desired magnitude
	 */
	public void setMagnitude(double magnitude) {
		double mag = getMagnitude();
		if (mag > 0) {
			double adjustFactor = magnitude / getMagnitude();
			x *= adjustFactor;
			y *= adjustFactor;
		}
	}
	
	/**
	 * Return a Robocode angle between 0 and 360 degrees for this velocity vector.
	 * Note that Robocode angles start facing up and proceed clockwise.
	 * 
	 * @return			Robocode angle between 0 and 360 degrees.
	 */
	public double getRoboAngle() {
		if (roboAngle == null) {
			// note: if roboAngle is not already set and x and y are zero, 
			// the returned angle will be some wack number.
			roboAngle = new Double(RCMath.getRobocodeAngle(x, y));
		}
		return roboAngle.doubleValue();
	}
	
	public Object clone() {
		return new VelocityVector(getRoboAngle(), getMagnitude());
	}
	
	public String toString() {
		NumberFormat nf = NumberFormat.getInstance();
		return ("(" + nf.format(this.x) + "," + nf.format(this.y) + "); mag: " + nf.format(getMagnitude()) + "; " + nf.format(getRoboAngle()) + " robocode degrees.");
	}
}

