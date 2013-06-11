package xander.core.event;

import robocode.Bullet;
import robocode.HitByBulletEvent;
import xander.core.track.Wave;

public interface OpponentWaveListener {
	
	public void oppWaveCreated(Wave wave);
	
	public void oppWaveHitBullet(Wave wave, Bullet oppBullet);
	
	public void oppWaveHit(Wave wave);
	
	public void oppNextWaveToHit(Wave wave);
	
	public void oppBulletHit(Wave wave, HitByBulletEvent hitByBulletEvent);
	
	public void oppWavePassing(Wave wave);
	
	public void oppWavePassed(Wave wave);
	
	public void oppWaveUpdated(Wave wave);
	
	public void oppWaveDestroyed(Wave wave);
}
