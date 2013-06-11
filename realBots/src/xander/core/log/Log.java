package xander.core.log;

import xander.core.Resources;

/**
 * Log logs messages for one class.  
 * 
 * @author Scott Arnold
 */
public class Log {
	
	/**
	 * Defines the different logging levels.
	 * 
	 * @author Scott Arnold
	 */
	public static enum Level {
		
		DEBUG(0,"DEBUG"),
		INFO(1,"INFO"),
		WARN(2,"WARN"),
		STAT(3,"STAT"),
		ERROR(4,"ERROR");
		
		private int priority;
		private String description;
		private Level(int priority, String description) {
			this.priority = priority;
			this.description = description;
		}
		public int getPriority() {
			return priority;
		}
		public String getDescription() {
			return description;
		}
	}
	
	private String className;
	private Level level;
	
	/**
	 * Create a new Log for the given class.  Level will default to 
	 * the default level used in the Logger class.
	 * 
	 * @param className    name of the class Log is for
	 */
	public Log(String className) {
		this(className, null);
	}
	
	/**
	 * Create a new Log for the given class at the given level.
	 * 
	 * @param className		name of the class Log is for
	 * @param level			logging level
	 */
	public Log(String className, Level level) {
		this.className = className;
		this.level = level;
	}
	
	public Level getLevel() {
		return level;
	}
	
	/**
	 * Returns whether or not log messages will be printed for the given level.
	 * 
	 * @param level       level to check
	 * 
	 * @return  whether or not log messages will be printed for the given level
	 */
	public boolean isActiveForLevel(Log.Level level) {
		Log.Level useLevel = (level == null)? Logger.defaultLevel : level;
		return level.getPriority() >= useLevel.getPriority();
	}
	
	public void setLevel(Level level) {
		this.level = level;
	}
	
	private void log(String s, Level msgLevel) {
		Log.Level useLevel = (level == null)? Logger.defaultLevel : level;
		if (msgLevel.getPriority() >= useLevel.getPriority()) {
			String[] lines = s.split("\n");
			String time = String.valueOf(Resources.getTime());
			for (int i=0; i<lines.length; i++) {
				System.out.println(className + " [" + time + "] " + msgLevel.getDescription() + ": " + lines[i]);
			}
		}
	}
	
	public void debug(String s) {
		log(s,Level.DEBUG);
	}
	
	public void info(String s) {
		log(s,Level.INFO);
	}
	
	public void warn(String s) {
		log(s,Level.WARN);
	}
	
	public void stat(String s) {
		log(s,Level.STAT);
	}
	
	public void error(String s) {
		log(s,Level.ERROR);
	}
}
