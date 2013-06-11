package xander.paint;

/**
 * Interface to be implemented by any class that wishes to have information about 
 * itself painted in the Xander Painting Framework.
 * 
 * @author Scott Arnold
 */
public interface Paintable {

	/**
	 * Returns a unique name for the Painter that is responsible for painting
	 * this Paintable.
	 * 
	 * @return   Painter name for this Paintable
	 */
	public String getPainterName();
	
}
