package xander.core;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.StatusEvent;
import robocode.WinEvent;
import xander.core.drive.DriveController;
import xander.core.gun.DisabledRobotGun;
import xander.core.gun.Gun;
import xander.core.gun.GunController;
import xander.core.io.BattleStats;
import xander.core.io.FileIO;
import xander.core.log.Log;
import xander.core.log.Logger;
import xander.core.math.RCMath;
import xander.core.paint.CPUUtilizationGraphData;
import xander.core.radar.RadarController;
import xander.core.track.RunTimeLogger;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;
import xander.paint.Paintables;

/**
 * Abstract robot class for robots built on the Xander 2 framework.
 * 
 * @author Scott Arnold
 */
public abstract class AbstractXanderRobot extends AdvancedRobot {

	public static final String STATS_KEY_TOTAL_BATTLES = "AXR:Total Battles";
	
	private static final Log log = Logger.getLog(AbstractXanderRobot.class);
	private static final ComponentChain componentChain = new ComponentChain();
	private static Gun disabledRobotGun;
	private static RunTimeLogger radarRTLogger;
	private static RunTimeLogger driveRTLogger;
	private static RunTimeLogger gunRTLogger;
	private static RunTimeLogger scannedRobotRTLogger;
	private static int skippedTurns;
	private static CPUUtilizationGraphData cpuUtilizationGraphData;
	
	private RobotEvents robotEvents = Resources.getRobotEvents();
	private RadarController radarController;
	private DriveController driveController;
	private GunController gunController;
	private SnapshotHistory snapshotHistory;
	private Rectangle2D.Double battleFieldSize;
	private ComponentSet componentSet;
	private boolean usingDisabledRobotGun;
	
	/**
	 * Sets robot styling attributes.  
	 * 
	 * Subclasses can override this method to provide robot styling.
	 *  
	 * @param robotStyle     robot style
	 */
	protected void style(RobotStyle robotStyle) {
		// stub method for subclasses
	}
	
	/**
	 * Set Xander framework configuration options.
	 * 
	 * Subclasses can override this method to customize robot configuration.
	 * 
	 * @param configuration   configuration
	 */
	protected void configure(Configuration configuration) {
		// stub method for subclasses
	}
	
	/**
	 * Record battle stats for the opponent.  This happens at the end of
	 * the battle for 1v1 battles only.  Battle stats are intended for 
	 * out-of-game performance analysis only; they are not meant to be used 
	 * by the robot itself.
	 * 
	 * Subclasses can override this method to record stats for the opponent.
	 * 
	 * @param oppStats      battle stats for the opponent
	 * @param totalBattlesAgainstOpponent  total battles for which stats have been recorded against the specific opponent, including this time
	 * 
	 * @returns   whether or not any values were updated
	 */
	protected boolean recordBattleStats(Map<String, String> oppStats, int totalBattlesAgainstOpponent) {
		// stub method for subclasses
		return false;
	}
	
	/**
	 * Record battle stats common for all opponents.  This happens at the end
	 * of the battle for 1v1 battles only.
	 * 
	 * @param commonStats   common stat map
	 * @param totalBattles  total battles for which stats have been recorded, including this time (will always be >= 1)
	 * 
	 * @return    whether or not any values were updated
	 */
	protected boolean recordCommonBattleStats(Map<String, String> commonStats, int totalBattles) {
		// stub method for subclasses
		return false;
	}
	
	/**
	 * This method should call addComponents(...) and/or addDefaultComponents(...) to 
	 * set up the radar(s), drive(s), and gun(s) for the robot.
	 */
	protected abstract void addComponents(ComponentChain componentChain);	
	
	@Override
	public void onCustomEvent(CustomEvent event) {
		super.onCustomEvent(event);
		robotEvents.onCustomEvent(event);
	}

	@Override
	public void onDeath(DeathEvent event) {
		super.onDeath(event);
		robotEvents.onDeath(event);
		Resources.onWinOrDeath();    // Robocode v1.6.1.2 issue work-around
	}

