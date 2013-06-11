package xander.cat.group.shield;

import xander.core.track.Wave;

/**
 * Interface for bullet shielding targeters that aim at opponent bullet waves.
 * 
 * @author Scott Arnold
 */
public interface BulletTargeter {

	/**
	 * Returns aim IN ROBO RADIANS.
	 * 
	 * @param wave   wave to calculate aim for
	 * 
	 * @return   aim in robo-radians
	 */
	public double getAim(Wave wave);
}
