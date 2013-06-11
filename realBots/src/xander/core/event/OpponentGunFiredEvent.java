package xander.core.event;

import xander.core.track.Snapshot;

public class OpponentGunFiredEvent {
	
	private Snapshot mySnapshot;
	private Snapshot opponentSnapshot;
	private double power;
	private long time;
	
	public OpponentGunFiredEvent(double power, Snapshot mySnapshot, Snapshot opponentSnapshot, long time) {
		this.power = power;
		this.mySnapshot = mySnapshot;
		this.opponentSnapshot = opponentSnapshot;
		this.time = time;
	}

	public Snapshot getMySnapshot() {
		return mySnapshot;
	}

	public Snapshot getOpponentSnapshot() {
		return opponentSnapshot;
	}

	public double getPower() {
		return power;
	}
	
	public long getTime() {
		return time;
	}
}
