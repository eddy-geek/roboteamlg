package teamlg.scenario;

import xander.core.Resources;
import xander.core.Scenario;
import xander.core.log.Logger;

public class DuelScenario implements Scenario {

	@Override
	public boolean applies() {
		boolean bDuel = Resources.getRobotProxy().getOthers() == 1;
		if (bDuel) Logger.getLog(getClass()).info("Duel");
		return bDuel;
	}

}
