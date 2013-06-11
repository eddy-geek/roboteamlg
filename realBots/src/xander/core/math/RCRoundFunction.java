package xander.core.math;

import xander.core.Resources;
import xander.core.event.RoundBeginListener;

/**
 * A special kind of function that provides a value for each round.  The Function used to create an 
 * instance of this class should assume that the first round corresponds to an x value of 0 and 
 * ideally should provide proper values regardless of how many rounds there are, but with the 
 * knowledge that the x value for the final round will be getNumRounds()-1. 
 * 
 * @author Scott Arnold
 */
public class RCRoundFunction implements RoundBeginListener {

	private Function function;
	private double y;
	
	public RCRoundFunction(Function function) {
		this.function = function;
		Resources.getRobotEvents().addRoundBeginListener(this);
	}

	@Override
	public void onRoundBegin() {
		double x = Resources.getRobotProxy().getRoundNum();
		this.y = function.getY(x);
		
	}

	public double getValueForRound() {
		return y;
	}
}
