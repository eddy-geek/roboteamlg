package xander.core.paint;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages data for the CPU Utilization graph.
 * 
 * @author Scott Arnold
 */
public class CPUUtilizationGraphData extends GraphData {

	public static final String PAINTER_NAME = "CPU Utilization";
	
	private long startTurnTime;
	private List<HorizontalRule> horizontalRules;
	
	public CPUUtilizationGraphData(int numDataPoints) {
		super(PAINTER_NAME, numDataPoints);
	}

	@Override
	public String getYAxisLabel() {
		return "ms";
	}

	@Override
	public String getXAxisLabel() {
		return "Time";
	}
	
	@Override
	public List<HorizontalRule> getHorizontalRules() {
		return horizontalRules;
	}

	private void addRule(double y, Color color, String description) {
		if (horizontalRules == null) {
			horizontalRules = new ArrayList<HorizontalRule>();
		}
		horizontalRules.add(new HorizontalRule(y, color, description));
	}
	
	public void setCPUConstant(double CPUConstant) {
		addRule(CPUConstant, Color.RED, "CPU Constant");
	}
	
	public void onTurnBegin() {
		this.startTurnTime = System.nanoTime();
	}
	
	public void onTurnEnd() {
		long turnTime = System.nanoTime() - this.startTurnTime;
		rollStartIndex();
		setRolledDataPoint(turnTime/1000000d);
	}
}
