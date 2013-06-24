package teamlg.gun;

import xander.cat.gun.power.SteppedHitRatioPowerSelector;
import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.log.Logger;
import xander.core.track.Snapshot;

public class DistancePowerSelector extends SteppedHitRatioPowerSelector {
	
	private RobotProxy robotProxy;

	public DistancePowerSelector(double[] stepHitRatios, double[] stepFirePowers) {
		super(stepHitRatios, stepFirePowers);
	}

	@Override
	public double getFirePower(Snapshot target) {
		if (robotProxy == null) {
			robotProxy = Resources.getRobotProxy();
		}
		
		// Get parent firePower
		double firePower = super.getFirePower(target);		
		
		// Apply distance ratio
		double d2 = Math.pow(target.getX() - robotProxy.getX(), 2) + Math.pow(target.getY() - robotProxy.getY(), 2);
		double distanceRate = 90000 / d2;
		Logger.getLog(getClass()).info("Distance rate [" + target.getName() + "]: " + distanceRate);
		
		// Apply opponents ratio
		double othersRate = (9.0 + robotProxy.getOthers()) / 10.0;
		Logger.getLog(getClass()).info("Others rate: " + othersRate);		
		
		// Consolidate
		firePower = Math.min(3, firePower * distanceRate * othersRate);		
		Logger.getLog(getClass()).info("Firing power: " + firePower);

		return firePower;
	}

}
