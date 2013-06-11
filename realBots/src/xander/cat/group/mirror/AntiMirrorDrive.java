package xander.cat.group.mirror;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.drive.Direction;
import xander.core.drive.DistancingEquation;
import xander.core.drive.Drive;
import xander.core.drive.DriveController;
import xander.core.drive.OrbitalDrivePredictor;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.math.VelocityVector;
import xander.core.track.Snapshot;

public class AntiMirrorDrive implements Drive {

	public static final double DRIVE_SPEED = RCPhysics.MAX_SPEED;
	
	private MirrorPlan mirrorPlan;
	private RobotProxy robotProxy;
	private OrbitalDrivePredictor orbitalDriver;
	
	public AntiMirrorDrive(MirrorPlan mirrorPlan, OrbitalDrivePredictor orbitalDriver) {
		this.orbitalDriver = orbitalDriver;
		this.mirrorPlan = mirrorPlan;
		this.robotProxy = Resources.getRobotProxy();
	}
	
	@Override
	public String getName() {
		return "Anti-Mirror Drive";
	}

	@Override
	public void onRoundBegin() {
		// no action required
	}

	@Override
	public void driveTo(Snapshot opponentSnapshot, DriveController driveController) {
		drive(driveController);
	}

	public static Point2D.Double getOrbitCenter(double x, double y, Rectangle2D.Double battlefieldBounds) {
		double angleToCenter = RCMath.getRobocodeAngleToCenter(x, y, battlefieldBounds);
		double distance = RCMath.getDistanceToIntersect(x, y, angleToCenter, battlefieldBounds);
		return RCMath.getLocation(x, y, distance, angleToCenter);
	}
	
	@Override
	public void drive(DriveController driveController) {
		Direction direction = mirrorPlan.getDirection(robotProxy.getTime());
		Point2D.Double center = getOrbitCenter(
				robotProxy.getX(), 
				robotProxy.getY(), 
				robotProxy.getBattleFieldSize());
		// note: distance (100) doesn't matter when using NO_ADJUST
		VelocityVector orbitVector = orbitalDriver.getSmoothedOrbitAngle(
				center, 100, direction, DistancingEquation.NO_ADJUST, DRIVE_SPEED);
		driveController.drive(orbitVector.getRoboAngle(), orbitVector.getMagnitude());
	}
}
