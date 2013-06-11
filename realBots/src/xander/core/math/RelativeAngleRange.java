package xander.core.math;

import java.util.HashMap;
import java.util.Map;

/**
 * Relative angle range where the counter-clockwise and clockwise values are 
 * offsets from some other angle.  Generally, the counter-clockwise angle will
 * be negative while the clockwise angle will be positive, but this is not required.
 * What is required is that the counter-clockwise offset is less than the 
 * clockwise offset, and should not exceed 360 degrees.
 * 
 * @author Scott Arnold
 */
public class RelativeAngleRange {

	private static final Map<String, Integer> offsetErrorCounts = new HashMap<String, Integer>();
	
	private double counterClockwiseOffset;
	private double clockwiseOffset;
	
	public static Map<String, Integer> getOffsetErrorCounts() {
		return offsetErrorCounts;
	}
	
	public RelativeAngleRange(double counterClockwiseOffset, double clockwiseOffset, String creator) {
		if (counterClockwiseOffset > clockwiseOffset) {
			//throw new IllegalArgumentException("Counter-clockwise offset must be less than the clockwise offset.");
			addErrorCount(creator);
			// reverse the values to avoid total chaos
			double holder = clockwiseOffset;
			clockwiseOffset = counterClockwiseOffset;
			counterClockwiseOffset = holder;
		}
		if (clockwiseOffset - counterClockwiseOffset > 360) {
			//throw new IllegalArgumentException("Total arc should not exceed 360 degrees.");
			addErrorCount(creator);
		}
		this.counterClockwiseOffset = counterClockwiseOffset;
		this.clockwiseOffset = clockwiseOffset;
	}
	
	private void addErrorCount(String creator) {
		Integer count = offsetErrorCounts.get(creator);
		count = (count == null)? Integer.valueOf(1) : Integer.valueOf(count.intValue() + 1);
		offsetErrorCounts.put(creator, count);
	}
	
	public double getCounterClockwiseOffset() {
		return counterClockwiseOffset;
	}
	
	public double getClockwiseOffset() {
		return clockwiseOffset;
	}
	
	public double getArc() {
		return clockwiseOffset - counterClockwiseOffset;
	}
}
