package xander.core.event;

import robocode.Bullet;
import robocode.BulletHitEvent;
import xander.core.track.Snapshot;
import xander.core.track.XBulletWave;

public interface MyWaveListener {

	public void myWaveCreated(XBulletWave wave);
	
	public void myWaveHitBullet(XBulletWave wave, Bullet myBullet);
	
	public void myWaveHit(XBulletWave wave, Snapshot opponentSnapshot);
	
	public void myBulletHit(XBulletWave wave, BulletHitEvent bulletHitEvent);
	
	public void myWavePassing(XBulletWave wave, Snapshot opponentSnapshot);
	
	public void myWavePassed(XBulletWave wave, Snapshot opponentSnapshot);
	
	public void myWaveDestroyed(XBulletWave wave);
}
