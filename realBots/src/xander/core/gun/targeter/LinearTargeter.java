package xander.core.gun.targeter;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.math.Linear;
import xander.core.math.LinearIntercept;
import xander.core.track.Snapshot;
import xander.core.track.Wave;

/**
 * Targeter that assumes target will maintain it's current heading and velocity.
 * 
 * @author Scott Arnold
 */
public class LinearTargeter implements Targeter {

	private RobotProxy robotProxy;
	
	public LinearTargeter() {
		this.robotProxy = Resources.getRobotProxy();
	}
	
	@Override
	public String getTargetingType() {
		return "Linear";
	}

	@Override
	public boolean canAimAt(Snapshot target) {
		return true;
	}

	@Override
	public double getAim(Snapshot target, Snapshot myself,
			Wave wave) {
		LinearIntercept targetVector = Linear.calculateTrajectory(
				target, 
				myself.getX(), 
				myself.getY(), 
				wave.getBulletVelocity(), 
				robotProxy.getBattleFieldSize(), 
				robotProxy.getTime());
		return (targetVector == null)? -1 : targetVector.getVelocityVector().getRoboAngle();
	}
}
