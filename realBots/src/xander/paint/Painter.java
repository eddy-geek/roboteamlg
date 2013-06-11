package xander.paint;

import java.awt.Graphics2D;

/**
 * Base interface that must be implemented by any class that wishes to paint on 
 * the battle field using the Xander Painting Framework.
 * 
 * @author Scott Arnold
 *
 * @param <T>   type of class that this painter can use to paint.
 */
public interface Painter<T extends Paintable> {

	public String getName();
	
	public Class<T> getPaintableClass();
	
	public void onPaint(Graphics2D g, T paintable);
}
