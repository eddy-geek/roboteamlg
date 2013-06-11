package xander.cat.group.ram;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.drive.Drive;
import xander.core.drive.DriveController;
import xander.core.math.Linear;
import xander.core.math.LinearIntercept;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.track.Snapshot;

/**
 * Drive to ram the opponent.  Uses direct approach at longer range and switches 
 * to linear approach at close range.
 * 
 * @author Scott Arnold
 */
public class RamDrive implements Drive {

	private RobotProxy robotProxy;
	
	public RamDrive() {
		this.robotProxy = Resources.getRobotProxy();
	}
	
	@Override
	public String getName() {
		return "Ram Drive";
	}

	@Override
	public void onRoundBegin() {
		// no action required
	}

	@Override
	public void driveTo(Snapshot opponentSnapshot,
			DriveController driveController) {
		double directlyAtOpponent = RCMath.getRobocodeAngle(
				robotProxy.getX(), robotProxy.getY(), 
				opponentSnapshot.getX(), opponentSnapshot.getY());
		LinearIntercept intercept = Linear.calculateTrajectory(opponentSnapshot, 
				robotProxy.getX(), robotProxy.getY(), RCPhysics.MAX_SPEED + 0.1, 
				robotProxy.getBattleFieldSize(), robotProxy.getTime());		
		if (opponentSnapshot.getDistance() > 250 || intercept == null) {
			// when farther away, it works better to just drive directly at them
			driveController.drive(directlyAtOpponent, RCPhysics.MAX_SPEED);
		} else if (opponentSnapshot.getDistance() > 75) {
			// blend direct and linear intercept
			double halfDiff = RCMath.getTurnAngle(directlyAtOpponent, intercept.getVelocityVector().getRoboAngle()) / 2d;
			double blended = RCMath.normalizeDegrees(directlyAtOpponent + halfDiff);
			driveController.drive(blended, RCPhysics.MAX_SPEED);
		} else {
			// in close, chase them using linear intercept
			driveController.drive(intercept.getVelocityVector().getRoboAngle(), RCPhysics.MAX_SPEED);
		}
	}

	@Override
	public void drive(DriveController driveController) {
		// no action required
	}
}
