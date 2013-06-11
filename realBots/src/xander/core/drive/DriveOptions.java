package xander.core.drive;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.track.Wave;
import xander.core.math.BasicFactorArrays;
import xander.core.math.RelativeAngleRange;
import xander.paint.Paintable;
import xander.paint.Paintables;

/**
 * Tool class for predicting where robot can get to before a wave hits.  This tool
 * will test driving at n test angles spiraling around in a circle.
 * 
 * @author Scott Arnold
 */
public class DriveOptions implements Paintable {
	
	public static final int IDX_DRIVE_HEADING = 0;
	public static final int IDX_FACTOR_ANGLE = 1;
	public static final int IDX_DRIVE_STATE_X = 2;
	public static final int IDX_DRIVE_STATE_Y = 3;
	
	private int dta;     // number of test angles in each direction (clockwise and counter-clockwise)
	private double ti;   // test angle increment (angle between test angles)
	private double[][] cwTestAngles;
	private double[][] ccwTestAngles;
	private int greatestCWIndex;
	private int greatestCCWIndex;
	private RelativeAngleRange mea;
	private DirectDrivePredictor predictor;
	private String painterName;
	
	public DriveOptions(int directionalTestAngles) {
		this.dta = directionalTestAngles;
		this.ti = 180d / this.dta;
		this.cwTestAngles = new double[this.dta][4];
		this.ccwTestAngles = new double[this.dta][4];		
	}
	
	public DriveOptions(String painterName, int directionalTestAngles) {
		this(directionalTestAngles);
		this.painterName = painterName;
		Paintables.addPaintable(this);
	}
	
	public DriveOptions(int directionalTestAngles, Rectangle2D.Double battlefieldBounds, Path2D.Double driveBounds) {
		this(directionalTestAngles);
		this.predictor = new DirectDrivePredictor(battlefieldBounds, driveBounds);
	}
	
	public DriveOptions(String painterName, int directionalTestAngles, Rectangle2D.Double battlefieldBounds, Path2D.Double driveBounds) {
		this(directionalTestAngles, battlefieldBounds, driveBounds);
		this.painterName = painterName;
		Paintables.addPaintable(this);
	}
	
	@Override
	public String getPainterName() {
		return painterName;
	}

	public void computeDriveOptions(Wave wave, DriveState defenderDriveState, long fromTime, DirectDrivePredictor predictor) {
		this.predictor = predictor;
		computeDriveOptions(wave, defenderDriveState, fromTime);
	}
	
	public void computeDriveOptions(Wave wave, DriveState defenderDriveState, long fromTime) {
		
		// setup which test angles are clockwise and which are counter-clockwise
		double inHeading = RCMath.getRobocodeAngle(defenderDriveState.getPosition(), wave.getOrigin());
		for (int i=0; i<this.dta; i++) {
			ccwTestAngles[i][0] = RCMath.normalizeDegrees(inHeading+(i+0.5d)*this.ti);
			cwTestAngles[i][0] = RCMath.normalizeDegrees(inHeading+180+(i+0.5d)*this.ti);
		}
		
		// determine greatest reachable factors and positions for all test angles, note greatest
		greatestCWIndex = -1;
		greatestCCWIndex = -1;
		for (int i=0; i<cwTestAngles.length; i++) {
			DriveState testDriveState = predictor.predictDriveStateUntilWaveHits(wave, defenderDriveState, cwTestAngles[i][0], RCPhysics.MAX_SPEED, fromTime);
			cwTestAngles[i][1] = BasicFactorArrays.getFactorAngle(wave, testDriveState.getPosition());
			cwTestAngles[i][2] = testDriveState.getX();
			cwTestAngles[i][3] = testDriveState.getY();
			if (greatestCWIndex == -1 || cwTestAngles[i][1] > cwTestAngles[greatestCWIndex][1]) {
				greatestCWIndex = i;
			}
			testDriveState = predictor.predictDriveStateUntilWaveHits(wave, defenderDriveState, ccwTestAngles[i][0], RCPhysics.MAX_SPEED, fromTime);
			ccwTestAngles[i][1] = BasicFactorArrays.getFactorAngle(wave, testDriveState.getPosition());
			ccwTestAngles[i][2] = testDriveState.getX();
			ccwTestAngles[i][3] = testDriveState.getY();
			if (greatestCCWIndex == -1 || ccwTestAngles[i][1] < ccwTestAngles[greatestCCWIndex][1]) {
				greatestCCWIndex = i;
			}
		}
		this.mea = new RelativeAngleRange(ccwTestAngles[greatestCCWIndex][1], cwTestAngles[greatestCWIndex][1], "DriveOptions");
	}
	
	public double[][] getClockwiseTestAngleValues() {
		return cwTestAngles;
	}
	
	public double[][] getCounterClockwiseTestAngleValues() {
		return ccwTestAngles;
	}
	
	public double[] getClockwiseMEAValues() {
		return cwTestAngles[greatestCWIndex];
	}
	
	public double[] getCounterClockwiseMEAValues() {
		return ccwTestAngles[greatestCCWIndex];
	}
	
	public int getMEAClockwiseIndex() {
		return greatestCWIndex;
	}
	
	public int getMEACounterClockwiseIndex() {
		return greatestCCWIndex;
	}
	
	public RelativeAngleRange getMEA() {
		return mea; 
	}
	
	public double getDriveHeading(boolean clockwise, int idx) {
		return clockwise? cwTestAngles[idx][0] : ccwTestAngles[idx][0];
	}
	
	public double getFactorAngle(boolean clockwise, int idx) {
		return clockwise? cwTestAngles[idx][1] : ccwTestAngles[idx][1];
	}
	
	public double getDriveStateX(boolean clockwise, int idx) {
		return clockwise? cwTestAngles[idx][2] : ccwTestAngles[idx][2];
	}
	
	public double getDriveStateY(boolean clockwise, int idx) {
		return clockwise? cwTestAngles[idx][3] : ccwTestAngles[idx][3];
	}	
}
