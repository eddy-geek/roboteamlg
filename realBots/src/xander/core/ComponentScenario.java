package xander.core;

import xander.core.drive.Drive;
import xander.core.gun.Gun;
import xander.core.radar.Radar;

/**
 * Associates a Scenario with a ComponentSet.  
 * 
 * @author Scott Arnold
 */
public class ComponentScenario {

	private Scenario scenario;
	private ComponentSet componentSet = new ComponentSet();
	
	public ComponentScenario(Scenario scenario, Component... components) {
		this.scenario = scenario;
		for (Component component : components) {
			if (component instanceof Radar) {
				this.componentSet.radar = (Radar) component;
			}
			if (component instanceof Drive) {
				this.componentSet.drive = (Drive) component;
			} 
			if (component instanceof Gun) {
				this.componentSet.gun = (Gun) component;
			}
		}
	}
	
	/**
	 * Loads a ComponentSet with the components from the ComponentSet contained
	 * in this ComponentScenario when the scenario is applicable.  Loading will
	 * only occur if the scenario applies and the ComponentSet to be loaded is 
	 * not already set up with the types of components associated with the 
	 * scenario.  If loading occurs, all non-null components for the scenario are 
	 * loaded, regardless of whether or not this would replace any non-null 
	 * components in the set being loaded.
	 * 
	 * @param componentSet    component set to load from this component scenario
	 */
	public void loadComponents(ComponentSet componentSet) {
		if (scenario.applies()) {
			if ((this.componentSet.radar != null && componentSet.radar == null)
					|| (this.componentSet.drive != null && componentSet.drive == null) 
					|| (this.componentSet.gun != null && componentSet.gun == null)) {
				if (this.componentSet.radar != null) {
					componentSet.radar = this.componentSet.radar;
				}
				if (this.componentSet.drive != null) {
					componentSet.drive = this.componentSet.drive;
				}
				if (this.componentSet.gun != null) {
					componentSet.gun = this.componentSet.gun;
				}
			}
		}
	}
}
