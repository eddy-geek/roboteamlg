package xander.core.math;

import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xander.core.track.Snapshot;
import xander.core.track.Wave;


/**
 * Utility class with various math functions for use in Robocode.
 * 
 * @author Scott Arnold
 */
public class RCMath {
	
	private static final double MAX_ESCAPE_ANGLE = Math.toDegrees(Math.asin(8 / RCPhysics.MIN_BULLET_VELOCITY));
	
	/**
	 * Normalize radians value to between 0 and 2*PI.
	 * 
	 * @param radians
	 * 
	 * @return   normalized radians
	 */
	public static double normalizeRadians(double radians) {
		final double circ = 2*Math.PI;
		while (radians < 0) radians += circ;
		while (radians > circ) radians -= circ;
		return radians;
	}
	
	/**
	 * Return a degree value between 0 and 360 for the given angle.  For 
	 * putting negative angles or angles over 360 degrees into the 0-360 range.
	 * 
	 * @param d				input angle, in degrees
	 * 
	 * @return				same angle in range 0 through 360
	 */
	public static double normalizeDegrees(double degrees) {
		while (degrees < 0) degrees += 360;
		while (degrees > 360) degrees -= 360;
		return degrees;
	}
	
	/**
	 * Converts degrees between Robocode and conventional.
	 * 
	 * @param degrees    degrees (either Robocode or conventional)
	 * 
	 * @return     degrees Robocode or conventional (opposite of argument)
	 */
	public static double convertDegrees(double degrees) {
		return normalizeDegrees(90 - degrees);
	}
	
	/**
	 * Get turn angle in degrees required to turn from an old heading to a
	 * new heading.  A negative returned value indicates a left turn, while
	 * a positive value indicates a right turn.  Heading values should be
	 * normalized.
	 *
	 * @param oldHeading		old/current heading in degrees
	 * @param newHeading		new/desired heading in degrees
	 *
	 * @return					degrees to turn (negative for left, positive for right)
	 */
	public static double getTurnAngle(double oldHeading, double newHeading) {
		double turnAngle = newHeading - oldHeading;
		if (Math.abs(turnAngle) > 180) {
			if (turnAngle > 0) {
				turnAngle -= 360;
			} else {
				turnAngle += 360;
			}
		}
		return turnAngle;
	}
	
	/**
	 * Convert radians value from Robocode format to conventional format.
	 * 
	 * @param roboRadians   robocode radians
	 * 
	 * @return              conventional radians
	 */
	public static double convertRadiansRobocodeToNormal(double roboRadians) {
		return normalizeRadians(Math.PI/2d - roboRadians);
	}
	
	/**
	 * Calculate the distance between two points p1 and p2.
	 * 
	 * @param p1    point 1
	 * @param p2    point 2
	 * 
	 * @return      distance between points
	 */
	public static double getDistanceBetweenPoints(Point2D.Double p1, Point2D.Double p2) {
		return getDistanceBetweenPoints(p1.x, p1.y, p2.x, p2.y);
	}
	
	/**
	 * Calculate the distance between two points (x1,y1) and (x2,y2).
	 *
	 * @param x1			point 1 x-coordinate
	 * @param y1			point 1 y-coordinate
	 * @param x2			point 2 x-coordinate
	 * @param y2			point 2 y-coordinate
	 *
	 * @return				distance between (x1,y1) and (x2,y2)
	 */
	public static double getDistanceBetweenPoints(double x1, double y1, double x2, double y2) {
		double xd = x2 - x1;
		double yd = y2 - y1;
		return Math.sqrt(xd*xd + yd*yd);
	}
	
	/**
	 * Get opponent's absolute position based on bearing and distance.  
	 * 
	 * @param bearingRadians	bearing to target in radians
	 * @param distance			distance to target
	 * @param myX				my x-coordinate
	 * @param myY				my y-coordinate
	 * @param myHeadingRadians	my heading in radians
	 * 
	 * @return					absolute position of scanned robot as (x,y)
	 */
	public static Point2D.Double getRobotPosition(double bearingRadians, double distance, double myX, double myY, double myHeadingInRadians) {
		double fixedBearing = bearingRadians + myHeadingInRadians;
		double s_ex = distance * Math.sin(fixedBearing) + myX;
		double s_ey = distance * Math.cos(fixedBearing) + myY;
		return new Point2D.Double(s_ex, s_ey);
	}
	
