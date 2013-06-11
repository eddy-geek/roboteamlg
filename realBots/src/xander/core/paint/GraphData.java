package xander.core.paint;

import java.awt.Color;
import java.util.List;

import xander.paint.Paintable;

public abstract class GraphData implements Paintable {

	public static class HorizontalRule {
		public HorizontalRule(double y, Color color, String description) {
			this.y = y;
			this.color = color;
			this.description = description;
		}
		public String description;
		public double y;
		public Color color;
	}
	
	private String painterName;
	private double[] dataPoints; 
	private double maxValue;
	private int startIndex;
	private int lastIndex;
	
	public GraphData(String painterName, int numDataPoints) {
		this.painterName = painterName;
		this.dataPoints = new double[numDataPoints];
		this.lastIndex = numDataPoints-1;
	}
	
	@Override
	public String getPainterName() {
		return painterName;
	}
	
	public abstract String getYAxisLabel();
	
	public abstract String getXAxisLabel();
	
	public abstract List<HorizontalRule> getHorizontalRules();
	
	protected void rollStartIndex() {
		startIndex++;
		lastIndex++;
		if (startIndex >= dataPoints.length) {
			startIndex = 0;
		}
		if (lastIndex >= dataPoints.length) {
			lastIndex = 0;
		}
	}
	
	protected void setDataPoint(int index, double value) {
		dataPoints[index] = value;
		if (value > maxValue) {
			maxValue = value;
		}
	}
	
	protected void setRolledDataPoint(double value) {
		setDataPoint(lastIndex, value);
	}
	
	public double[] getDataPoints() {
		return dataPoints;
	}
	
	public double getMaxValue() {
		return maxValue;
	}
	
	public int getStartIndex() {
		return startIndex;
	}
}
