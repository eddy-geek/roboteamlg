package xander.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a chain of component scenarios used to choose a drive, gun, and radar 
 * for the robot.
 * 
 * @author Scott Arnold
 */
public class ComponentChain {
	
	private List<ComponentScenario> componentScenarios = new ArrayList<ComponentScenario>();
	private List<RegisteredComponentListener> registeredComponentListeners = new ArrayList<RegisteredComponentListener>();
	
	/**
	 * Adds the given set of components that should be used together for the given scenario.
	 * 
	 * @param scenario      scenario in which to use the components
	 * @param components    components to use for the scenario
	 */
	public void addComponents(Scenario scenario, Component... components) {
		componentScenarios.add(new ComponentScenario(scenario, components));
		for (Component component : components) {
			Resources.getRobotEvents().addRoundBeginListener(component);
			for (RegisteredComponentListener listener : registeredComponentListeners) {
				listener.componentRegistered(component);
			}
		}
	}
	
	/**
	 * Adds the given set of components as default components.  This should only be called
	 * after any components are added with specific scenarios.
	 * 
	 * @param components    default components
	 */
	public void addDefaultComponents(Component... components) {
		for (Component component : components) {
			// add each component separately so they are not treated as a set
			addComponents(new DefaultScenario(), component);
		}
	}
	
	/**
	 * Loads the given component set with components from the chain.
	 * 
	 * @param componentSet    component set to load
	 */
	void loadComponents(ComponentSet componentSet) {
		componentSet.clear();
		int i=0;
		while (!componentSet.isComplete() && i<componentScenarios.size()) {
			ComponentScenario componentScenario = componentScenarios.get(i);
			componentScenario.loadComponents(componentSet);
			i++;
		}		
	}
	
	public void addRegisteredComponentListener(RegisteredComponentListener listener) {
		this.registeredComponentListeners.add(listener);
	}
}