	/**
	 * Returns whether or not the two given double values differ by less than the 
	 * given amount.
	 * 
	 * @param a    first double value
	 * @param b    second double value
	 * @param m    amount to check difference against
	 * 
	 * @return     whether or not two given double values differ by less than the given amount.
	 */
	public static boolean differenceLessThan(double a, double b, double m) {
		return Math.abs(a - b) < m;
	}
	
	/**
	 * Return whether or not the difference between two values is less than a given
	 * percentage.  Percentage should range 0.0 - 1.0.
	 *
	 * @param a				first number
	 * @param b				second number
	 * @param percent		precentage as a decimal (0.0 - 1.0)
	 *
	 * @return				whether or not given numbers are within a given percentage of each other
	 */
	public static boolean differenceLessThanPercent(double a, double b, double percent) {
		return (a == 0 && b == 0)
			|| Math.abs((a - b) / Math.max(Math.abs(a),Math.abs(b))) < percent;
	}
	
	/**
	 * Returns a coordinate location starting from (x,y) and proceeding the given
	 * travel distance at the given heading.
	 * 
	 * @param x                    starting x-coordinate
	 * @param y                    starting y-coordinate
	 * @param travelDistance       distance to travel from starting coordinate
	 * @param headingRoboDegrees   heading to travel in (in Robocode degrees)
	 * 
	 * @return                     location after traveling the given distance at the given heading
	 */
	public static Point2D.Double getLocation(double x, double y, double travelDistance, double headingRoboDegrees) {
		double pheta = Math.toRadians(convertDegrees(headingRoboDegrees));
		Point2D.Double location = new Point2D.Double();
		location.x = x + travelDistance * Math.cos(pheta);
		location.y = y + travelDistance * Math.sin(pheta);
		return location;
	}
	
	/**
	 * Returns the maximum angle in degrees an opponent can move from a bullet
	 * moving at the given speed.
	 * 
	 * @param bulletVelocity    velocity of bullet
	 * 
	 * @return   maximum escape angle of opponent (in degrees)
	 */
	public static double getMaximumEscapeAngle(double bulletVelocity) {
		return Math.toDegrees(Math.asin(8 / bulletVelocity));
	}
	
	/**
	 * Returns a weighting value from 0 to 1 where 1 corresponds to the maximum possible escape angle 
	 * and 0 corresponds to the minimum possible escape angle.
	 * 
	 * @param wave  the wave
	 * 
	 * @return  weighted value of max escape angle significance from 0 to 1
	 */
	public static double getMEASignificance(Wave wave) {
		return getMEASignificance(wave.getInitialMEA(), wave.getOriginDistance());
	}
	
	/**
	 * Returns a weighting value from 0 to 1 where 1 corresponds to the maximum possible escape angle 
	 * and 0 corresponds to the minimum possible escape angle.
	 * 
	 * @param MEA         MEA for the defender
	 * @param distance    initial distance between attacker and defender (when wave was fired)
	 * 
	 * @return  weighted value of max escape angle significance from 0 to 1
	 */
	public static double getMEASignificance(RelativeAngleRange MEA, double distance) {
		double robotWidthDegrees = 180d*RCPhysics.ROBOT_WIDTH/(Math.PI*distance);
		double sArc = limit(Math.abs(MEA.getArc()) - robotWidthDegrees, 0, MAX_ESCAPE_ANGLE);
		return 1d - (sArc / MAX_ESCAPE_ANGLE);
	}
	
	/**
	 * Returns the Robocode angle between 0 and 360 degrees from one robot snapshot to another.
	 * 
	 * @param fromRobot    from snapshot
	 * @param toRobot      to snapshot
	 * 
	 * @return             Robocode angle from one snapshot to the other
	 */
	public static double getRobocodeAngle(Snapshot fromRobot, Snapshot toRobot) {
		return getRobocodeAngle(fromRobot.getX(), fromRobot.getY(), toRobot.getX(), toRobot.getY());
	}
	
