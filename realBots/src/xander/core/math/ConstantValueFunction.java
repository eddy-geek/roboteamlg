package xander.core.math;

public class ConstantValueFunction implements Function {

	private double constant;
	
	public ConstantValueFunction(double constant) {
		this.constant = constant;
	}
	
	@Override
	public double getY(double x) {
		return constant;
	}
}
