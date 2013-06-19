package teamlg.scenario;

import xander.core.Resources;
import xander.core.Scenario;
import xander.core.log.Logger;
import xander.core.track.Snapshot;

/**
 * Scenario active when I am the Stronger robot
 * 
 * @author flo
 *
 */
public class StrongerScenario implements Scenario {

	@Override
	public boolean applies() {
		double maxEnergy = 0;
		for (String aRobotName : Resources.getOtherRobots().getRobotList()) {
			Snapshot aSnapshot = Resources.getSnapshotHistory().getSnapshot(aRobotName);
			if (aSnapshot != null) {
				maxEnergy = Math.max(maxEnergy, aSnapshot.getEnergy());
			}
		}
		Logger.getLog(getClass()).info("Stronger energy: " + maxEnergy);
		boolean bStronger = Resources.getRobotProxy().getEnergy() >= maxEnergy;
		if (bStronger) Logger.getLog(getClass()).info("Stronger");
		return bStronger;
	}

}
