package xander.core.track;

public class BulletShadow {

	private double counterClockwiseAngle;
	private double clockwiseAngle;
	
	public BulletShadow(double counterClockwiseAngle, double clockwiseAngle) {
		this.counterClockwiseAngle = counterClockwiseAngle;
		this.clockwiseAngle = clockwiseAngle;
	}

	public double getCounterClockwiseAngle() {
		return counterClockwiseAngle;
	}

	public double getClockwiseAngle() {
		return clockwiseAngle;
	}
}
