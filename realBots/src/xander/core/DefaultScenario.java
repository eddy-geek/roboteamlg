package xander.core;

/**
 * Container scenario for default components.  A default scenario always applies.
 * 
 * @author Scott Arnold
 */
public class DefaultScenario implements Scenario {

	@Override
	public boolean applies() {
		return true;
	}

}
