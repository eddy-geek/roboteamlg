package xander.core.drive;

/**
 * Enum to represent circular direction of clockwise or counter-clockwise.
 * 
 * @author Scott Arnold
 */
public enum Direction {
	
	CLOCKWISE(1), COUNTER_CLOCKWISE(-1);
	
	private int directionUnit;
	
	private Direction(int directionUnit) {
		this.directionUnit = directionUnit;
	}
	
	/**
	 * Returns the direction unit; 1 for Clockwise, -1 for Counter-Clockwise.
	 * 
	 * @return    direction unit
	 */
	public int getDirectionUnit() {
		return directionUnit;
	}
	
	public boolean isClockwise() {
		return this == CLOCKWISE;
	}
	
	public Direction reverse() {
		return (this == CLOCKWISE)? COUNTER_CLOCKWISE : CLOCKWISE;
	}
}
