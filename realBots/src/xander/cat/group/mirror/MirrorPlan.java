package xander.cat.group.mirror;

import xander.core.Resources;
import xander.core.RobotEvents;
import xander.core.drive.Direction;
import xander.core.event.RoundBeginListener;
import xander.core.event.TurnListener;

/**
 * Stores drive plan for use by the AntiMirrorDrive and AntiMirrorGun.
 * 
 * @author Scott Arnold
 */
public class MirrorPlan implements TurnListener, RoundBeginListener {

	private int[] tickCounts = new int[20];
	private Direction direction = Direction.CLOCKWISE;
	private int timeIndex;
	private long time;
	private int reversalTickRange;
	
	public MirrorPlan(RobotEvents robotEvents, int reversalTickRange) {
		this.reversalTickRange = reversalTickRange;
		robotEvents.addTurnListener(this);
		robotEvents.addRoundBeginListener(this);
	}

	private void generateTickCount(int timeIndex) {
		tickCounts[timeIndex] = (int) Math.round(Math.random()*reversalTickRange) + 5;
	}
	
	public Direction getDirection(long futureTime) {
		long timeCounter = futureTime - time;
		int index = timeIndex;
		Direction d = direction;
		while (timeCounter > 0) {
			timeCounter -= tickCounts[index];
			if (timeCounter > 0) {
				d = d.reverse();
			}
			index++;
			if (index == tickCounts.length) {
				index = 0;
			}
		}
		return d;
	}

	@Override
	public void onRoundBegin() {
		for (int i=0; i<tickCounts.length; i++) {
			generateTickCount(i);
		}
		timeIndex = 0;
		time = Resources.getTime();	
	}

	@Override
	public void onTurnBegin() {
		long currentTime = Resources.getTime();
		int elapsed = (int) (currentTime - time);
		while (elapsed > 0) {
			int reduce = Math.min(elapsed, tickCounts[timeIndex]);
			tickCounts[timeIndex] -= reduce;
			if (tickCounts[timeIndex] == 0) {
				generateTickCount(timeIndex);
				timeIndex++;
				direction = direction.reverse();
				if (timeIndex == tickCounts.length) {
					timeIndex = 0;
				}
			}
			elapsed -= reduce;
		}
		time = currentTime;
	}

	@Override
	public void onTurnEnd() {
		// no action required
	}
}