	@Override
	public void onSkippedTurn(SkippedTurnEvent event) {
		super.onSkippedTurn(event);
		skippedTurns++;
		robotEvents.onSkippedTurn(event);
	}

	@Override
	public void onBattleEnded(BattleEndedEvent event) {
		super.onBattleEnded(event);
		robotEvents.onBattleEnded(event);
		if (skippedTurns > 0) {
			FileIO.logSkippedTurns(this, skippedTurns);
		}
		if (snapshotHistory.getOpponentCount() == 1) {
			BattleStats battleStats = FileIO.getBattleStats(Resources.getConfiguration());
			if (battleStats != null && snapshotHistory.getLastOpponentScanned() != null) {
				String opponentName = snapshotHistory.getLastOpponentScanned().getName();
				Map<String, String> oppStats = battleStats.getStatsForRobot(opponentName, true);
				Map<String, String> commonStats = battleStats.getCommonStats();
				
				// update total battles statistic
				int totalBattlesAgainstOpp = 1;
				String totalBattlesAgainstOppStored = oppStats.get(STATS_KEY_TOTAL_BATTLES);
				if (totalBattlesAgainstOppStored != null) {
					try {
						totalBattlesAgainstOpp += Integer.parseInt(totalBattlesAgainstOppStored);
					} catch (Exception e) { }
				}
				oppStats.put(STATS_KEY_TOTAL_BATTLES, String.valueOf(totalBattlesAgainstOpp));
				int totalBattles = 1;
				String totalBattlesStored = commonStats.get(STATS_KEY_TOTAL_BATTLES);
				if (totalBattlesStored != null) {
					try {
						totalBattles += Integer.parseInt(totalBattlesStored);
					} catch (Exception e) { }
				}
				commonStats.put(STATS_KEY_TOTAL_BATTLES, String.valueOf(totalBattles));
				
				// call methods for robot to update it's own statistics
				boolean oppStatsUpdated = recordBattleStats(oppStats, totalBattlesAgainstOpp);
				boolean commonStatsUpdated = recordCommonBattleStats(commonStats, totalBattles);
				
				// add statistics for run times, if configuration requests
				if (Resources.getConfiguration().isSaveComponentRunTimesCommon()) {
					commonStatsUpdated = true;
					driveRTLogger.saveTo(commonStats, totalBattles);
					gunRTLogger.saveTo(commonStats, totalBattles);
					radarRTLogger.saveTo(commonStats, totalBattles);
				}
				if (Resources.getConfiguration().isSaveComponentRunTimesIndividual()) {
					driveRTLogger.saveTo(oppStats, totalBattlesAgainstOpp);
					gunRTLogger.saveTo(oppStats, totalBattlesAgainstOpp);
					radarRTLogger.saveTo(oppStats, totalBattlesAgainstOpp);
				}
				
				// save the battle statistics
				if (oppStatsUpdated || commonStatsUpdated) {
					log.info("Updating battle statistics...");
					FileIO.saveBattleStats();
					log.info("Battle statistics updated.");
				}
			}
		}
	}

	@Override
	public void onBulletHit(BulletHitEvent event) {
		super.onBulletHit(event);
		robotEvents.onBulletHit(event);
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		super.onBulletHitBullet(event);
		robotEvents.onBulletHitBullet(event);
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		super.onBulletMissed(event);
		robotEvents.onBulletMissed(event);
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		super.onHitByBullet(event);
		robotEvents.onHitByBullet(event);
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		super.onHitRobot(event);
		robotEvents.onHitRobot(event);
	}

	@Override
	public void onHitWall(HitWallEvent event) {
		super.onHitWall(event);
		robotEvents.onHitWall(event);
	}

	@Override
	public void onKeyPressed(KeyEvent e) {
		super.onKeyPressed(e);
		robotEvents.onKeyPressed(e);
	}

	@Override
	public void onKeyReleased(KeyEvent e) {
		super.onKeyReleased(e);
		robotEvents.onKeyReleased(e);
	}

