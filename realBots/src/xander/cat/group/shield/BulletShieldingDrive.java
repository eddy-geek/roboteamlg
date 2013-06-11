package xander.cat.group.shield;

import xander.core.Resources;
import xander.core.drive.Drive;
import xander.core.drive.DriveController;
import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.track.Snapshot;
import xander.core.track.Wave;

public class BulletShieldingDrive implements Drive {

	//private static final Log log = Logger.getLog(BulletShieldingDrive.class);
	private static enum State {
		RESETTING, MOVING_TO_STANDING, AT_STANDING, REQ_MOVE_TO_FIRE, MOVING_TO_FIRE, AT_FIRING, REQ_MOVE_TO_STANDING;
	}
	private State state = State.RESETTING;
	private double shiftAmount;
	
	public BulletShieldingDrive() {
	}
	
	public String getDstate() {
		return state.toString();
	}
	
	@Override
	public String getName() {
		return "Bullet Shielding Drive";
	}

	@Override
	public void onRoundBegin() {
		reset();
	}
	
	public void reset() {
		this.state = State.RESETTING;
	}
	
	//private String getLoc() {
	//	return "(" + Logger.format(Resources.getRobotProxy().getX()) + ", " + Logger.format(Resources.getRobotProxy().getY()) + ") heading " + Logger.format(Resources.getRobotProxy().getHeadingDegrees());
	//}
	
	@Override
	public void driveTo(Snapshot opponentSnapshot, DriveController driveController) {
		// handle currently moving first
		if (state == State.MOVING_TO_FIRE || state == State.MOVING_TO_STANDING) {
			if (Math.abs(Resources.getRobotProxy().getVelocity()) < 0.01) {  // position has been reached (using velocity instead of distance remaining due to buggy behavior of distance remaining)
				if (state == State.MOVING_TO_FIRE) {
					state = State.AT_FIRING;
					//log.stat("At firing pos: " + getLoc());
				} else if (state == State.MOVING_TO_STANDING) {
					state = State.AT_STANDING;
					//log.stat("At standing pos: " + getLoc());
				}
			} else {
				return; // still moving, wait for move to finish
			}
		}
		// if not currently moving, check for requests to move
		if (state == State.REQ_MOVE_TO_FIRE) {
			state = State.MOVING_TO_FIRE;
			driveController.setTurnRightDegrees(0);
			driveController.setAhead(shiftAmount);         // shift is needed for opponents standing still?  why?????
			driveController.setMaxVelocity(RCPhysics.MAX_SPEED);
		} else if (state == State.REQ_MOVE_TO_STANDING) {
			state = State.MOVING_TO_STANDING;
			driveController.setTurnRightDegrees(0);
			driveController.setBack(shiftAmount);
			driveController.setMaxVelocity(RCPhysics.MAX_SPEED);
		} else if (state != State.AT_FIRING) {  // don't want any turning to happen when at firing position
			// not moving and no requests; just turn to be perpendicular to opponent
			if (state == State.RESETTING) {
				state = State.MOVING_TO_STANDING;
			}
			Snapshot mySnapshot = Resources.getSnapshotHistory().getMySnapshot();
			double perpAngle = RCMath.normalizeDegrees(RCMath.getRobocodeAngle(mySnapshot, opponentSnapshot) + 90);
			driveController.drive(perpAngle, 0);
		}
	}
	
	@Override
	public void drive(DriveController driveController) {
		driveController.drive(Resources.getRobotProxy().getBackAsFrontHeadingDegrees(), 0);
	}
	
	public int getMoveTimeNeededForWave(Wave wave) {
		return (Math.abs(wave.getInitialAttackerSnapshot().getVelocity()) < 0.01)? 2 : 0;
	}
	
	public void requestMoveToFiringPosition(Wave targetedWave) {
		// shift when opp is not moving to prevent parallel shots but stand still if opp is moving
		// set the shiftAmount accordingly
		shiftAmount = (Math.abs(targetedWave.getInitialAttackerSnapshot().getVelocity()) < 0.01)? 0.01 : 0;
		if (shiftAmount == 0) {
			state = State.AT_FIRING;
		} else {
			state = State.REQ_MOVE_TO_FIRE;
		}
	}
	
	public void requestMoveToStandingPosition() {
		if (shiftAmount == 0) {
			state = State.AT_STANDING;
		} else {
			state = State.REQ_MOVE_TO_STANDING;
		}
	}
	
	public boolean isAtFiringPosition() {
		return state == State.AT_FIRING;
	}
	
	public boolean isAtStandingPosition() {
		return state == State.AT_STANDING;
	}
}