	/**
	 * Return a Robocode angle between 0 and 360 degrees for the given x and y
	 * offsets.  Note that Robocode angles start facing up and proceed clockwise.
	 * If both x and y are zero, the result is undefined, but this method will
	 * return a 180 in such case.
	 * 
	 * @return			Robocode angle between 0 and 360 degrees.
	 */
	public static double getRobocodeAngle(double x, double y) {
		double angle = (x == 0)? 90 : Math.abs(Math.atan(y / x)) * (180d / Math.PI);
		if (x >= 0) {
			if (y > 0) {
				angle = 90 - angle;
			} else {
				angle += 90;
			}
		} else {
			if (y > 0) {
				angle += 270;
			} else {
				angle = 270 - angle;
			}
		}
		return angle;
	}
	
	/**
	 * Returns Robocode angle between 0 and 360 degrees from one point to another.
	 * 
	 * @param fromPoint    from point
	 * @param toPoint      to point
	 * 
	 * @return             Robocode angle in degrees from one point to the other
	 */
	public static double getRobocodeAngle(Point2D.Double fromPoint, Point2D.Double toPoint) {
		return getRobocodeAngle(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y);
	}
	
	/**
	 * Returns a robocode angle between 0 and 360 degrees for the direction that
	 * goes from one point through another.
	 * 
	 * @param fromX     from x-coordinate
	 * @param fromY     from y-coordinate
	 * @param toX       to x-coordinate
	 * @param toY       to y-coordinate
	 * 
	 * @return          robocode angle determined by the two given points
	 */
	public static double getRobocodeAngle(double fromX, double fromY, double toX, double toY) {
		return getRobocodeAngle(toX - fromX, toY - fromY);
	}
	
	/**
	 * Returns the angle from point (x,y) to the center of the battle field.
	 * 
	 * @param x     x-coordinate
	 * @param y     y-coordinate
	 * @param bb    battlefield bounds
	 * 
	 * @return      Robocode angle from (x,y) to center of battlefield
	 */
	public static double getRobocodeAngleToCenter(double x, double y, Rectangle2D.Double bb) {
		return getRobocodeAngle(x, y, bb.getCenterX(), bb.getCenterY());
	}
	
	/**
	 * Limits the given value to a range from the min value to the max value 
	 * inclusive.  
	 * 
	 * @param value    input value
	 * @param min      min value the value should have
	 * @param max      max value the value should have
	 * 
	 * @return         value, or the min or max if value is not within the min to max range
	 */
	public static double limit(double value, double min, double max) {
		if (value < min || value > max) {
			return (value < min)? min : max;
		}
		return value;
	}
	
	/**
	 * Returns whether or not the given value is between the two end point values (inclusive).
	 * End point values need not be in any order.
	 * 
	 * @param value   value to test
	 * @param end1    first end point
	 * @param end2    second end points
	 * 
	 * @return        whether or not value is between the two end point values (inclusive)
	 */
	public static boolean between(double value, double end1, double end2) {
		if (end1 > end2) {
			return (value >= end2 && value <= end1);
		} else {
			return (value >= end1 && value <= end2);
		}
	}
	
	/**
	 * Shrinks a rectangle by the given amount of each side, keeping the same center point.
	 * 
	 * @param source           source rectangle
	 * @param shrinkSidesBy    amount to shrink each side by
	 * 
	 * @return                 smaller rectangle
	 */
	public static Rectangle2D.Double shrink(Rectangle2D.Double source, double shrinkSidesBy) {
		return new Rectangle2D.Double(
				source.x + shrinkSidesBy, 
				source.y + shrinkSidesBy, 
				source.width - shrinkSidesBy*2d, 
				source.height - shrinkSidesBy*2d);
	}
	
	/**
	 * Returns the distance from a point within a box to the edge of a box
	 * given the point and a heading in robocode degrees.
	 * 
	 * @param x                     x-coordinate of point inside box
	 * @param y                     y-coordinate of point inside box
	 * @param headingRoboDegrees    heading from point in robocode degrees
	 * @param box                   box containing point
	 * 
	 * @return                      distance between point and edge of box given heading
	 */
	public static double getDistanceToIntersect(double x, double y, double headingRoboDegrees, Rectangle2D.Double box) {
		VelocityVector vv = new VelocityVector(headingRoboDegrees, 1);
		double endX = (vv.getX() > 0)? box.getMaxX() : box.getMinX();
		double endY = (vv.getY() > 0)? box.getMaxY() : box.getMinY();
		double timeToX = (endX - x) / vv.getX();
		double timeToY = (endY - y) / vv.getY();
		double timeToIntercept = Math.min(timeToX, timeToY);
		double x_i = x + timeToIntercept * vv.getX();
		double y_i = y + timeToIntercept * vv.getY();
		return getDistanceBetweenPoints(x, y, x_i, y_i);
	}
	