	@Override
	public void onKeyTyped(KeyEvent e) {
		super.onKeyTyped(e);
		robotEvents.onKeyTyped(e);
	}

	@Override
	public void onMouseClicked(MouseEvent e) {
		super.onMouseClicked(e);
		robotEvents.onMouseClicked(e);
	}

	@Override
	public void onMouseDragged(MouseEvent e) {
		super.onMouseDragged(e);
		robotEvents.onMouseDragged(e);
	}

	@Override
	public void onMouseEntered(MouseEvent e) {
		super.onMouseEntered(e);
		robotEvents.onMouseEntered(e);
	}

	@Override
	public void onMouseExited(MouseEvent e) {
		super.onMouseExited(e);
		robotEvents.onMouseExited(e);
	}

	@Override
	public void onMouseMoved(MouseEvent e) {
		super.onMouseMoved(e);
		robotEvents.onMouseMoved(e);
	}

	@Override
	public void onMousePressed(MouseEvent e) {
		super.onMousePressed(e);
		robotEvents.onMousePressed(e);
	}

	@Override
	public void onMouseReleased(MouseEvent e) {
		super.onMouseReleased(e);
		robotEvents.onMouseReleased(e);
	}

	@Override
	public void onMouseWheelMoved(MouseWheelEvent e) {
		super.onMouseWheelMoved(e);
		robotEvents.onMouseWheelMoved(e);
	}

	@Override
	public void onPaint(Graphics2D g) {
		super.onPaint(g);
		robotEvents.onPaint(g);
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		super.onRobotDeath(event);
		robotEvents.onRobotDeath(event);
	}

	@Override
	public void onRoundEnded(RoundEndedEvent event) {
		super.onRoundEnded(event);
		robotEvents.onRoundEnded(event);
		Resources.endRound();
		if (skippedTurns > 0) {
			log.warn("Skipped turns so far: " + skippedTurns);
		}
		RunTimeLogger.logAll();
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		scannedRobotRTLogger.start();
		super.onScannedRobot(event);
		robotEvents.onScannedRobot(event);
		scannedRobotRTLogger.stop();
	}

	@Override
	public void onStatus(StatusEvent e) {
		super.onStatus(e);
		robotEvents.onStatus(e);
	}

	@Override
	public void onWin(WinEvent event) {
		super.onWin(event);
		robotEvents.onWin(event);
		Resources.onWinOrDeath();  // Robocode v1.6.1.2 issue work-around
	}

	public Rectangle2D.Double getBattleFieldSize() {
		return battleFieldSize;
	}
	
	public int getSkippedTurns() {
		return skippedTurns;
	}
	
	public String getActiveRadarName() {
		if (componentSet == null) {
			return "";
		} else if (componentSet.radar == null) {
			return "None";
		} else {
			return componentSet.radar.getName();
		}
	}
	
	public String getActiveGunName() {
		if (usingDisabledRobotGun) {
			return disabledRobotGun.getName();
		} else if (componentSet == null) {
			return "";
		} else if (componentSet.gun == null) {
			return "None";
		} else {
			return componentSet.gun.getName();
		}
	}
	
	public String getActiveDriveName() {
		if (componentSet == null) {
			return "";
		} else if (componentSet.drive == null) {
			return "None";
		} else {
			return componentSet.drive.getName();
		}
	}
	
