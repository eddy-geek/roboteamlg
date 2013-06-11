package xander.core.gun;

import xander.core.track.Snapshot;

public interface AutoFireCondition {

	public boolean isSatisfied(Snapshot target);
}
