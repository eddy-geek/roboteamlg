package xander.core.math;

public class LinearIntercept {

	private VelocityVector velocityVector;
	private double timeToIntercept;
	private double distanceToIntercept;
	
	public LinearIntercept(VelocityVector velocityVector, double timeToIntercept) {
		this.velocityVector = velocityVector;
		this.timeToIntercept = timeToIntercept;
		this.distanceToIntercept = velocityVector.getMagnitude() * timeToIntercept;
	}
	
	public VelocityVector getVelocityVector() {
		return velocityVector;
	}

	public double getTimeToIntercept() {
		return timeToIntercept;
	}

	public double getDistanceToIntercept() {
		return distanceToIntercept;
	}
}
