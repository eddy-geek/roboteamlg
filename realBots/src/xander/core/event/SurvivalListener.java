package xander.core.event;

import robocode.DeathEvent;
import robocode.RobotDeathEvent;
import robocode.WinEvent;

public interface SurvivalListener {
	
	public void onWin(WinEvent event);
	
	public void onDeath(DeathEvent event);
	
	public void onRobotDeath(RobotDeathEvent event);
}
