package xander.core.math;

import xander.core.log.Log;
import xander.core.log.Logger;
import xander.core.track.Snapshot;

public class Circular {
	
	private static final Log log = Logger.getLog(Circular.class);
	
	private static final double DEFAULT_ERROR_TOLERANCE = 0.1;
	private static final int SAMPLE_POINTS = 6;
	private static final int REFINEMENTS = 2;
	
	/**
	 * Get the center point of a robot's circular path.  If the path does not
	 * appear to be circular, null is returned.
	 * 
	 * @param target_t1			first/more recent target robot snapshot
	 * @param target_t0         second/older target robot snapshot
	 * 
	 * @return					center point for target robot's circular path
	 */
	public static double[] getCenterPoint(Snapshot target_t1, Snapshot target_t0) {
		if (target_t1 == null || target_t0 == null || target_t1.getVelocity() == 0) return null;
		if (RCMath.differenceLessThanPercent(target_t0.getHeadingRoboRadians(),target_t1.getHeadingRoboRadians(),0.025d)) {
			return null;
		}
//		System.out.println("Getting center point for " + target_t1.getName() + "; x1=" + target_t0.getX() + "; y1=" + target_t0.getY() + "; x2=" + target_t1.getX() + "; y2=" + target_t1.getY());
		//float velocity = (float) ((target_t1.getVelocity() + target_t0.getVelocity()) / 2d);
		float velocity = (float) target_t1.getVelocity();

		double s1_ex = target_t0.getX();
		double s1_ey = target_t0.getY();
		
		double s2_ex = target_t1.getX();
		double s2_ey = target_t1.getY();
		
		VelocityVector sre1pv = new VelocityVector(target_t0.getHeadingRoboDegrees()+90,velocity);
		VelocityVector sre2pv = new VelocityVector(target_t1.getHeadingRoboDegrees()+90,velocity);
		Double slope1 = (sre1pv.getX() == 0)? null : new Double(sre1pv.getY() / sre1pv.getX());
		Double slope2 = (sre2pv.getX() == 0)? null : new Double(sre2pv.getY() / sre2pv.getX());

		double center_x = 0, center_y = 0;
		// note: center_x and center_y are from (0,0), not relative to me.
		// this makes it easier to account for my own movement
		if (slope1 == null) {
			if (slope2 == null) {
				return null;
			} else {
				center_x = s1_ex;
				double c = s2_ey - slope2.doubleValue() * s2_ex;
				center_y = slope2.doubleValue() * center_x + c;
			}
		} else {
			if (slope2 == null) {
				center_x = s2_ex;
				double c = s1_ey - slope1.doubleValue() * s1_ex;
				center_y = slope1.doubleValue() * center_x + c;
			} else {
				double c1 = s1_ey - slope1.doubleValue() * s1_ex;
				double c2 = s2_ey - slope2.doubleValue() * s2_ex;
				center_x =  (c2 - c1) / (slope1.doubleValue() - slope2.doubleValue());
				center_y = slope1.doubleValue() * center_x + c1;
				double center2_y = slope2.doubleValue() * center_x + c2;
				if (!RCMath.differenceLessThanPercent(center_y,center2_y,DEFAULT_ERROR_TOLERANCE)) {
					return null;
				} 
			}
		}
		double d1 = RCMath.getDistanceBetweenPoints(sre1pv.getX(),sre1pv.getY(),center_x,center_y);
		double d2 = RCMath.getDistanceBetweenPoints(sre2pv.getX(),sre2pv.getY(),center_x,center_y);
		if (!RCMath.differenceLessThanPercent(d1,d2,DEFAULT_ERROR_TOLERANCE)) {
			return null;
		} else {
			return new double[] {center_x, center_y};
		}
	}
	