	/**
	 * Returns an arc where the y-axis is inverted from conventional.  This inversion is useful
	 * for painting arcs with conventional values in the Robocode coordinate space where the
	 * y-axis is inverted from Java conventional.  
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param start
	 * @param extent
	 * @param type
	 * 
	 * @return           y-axis inverted arc
	 */
	public static Arc2D.Double getYAxisInvertedArc(double x, double y, double w, double h, double start, double extent, int type) {
		// Arc2D allows negative extends, so we can just negate the start and extent and normalize the negated start
		return new Arc2D.Double(x, y, w, h, normalizeDegrees(-start), -extent, type);
	}
	
	/**
	 * Returns the back-as-front heading of the robot of given snapshot in degrees.
	 * 
	 * @param snapshot   snapshot of robot
	 * 
	 * @return           back-as-front heading of robot
	 */
	public static double getBackAsFrontHeading(Snapshot snapshot) {
		double bafHeading = snapshot.getHeadingRoboDegrees();
		if (snapshot.getVelocity() < 0) {
			bafHeading = normalizeDegrees(bafHeading + 180);
		}
		return bafHeading;
	}
	
	/**
	 * Returns the k-permutations for the integers in the array S.
	 * Integers in the array should be unique.
	 * 
	 * <pre>
	 * def k_permutation (S, k):
	 *	if k==0: yield []
	 *	else:
	 *		for i in xrange(len(S)):
	 *			for p in k_permutation(S[:i]+S[i+1:],k-1):
	 *				yield p+[S[i]]
	 * </pre>
	 * 
	 * @param S      array of unique integers
	 * @param k      size of permutation desired
	 * 
	 * @return       k-permutations for the array S
	 **/
	public static List<int[]> getKPermutations(int[] S, int k) {
		if (k == 0) {
			List<int[]> kperms = new ArrayList<int[]>();
			kperms.add(new int[] {});
			return kperms;
		} else {
			List<int[]> kperms = new ArrayList<int[]>();
			int[] nS = new int[S.length-1];
			for (int i=0; i<S.length; i++) {
				if (i > 0) {
					System.arraycopy(S, 0, nS, 0, i);
				}
				if (i < S.length-1) {
					System.arraycopy(S, i+1, nS, i, S.length-i-1);
				}
				for (int[] p : getKPermutations(nS, k-1)) {
					int[] np = new int[p.length+1];
					System.arraycopy(p, 0, np, 0, p.length);
					np[p.length] = S[i];
					kperms.add(np);
				}
			}
			return kperms;
		}
	}
	
	/**
	 * Returns the k-combinations for the integers in the array S.
	 * Integers in the array should be unique.
	 * 
	 * @param S      array of unique integers
	 * @param k      size of permutation desired
	 * 
	 * @return       k-combinations for the array S
	 */
	public static List<int[]> getKCombinations(int[] S, int k) {
		List<int[]> kperms = getKPermutations(S, k);
		for (Iterator<int[]> iter = kperms.iterator(); iter.hasNext();) {
			int[] perm = iter.next();
			boolean ordered = true;
			for (int i=1; i<perm.length; i++) {
				if (perm[i] < perm[i-1]) {
					ordered = false;
				}
			}
			if (!ordered) {
				iter.remove();
			}
		}
		return kperms;
	}
	
	/**
	 * Returns all possible k-combinations for the integers in the array S.
	 * Don't use this for S arrays over length 6, as the speed goes down
	 * exponentially.
	 * 
	 * @param S    array of integers
	 * 
	 * @return     all possible k-combinations
	 */
	public static List<int[]> getAllKCombinations(int[] S) {
		List<int[]> allKPerms = new ArrayList<int[]>();
		for (int k=1; k<=S.length; k++) {
			allKPerms.addAll(getKCombinations(S, k));
		}
		return allKPerms;
	}
	
	/**
	 * Returns the standard deviation for the values in the given array.
	 * 
	 * @param array
	 * 
	 * @return    standard deviation
	 */
	public static double getStandardDeviation(double[] array) {
		return getStandardDeviation(array, 0, array.length-1);
	}
	
