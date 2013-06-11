package xander.core.drive;

import xander.core.track.Snapshot;

public interface DriveSelector {

	public int selectDrive(Drive[] drives, Snapshot snapshot);
}
