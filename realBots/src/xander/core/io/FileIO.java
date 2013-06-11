package xander.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import robocode.AdvancedRobot;
import robocode.RobocodeFileOutputStream;
import robocode.RobocodeFileWriter;
import xander.core.Configuration;
import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.log.Log;
import xander.core.log.Logger;

public class FileIO {

	private static final Log log = Logger.getLog(FileIO.class);
	private static final String BATTLE_STATS_SUFFIX = "_BattleStats";
	private static final String BATTLE_STATS_EXT = ".dat";
	
	private static BattleStats battleStats;
	
	private static String getFileName(String fileNameSuffix, String extension) {
		RobotProxy robotProxy = Resources.getRobotProxy();
		String fileName = robotProxy.getName().split(" ")[0] + fileNameSuffix;
		if (extension != null && extension.length() > 0) {
			if (extension.startsWith(".")) {
				fileName = fileName + extension;
			} else {
				fileName = fileName + "." + extension;
			}
		}
		return fileName;
	}
	
	public static BattleStats getBattleStats(Configuration configuration) {
		if (battleStats == null) {
			loadBattleStats(configuration);
		}
		return battleStats;
	}
	
	public static void loadBattleStats(Configuration configuration) {
		String battleStatsFileName = getFileName(BATTLE_STATS_SUFFIX, BATTLE_STATS_EXT);
		String battleStatsZipFileName = getFileName(BATTLE_STATS_SUFFIX, ".zip");
		
		// clean up battle stats file from previous version if it exists
		File battleStatsFile = Resources.getRobotProxy().getDataFile(battleStatsFileName);
		if (battleStatsFile.exists()) {
			battleStatsFile.delete();
		}
		
		File battleStatsZipFile = Resources.getRobotProxy().getDataFile(battleStatsZipFileName);
		ObjectInput oi = null;
		if (battleStatsZipFile.exists()) {
			try {
				ZipInputStream zis = new ZipInputStream(new FileInputStream(battleStatsZipFile));
				zis.getNextEntry();
				oi = new ObjectInputStream(zis);
				battleStats = (BattleStats) oi.readObject();
				oi.close();
			} catch (Exception e) {
				log.error("Unable to load existing battle stats.  Creating new battle stats.");
				battleStats = new BattleStats();
			} finally {
				if (oi != null) {
					try {
						oi.close();
					} catch (Exception e) { }
				}
			}
		} else {
			battleStats = new BattleStats();
		}
		String robotName = Resources.getRobotProxy().getName();
		if (battleStats != null && !robotName.equals(battleStats.robotName)) {
			boolean clearBattleStatsOnNewVersion = configuration.isClearBattleStatsOnNewVersion();
			battleStats.setClearStatsOnNewVersion(clearBattleStatsOnNewVersion);
			if (clearBattleStatsOnNewVersion) {
				battleStats.clear();
			}
			battleStats.robotName = robotName;
		}
	}
	
	public static void saveBattleStats() {
		if (battleStats == null) {
			return;
		}
		String battleStatsFileName = getFileName(BATTLE_STATS_SUFFIX, BATTLE_STATS_EXT);
		String battleStatsZipFileName = getFileName(BATTLE_STATS_SUFFIX, ".zip");
		File battleStatsZipFile = Resources.getRobotProxy().getDataFile(battleStatsZipFileName);
		ObjectOutput oo = null;
		try {
			ZipOutputStream zos = new ZipOutputStream(new RobocodeFileOutputStream(battleStatsZipFile));
			zos.putNextEntry(new ZipEntry(battleStatsFileName));
			oo = new ObjectOutputStream(zos);
			oo.writeObject(battleStats);
		} catch (Exception e) {
			log.error("Unable to save battle stats.");
		} finally {
			if (oo != null) {
				try {
					oo.close();
				} catch (Exception e) { }
			}
		}
	}
	
	public static void logException(AdvancedRobot robot, Exception e) {
		String fileName = robot.getName().split(" ")[0] + "_Exception.txt";
		File exceptionFile = robot.getDataFile(fileName);
		log.error("Fatal exception occurred.");
		log.error("Writing stack trace to " + exceptionFile.getAbsolutePath());
		RobocodeFileWriter writer = null;
		try {
			writer = new RobocodeFileWriter(exceptionFile);
			String s = e.getClass().getName() + ": " + e.getMessage() + "\n";
			writer.write(s);
			log.error(s);
			for (StackTraceElement ste : e.getStackTrace()) {
				s = ste.toString() + "\n";
				writer.write(s);
				log.error(s);
			}
			writer.close();
		} catch (IOException ioe) {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception ce) { }
			}
		}		
	}
	
	public static void logSkippedTurns(AdvancedRobot robot, int skippedTurns) {
		if (skippedTurns > 0) {
			String fileName = robot.getName().split(" ")[0] + "_SkippedTurns.txt";
			File exceptionFile = robot.getDataFile(fileName);
			log.warn(skippedTurns + " turns skipped.");
			RobocodeFileWriter writer = null;
			try {
				writer = new RobocodeFileWriter(exceptionFile);
				String s = "Turns skipped for " + robot.getName() + ": " + skippedTurns + "\n";
				writer.write(s);
				writer.close();
			} catch (IOException ioe) {
				if (writer != null) {
					try {
						writer.close();
					} catch (Exception ce) { }
				}
			}			
		}
	}
}
