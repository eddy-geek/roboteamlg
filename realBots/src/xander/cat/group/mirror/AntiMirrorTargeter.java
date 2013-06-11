package xander.cat.group.mirror;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.drive.Direction;
import xander.core.drive.DistancingEquation;
import xander.core.drive.DriveState;
import xander.core.drive.OrbitalDrivePredictor;
import xander.core.gun.targeter.Targeter;
import xander.core.math.RCMath;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;
import xander.core.track.Wave;

public class AntiMirrorTargeter implements Targeter {

	private MirrorPlan mirrorPlan;
	private MirrorDetector mirrorDetector;
	private RobotProxy robotProxy;
	private SnapshotHistory snapshotHistory;
	private OrbitalDrivePredictor orbitalDriver;
	
	public AntiMirrorTargeter(MirrorPlan mirrorPlan, MirrorDetector mirrorDetector, OrbitalDrivePredictor orbitalDriver) {
		this.orbitalDriver = orbitalDriver;
		this.mirrorPlan = mirrorPlan;
		this.mirrorDetector = mirrorDetector;
		this.robotProxy = Resources.getRobotProxy();
		this.snapshotHistory = Resources.getSnapshotHistory();
	}
	
	@Override
	public String getTargetingType() {
		return "Mirror";
	}
	
	@Override
	public boolean canAimAt(Snapshot target) {
		// ensure starting snapshot exists
		long currentTime = robotProxy.getTime();
		long time = currentTime - mirrorDetector.getMirrorDetectedTicksAgo();
		return snapshotHistory.getMySnapshot(time, false) != null;
	}
	
	@Override
	public double getAim(Snapshot target,
			Snapshot myself, Wave wave) {
		long currentTime = robotProxy.getTime();
		long time = currentTime - mirrorDetector.getMirrorDetectedTicksAgo();
		// calculate our position when bullet would reach other side
		double bulletVelocity = wave.getBulletVelocity();
		double distanceToOpponent = Double.MAX_VALUE;
		double bulletTravelDistance = 0;
		Snapshot mySnapshot = snapshotHistory.getMySnapshot(time, false);
		DriveState myDriveState = new DriveState(mySnapshot);
		Rectangle2D.Double battlefieldBounds = robotProxy.getBattleFieldSize();
		while (bulletTravelDistance < distanceToOpponent - bulletVelocity*Math.abs(myDriveState.getVelocity()/8)) { // slight adjustment as opponents seem to lag a little at speed 
			bulletTravelDistance += bulletVelocity;
			Point2D.Double center = AntiMirrorDrive.getOrbitCenter(
					robotProxy.getX(), 
					robotProxy.getY(), 
					battlefieldBounds);
			if (time < currentTime) {
				// allow guess here -- exception occurred here once when snapshot wasn't found
				mySnapshot = snapshotHistory.getMySnapshot(time, true);
				myDriveState.setState(mySnapshot);
			} else {
				Direction direction = mirrorPlan.getDirection(time);
				orbitalDriver.advanceOrbitalDriveState(
						myDriveState,  
						center, 100, direction, 1, 
						AntiMirrorDrive.DRIVE_SPEED, DistancingEquation.NO_ADJUST);
			}
			time++;
			distanceToOpponent = 2 * RCMath.getDistanceBetweenPoints(
					myDriveState.getPosition().x, 
					myDriveState.getPosition().y, 
					battlefieldBounds.getCenterX(), 
					battlefieldBounds.getCenterY());
		}
		// aim gun at to hit opposing position
		double futureOppX = battlefieldBounds.getMaxX() - myDriveState.getPosition().x;
		double futureOppY = battlefieldBounds.getMaxY() - myDriveState.getPosition().y;
		double targetAngle = RCMath.getRobocodeAngle(myself.getX(), myself.getY(), futureOppX, futureOppY);
		return targetAngle;
	}
}
