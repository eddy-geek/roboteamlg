package xander.cat.group.ram;

import xander.core.gun.Aim;
import xander.core.gun.Gun;
import xander.core.gun.GunController;
import xander.core.track.Snapshot;

/**
 * Gun to use when ramming.  In general, this is just a disabled gun.  It is a good idea to 
 * use this rather than not specifying a gun, as it helps lock in the scenario in the component
 * chain; if no gun is used with the scenario, it is possible for some other scenario to take over.
 * 
 * @author Scott Arnold
 */
public class RamGun implements Gun {

	@Override
	public String getName() {
		return "Ram Gun";
	}

	@Override
	public void onRoundBegin() {
		// no action required
	}

	@Override
	public boolean fireAt(Snapshot target, Snapshot myself,
			GunController gunController) {
		return false;
	}

	@Override
	public Aim getAim(Snapshot target, Snapshot myself) {
		return null;
	}

	@Override
	public boolean canFireAt(Snapshot target) {
		return false;
	}
}
