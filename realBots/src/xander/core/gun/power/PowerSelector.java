package xander.core.gun.power;

import xander.core.track.Snapshot;

/**
 * Interface to be implemented by classes responsible for selecting the fire
 * power to use against opponents.  Power selectors do not need to adjust
 * fire power at low robot energy levels (for self or the opponent); the Xander
 * framework will do this automatically so long as a value of true is 
 * returned from method isAutoAdjustAllowed().
 * 
 * @author Scott Arnold
 */
public interface PowerSelector {
	
	/**
	 * Returns the fire power to use for the given target.
	 * 
	 * @param target    target being fired upon
	 * 
	 * @return    fire power to use for the given target
	 */
	public double getFirePower(Snapshot target);
	
	/**
	 * Returns the minimum power the power selector can return.
	 * 
	 * @return    minimum power the power selector can return
	 */
	public double getMinimumPower();
	
	/**
	 * Returns the maximum power the power selector can return.
	 * 
	 * @return    maximum power the power selector can return
	 */
	public double getMaximumPower();
	
	/**
	 * Returns whether or not this power selector allows the framework to auto-adjust
	 * the power based on robot energy levels.  Most power selectors should return true
	 * from this method.
	 * 
	 * @return  whether or not this power selector allows the framework to auto-adjust the power
	 */
	public boolean isAutoAdjustAllowed();
}
