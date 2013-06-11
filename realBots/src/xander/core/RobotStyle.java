package xander.core;

import java.awt.Color;

import robocode.Robot;

/**
 * Stores and applies robot styling attributes such as colors.
 * 
 * @author Scott Arnold
 */
public class RobotStyle {
	
	private Color radarColor;
	private Color bodyColor;
	private Color gunColor;
	private Color bulletColor;
	private Color scanArcColor;
	
	public void setColors(Color bodyColor, Color gunColor, Color radarColor) {
		this.radarColor = radarColor;
		this.bodyColor = bodyColor;
		this.gunColor = gunColor;		
	}
	public Color getRadarColor() {
		return radarColor;
	}
	public void setRadarColor(Color radarColor) {
		this.radarColor = radarColor;
	}
	public Color getBodyColor() {
		return bodyColor;
	}
	public void setBodyColor(Color bodyColor) {
		this.bodyColor = bodyColor;
	}
	public Color getGunColor() {
		return gunColor;
	}
	public void setGunColor(Color gunColor) {
		this.gunColor = gunColor;
	}
	public Color getBulletColor() {
		return bulletColor;
	}
	public void setBulletColor(Color bulletColor) {
		this.bulletColor = bulletColor;
	}
	public Color getScanArcColor() {
		return scanArcColor;
	}
	public void setScanArcColor(Color scanArcColor) {
		this.scanArcColor = scanArcColor;
	}
	void apply(Robot robot) {
		if (bodyColor != null) {
			robot.setBodyColor(bodyColor);
		}
		if (bulletColor != null) {
			robot.setBulletColor(bulletColor);
		}
		if (gunColor != null) {
			robot.setGunColor(gunColor);
		}
		if (radarColor != null) {
			robot.setRadarColor(radarColor);
		}
		if (scanArcColor != null) {
			robot.setScanColor(scanArcColor);
		}		
	}	
}
