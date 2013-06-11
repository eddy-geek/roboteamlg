package xander.core.event;

import robocode.BattleEndedEvent;
import robocode.RoundEndedEvent;

public interface RoundListener {
	
	public void onBattleEnded(BattleEndedEvent event);
	
	public void onRoundEnded(RoundEndedEvent event);
}
