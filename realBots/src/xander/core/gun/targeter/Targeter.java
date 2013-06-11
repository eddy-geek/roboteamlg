package xander.core.gun.targeter;

import xander.core.track.Snapshot;
import xander.core.track.Wave;

public interface Targeter {
	/**
	 * Returns a word describing what type of targeting this targeter uses.  
	 * The word should be unique among targeters.
	 * 
	 * @return     name of gun
	 */
	public String getTargetingType();
	
	/**
	 * Returns whether or not this targeter is currently capable of aiming  
	 * at the given robot.  This method should be lightweight (most
	 * targeters should just return true).
	 * 
	 * @param target           snapshot of robot to fire at
	 * 
	 * @return                 whether or not gun can fire on opponent
	 */
	public boolean canAimAt(Snapshot target);
		
	/**
	 * Returns information on where to aim to shoot at the given opponent.
	 * 
	 * @param target            snapshot of target to aim at
	 * @param myself            snapshot of self
	 * @param wave              wave (useful to surfers, but technically doesn't exist until a gun fires)
	 * 
	 * @return          angle in Robocode degrees indicating where to aim to shoot at opponent
	 */
	public double getAim(Snapshot target, Snapshot myself, Wave wave);
}
