package xander.core.math;

import java.awt.geom.Rectangle2D;

import xander.core.track.Snapshot;

public class Linear {
	
	/**
	 * Calculate the trajectory needed to hit a target moving on a linear path. 
	 * Linear calculation may be adjusted to keep shot within a certain bounds.
	 * Null can be returned if shot appears impossible.
	 *
	 * @param target		the target robot
	 * @param initialX		firing location x-coordinate
	 * @param initialY		firing location y-coordinate
	 * @param v_p			projectile velocity
	 * @param bounds		allowed firing range (usually battlefield bounds)
	 * @param time			current time
	 *
	 * @return				LinearIntercept for hitting the target
	 */
	public static LinearIntercept calculateTrajectory(Snapshot target, double initialX, double initialY, double v_p, Rectangle2D.Double bounds, long time) {
		if (target == null) return null;
		
		// get targets starting position and velocity components
		double[] velocityComponents = target.getXYShift();
		double v_ex = velocityComponents[0];
		double v_ey = velocityComponents[1];
		double targetInitX = target.getX();
		double targetInitY = target.getY();
		
		// if scan is from the past, move it forward to current point in time
		long timeLapse = time - target.getTime();
		if (timeLapse > 0) {
			targetInitX += (v_ex * timeLapse);
			targetInitY += (v_ey * timeLapse);
		}
		
		// get relative position
		double s_ex = targetInitX - initialX;
		double s_ey = targetInitY - initialY;

		// perform the computation
		double v_p2 = v_p * v_p;
		double s_ex2 = s_ex * s_ex;
		double s_ey2 = s_ey * s_ey;
		double v_ey2 = v_ey * v_ey;
		double v_ex2 = v_ex * v_ex;

		double rx =
			s_ex2 * s_ey2 * v_p2
			+ s_ex2 * s_ex2 * v_p2
			- s_ex2 * s_ex2 * v_ey2
			- s_ey2 * v_ex2 * s_ex2
			+ 2d * s_ex2 * s_ex * v_ey * s_ey * v_ex;
		rx = 2d * Math.sqrt(rx);
		double xns = -2d * s_ey * v_ey * s_ex + 2d * v_ex * s_ey2;
		double x1n = xns + rx;
		double x2n = xns - rx;
		double d = 2d * (s_ey2 + s_ex2);
		// note: since all battlefield coordinates are positive, we don't need to
		// worry about the value of d being zero and causing divide-by-zero errors.
		double x1 = x1n / d;
		double x2 = x2n / d;

		double ry =
			-1d * s_ey2 * s_ex2 * v_ey2
			+ 2d * s_ey2 * s_ey * s_ex * v_ey * v_ex
			- s_ey2 * s_ey2 * v_ex2
			+ s_ey2 * s_ey2 * v_p2
			+ s_ex2 * s_ey2 * v_p2;
		ry = 2d * Math.sqrt(ry);
		double yns = 2d * v_ey * s_ex2 - 2d * v_ex * s_ey * s_ex;
		double y1n = yns + ry;
		double y2n = yns - ry;
		double y1 = y1n / d;
		double y2 = y2n / d;

		// determine which values to use (x1, x2, y1, y2) by checking for time > 0
		Double t = (Math.abs(x2 - v_ex) < 0.001)? null : new Double(s_ex / (x2 - v_ex));
		Double t2 = (Math.abs(x1 - v_ex) < 0.001)? null : new Double(s_ex / (x1 - v_ex));
		if ((t == null || t.isNaN() || t.doubleValue() < 0) 
			&& (t2 == null || t2.isNaN() || t2.doubleValue() < 0)) {
			// unable to hit (velocity too slow, must be chasing)
			return null;
		}
		double x = (t == null || t.doubleValue() < 0)? x1 : x2;
		Double ty = (Math.abs(y1 - v_ey) < 0.001)? null : new Double(s_ey / (y1 - v_ey));
		double y = (ty == null || ty.doubleValue() < 0)? y2 : y1;
		
		// from here forward, t will be the correct time value
		if (t == null || t.doubleValue() < 0) t = t2;

		// check for shot outside of bounds and adjust accordingly	
		// misc. note: improves targeting on sample.Walls by 7%
		if (bounds != null) {
			double target_x = x * t.doubleValue() + initialX;
			double target_y = y * t.doubleValue() + initialY;
			if (target_x < bounds.getMinX()) {
				x = (bounds.getMinX() - initialX) / t.doubleValue();
			} else if (target_x > bounds.getMaxX()) {
				x = (bounds.getMaxX() - initialX) / t.doubleValue();
			}
			if (target_y < bounds.getMinY()) {
				y = (bounds.getMinY() - initialY) / t.doubleValue();
			} else if (target_y > bounds.getMaxY()) {
				y = (bounds.getMaxY() - initialY) / t.doubleValue();
			}
		}
		
		VelocityVector vv = new VelocityVector(x,y,0);
		vv.setMagnitude(v_p);	// in case shot was adjusted due to bounds checking
		return new LinearIntercept(vv, t.doubleValue());
	}
}
