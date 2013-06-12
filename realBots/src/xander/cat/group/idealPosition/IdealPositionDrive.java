package xander.cat.group.idealPosition;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.drive.Drive;
import xander.core.drive.DriveController;
import xander.core.math.LinearEquation;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.track.Snapshot;

/**
 * This drive positions us in a reasonable spot not too close to the opponent
 * nor to close to any wall.  The drive path is virtually straight, so this 
 * drive should only be used when no one is shooting as us.
 * 
 * @author Scott Arnold
 */
public class IdealPositionDrive implements Drive {

	private RobotProxy robot;
	private Rectangle2D.Double idealBounds;
	private Rectangle2D.Double battlefieldBounds;
	private double adjustRange = 300;
	private LinearEquation offsetAdjustEq = new LinearEquation(
			adjustRange, 0, RCPhysics.ROBOT_WIDTH*3, 90, 0, 90);
	
	public IdealPositionDrive() {
		this.robot = Resources.getRobotProxy();
		this.battlefieldBounds = robot.getBattleFieldSize();
		this.idealBounds = RCMath.shrink(this.battlefieldBounds, 40);
	}
	
	@Override
	public String getName() {
		return "Ideal Position Drive";
	}

	@Override
	public void onRoundBegin() {
		// no action required
	}

	@Override
	public void driveTo(Snapshot opponentSnapshot, DriveController driveController) {
		Point2D counterPosition = getIdealCounterPosition(
				opponentSnapshot.getX(), opponentSnapshot.getY());
		double angleToDrive = RCMath.getRobocodeAngle(
				robot.getX(), robot.getY(), 
				counterPosition.getX(), counterPosition.getY());
		if (opponentSnapshot.getDistance() < adjustRange) {
			double bearing = RCMath.getRobocodeAngle(
					robot.getX(), robot.getY(), 
					opponentSnapshot.getX(), opponentSnapshot.getY());
			double offset = RCMath.getTurnAngle(angleToDrive, bearing);
			double minOffset = offsetAdjustEq.getY(opponentSnapshot.getDistance());
			if (Math.abs(offset) < minOffset) {
				double diff = minOffset - Math.abs(offset);
				if (offset < 0) {
					angleToDrive = RCMath.normalizeDegrees(angleToDrive + diff);
				} else {
					angleToDrive = RCMath.normalizeDegrees(angleToDrive - diff);
				}
			}
		}
		driveController.drive(angleToDrive, RCPhysics.MAX_SPEED);
	}

	public void drive(DriveController driveController) {
		if (robot.getOthers() == 0) {
			driveController.drive(0, 0);
		} else {
			double angleToDrive = RCMath.getRobocodeAngleToCenter(
					robot.getX(), 
					robot.getY(), 
					battlefieldBounds);
			driveController.drive(angleToDrive, RCPhysics.MAX_SPEED);
		}
	}

	public Point2D getIdealCounterPosition(double opponentX, double opponentY) {
		double oppAngleToCenter = RCMath.getRobocodeAngleToCenter(opponentX, opponentY, battlefieldBounds);
		double dist = RCMath.getDistanceToIntersect(opponentX, opponentY, oppAngleToCenter, battlefieldBounds);
		Point2D.Double target = RCMath.getLocation(opponentX, opponentY, dist/2d, oppAngleToCenter);
		target.x = RCMath.limit(target.x, idealBounds.getMinX(), idealBounds.getMaxX());
		target.y = RCMath.limit(target.y, idealBounds.getMinY(), idealBounds.getMaxY());
		return target;
	}
}
