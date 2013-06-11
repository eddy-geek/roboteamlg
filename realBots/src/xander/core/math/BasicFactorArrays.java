package xander.core.math;

import java.awt.geom.Point2D;

import xander.core.math.RCMath;
import xander.core.math.RCPhysics;
import xander.core.track.Wave;

public class BasicFactorArrays {
	
	public static double getFactorAngle(Wave bulletWave, double defenderX, double defenderY) {
		double zeroAngle = RCMath.getRobocodeAngle(bulletWave.getOrigin(), bulletWave.getInitialDefenderSnapshot().getLocation());
		double currentAngle = RCMath.getRobocodeAngle(bulletWave.getOriginX(), bulletWave.getOriginY(), defenderX, defenderY);
		return RCMath.getTurnAngle(zeroAngle, currentAngle);		
	}
		
	public static double getFactorAngle(Wave bulletWave, Point2D.Double currentDefenderLocation) {
		double zeroAngle = RCMath.getRobocodeAngle(bulletWave.getOrigin(), bulletWave.getInitialDefenderSnapshot().getLocation());
		double currentAngle = RCMath.getRobocodeAngle(bulletWave.getOrigin(), currentDefenderLocation);
		return RCMath.getTurnAngle(zeroAngle, currentAngle);
	}
	
	public static double getFactorAngle(Wave bulletWave, double robocodeAngleDegrees) {
		double zeroAngle = RCMath.getRobocodeAngle(bulletWave.getOrigin(), bulletWave.getInitialDefenderSnapshot().getLocation());
		return RCMath.getTurnAngle(zeroAngle, robocodeAngleDegrees);
	}
	
	public static int getFactorIndex(double preciseFactorIndex) {
		return (int) Math.round(Math.floor(preciseFactorIndex));
	}
	
	public static double getEstimatedFactorIndexRobotWidth(int numFactors, double distance, double bulletVelocity) {
		double circ = 2*Math.PI*distance;
		double bodyWidthDegrees = 360d*RCPhysics.ROBOT_WIDTH/circ;
		double mea = RCMath.getMaximumEscapeAngle(bulletVelocity);
		double rbfactor = bodyWidthDegrees / mea;
		return RCMath.limit(numFactors/2d + rbfactor * numFactors/2d, 0, numFactors-0.001) - (double)numFactors/2d;
	}
	
	public static int getMostWeightedFactorIndex(double[] array, int beginFactorIndex, int endFactorIndex) {
		// find the best division in the range
		int bestFactorIndex = -1;
		double bestFactorValue = Double.NEGATIVE_INFINITY;
		int tieCount = 0;
		boolean runningTie = false;
		for (int i=beginFactorIndex; i<=endFactorIndex; i++) {
			if (array[i] > bestFactorValue) {
				bestFactorValue = array[i];
				bestFactorIndex = i;
				tieCount = 0;
				runningTie = true;
			} else if (runningTie && array[i] == bestFactorValue) {
				tieCount++;
			}  else {
				runningTie = false;
			}
		}
		return bestFactorIndex + (tieCount / 2);
	}
	
	public static double getDistanceDangerMultiplier(double distance, double flatDistance, double criticalDistance) {	
		return (distance < flatDistance)? Math.pow((flatDistance-criticalDistance)/(Math.max(distance,criticalDistance+0.1)-criticalDistance),2) : 1;
	}
}
