package xander.core.radar;

import xander.core.Component;
import xander.core.track.Snapshot;

/**
 * Interface to be implemented by XanderBot radar classes.
 * 
 * @author Scott Arnold
 */
public interface Radar extends Component {

	/**
	 * Prepare next sweep of radar and return snapshot of robot currently
	 * being focused upon.
	 * 
	 * @param radarController  radar controller
	 * 
	 * @return     snapshot of robot to attack
	 */
	public Snapshot search(RadarController radarController);
}
