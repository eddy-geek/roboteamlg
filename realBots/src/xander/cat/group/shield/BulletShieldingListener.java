package xander.cat.group.shield;

import xander.core.track.Wave;
import xander.core.track.XBulletWave;

public interface BulletShieldingListener {

	public void shieldingShotHit(XBulletWave myWave, Wave opponentWave);
	
	public void shieldingShotMissed(XBulletWave myWave, Wave opponentWave);
}
