package xander.core.event;

import xander.core.track.XBulletWave;

public interface MyVirtualWaveListener {
	
	public void myVirtualWaveCreated(XBulletWave wave);
	
	public void myVirtualWaveHit(XBulletWave wave);
	
	public void myVirtualBulletHit(XBulletWave wave);
	
	public void myVirtualWavePassing(XBulletWave wave);
	
	public void myVirtualWavePassed(XBulletWave wave);
	
	public void myVirtualWaveDestroyed(XBulletWave wave);
}
