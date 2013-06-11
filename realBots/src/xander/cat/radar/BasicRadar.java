package xander.cat.radar;

import xander.core.Resources;
import xander.core.log.Log;
import xander.core.log.Logger;
import xander.core.math.RCMath;
import xander.core.radar.Radar;
import xander.core.radar.RadarController;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;

/**
 * BasicRadar locks on to the latest scanned robot.
 * 
 * @author Scott Arnold
 */
public class BasicRadar implements Radar {

	private static final Log log = Logger.getLog(BasicRadar.class);  
	
	private SnapshotHistory snapshotHistory;
	private double searchSweep;
	private double focusSweep;
	private long focusTargetTime;
	
	public BasicRadar(double searchSweepDegrees, double focusSweepDegrees) {
		this.searchSweep = searchSweepDegrees;
		this.focusSweep = focusSweepDegrees;
		this.snapshotHistory = Resources.getSnapshotHistory();
	}
	
	public String getName() {
		return "Basic Radar";
	}
	
	@Override
	public void onRoundBegin() {
		// no action required
	}

	public Snapshot search(RadarController radarController) {
		double degrees = searchSweep;
		Snapshot opp = snapshotHistory.getLastOpponentScanned();
		if (opp != null && opp.getTime() != focusTargetTime) {
			this.focusTargetTime = opp.getTime();
			Snapshot me = snapshotHistory.getMySnapshot(opp.getTime(), false);
			if (me == null) {
				log.error("Unable to retrieve my history for time " + opp.getTime() + "; unable to focus on target!");
			} else {
				// for better tracking of target, radar is aimed for one time unit in advance
				double[] nextOppXY = opp.getNextXY();
				double[] nextMyXY = me.getNextXY();
				double targetRadarHeading = RCMath.getRobocodeAngle(
						nextOppXY[0] - nextMyXY[0], nextOppXY[1] - nextMyXY[1]);
				degrees = RCMath.getTurnAngle(
						radarController.getRadarHeadingDegrees(),
						targetRadarHeading);
				if (degrees < 0) {
					degrees -= (focusSweep/2d);
				} else {
					degrees += (focusSweep/2d);
				}				
			}
		} else {
			opp = null;
		}
		radarController.setTurnRadarRightDegrees(degrees);
		return opp;
	}
}
