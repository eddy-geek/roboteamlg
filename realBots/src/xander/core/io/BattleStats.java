package xander.core.io;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class BattleStats implements Serializable {

	public static final String COMMON_STAT_KEY = "DfPEdfeWQzfcfckRlFDSo";
	
	private static final long serialVersionUID = 20110928L;
	private static final NumberFormat numberFormatter = NumberFormat.getNumberInstance();
	
	String robotName;    // name of our robot, including version
	private boolean clearStatsOnNewVersion = true;
	Map<String, Map<String, String>> statMap = new HashMap<String, Map<String, String>>();
	
	/**
	 * Update an average double statistic value in the given statistics map.
	 * 
	 * @param statMap         the statistics map, either for an individual robot or for all robots
	 * @param key             the key for the statistic
	 * @param newValue        the new value to factor into the average
	 * @param decimalPlaces   the number of decimal places to maintain
	 * @param totalBattles    the total number of battles, including the battle where the new value was obtained
	 */
	public static void updateAveragedStatValue(Map<String, String> statMap, String key, double newValue, int decimalPlaces, int totalBattles) {
		String previousFormattedValue = statMap.get(key);
		if (previousFormattedValue != null) {
			double previousValue = newValue;
			try {
				previousValue = Double.parseDouble(previousFormattedValue);
			} catch (Exception e) {
			}
			newValue = (newValue + (totalBattles-1) * previousValue) / (double)totalBattles;
		}
		numberFormatter.setMinimumFractionDigits(decimalPlaces);
		numberFormatter.setMaximumFractionDigits(decimalPlaces);
		statMap.put(key, numberFormatter.format(newValue));
	}
	
	public boolean isClearStatsOnNewVersion() {
		return clearStatsOnNewVersion;
	}

	public void setClearStatsOnNewVersion(boolean clearStatsOnNewVersion) {
		this.clearStatsOnNewVersion = clearStatsOnNewVersion;
	}

	void clear() {
		statMap.clear();
	}
	
	public Map<String, String> getStatsForRobot(String robotName, boolean create) {
		Map<String, String> robotStats = statMap.get(robotName);
		if (robotStats == null && create) {
			robotStats = new HashMap<String, String>();
			statMap.put(robotName, robotStats);
		} 
		return robotStats;
	}
	
	public Map<String, String> getCommonStats() {
		return getStatsForRobot(COMMON_STAT_KEY, true);
	}
	
	public String getStatForRobot(String robotName, String statName) {
		Map<String, String> robotStats = statMap.get(robotName);
		if (robotStats != null) {
			return robotStats.get(statName);
		} else {
			return null;
		}
	}
	
	public void setStatForRobot(String robotName, String statName, String statValue) {
		Map<String, String> robotStats = statMap.get(robotName);
		if (robotStats == null) {
			robotStats = new HashMap<String, String>();
			statMap.put(robotName, robotStats);
		}
		robotStats.put(statName, statValue);
	}
	
	public void removeStatForRobot(String robotName, String statName) {
		Map<String, String> robotStats = statMap.get(robotName);
		if (robotStats != null) {
			robotStats.remove(statName);
			if (robotStats.size() == 0) {
				statMap.remove(robotName);
			}
		}
	}
	
	public void removeStatsForRobot(String robotName) {
		statMap.remove(robotName);
	}
}