	/**
	 * Returns the standard deviation for the subset of values in the given array
	 * defined by the begin and end index.
	 * 
	 * @param array
	 * @param beginIndex
	 * @param endIndex
	 * @return
	 */
	public static double getStandardDeviation(double[] array, int beginIndex, int endIndex) {
		double sum = 0;
		for (int i=beginIndex; i<=endIndex; i++) {
			sum += array[i];
		}
		if (sum == 0) {
			return 0;
		}
		double avg = sum / (endIndex - beginIndex + 1);
		sum = 0;
		for (int i=beginIndex; i<=endIndex; i++) {
			double diff = avg - array[i];
			sum += (diff * diff);
		}
		return Math.sqrt(sum / avg);
	}
	
	/**
	 * Returns the intersection points between the given circle and the line that passes
	 * through the two given points.  If there are no intersections, a value of null is 
	 * returned.  
	 * 
	 * @param circleCenter    center of circle
	 * @param circleRadius    radius of circle
	 * @param linePoint1      first line point
	 * @param linePoint2      second line point
	 * 
	 * @return    Array of points that comprise the intersections between the circle and line.  Returns null if there are no intersections.
	 */
	public static Point2D.Double[] getCircleToLineIntersections(Point2D.Double circleCenter, double circleRadius, Point2D.Double linePoint1, Point2D.Double linePoint2) {
		// first, check for non-intersecting or tangent
		// make use of existing LinearEquation class
		LinearEquation le = new LinearEquation(linePoint1.x, linePoint1.y, linePoint2.x, linePoint2.y);
		LinearEquation perpLe = le.getPerpendicularThroughPoint(circleCenter.x, circleCenter.y);
		double[] perpLeIntersect = LinearEquation.getIntersection(le, perpLe);
		double checkDist = getDistanceBetweenPoints(circleCenter.x, circleCenter.y, perpLeIntersect[0], perpLeIntersect[1]);
		if (checkDist > circleRadius) {
			// no intersection
			return null;
		} else if (checkDist == circleRadius) {
			// tangent
			return new Point2D.Double[] {new Point2D.Double(perpLeIntersect[0], perpLeIntersect[1])};
		} else {
			// two intersection points
			Point2D.Double[] intersections = new Point2D.Double[2];
			// to simplify the equations, translate coordinates such that circle is centered at (0, 0)
			// using line equation of y = mx + h, circle equation x^2 + y^2 = r^2
			// also, need to handle vertical lines as special case (lines of the form x = c)
			if (le.getSlope() == null) {
				// solve for y^2 = r^2 - x^2, or y = sqrt(r^2 - x^2)
				// note that we translate x and y values by circle center position
				double x = linePoint1.x - circleCenter.x;  
				double y = Math.sqrt(circleRadius*circleRadius - x*x);
				intersections[0] = new Point2D.Double(x + circleCenter.x, y + circleCenter.y);
				intersections[1] = new Point2D.Double(x + circleCenter.x, -y + circleCenter.y);
			} else {
				// solve circle and line equations using quadratic equation
				// remember to translate for circle center position
				double m = le.getSlope().doubleValue();
				double sx = linePoint1.x - circleCenter.x;
				double sy = linePoint1.y - circleCenter.y;
				double h = sy - m*sx; // substitute h for b in line eq, as b will be used for quadratic
				double a = 1 + m*m;
				double b = 2*m*h;
				double c = h*h - circleRadius*circleRadius;
				double S = Math.sqrt(b*b - 4*a*c);
				double x0 = (-b + S) / (2*a);
				double x1 = (-b - S) / (2*a);
				double y0 = m*x0 + h;
				double y1 = m*x1 + h;
				intersections[0] = new Point2D.Double(x0 + circleCenter.x, y0 + circleCenter.y);
				intersections[1] = new Point2D.Double(x1 + circleCenter.x, y1 + circleCenter.y);
			}
			return intersections;
		}
	}
	
	/**
	 * Parse a String to an int, returning a default value if string cannot be parsed.
	 * 
	 * @param intValueAsString
	 * @param defaultValue
	 * 
	 * @return    int value of string, or default value if string cannot be parsed
	 */
	public static int parseInt(String intValueAsString, int defaultValue) {
		try {
			return Integer.parseInt(intValueAsString);
		} catch (Exception e) {
			return defaultValue;
		}
	}
}
