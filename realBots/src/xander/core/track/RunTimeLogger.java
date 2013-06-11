package xander.core.track;

import java.util.HashMap;
import java.util.Map;

import xander.core.io.BattleStats;
import xander.core.log.Log;
import xander.core.log.Logger;

public class RunTimeLogger {

	private static final Log log = Logger.getLog(RunTimeLogger.class);
	private static final Map<String, RunTimeLogger> loggers = new HashMap<String, RunTimeLogger>();
	
	private String loggerFor;
	private double cumulativeTime;
	private int numExecutions;
	private double[] peakTimes = new double[5];
	private long startTime, stopTime;
	private boolean active = true;
	
	public static RunTimeLogger getLoggerFor(String loggerFor) {
		RunTimeLogger runTimeLogger = loggers.get(loggerFor);
		if (runTimeLogger == null) {
			runTimeLogger = new RunTimeLogger(loggerFor);
			loggers.put(loggerFor, runTimeLogger);
		}
		return runTimeLogger;
	}
	
	public static void logAll() {
		for (RunTimeLogger rtLogger : loggers.values()) {
			rtLogger.log();
		}
	}
	
	private RunTimeLogger(String loggerFor) {
		this.loggerFor = loggerFor;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public void start() {
		startTime = System.nanoTime();
	}
	
	public void stop() {
		stopTime = System.nanoTime();
		if (active) {
			double exeTimeMS = (double)(stopTime - startTime)/1000000d;
			numExecutions++;
			cumulativeTime += exeTimeMS;
			if (exeTimeMS > peakTimes[peakTimes.length-1]) {
				int i = 0;
				while (exeTimeMS < peakTimes[i]) {
					i++;
				}
				for (int j=peakTimes.length-1; j>i; j--) {
					peakTimes[j] = peakTimes[j-1];
				}
				peakTimes[i] = exeTimeMS;
			}
		}
	}
	
	public double getAverageExecutionTime() {
		return (numExecutions > 0)? cumulativeTime / (double)numExecutions : 0;
	}
	
	public double[] getPeaks() {
		return peakTimes;
	}
	
	public void log() {
		if (active) {
			double avgExeTime = cumulativeTime / (double)numExecutions;
			StringBuilder sb = new StringBuilder();
			sb.append(loggerFor).append(": ").append(Logger.format(avgExeTime, 3)).append(" ms average, ");
			for (int i=0; i<peakTimes.length; i++) {
				if (i>0) {
					sb.append(';');
				}
				sb.append(Logger.format(peakTimes[i], 3));
			}
			sb.append(" ms peaks.");
			log.info(sb.toString());
		}
	}
	
	public void saveTo(Map<String, String> statsMap, int totalBattles) {
		String key = "Runtime Avg: " + loggerFor;
		BattleStats.updateAveragedStatValue(statsMap, key, getAverageExecutionTime(), 3, totalBattles);
		for (int i=0; i<peakTimes.length; i++) {
			key = "Runtime P" + (i+1) + ": " + loggerFor;
			BattleStats.updateAveragedStatValue(statsMap, key, peakTimes[i], 3, totalBattles);
		}
	}
}
