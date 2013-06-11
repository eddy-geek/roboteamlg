package xander.core.gun.detect;

import java.awt.geom.Point2D;

import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.HitByBulletEvent;
import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.event.MyWaveListener;
import xander.core.event.OpponentWaveListener;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;
import xander.core.track.Wave;
import xander.core.track.XBulletWave;

public abstract class TargetingDetector implements MyWaveListener, OpponentWaveListener {

	protected RobotProxy robotProxy;
	protected SnapshotHistory snapshotHistory;
	private int hits;
	private int shots;
	private String name;
	private boolean offensive;
	
	public TargetingDetector(String name, boolean offensive) {
		this.name = name;
		this.offensive = offensive;
		if (offensive) {
			Resources.getWaveHistory().addMyWaveListener(this);
		} else {
			Resources.getWaveHistory().addOpponentWaveListener(this);
		}
		this.robotProxy = Resources.getRobotProxy();
		this.snapshotHistory = Resources.getSnapshotHistory();
	}
	
	public abstract double getDetectionAngle(Wave wave);
	
	protected abstract double getSloppyAimTolerance();
	
	public String getName() {
		return name;
	}
	
	/**
	 * Returns detection percentage in range 0 to 1.
	 * 
	 * @return    detection percentage in range 0 to 1.
	 */
	public double getDetectionPercentage() {
		if (shots == 0) {
			return 0;
		}
		return (double)hits/(double)shots;
	}

	public int getShotsTested() {
		return shots;
	}
	
	@Override
	public void oppWaveCreated(Wave wave) {
		// no action required
	}

	@Override
	public void oppWaveHitBullet(Wave wave, Bullet oppBullet) {
		// TODO: Add detection for bullet-hit-bullet?
	}

	@Override
	public void oppWaveHit(Wave wave) {
		if (!offensive) {
			double detectionAngle = getDetectionAngle(wave);
			//TODO: Handle detection angle < 0
			double travelDistance = RCMath.getDistanceBetweenPoints(
					wave.getOriginX(), wave.getOriginY(), robotProxy.getX(), robotProxy.getY());
			Point2D.Double detectionPosition = RCMath.getLocation(
					wave.getOriginX(), wave.getOriginY(), travelDistance, detectionAngle);
			double difference = RCMath.getDistanceBetweenPoints(
					detectionPosition.x, detectionPosition.y, robotProxy.getX(), robotProxy.getY());
			if (difference <= RCPhysics.ROBOT_HALF_WIDTH) {
				shots++;
			}
		}
	}

	@Override
	public void oppNextWaveToHit(Wave wave) {
		// no action required
	}

	@Override
	public void oppBulletHit(Wave wave, HitByBulletEvent hitByBulletEvent) {
		if (!offensive) {
			double detectionAngle = getDetectionAngle(wave);
			double travelDistance = RCMath.getDistanceBetweenPoints(
					wave.getOriginX(), wave.getOriginY(), robotProxy.getX(), robotProxy.getY());
			Point2D.Double detectionPosition = RCMath.getLocation(
					wave.getOriginX(), wave.getOriginY(), travelDistance, detectionAngle);
			double difference = RCMath.getDistanceBetweenPoints(
					detectionPosition.x, detectionPosition.y, robotProxy.getX(), robotProxy.getY());
			if (difference <= RCPhysics.ROBOT_HALF_WIDTH) {
				hits++;  // no need to increment shots as oppWaveHit(...) will have handled it
			} else if (difference <= RCPhysics.ROBOT_HALF_WIDTH + getSloppyAimTolerance()) {
				shots++;
				hits++;
			} else {
				shots++;
			}
		}
	}

	@Override
	public void oppWavePassing(Wave wave) {
		// no action required
	}

	@Override
	public void oppWavePassed(Wave wave) {
		// no action required
	}

	@Override
	public void oppWaveUpdated(Wave wave) {
		// no action required
	}

	@Override
	public void oppWaveDestroyed(Wave wave) {
		// no action required
	}

	@Override
	public void myWaveCreated(XBulletWave wave) {
		// no action required
	}

	@Override
	public void myWaveHitBullet(XBulletWave wave, Bullet myBullet) {
		// no action required	
	}

	@Override
	public void myWaveHit(XBulletWave wave, Snapshot opponentSnapshot) {
		if (offensive) {
			shots++;
			double detectionAngle = getDetectionAngle(wave);	
			double travelDistance = RCMath.getDistanceBetweenPoints(
					wave.getOriginX(), wave.getOriginY(), opponentSnapshot.getX(), opponentSnapshot.getY());
			Point2D.Double detectionPosition = RCMath.getLocation(
					wave.getOriginX(), wave.getOriginY(), travelDistance, detectionAngle);
			double difference = RCMath.getDistanceBetweenPoints(
					detectionPosition.x, detectionPosition.y, opponentSnapshot.getX(), opponentSnapshot.getY());
			if (difference <= RCPhysics.ROBOT_HALF_WIDTH) {
				hits++;
			}			
		}	
	}

	@Override
	public void myBulletHit(XBulletWave wave, BulletHitEvent bulletHitEvent) {
		// no action required
	}

	@Override
	public void myWavePassing(XBulletWave wave, Snapshot opponentSnapshot) {
		// no action required
	}

	@Override
	public void myWavePassed(XBulletWave wave, Snapshot opponentSnapshot) {
		// no action required	
	}

	@Override
	public void myWaveDestroyed(XBulletWave wave) {
		// no action required
	}
}