	/**
	 * Calculate the approximate trajectory needed to hit a target moving in a circular
	 * path given various information.  Accuracy is determined by the number of sample
	 * points and refinements; these values should be tuned to provide sufficient
	 * accuracy without being excessive.
	 * 
	 * Trajectory assumes robot will continue at it's current velocity.
	 * If target velocity is 0 a head-on shot is returned.  It might be better to use
	 * some other form of targeting if target velocity is 0.
	 *
	 * @param target		the target
	 * @param initialX		x-coordinate of firing location
	 * @param initialY		y-coordinate of firing location
	 * @param centerPoint	center of circular arc
	 * @param v_p			projectile velocity
	 * @param time			current time
	 * 
	 * @return				VelocityVector for hitting the target
	 * 
	 * @throws 				TargetingException
	 */
	public static VelocityVector calculateTrajectory(Snapshot target, double initialX, double initialY, double[] centerPoint, double v_p, long time) {

		if (target.getVelocity() == 0) {
			double headOn = RCMath.getRobocodeAngle(initialX, initialY, target.getX(), target.getY());
			return new VelocityVector(headOn, (float)v_p);
		}
		
		double c_x = centerPoint[0];
		double c_y = centerPoint[1];
		
		// make values relative to me 
		// note: s_ex and s_ey values will be wrong if scan is not from time now; adjustments for this are made further down.
		double s_ex = target.getX() - initialX;
		double s_ey = target.getY() - initialY;
		c_x -= initialX;
		c_y -= initialY;
		
		// calculate radius and my distance to center
		double r = RCMath.getDistanceBetweenPoints(s_ex, s_ey, c_x, c_y);
		double myDistanceToCenter = Math.sqrt(c_x*c_x + c_y*c_y);

		// determine if enemy motion is clockwise or counter-clockwise
		double[] traj = target.getXYShift();
		double v_a = Math.abs(target.getVelocity()) / r;	// by default, angular velocity is counter-clockwise
		// change angular velocity to clockwise if necessary
		if (s_ey > c_y && traj[0] > 0) {
			v_a = -v_a;
		} else if (s_ey < c_y && traj[0] < 0) {
			v_a = -v_a;
		} else if (s_ex > c_x && traj[1] < 0) {
			v_a = -v_a;
		} else if (s_ex < c_x && traj[1] > 0) {
			v_a = -v_a;
		}

		// determine time point t where calculated position matches position at time of scan
		double acos = Math.acos((s_ex - c_x) / r);	// acos returns 0 - PI
		double t_o = acos / v_a; // offset time, scenario 0 - PI
		if (Double.isNaN(t_o)) {
			log.error("v_a = " + v_a + "; acos = " + acos + "; r = " + r + "; s_ex = " + s_ex + "; c_x = " + c_x);
			return null;
		}
		double y1_t = r * Math.sin(v_a * t_o) + c_y;	// does x(t) compute correctly for t = t_0 ?
		if (!RCMath.differenceLessThanPercent(y1_t,s_ey,DEFAULT_ERROR_TOLERANCE)) {
			t_o = (Math.PI * 2 - acos) / v_a;	// offset time, scenario 2PI - acos
		}

		// adjust t_o for position at current time
		t_o += (time - target.getTime());
		
		// setup refinement loop for first run
		int index = -1;
		int nindex = -1;
		double startPoint = myDistanceToCenter - r;		// for initial sample test
		double stopPoint = myDistanceToCenter + r;		// for initial sample test
		double[] sampleDistances = new double[SAMPLE_POINTS];
		double[] t_fs = new double[SAMPLE_POINTS];
		double[] u_exs = new double[SAMPLE_POINTS];
		double[] u_eys = new double[SAMPLE_POINTS];
		double[] d_ts = new double[SAMPLE_POINTS];
		
		for (int refineLoop=0; refineLoop<REFINEMENTS; refineLoop++) {
			
			// setup refinement starting point and increment for this run (not done on first run)
			if (index >= 0) {
				startPoint = Math.min(sampleDistances[index],sampleDistances[nindex]);
				stopPoint = Math.max(sampleDistances[index],sampleDistances[nindex]);
			}
					
			// determine sample point increment and my distance to sample points (note: c_x and c_y are now relative to me)
			double increment = (stopPoint - startPoint) / (SAMPLE_POINTS - 1d); 
			for (int i=0; i<SAMPLE_POINTS; i++) {
				sampleDistances[i] = startPoint + increment*i;
			}
	
			// determine how long it will take for bullet to reach sample points
			double[] t_cs = new double[SAMPLE_POINTS];
			for (int i=0; i<SAMPLE_POINTS; i++) {
				t_cs[i] = sampleDistances[i] / v_p;
			}
	
			// determine where enemy will be at time when bullet would have reached sample points of circular arc
			for (int i=0; i<SAMPLE_POINTS; i++) {
				t_fs[i] = t_o + t_cs[i];
				u_exs[i] = r * Math.cos(v_a * t_fs[i]) + c_x;
				u_eys[i] = r * Math.sin(v_a * t_fs[i]) + c_y;
			}
			
			// see which sampled point is closest to target
			//double[] d_ts = new double[SAMPLE_POINTS];
			for (int i=0; i<SAMPLE_POINTS; i++) {
				d_ts[i] = Math.abs(sampleDistances[i] - RCMath.getDistanceBetweenPoints(0,0,u_exs[i],u_eys[i]));
			}
			// save index of closest and next-closest match
			double closest = Double.POSITIVE_INFINITY;
			double nextClosest = Double.POSITIVE_INFINITY;
			for (int i=0; i<SAMPLE_POINTS; i++) {
				if (d_ts[i] < closest) {
					nindex = index;
					nextClosest = closest;
					index = i;
					closest = d_ts[i];
				} else if (d_ts[i] < nextClosest) {
					nindex = i;
					nextClosest = d_ts[i];
				}
			}		
		}
		
		// now aim for where enemy location was calculated (crude, but good enough)
		// note: his position is relative to me)
		if (index == -1) {
			log.warn("No closest sample point found.  Circular trajectory cannot be calculated.");
			log.warn("t_o = " + Logger.format(t_o));
			return null;
		}
		double v_fh = Math.sqrt(u_exs[index]*u_exs[index] + u_eys[index]*u_eys[index]);
		double v_x = (u_exs[index] * v_p) / v_fh;
		double v_y = (u_eys[index] * v_p) / v_fh;
		return new VelocityVector(v_x,v_y,0);
	}
}
