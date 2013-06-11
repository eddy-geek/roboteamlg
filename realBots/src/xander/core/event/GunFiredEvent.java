package xander.core.event;

import xander.core.gun.Gun;
import xander.core.track.Snapshot;

public class GunFiredEvent {
	
	private Gun gun;
	private Snapshot mySnapshot;
	private Snapshot opponentSnapshot;
	private double aim;
	private double power;
	
	public GunFiredEvent(Gun gun, double aim, double power, Snapshot mySnapshot, Snapshot opponentSnapshot) {
		this.gun = gun;
		this.aim = aim;
		this.power = power;
		this.mySnapshot = mySnapshot;
		this.opponentSnapshot = opponentSnapshot;
	}

	public Gun getGun() {
		return gun;
	}

	public Snapshot getMySnapshot() {
		return mySnapshot;
	}

	public Snapshot getOpponentSnapshot() {
		return opponentSnapshot;
	}

	public double getAim() {
		return aim;
	}

	public double getPower() {
		return power;
	}
}
