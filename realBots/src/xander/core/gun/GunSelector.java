package xander.core.gun;

import xander.core.track.Snapshot;

public interface GunSelector {

	public int selectGun(Gun[] guns, Snapshot target);
}
