package xander.core;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

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
import xander.core.event.CollisionListener;
import xander.core.event.BulletHitListener;
import xander.core.event.PaintListener;
import xander.core.event.RoundBeginListener;
import xander.core.event.RoundListener;
import xander.core.event.ScannedRobotListener;
import xander.core.event.SkippedTurnListener;
import xander.core.event.SurvivalListener;
import xander.core.event.TurnListener;

/**
 * Single control point for all common events and listeners.
 * 
 * @author Scott Arnold
 */
public class RobotEvents {
	
	private List<CollisionListener> collisionListeners = new ArrayList<CollisionListener>();
	private List<TurnListener> turnListeners = new ArrayList<TurnListener>();
	private List<ScannedRobotListener> scannedRobotListeners = new ArrayList<ScannedRobotListener>();
	private List<BulletHitListener> bulletHitListeners = new ArrayList<BulletHitListener>();
	private List<RoundListener> roundListeners = new ArrayList<RoundListener>();
	private List<RoundBeginListener> roundBeginListeners = new ArrayList<RoundBeginListener>();
	private List<SurvivalListener> survivalListeners = new ArrayList<SurvivalListener>();
	private List<SkippedTurnListener> skippedTurnListeners = new ArrayList<SkippedTurnListener>();
	private List<PaintListener> painters = new ArrayList<PaintListener>();
	private List<MouseListener> mouseListeners = new ArrayList<MouseListener>();
	private List<MouseMotionListener> mouseMotionListeners = new ArrayList<MouseMotionListener>();
	
	public void addCollisionListener(CollisionListener listener) {
		this.collisionListeners.add(listener);
	}
	
	public void addTurnListener(TurnListener listener) {
		this.turnListeners.add(listener);
	}
	
	public void addScannedRobotListener(ScannedRobotListener listener) {
		this.scannedRobotListeners.add(listener);
	}
	
	public void addBulletHitListener(BulletHitListener listener) {
		this.bulletHitListeners.add(listener);
	}
	
	public void addRoundListener(RoundListener listener) {
		this.roundListeners.add(listener);
	}
	
	public void addRoundBeginListener(RoundBeginListener listener) {
		this.roundBeginListeners.add(listener);
	}
	
	public void addSurvivalListener(SurvivalListener listener) {
		this.survivalListeners.add(listener);
	}
	
	public void addSkippedTurnListener(SkippedTurnListener listener) {
		this.skippedTurnListeners.add(listener);
	}
	
	public void addPainter(PaintListener painter) {
		this.painters.add(painter);
	}
	
	public void addMouseListener(MouseListener mouseListener) {
		this.mouseListeners.add(mouseListener);
	}
	
	public void addMouseMotionListener(MouseMotionListener mouseMotionListener) {
		this.mouseMotionListeners.add(mouseMotionListener);
	}
	
	void onCustomEvent(CustomEvent event) {
	}

	void onDeath(DeathEvent event) {
		for (SurvivalListener listener : survivalListeners) {
			listener.onDeath(event);
		}
	}

	void onSkippedTurn(SkippedTurnEvent event) {
		for (SkippedTurnListener listener : skippedTurnListeners) {
			listener.onSkippedTurn(event);
		}
	}

	void onBattleEnded(BattleEndedEvent event) {
		for (RoundListener listener : roundListeners) {
			listener.onBattleEnded(event);
		}
	}

	void onBulletHit(BulletHitEvent event) {
		for (BulletHitListener listener : bulletHitListeners) {
			listener.onBulletHit(event);
		}
	}

	void onBulletHitBullet(BulletHitBulletEvent event) {
		for (BulletHitListener listener : bulletHitListeners) {
			listener.onBulletHitBullet(event);
		}
	}

	void onBulletMissed(BulletMissedEvent event) {
		for (BulletHitListener listener : bulletHitListeners) {
			listener.onBulletMissed(event);
		}
	}

	void onHitByBullet(HitByBulletEvent event) {
		for (BulletHitListener listener : bulletHitListeners) {
			listener.onHitByBullet(event);
		}
	}

	void onHitRobot(HitRobotEvent event) {
		for (CollisionListener listener : collisionListeners) {
			listener.onHitRobot(event);
		}
	}

	void onHitWall(HitWallEvent event) {
		for (CollisionListener listener : collisionListeners) {
			listener.onHitWall(event);
		}
	}

	void onKeyPressed(KeyEvent e) {
	}

	void onKeyReleased(KeyEvent e) {
	}

	void onKeyTyped(KeyEvent e) {
	}

	void onMouseClicked(MouseEvent e) {
		for (MouseListener listener : mouseListeners) {
			listener.mouseClicked(e);
		}
	}

	void onMouseDragged(MouseEvent e) {
		for (MouseMotionListener listener : mouseMotionListeners) {
			listener.mouseDragged(e);
		}
	}

	void onMouseEntered(MouseEvent e) {
		for (MouseListener listener : mouseListeners) {
			listener.mouseEntered(e);
		}
	}

	void onMouseExited(MouseEvent e) {
		for (MouseListener listener : mouseListeners) {
			listener.mouseExited(e);
		}
	}

	void onMouseMoved(MouseEvent e) {
		for (MouseMotionListener listener : mouseMotionListeners) {
			listener.mouseMoved(e);
		}
	}

	void onMousePressed(MouseEvent e) {
		for (MouseListener listener : mouseListeners) {
			listener.mousePressed(e);
		}
	}

	void onMouseReleased(MouseEvent e) {
		for (MouseListener listener : mouseListeners) {
			listener.mouseReleased(e);
		}
	}

	void onMouseWheelMoved(MouseWheelEvent e) {
	}

	void onPaint(Graphics2D g) {
		for (PaintListener painter : painters) {
			painter.onPaint(g);
		}
	}

	void onRobotDeath(RobotDeathEvent event) {
		for (SurvivalListener listener : survivalListeners) {
			listener.onRobotDeath(event);
		}
	}

	void onRoundBegin() {
		for (RoundBeginListener listener : roundBeginListeners) {
			listener.onRoundBegin();
		}
	}
	
	void onRoundEnded(RoundEndedEvent event) {
		for (RoundListener listener : roundListeners) {
			listener.onRoundEnded(event);
		}
	}

	void onScannedRobot(ScannedRobotEvent event) {
		for (ScannedRobotListener listener : scannedRobotListeners) {
			listener.onScannedRobot(event);
		}
	}

	void onStatus(StatusEvent e) {
	}

	void onWin(WinEvent event) {
		for (SurvivalListener listener : survivalListeners) {
			listener.onWin(event);
		}
	}
	
	void onTurnBegin() {
		for (TurnListener turnListener : turnListeners) {
			turnListener.onTurnBegin();
		}
	}
	
	void onTurnEnd() {
		for (TurnListener turnListener : turnListeners) {
			turnListener.onTurnEnd();
		}		
	}
}
