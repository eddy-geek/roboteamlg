package xander.cat.group.ram;

import java.awt.geom.Rectangle2D;

import robocode.Bullet;
import robocode.HitByBulletEvent;
import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.drive.Direction;
import xander.core.drive.Drive;
import xander.core.drive.DriveController;
import xander.core.drive.OrbitalDrivePredictor;
import xander.core.event.OpponentWaveListener;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.math.VelocityVector;
import xander.core.track.Snapshot;
import xander.core.track.Wave;
import xander.core.track.WaveHistory;

/**
 * Drive for escaping ramming robots.
 * 
 * @author Scott Arnold
 */
public class RamEscapeDrive implements Drive, OpponentWaveListener {

	private RobotProxy robotProxy;
	private OrbitalDrivePredictor orbitalDriver;
	private WaveHistory waveHistory;
	private Rectangle2D.Double bb;
	private double baseTurn = 50;
	private double currentTurn;
	private Wave currentWave;
	private Wave turnWave;

	public RamEscapeDrive() {
		this.orbitalDriver = new OrbitalDrivePredictor();
		this.robotProxy = Resources.getRobotProxy();
		this.waveHistory = Resources.getWaveHistory();
		this.bb = robotProxy.getBattleFieldSize();
		waveHistory.addOpponentWaveListener(this);
	}
	
	@Override
	public String getName() {
		return "Ram Escape Drive";
	}

	@Override
	public void driveTo(Snapshot opponentSnapshot,
			DriveController driveController) {
		double x = robotProxy.getX();
		double y = robotProxy.getY();
		double awayAngle = RCMath.getRobocodeAngle(
				opponentSnapshot.getX(), opponentSnapshot.getY(), x, y);
		double currentAngle = robotProxy.getBackAsFrontHeadingDegrees();
		double driveAngle = awayAngle;
		if (Math.abs(RCMath.getTurnAngle(currentAngle, awayAngle)) < 90) {
			driveAngle = currentAngle;
		}
		if (waveHistory.getOpponentActiveWaveCount() != 0) {
			if (currentWave != turnWave) {
				// generally, we want to turn towards the center...
				Direction orbitDirection = orbitalDriver.getOribitalDirection(bb.getCenterX(), bb.getCenterY());
				currentTurn = baseTurn * orbitDirection.getDirectionUnit();
				// ...unless there is room to turn away from center and 
				// turning towards center puts us in path of the opponent.
				boolean turnAwayViable = RCMath.getDistanceToIntersect(x, y, 
						currentAngle - 90*orbitDirection.getDirectionUnit(), bb) > 110;
				if (turnAwayViable) {
					double headingToOpp = RCMath.normalizeDegrees(awayAngle + 180);
					double oppBearing = RCMath.getTurnAngle(currentAngle, headingToOpp);
					if ((orbitDirection == Direction.CLOCKWISE && oppBearing > 90 && oppBearing < 145)
							|| (orbitDirection == Direction.COUNTER_CLOCKWISE && oppBearing > -145 && oppBearing < -90)) {
						currentTurn *= -1;
					}
				}
				turnWave = currentWave;
			}
			// set the turn
			driveAngle = RCMath.normalizeDegrees(driveAngle + currentTurn);
		}
		// now drive
		VelocityVector driveVector = orbitalDriver.getSmoothedOrbitAngle(driveAngle, RCPhysics.MAX_SPEED);
		driveController.drive(driveVector.getRoboAngle(), driveVector.getMagnitude());
	}

	@Override
	public void drive(DriveController driveController) {
		// no action required; can't escape what we can't see
	}

	@Override
	public void onRoundBegin() {
		currentWave = null;
		turnWave = null;
	}

	@Override
	public void oppWaveCreated(Wave wave) {
		// no action required	
	}

	@Override
	public void oppWaveHitBullet(Wave wave, Bullet oppBullet) {
		// no action required
	}

	@Override
	public void oppWaveHit(Wave wave) {
		// no action required	
	}

	@Override
	public void oppNextWaveToHit(Wave wave) {
		currentWave = wave;	
	}

	@Override
	public void oppBulletHit(Wave wave, HitByBulletEvent hitByBulletEvent) {
		// no action required	
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
}
