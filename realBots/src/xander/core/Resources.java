package xander.core;

import teamlg.bot.RobotList;
import xander.core.drive.DriveController;
import xander.core.gun.GunController;
import xander.core.radar.RadarController;
import xander.core.track.DriveStats;
import xander.core.track.GunStats;
import xander.core.track.OpponentGunWatcher;
import xander.core.track.SnapshotHistory;
import xander.core.track.WaveHistory;


/**
 * Manager for all static resources of the robot.  In essence, this serves as 
 * a service locator, providing a single lookup point for resources such as:
 * <ul>
 *   <li>RobotEvents - for registering objects as event listeners</li>
 *   <li>RobotProxy - for access to all of the normal robot getXxx methods.</li>
 *   <li>SnapshotHistory - log of robot snapshots going back a set number of turns.</li>
 *   <li>WaveHistory - log of bullet waves for self and opponent.</li>
 *   <li>GunStats - variety of gun-related statistics.</li>
 *   <li>DriveStats - variety of drive-related statistics.</li>
 * </ul>
 * Also manages some framework-only resources, which are only available to classes 
 * within the package, including:
 * <ul>
 *   <li>Controllers - for access to all of the normal robot setXxx methods for the drive, gun, and radar.</li>
 *   <li>RobotStyle - robot styling attributes.</li>
 *   <li>Configuration - robot configuration settings.</li>
 *   <li>OpponentGunWatcher - framework component for detecting when opponent fires bullets.</li>
 * </ul>
 * In addition to providing access to services, this class also provides convenience methods
 * for getting the round time and cumulative time.
 * 
 * @author Scott Arnold
 */
public class Resources {

	private static long cumulativeTimeFromPreviousRounds;
	private static boolean roundActive;
	private static RadarController radarController = new RadarController();
	private static DriveController driveController = new DriveController();
	private static GunController gunController = new GunController();
	private static OpponentGunWatcher opponentGunWatcher;
	private static RobotProxy robotProxy = new RobotProxy();
	private static RobotEvents robotEvents = new RobotEvents();
	private static GunStats gunStats;
	private static DriveStats driveStats;
	private static SnapshotHistory snapshotHistory;
	private static WaveHistory waveHistory;
	private static Configuration configuration = new Configuration();
	private static RobotStyle robotStyle = new RobotStyle();
	private static long winOrDeathTime;
        private static RobotList robotList;
	
	static void initialize(AbstractXanderRobot robot, ComponentChain chain) {
		robotProxy.setRobot(robot);
		snapshotHistory = new SnapshotHistory(robot.getName(), robotProxy, configuration, robotEvents);

		opponentGunWatcher = new OpponentGunWatcher(snapshotHistory, configuration);
		waveHistory = new WaveHistory(gunController, opponentGunWatcher, 
				robotEvents, robotProxy, snapshotHistory, configuration);
		gunStats = new GunStats(robotProxy, waveHistory, robotEvents, configuration);
		driveStats = new DriveStats(robotProxy, robotEvents, configuration, chain);
                robotList = new RobotList();
	}
	
	static void beginRound(AbstractXanderRobot robot) {
		if (roundActive) {
			// work around for endRound not getting called on Robocode v1.6.1.2 used in RoboResearch
			cumulativeTimeFromPreviousRounds += winOrDeathTime;
		}
		robotProxy.setRobot(robot);
		radarController.setRobot(robot);
		driveController.setRobot(robot);
		gunController.setRobot(robot);
		roundActive = true;
	}
	
	static void endRound() {
		if (roundActive) {
			if (robotProxy.robot != null) {
				cumulativeTimeFromPreviousRounds += robotProxy.getTime();
			}
			roundActive = false;
		}
	}
	
	// work around for endRound not getting called on Robocode v1.6.1.2 used in RoboResearch
	static void onWinOrDeath() {  
		if (robotProxy.robot != null) {
			winOrDeathTime = robotProxy.getTime();
		}
	}
	
	public static long getTime() {
		return (robotProxy.robot == null)? 0 : robotProxy.getTime();
	}
	
	public static long getCumulativeTime() {
		if (robotProxy.robot == null) {
			return 0; 
		} else if (roundActive) {
			return cumulativeTimeFromPreviousRounds + robotProxy.getTime();
		} else {
			return cumulativeTimeFromPreviousRounds;
		}
	}
	
	public static RobotProxy getRobotProxy() {
		return robotProxy;
	}
	
	public static RobotEvents getRobotEvents() {
		return robotEvents;
	}
	
	public static SnapshotHistory getSnapshotHistory() {
		return snapshotHistory;
	}
	
	public static WaveHistory getWaveHistory() {
		return waveHistory;
	}
	
	public static GunStats getGunStats() {
		return gunStats;
	}
	
	public static DriveStats getDriveStats() {
		return driveStats;
	}
	
	static RadarController getRadarController() {
		return radarController;
	}
	
	static DriveController getDriveController() {
		return driveController;
	}
	
	static GunController getGunController() {
		return gunController;
	}
	
	static Configuration getConfiguration() {
		return configuration;
	}
	
	static RobotStyle getRobotStyle() {
		return robotStyle;
	}
        
        public static RobotList getOtherRobots() { 
            return robotList;
        }
	
}
