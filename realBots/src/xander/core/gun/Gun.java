package xander.core.gun;

import xander.core.Component;
import xander.core.track.Snapshot;

public interface Gun extends Component {
	
	/**
	 * Aim and fire upon the given opponent.  Since guns are also required
	 * to have the getAim method, this method can generally call that 
	 * method to get the aim.  The only extra work in this method is 
	 * actually turning the gun and firing.
	 * 
	 * @param target           snapshot of opponent
	 * @param myself           snapshot of self
	 * @param gunController    gun controller
	 * 
	 * @return          whether or not a bullet was fired
	 */
	public boolean fireAt(Snapshot target, Snapshot myself, GunController gunController);

	/**
	 * Returns the aim information for the given opponent.  
	 * 
	 * @param target    the opponent to target
	 * @param myself    snapshot of self
	 * 
	 * @return          aim information for the given opponent
	 */
	public Aim getAim(Snapshot target, Snapshot myself);
	
	/**
	 * Returns whether or not this gun is currently capable of aiming and 
	 * firing on the given robot.  This method should be lightweight (most
	 * guns should just return true).
	 * 
	 * @param target           snapshot of robot to fire at
	 * 
	 * @return                 whether or not gun can fire on opponent
	 */
	public boolean canFireAt(Snapshot target);
}
