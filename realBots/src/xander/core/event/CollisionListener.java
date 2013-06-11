package xander.core.event;

import robocode.HitRobotEvent;
import robocode.HitWallEvent;

public interface CollisionListener {
	
	public void onHitRobot(HitRobotEvent event);
	
	public void onHitWall(HitWallEvent event);
}
