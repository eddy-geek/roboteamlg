package xander.core.event;

import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;

public interface BulletHitListener {
	
	public void onBulletHit(BulletHitEvent event);
	
	public void onBulletHitBullet(BulletHitBulletEvent event);
	
	public void onBulletMissed(BulletMissedEvent event);
	
	public void onHitByBullet(HitByBulletEvent event);
	
}
