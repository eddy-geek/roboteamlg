package xander.core.drive;

import java.awt.geom.Point2D;
import java.util.List;

public class DrivePrediction {

	private List<Point2D.Double> drivePath;
	private DriveState finalDriveState;
	
	public DrivePrediction(DriveState finalDriveState, List<Point2D.Double> drivePath) {
		this.finalDriveState = finalDriveState;
		this.drivePath = drivePath;
	}

	public List<Point2D.Double> getDrivePath() {
		return drivePath;
	}

	public DriveState getFinalDriveState() {
		return finalDriveState;
	}
}
