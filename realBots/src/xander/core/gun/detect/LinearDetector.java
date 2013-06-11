package xander.core.gun.detect;

import xander.core.math.Linear;
import xander.core.math.LinearIntercept;
import xander.core.math.RCPhysics;
import xander.core.track.Wave;

public class LinearDetector extends TargetingDetector {

	public LinearDetector(boolean offensive) {
		super("Linear Detector", offensive);
	}

	@Override
	public double getDetectionAngle(Wave wave) {
		LinearIntercept linearTrajectory = Linear.calculateTrajectory(
				wave.getInitialDefenderSnapshot(), 
				wave.getOriginX(), wave.getOriginY(), wave.getBulletVelocity(), 
				robotProxy.getBattleFieldSize(), wave.getOriginTime());
		return (linearTrajectory == null)? -1 : linearTrajectory.getVelocityVector().getRoboAngle();
	}

	@Override
	protected double getSloppyAimTolerance() {
		return RCPhysics.ROBOT_HALF_WIDTH;
	}

}
