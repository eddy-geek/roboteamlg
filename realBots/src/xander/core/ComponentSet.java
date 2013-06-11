package xander.core;

import xander.core.drive.Drive;
import xander.core.gun.Gun;
import xander.core.radar.Radar;

/**
 * Container class for a set of robot components, which can include 
 * a radar, gun, and drive.
 * 
 * @author Scott Arnold
 */
public class ComponentSet {

	Radar radar;
	Drive drive;
	Gun gun;
	
	public void clear() {
		radar = null;
		drive = null;
		gun = null;
	}
	
	public boolean isComplete() {
		return (radar != null && drive != null && gun != null);
	}
}
