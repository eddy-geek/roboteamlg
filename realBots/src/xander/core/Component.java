package xander.core;

import xander.core.event.RoundBeginListener;

/**
 * Interface for robot components; namely, drives, guns, and radars.  Components
 * must have a name.  It is assumed that most components will need to perform 
 * begin-of-round initializations, so components by default are also round begin listeners.
 * 
 * @author Scott Arnold
 */
public interface Component extends RoundBeginListener {

	/**
	 * Returns the component's name.
	 * 
	 * @return    component name
	 */
	public String getName();
}
