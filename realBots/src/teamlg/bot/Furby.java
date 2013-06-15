package teamlg.bot;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Set;

import robocode.WinEvent;
import teamlg.drive.antiGrav.AntiGravityDrive;
import teamlg.radar.SpinningRadar;
import xander.cat.group.idealPosition.IdealPositionDrive;
import xander.cat.group.mirror.AntiMirrorDrive;

import xander.cat.group.mirror.MirrorFactory;
import xander.cat.group.ram.RamFactory;
import xander.cat.group.rem.REMFactory;
import xander.cat.group.shield.BulletShieldingAutoFireCondition;
import xander.cat.group.shield.BulletShieldingController;
import xander.cat.group.shield.BulletShieldingFactory;
import xander.cat.gun.power.SteppedHitRatioPowerSelector;
import xander.cat.radar.BasicRadar;
import xander.cat.scenario.CircularDriveScenario;
import xander.cat.scenario.NoOpponentWavesScenario;
import xander.core.AbstractXanderRobot;
import xander.core.ComponentChain;
import xander.core.Configuration;
import xander.core.Resources;
import xander.core.RobotStyle;
import xander.core.Scenario;
import xander.core.drive.DriveBoundsFactory;
import xander.core.gun.XanderGun;
import xander.core.gun.power.PowerSelector;
import xander.core.gun.targeter.CircularTargeter;
import xander.core.gun.targeter.LinearTargeter;
import xander.core.io.BattleStats;
import xander.core.math.RCMath;
import xander.core.track.DriveStats;
import xander.core.track.GunStats;

/**
 * Robocode advanced robot and flagship of the Xander robot fleet.
 * XanderCat is a multi-mode robot built on the Xander 2.0 framework.
 * 
 * @author Scott Arnold
 */
public class Furby extends AbstractXanderRobot {

	private static CircularDriveScenario circularDriverScenario;
	private static SteppedHitRatioPowerSelector steppedPowerSelector;
	private static PowerSelector mainPowerSelector;
	private static Path2D.Double driveBounds;
	private static boolean[] wins;
	private static BulletShieldingController bsc;
	
	@Override
	protected void style(RobotStyle robotStyle) {
		robotStyle.setColors(Color.PINK, Color.BLACK, Color.PINK);  // radar color changed to red at Alexander's request :)
		robotStyle.setBulletColor(Color.PINK);
		robotStyle.setScanArcColor(Color.GREEN);
	}

	@Override
	protected void configure(Configuration configuration) {
		configuration.setAutoFireOnDisabledOpponents(true);  
		configuration.addAutoFireCondition(new BulletShieldingAutoFireCondition());
		configuration.setLogComponentRunTimes(true);
		configuration.setLogDriveTimes(true);
		configuration.setSnapshotHistorySize(120);
		Rectangle2D.Double dbRec = new Rectangle2D.Double(60, 10, getBattleFieldSize().width-120, getBattleFieldSize().height-20); // being lazy here and assuming 800 x 600
		driveBounds = DriveBoundsFactory.getSmoothedRectangleBounds(dbRec, -0.105, 0.4);
		Path2D.Double oppDriveBounds = DriveBoundsFactory.getRectangularBounds(getBattleFieldSize());
		configuration.setUsePreciseMEAForMyWaves(true, oppDriveBounds);
		PowerSelector powerSelector = configuration.getDisabledOpponentPowerSelector();
		powerSelector = REMFactory.getX5PowerSelector(powerSelector);
		configuration.setDisabledOpponentPowerSelector(powerSelector);
		steppedPowerSelector = new SteppedHitRatioPowerSelector(
				new double[] {0.17}, new double[] {1.85, 1.99});
		steppedPowerSelector.setPowerDrop(0.135, 15, 0);
		mainPowerSelector = REMFactory.getX5PowerSelector(steppedPowerSelector);
		wins = new boolean[getNumRounds()]; 
	}
	
