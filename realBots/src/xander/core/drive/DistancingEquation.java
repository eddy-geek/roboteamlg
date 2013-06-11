package xander.core.drive;

import xander.core.math.LinearEquation;
import xander.core.track.Wave;

/**
 * Used with orbital and semi-orbital drive strategies to determine how much to
 * modify a tangent angle to back away from or advance towards the opponent
 * when too close or too far away.
 * 
 * Values returned by this class do not take direction into account.  Negative
 * values indicate advance, positive values indicate retreat.
 * 
 * @author Scott Arnold
 */
public class DistancingEquation {

	public static final DistancingEquation NO_ADJUST = new DistancingEquation(0, 100, 200, 0, 0);
	
	private LinearEquation retreatEquation;
	private LinearEquation advanceEquation;
	private double optimalDistance;
	private double maxRetreatDistance;
	private double maxRetreatAngle;
	private double maxAdvanceAngle;
	
	/**
	 * Constructs a new retreat equation.
	 * 
	 * @param maxRetreatDistance   distance at which retreat angle should be at maximum
	 * @param optimalDistance      distance at which there should be no retreat or advance
	 * @param maxAdvanceDistance   distance at which advance angle should be at maximum
	 * @param maxRetreatAngle      maximum angle of retreat in degrees (positive value)
	 * @param maxAdvanceAngle      maximum angle of advance in degrees (positive value)
	 */
	public DistancingEquation(double maxRetreatDistance, double optimalDistance, double maxAdvanceDistance, double maxRetreatAngle, double maxAdvanceAngle) {
		setDistancing(maxRetreatDistance, optimalDistance, maxAdvanceDistance, maxRetreatAngle, maxAdvanceAngle);
	}
	
	public void setDistancing(double maxRetreatDistance, double optimalDistance, double maxAdvanceDistance, double maxRetreatAngle, double maxAdvanceAngle) {
		this.optimalDistance = optimalDistance;
		this.maxRetreatDistance = maxRetreatDistance;
		this.maxAdvanceAngle = maxAdvanceAngle;
		this.maxRetreatAngle = maxRetreatAngle;
		this.retreatEquation = new LinearEquation(
				maxRetreatDistance, maxRetreatAngle, 
				optimalDistance, 0, 
				0, maxRetreatAngle);
		this.advanceEquation = new LinearEquation(
				optimalDistance, 0,
				maxAdvanceDistance, -maxAdvanceAngle,
				-maxAdvanceAngle, 0);
	}
	
	/**
	 * Returns the distance at and under which the retreat angle is at maximum.
	 * 
	 * @return       maximum retreat distance
	 */
	public double getMaxRetreatDistance() {
		return maxRetreatDistance;
	}
	
	/**
	 * Returns optimal distance where there is no retreat or advance.
	 * 
	 * @return       optimal distance
	 */
	public double getOptimalDistance() {
		return optimalDistance;
	}
	
	/**
	 * Returns the maximum retreat angle.
	 * 
	 * @return
	 */
	public double getMaxRetreatAngle() {
		return maxRetreatAngle;
	}

	/**
	 * Returns the maximum advance angle.  While the getAdjustAngle(...)
	 * method returns negative values for advance angles, this method
	 * will return a positive value.
	 * 
	 * @return
	 */
	public double getMaxAdvanceAngle() {
		return maxAdvanceAngle;
	}

	/**
	 * Returns the retreat angle in degrees to use for the given distance.  Value 
	 * returned is positive for retreat, negative for advance.
	 * 
	 * @param distance      distance from opponent
	 * @param wave          wave being surfed
	 * 
	 * @return              angle to modify tangent angle by
	 */
	public double getAdjustAngle(double distance, Wave wave) {
		if (distance < optimalDistance) { 
			return retreatEquation.getY(distance);
		} else {
			return advanceEquation.getY(distance);
		}
	}
}
