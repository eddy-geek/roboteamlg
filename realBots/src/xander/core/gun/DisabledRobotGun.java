package xander.core.gun;

import java.util.List;

import xander.core.Configuration;
import xander.core.gun.targeter.HeadOnTargeter;
import xander.core.track.Snapshot;

/**
 * Gun for firing at disabled opponents.  This gun will only be used by the framework on
 * disabled robots and only if any and all set auto fire conditions are met.
 * 
 * @author Scott Arnold
 */
public class DisabledRobotGun implements Gun {

	private XanderGun headOnGun;
	private List<AutoFireCondition> autoFireConditions;
	
	public DisabledRobotGun(Configuration configuration) {
		this.headOnGun = new XanderGun(new HeadOnTargeter(), 
				configuration.getDisabledOpponentPowerSelector());
		this.autoFireConditions = configuration.getAutoFireConditions();
	}
	
	@Override
	public String getName() {
		return "Disabled Robot Gun";
	}

	@Override
	public void onRoundBegin() {
		// no action required
	}

	@Override
	public boolean fireAt(Snapshot target, Snapshot myself,
			GunController gunController) {
		return headOnGun.fireAt(target, myself, gunController);
	}

	@Override
	public Aim getAim(Snapshot target, Snapshot myself) {
		return headOnGun.getAim(target, myself);
	}

	@Override
	public boolean canFireAt(Snapshot target) {
		if (autoFireConditions != null) {
			for (AutoFireCondition condition : autoFireConditions) {
				if (!condition.isSatisfied(target)) {
					return false;
				}
			}
		}
		return headOnGun.canFireAt(target);
	}
}