	@Override
	protected boolean recordBattleStats(Map<String, String> oppStats, int numBattles) {
		GunStats gunStats = Resources.getGunStats();
		DriveStats driveStats = Resources.getDriveStats();
		double oHR = gunStats.getOverallOpponentHitRatio();
		double mHR = gunStats.getOverallHitRatio();	
		double cda = circularDriverScenario.getAppliesPercentage();
		double dpc = steppedPowerSelector.getDropPowerCount();
		BattleStats.updateAveragedStatValue(oppStats, "OppHitRatio", oHR, 3, numBattles);
		BattleStats.updateAveragedStatValue(oppStats, "MyHitRatio", mHR, 3, numBattles);
		BattleStats.updateAveragedStatValue(oppStats, "CircularApplies", cda, 3, numBattles);
		BattleStats.updateAveragedStatValue(oppStats, "DropPowerCount", dpc, 1, numBattles);		
		for (int i=0; i<2; i++) {
			int bf = gunStats.getActionedBulletsFired("GF Gun " + i);
			// for the guns, we are only showing numbers for the last battle instead of an average
			oppStats.put("GF"+i, String.valueOf(bf));
		}
		Set<String> driveNames = driveStats.getDriveNames();
		for (String driveName : driveNames) {
			double dup = driveStats.getDriveUsagePercent(driveName);
			BattleStats.updateAveragedStatValue(oppStats, "D:"+driveName, dup, 3, numBattles);
		}
//		String[] loggers = new String[] {"Config", "Construct", "LoadStats", "Radar", "Drive", "Gun"};
//		for (String loggerName : loggers) {
//			RunTimeLogger rtl = RunTimeLogger.getLoggerFor(loggerName);
//			rtl.saveTo(statsmap, totalbattles);
//		}
		return true;
	}

	@Override
	protected boolean recordCommonBattleStats(Map<String, String> commonStats, int totalBattles) {
		for (int i=0; i<wins.length; i++) {
			String roundKey = (i < 10)? "R 0" + i + " Wins" : "R " + i + " Wins";
			int roundWins = RCMath.parseInt(commonStats.get(roundKey), 0);
			if (wins[i]) {
				roundWins++;
			}
			commonStats.put(roundKey, String.valueOf(roundWins));
		}
		double wallHits = Resources.getDriveStats().getWallHits();
		BattleStats.updateAveragedStatValue(commonStats, "Avg Wall Hits", wallHits, 2, totalBattles);
		double avgWallHitDmg = Resources.getDriveStats().getAverageWallHitDamage();
		BattleStats.updateAveragedStatValue(commonStats, "Avg Wall Hit Dmg", avgWallHitDmg, 2, totalBattles);
		double skippedTurns = getSkippedTurns();
		BattleStats.updateAveragedStatValue(commonStats, "Skipped Turns", skippedTurns, 2, totalBattles);
		String fc = bsc.getFireCheckString();
		if (fc != null) {
			commonStats.put("Fire Check", fc);
		}
		return true;
	}
        

	@Override
	protected void addComponents(ComponentChain chain) {
		
//		FactorArrayPainter wsDriveFAPainter = new FactorArrayPainter("WS Drive FA", "WS Drive", false);
//		FactorArrayPainter tdDriveFAPainter = new FactorArrayPainter("TD Drive FA", "TD Drive", false);
//		tdDriveFAPainter.setPegColor(Color.PINK);
//		XanderPaintManager.getInstance(this).enable(getBattleFieldHeight(), wsDriveFAPainter, tdDriveFAPainter);
//		PaintSource<Path2D.Double> dbSource = new PaintSource<Path2D.Double>("Drive Bounds", driveBounds);
//		XanderPaintManager.getInstance(this).enable(getBattleFieldHeight(), new BulletShieldingPainter());
		
		// RADAR
		// DRIVES
		
		/*RamFactory.addRamComponents(chain, 2d, 0.1d, 30);
		
		RamFactory.addAntiRamComponents(chain);
		
		bsc = BulletShieldingFactory.addBulletShieldingComponents(chain, 0, 0d,  true, true);
		
		MirrorFactory.addAntiMirrorComponents(chain, 20, 4, 25);
				
		Scenario ipScenario = new NoOpponentWavesScenario();
		
		
		
		// GUNS  
		
		// a special scenario just for our circular drivers out there!
		XanderGun circularGun = new XanderGun(new CircularTargeter(), mainPowerSelector);
		circularDriverScenario = new CircularDriveScenario(circularGun);
		chain.addComponents(circularDriverScenario, circularGun);*/
		
		
		// default components will be 
                // A Anti Gravity Drive
                // - a linear gun
                
                SpinningRadar aDefaultRadar = new SpinningRadar(2*Math.PI);
                XanderGun aDefaultGun = new XanderGun(new LinearTargeter(), mainPowerSelector);
                AntiGravityDrive aDefaultDrive = new AntiGravityDrive( getBattleFieldWidth(), getBattleFieldHeight() );
                
                chain.addDefaultComponents( aDefaultRadar,aDefaultGun, aDefaultDrive);
	}

	@Override
	public void onWin(WinEvent event) {
		wins[getRoundNum()] = true;
		super.onWin(event);
	}		
}