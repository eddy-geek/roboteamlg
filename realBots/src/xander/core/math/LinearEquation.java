package xander.core.math;

/**
 * A basic linear equation with optional minimum and maximum y values.
 * 
 * @author Scott Arnold
 */
public class LinearEquation implements Function {
	
	private Double slope;
	private double b;
	private Double minimumY;
	private Double maximumY;
	
	/**
	 * Return the intersection point of two linear equations.  The intersection
	 * point is returned as a 2-element array of double with the first element
	 * representing x and the second element representing y.  If the slopes of both
	 * lines are the same, null is returned.
	 * 
	 * @param le1			the first linear equation
	 * @param le2			the second linear equation
	 * 
	 * @return				the intersection point as a 2-element array of double
	 */
	public static double[] getIntersection(LinearEquation le1, LinearEquation le2) {
		if (le1.slope == null || le2.slope == null) {
			if (le1.slope == null && le2.slope == null) {
				return null;
			}
			double x = (le1.slope == null)? le1.b : le2.b;
			double y = (le1.slope == null)? le2.getY(x) : le1.getY(x);
			return new double[] {x,y};
		} else {
			if (le1.slope.equals(le2.slope)) {
				return null;
			}
			double x = (le2.b - le1.b) / (le1.slope.doubleValue() - le2.slope.doubleValue());
			double y = le1.getY(x);
			return new double[] {x,y};
		}
	}
	
	/**
	 * Create a new LinearEquation for a line passing through the two given points.
	 * 
	 * @param x1		first x-coordinate
	 * @param y1		first y-coordinate
	 * @param x2		second x-coordinate
	 * @param y2		second y-coordinate
	 */
	public LinearEquation(double x1, double y1, double x2, double y2) {
		if (x2 != x1) {
			slope = new Double((y2 - y1)/(x2 - x1));
			b = y1 - slope.doubleValue() * x1;
		} else {
			b = x1;
		}		
	}
	
	/**
	 * Create a new LinearEquation for a line passing through the two given points
	 * with the given minimum and maximum values for the y-coordinate when computing
	 * y values.
	 * 
	 * @param x1		first x-coordinate
	 * @param y1		first y-coordinate
	 * @param x2		second x-coordinate
	 * @param y2		second y-coordinate
	 * @param minimumY	minimum value to return for computed y values
	 * @param maximumY	maximum value to return for computed y values
	 */
	public LinearEquation(double x1, double y1, double x2, double y2, double minimumY, double maximumY) {
		this(x1, y1, x2, y2);
		this.minimumY = new Double(minimumY);
		this.maximumY = new Double(maximumY);
	}
	
	/**
	 * Create a new linear equation with given slope "m" and constant "b" such 
	 * that y = mx + b.
	 * 
	 * @param slope			slope of the linear equation
	 * @param b				fixed constant of the linear equation
	 */
	public LinearEquation(Double slope, double b) {
		this.slope = slope;
		this.b = b;
	}
	
	/**
	 * Get the y-coordinate for a given x-coordinate value for this linear
	 * equation.  y value returned may be constrained by pre-set minimum y
	 * and maximum y values.  For equations of the form x = n, a value of Double.NaN is returned.
	 * 
	 * @param x		x-coordinate to compute y-coordinate for
	 * 
	 * @return		y-coordinate for the given x-coordinate
	 */
	public double getY(double x) {
		if (slope == null) {
			return Double.NaN;
		} else {
			double y = slope.doubleValue() * x + b;
			if (minimumY != null && minimumY.doubleValue() > y) {
				return minimumY.doubleValue();
			} else if(maximumY != null && maximumY.doubleValue() < y) {
				return maximumY.doubleValue();
			} else {
				return y;
			}
		}
	}
	
	/**
	 * Get the x-coordinate for a given y-coordinate value for this linear equation.  For equations
	 * of the form y = n, a value of Double.NaN is returned.  Note:  This method is not affected by 
	 * internal minimum and maximum Y values being set.
	 * 
	 * @param y			y-coordinate to compute x-coordinate for
	 * 
	 * @return			x-coordinate for the given y-coordinate
	 */
	public double getX(double y) {
		if (slope == null) {
			return b;
		} else if (slope.doubleValue() == 0) {
			return Double.NaN;
		} else {
			return (y - b) / slope.doubleValue();
		}
	}

	/**
	 * Returns the slope of the line.  Returns null for vertical lines of the form x = b.
	 * 
	 * @return   slope of line, or null for vertical lines.
	 */
	public Double getSlope() {
		return slope;
	}
	
	/**
	 * Returns the "b" component of the line with equation y = mx + b.
	 * 
	 * @return    b component of line with equation y = mx + b.
	 */
	public double getB() {
		return b;
	}
	
	public Double getMinimumY() {
		return minimumY;
	}
	
	public Double getMaximumY() {
		return maximumY;
	}
	
	/**
	 * Return a LinearEquation for a line perpendicular to this line that runs
	 * through the given point (x,y).
	 * 
	 * @param x				x component of a point on the desired line
	 * @param y				y component of a point on the desired line
	 * 
	 * @return				LinearEquation for line perpendicular to this line
	 */
	public LinearEquation getPerpendicularThroughPoint(double x, double y) {
		Double newSlope = null;
		if (slope == null) {
			newSlope = new Double(0);
		} else if (slope != null && slope.doubleValue() != 0) {
			newSlope = new Double(-1d / slope.doubleValue());
		}
		double newB = (newSlope == null)? x : y - newSlope.doubleValue() * x;
		return new LinearEquation(newSlope,newB);
	}
}
