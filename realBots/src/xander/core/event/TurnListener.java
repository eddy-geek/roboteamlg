package xander.core.event;

/**
 * Implemented by any classes that wish to be informed each time the main
 * turn loop is executed.  Event is fired immediately before calling
 * execute() on the main loop.
 * 
 * @author Scott Arnold
 */
public interface TurnListener {

	public void onTurnBegin();
	
	public void onTurnEnd();
}