	// one-time first round initialization
	private void initialize() {
		log.info("Configuring robot...");
		Resources.getRobotProxy().setRobot(this);  // makes RobotProxy usable within the configure method call 
		Configuration configuration = Resources.getConfiguration();
		style(Resources.getRobotStyle());
		configure(configuration);
		Resources.initialize(this, componentChain);
		if (configuration.isAutoFireOnDisabledOpponents()) {
			disabledRobotGun = new DisabledRobotGun(configuration);
		}
		log.info("Setting up component chain...");
		addComponents(componentChain);
		radarRTLogger = RunTimeLogger.getLoggerFor("Radar");
		driveRTLogger = RunTimeLogger.getLoggerFor("Drive");
		gunRTLogger = RunTimeLogger.getLoggerFor("Gun");
		scannedRobotRTLogger = RunTimeLogger.getLoggerFor("ScannedRobotEvent");
		if (!configuration.isLogComponentRunTimes() 
				&& !configuration.isSaveComponentRunTimesCommon()
				&& !configuration.isSaveComponentRunTimesIndividual()) {
			radarRTLogger.setActive(false);
			driveRTLogger.setActive(false);
			gunRTLogger.setActive(false);
		}
		if (!configuration.isLogScannedRobotEventTime()) {
			scannedRobotRTLogger.setActive(false);
		}
		int dataPoints = configuration.getCPUUtilizationDataPoints();
		cpuUtilizationGraphData = new CPUUtilizationGraphData(dataPoints);
		if (configuration.getCpuConstantMS() != null) {
			cpuUtilizationGraphData.setCPUConstant(configuration.getCpuConstantMS().doubleValue());
		}
		Paintables.addPaintable(cpuUtilizationGraphData);
		log.info("Loading previous battle statistics...");
		FileIO.loadBattleStats(configuration);
		log.info("Ready for battle!");
	}
	
	public void run() {
		try {
			setAdjustGunForRobotTurn(true);
			setAdjustRadarForGunTurn(true);
			setAdjustRadarForRobotTurn(true);
			this.battleFieldSize = new Rectangle2D.Double(0, 0, 
					getBattleFieldWidth(), getBattleFieldHeight());
			if (getRoundNum() == 0) {
				initialize();
			}
			Resources.beginRound(this);
			Resources.getRobotStyle().apply(this);
			this.snapshotHistory = Resources.getSnapshotHistory();
			this.radarController = Resources.getRadarController();
			this.driveController = Resources.getDriveController();
			this.gunController = Resources.getGunController();
			this.componentSet = new ComponentSet();
			this.usingDisabledRobotGun = false;
			robotEvents.onRoundBegin();
			while (true) {
				if (cpuUtilizationGraphData != null) {
					cpuUtilizationGraphData.onTurnBegin();
				}
				robotEvents.onTurnBegin();
				componentChain.loadComponents(componentSet);
				Snapshot targetRobot = null;
				if (componentSet.radar != null) {
					radarRTLogger.start();
					targetRobot = componentSet.radar.search(radarController);
					radarRTLogger.stop();
				}
				if (targetRobot == null) {
					if (componentSet.drive != null) {
						driveRTLogger.start();
						componentSet.drive.drive(driveController);
						driveRTLogger.stop();
					}
				} else {
					if (componentSet.drive != null) {
						driveRTLogger.start();
						componentSet.drive.driveTo(targetRobot, driveController);
						driveRTLogger.stop();
					}
					if (componentSet.gun != null) {
						Snapshot myself = snapshotHistory.getMySnapshot(targetRobot.getTime(), true);
						if (targetRobot.getEnergy() <= 0 && disabledRobotGun != null && disabledRobotGun.canFireAt(targetRobot)) {
							usingDisabledRobotGun = true;
							disabledRobotGun.fireAt(targetRobot, myself, gunController);
						} else {
							usingDisabledRobotGun = false;
							if (componentSet.gun.canFireAt(targetRobot)) {
								gunRTLogger.start();
								componentSet.gun.fireAt(targetRobot, myself, gunController);
								gunRTLogger.stop();
							} else {
								// just point the gun in the opponent's general direction
								double oppHeading = RCMath.getRobocodeAngle(myself.getLocation(), targetRobot.getLocation());
								double turn = RCMath.getTurnAngle(getGunHeading(), oppHeading);
								gunController.setTurnGunRightDegrees(turn);
							}
						}						
					}
				}
				robotEvents.onTurnEnd();
				if (cpuUtilizationGraphData != null) {
					cpuUtilizationGraphData.onTurnEnd();
				}
				execute();
			}
		} catch (Exception e) {
			Resources.endRound();
			FileIO.logException(this, e);
		}
	}
}
