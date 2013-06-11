package xander.cat.group.mirror;

import java.awt.geom.Rectangle2D;

import robocode.ScannedRobotEvent;

import xander.core.Resources;
import xander.core.RobotEvents;
import xander.core.event.RoundBeginListener;
import xander.core.event.ScannedRobotListener;
import xander.core.log.Log;
import xander.core.log.Logger;
import xander.core.math.RCPhysics;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;
import xander.paint.Paintable;

/**
 * Detects opponents that attempt to perfectly mirror our driving pattern.
 * Presently only guaranteed to work properly for 1v1.
 * 
 * @author Scott Arnold
 */
public class MirrorDetector implements RoundBeginListener, ScannedRobotListener, Paintable {

	private static final Log log = Logger.getLog(MirrorDetector.class);
	private static final double BOX_SIZE = RCPhysics.ROBOT_WIDTH + 10;
	
	private boolean[][] mirrorHits;
	private Rectangle2D.Double mirrorBounds;
	private Rectangle2D.Double battlefieldBounds;
	private SnapshotHistory snapshotHistory;
	private int scanIndex;
	private double mirrorThreshold = 0.75;
	private int scannedTicks;
	private float maxHitPercent;
	private int maxHitPercentTicksAgo;
	private long lastMirrorDetectionTick;
	private long mirrorDetectionHits;
	
	public MirrorDetector(int scanDepth, int scannedTicks) {
		this.snapshotHistory = Resources.getSnapshotHistory();
		if (scannedTicks > snapshotHistory.getHistorySize()) {
			scannedTicks = snapshotHistory.getHistorySize();
			log.warn("Robot histories are not long enough for requested scan ticks; reducing scan ticks to " + scannedTicks);
		}
		this.scannedTicks = scannedTicks;
		this.mirrorHits = new boolean[scannedTicks][scanDepth];
		this.battlefieldBounds = Resources.getRobotProxy().getBattleFieldSize();
		this.mirrorBounds = new Rectangle2D.Double(0, 0, 1, 1);  // initialize it to anything
		RobotEvents robotEvents = Resources.getRobotEvents();
		robotEvents.addScannedRobotListener(this);
		robotEvents.addRoundBeginListener(this);
	}

	@Override
	public void onRoundBegin() {
		// it will take the opponent a short while to get into mirror position, so reset mirror hits on startup
		for (int i=0; i<mirrorHits.length; i++) {
			for (int j=0; j<mirrorHits[i].length; j++) {
				mirrorHits[i][j] = false;
			}
		}
		this.mirrorDetectionHits = 0;
		this.lastMirrorDetectionTick = -1;
	}

	public void onScannedRobot(ScannedRobotEvent event) {
		// each scan we check for new histories
		// we don't worry about if it's the same robot for melee as it won't really cause any negative effects
		Snapshot opp = snapshotHistory.getSnapshot(event.getName(), event.getTime(), false);
		if (opp != null) {
			for (int i=0; i<scannedTicks; i++) {
				Snapshot me = snapshotHistory.getMySnapshot(Math.max(0, event.getTime()-i), false);
				if (me != null) {
					double x = battlefieldBounds.getMaxX()-me.getX()-BOX_SIZE/2d;
					double y = battlefieldBounds.getMaxY()-me.getY()-BOX_SIZE/2d;
					mirrorBounds.setRect(x, y, BOX_SIZE, BOX_SIZE);
					mirrorHits[i][scanIndex] = mirrorBounds.contains(opp.getLocation());
				}
			}
		}
		// update scan index
		scanIndex++;
		if (scanIndex == mirrorHits[0].length) {
			scanIndex = 0;
		}
	}
	
	public boolean isMirrorDetected() {
		boolean mirrorDetected = false;
		this.maxHitPercent = 0;
		for (int i=0; i<scannedTicks; i++) {
			int hitCount = 0;
			for (boolean mirrorHit : mirrorHits[i]) {
				if (mirrorHit) {
					hitCount++;
				}
			}
			float hitPercent = (float) hitCount / (float) mirrorHits[0].length;
			if (hitPercent > mirrorThreshold) {
				if (hitPercent > this.maxHitPercent) {
					mirrorDetected = true;	
					this.maxHitPercent = hitPercent;
					this.maxHitPercentTicksAgo = i;
				}
			}
		}
		if (mirrorDetected) {
			this.mirrorDetectionHits++;
			this.lastMirrorDetectionTick = Resources.getRobotProxy().getTime();
		}
		return mirrorDetected;
	}
	
	public long getLastMirrorDetectionTime() {
		return lastMirrorDetectionTick;
	}
	
	public int getMirrorDetectedTicksAgo() {
		return this.maxHitPercentTicksAgo;
	}

	public long getMirrorDetectionHitsThisRound() {
		return mirrorDetectionHits;
	}
	
	public double getMirrorDetectionPercentThisRound() {
		long time = Resources.getRobotProxy().getTime();
		if (time <= 0) {
			return 0;
		} else {
			return (double)mirrorDetectionHits / (double)time;
		}
	}
	
	@Override
	public String getPainterName() {
		return null;
	}
}
