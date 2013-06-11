package xander.core.log;

import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;

/**
 * Class for managing logs.  Also provides convenience methods for formatting 
 * data as strings.
 * 
 * @author Scott Arnold
 */
public class Logger {

	protected static Log.Level defaultLevel = Log.Level.INFO;
	protected static final NumberFormat nf = NumberFormat.getInstance();
	protected static final Map<Class<?>, Log> logs = new HashMap<Class<?>, Log>();

	public static void setDefaultLogLevel(Log.Level level) {
		defaultLevel = level;
	}
	
	public static Log.Level getDefaultLogLevel() {
		return defaultLevel;
	}
	
	public static void setLogLevel(Class<?> c, Log.Level level) {
		Log log = logs.get(c);
		if (log != null) {
			log.setLevel(level);
		}
	}
	
	public static Log getLog(Class<?> c) {
		return getLog(c, null);
	}
	
	public static Log getLog(Class<?> c, Log.Level level) {
		Log log = logs.get(c);
		if (log == null) {  // the normal case
			log = new Log(c.getName(), level);
			logs.put(c, log);
		}
		return log;
	}
	
	public static String formatPosition(double x, double y) {
		return "(" + format(x) + "," + format(y)+ ")"; 
	}
	
	public static String format(Point2D.Double p) {
		return "(" + format(p.x) + "," + format(p.y)+ ")"; 
	}
	
	public static String format(double d, int minFractionDigits, int maxFractionDigits) {
		nf.setMaximumFractionDigits(maxFractionDigits);
		nf.setMinimumFractionDigits(minFractionDigits);
		return nf.format(d);
	}
	
	public static String format(double d, int maxFractionDigits) {
		nf.setMaximumFractionDigits(maxFractionDigits);
		return nf.format(d);
	}
	
	/**
	 * Formats the given double as a String with maxFractionDigits set to 1.
	 * 
	 * @param d    double value to format as String
	 * 
	 * @return     double value formatted as String with 1 decimal place
	 */
	public static String format(double d) {
		return format(d, 1);
	}
	
	public static String format(int[] intArray) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<intArray.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(intArray[i]);
		}
		return "[" + sb.toString() + "]";
	}
	
	public static String format(double[] dArray, int maxFractionDigits) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<dArray.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(format(dArray[i], maxFractionDigits));
		}
		return "[" + sb.toString() + "]";
	}
	
	public static String formatRobotState(AdvancedRobot robot) {
		StringBuilder sb = new StringBuilder();
		sb.append(";x=").append(format(robot.getX(),3));
		sb.append(";y=").append(format(robot.getY(),3));
		sb.append(";heading(radians)=").append(format(robot.getHeadingRadians(),4));
		sb.append(";velocity=").append(format(robot.getVelocity(),2));
		sb.append(";gunHeading(radians)=").append(format(robot.getGunHeadingRadians(),4));
		sb.append(";energy=").append(format(robot.getEnergy(),2));
		sb.append(";gunHeat=").append(format(robot.getGunHeat(),2));
		return sb.toString();
	}
}
