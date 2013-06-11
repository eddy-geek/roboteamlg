package xander.core.drive;

import xander.core.track.Snapshot;

public class DriveArray implements Drive {

	private DriveSelector driveSelector;
	private Drive[] drives;
	private int activeDriveIndex;
	
	public DriveArray(DriveSelector driveSelector, Drive... drives) {
		this.driveSelector = driveSelector;
		this.drives = drives;
	}
	
	@Override
	public String getName() {
		return drives[activeDriveIndex].getName();
	}

	@Override
	public void onRoundBegin() {
		for (Drive drive : drives) {
			drive.onRoundBegin();
		}
	}

	@Override
	public void driveTo(Snapshot opponentSnapshot,
			DriveController driveController) {
		activeDriveIndex = driveSelector.selectDrive(drives, opponentSnapshot);
		drives[activeDriveIndex].driveTo(opponentSnapshot, driveController);
	}

	@Override
	public void drive(DriveController driveController) {
		activeDriveIndex = driveSelector.selectDrive(drives, null);
		drives[activeDriveIndex].drive(driveController);
	}
}
