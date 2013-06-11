package xander.paint;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a map of paintable objects available for use by the Xander Painting
 * Framework.  
 * 
 * Any class/object that wishes to have information about it painted should 
 * implement the Paintable interface and add itself to the paintables maintained
 * herein.
 * 
 * @author Scott Arnold
 */
public class Paintables {

	private static final Paintables instance = new Paintables();
	
	private Map<String, Paintable> paintables = new HashMap<String, Paintable>();
	private Map<Class<?>, Paintable> genericPaintables = new HashMap<Class<?>, Paintable>();
	
	private Paintables() {
	}
	
	public static Paintables getInstance() {
		return instance;
	}
	
	public static void addPaintable(Paintable paintable) {
		instance.add(paintable);
	}
	
	public static Map<String, Paintable> getPaintables() {
		return instance.paintables;
	}
	
	public static Map<Class<?>, Paintable> getGenericPaintables() {
		return instance.genericPaintables;
	}
	
	public Paintable getPaintable(Painter<?> painter) {
		Paintable paintable = paintables.get(painter.getName());
		if (paintable == null) {
			paintable = genericPaintables.get(painter.getPaintableClass());
		}
		return paintable;
	}
	
	public void add(Paintable paintable) {
		if (paintable.getPainterName() == null) {
			genericPaintables.put(paintable.getClass(), paintable);
		} else {
			paintables.put(paintable.getPainterName(), paintable);
		}
	}
}
