package xander.core.drive;

import xander.core.Component;
import xander.core.track.Snapshot;

public interface Drive extends Component {
	
	/**
	 * Drive into optimal firing range of the given robot.
	 * 
	 * @param opponentSnapshot   robot to drive against
	 * @param driveController    drive controller
	 */
	public void driveTo(Snapshot opponentSnapshot, DriveController driveController);
	
	/**
	 * Drive without any specific target.
	 * 
	 * @param driveController    drive controller
	 */
	public void drive(DriveController driveController);
}
