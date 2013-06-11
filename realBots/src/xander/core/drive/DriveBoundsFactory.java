package xander.core.drive;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import xander.core.math.RCMath;
import xander.core.math.RCPhysics;

public class DriveBoundsFactory {

	public static Path2D.Double getRectangularBounds(Rectangle2D.Double bb) {
		Path2D.Double path = new Path2D.Double();
		double rhw = RCPhysics.ROBOT_HALF_WIDTH;
		path.moveTo(bb.getMinX()+rhw, bb.getMinY()+rhw);
		path.lineTo(bb.getMaxX()-rhw, bb.getMinY()+rhw);
		path.lineTo(bb.getMaxX()-rhw, bb.getMaxY()-rhw);
		path.lineTo(bb.getMinX()+rhw, bb.getMaxY()-rhw);
		path.closePath();
		return path;
	}
	
	public static Path2D.Double getSmoothedRectangleBounds(Rectangle2D.Double bb) {
		return getSmoothedRectangleBounds(bb, -0.025, 1d/3d);
	}
	
	public static Path2D.Double getSmoothedRectangleBounds(Rectangle2D.Double bb, double cornerMultiplier, double beginCurve) {
		Path2D.Double path = new Path2D.Double();
		double rhw = RCPhysics.ROBOT_HALF_WIDTH;
		double cornerToCornerLength = Math.sqrt(bb.height*bb.height + bb.width*bb.width);
		double overLength = cornerToCornerLength * cornerMultiplier;
		double cornerHeading = RCMath.getRobocodeAngle(bb.getMinX(), bb.getMinY(), bb.getMaxX(), bb.getMaxY());
		Point2D.Double overPos = RCMath.getLocation(bb.getMinX(), bb.getMinY(), overLength, cornerHeading);
		double xd = bb.getWidth() * beginCurve;
		double yd = bb.getHeight() * beginCurve;
		path.moveTo(bb.getMinX()+rhw, bb.getMinY()+yd);
		path.quadTo(bb.getMinX()-overPos.x, bb.getMinY()-overPos.y, bb.getMinX()+xd, bb.getMinY()+rhw);
		path.lineTo(bb.getMaxX()-xd, bb.getMinY()+rhw);
		path.quadTo(bb.getMaxX()+overPos.x, bb.getMinY()-overPos.y, bb.getMaxX()-rhw, bb.getMinY()+yd);
		path.lineTo(bb.getMaxX()-rhw, bb.getMaxY()-yd);
		path.quadTo(bb.getMaxX()+overPos.x, bb.getMaxY()+overPos.y, bb.getMaxX()-xd, bb.getMaxY()-rhw);
		path.lineTo(bb.getMinX()+xd, bb.getMaxY()-rhw);
		path.quadTo(bb.getMinX()-overPos.x, bb.getMaxY()+overPos.y, bb.getMinX()+rhw, bb.getMaxY()-yd);
		path.closePath();
		return path;
	}
	
	public static Path2D.Double getCompressedRectangleBounds(Rectangle2D.Double bb) {
		Path2D.Double path = new Path2D.Double();
		double rhw = RCPhysics.ROBOT_HALF_WIDTH;
		double cornerToCornerLength = Math.sqrt(bb.height*bb.height + bb.width*bb.width);
		double overLength = cornerToCornerLength * -0.025;
		double cornerHeading = RCMath.getRobocodeAngle(0, 0, bb.getMaxX(), bb.getMaxY());
		Point2D.Double overPos = RCMath.getLocation(0, 0, overLength, cornerHeading);
		double xd = bb.getWidth()/3d;
		double yd = bb.getHeight()/3d;
		path.moveTo(bb.getMinX()+rhw, bb.getMinY()+yd);
		double compress = 25;
		path.quadTo(bb.getMinX()-overPos.x, bb.getMinY()-overPos.y, bb.getMinX()+xd, bb.getMinY()+rhw);
		path.quadTo(bb.getCenterX(), bb.getMinY()+rhw+compress, bb.getMaxX()-xd, bb.getMinY()+rhw);
		path.quadTo(bb.getMaxX()+overPos.x, bb.getMinY()-overPos.y, bb.getMaxX()-rhw, bb.getMinY()+yd);
		path.quadTo(bb.getMaxX()-rhw-compress, bb.getCenterY(), bb.getMaxX()-rhw, bb.getMaxY()-yd);
		path.quadTo(bb.getMaxX()+overPos.x, bb.getMaxY()+overPos.y, bb.getMaxX()-xd, bb.getMaxY()-rhw);
		path.quadTo(bb.getCenterX(), bb.getMaxY()-rhw-compress, bb.getMinX()+xd, bb.getMaxY()-rhw);
		path.quadTo(bb.getMinX()-overPos.x, bb.getMaxY()+overPos.y, bb.getMinX()+rhw, bb.getMaxY()-yd);
		path.quadTo(bb.getMinX()+rhw+compress, bb.getCenterY(), bb.getMinX()+rhw, bb.getMinY()+yd);
		path.closePath();
		return path;
	}
}
