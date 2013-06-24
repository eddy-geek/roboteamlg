package xander.core;

import robocode.*;
import xander.core.event.BulletHitListener;
import xander.core.event.SurvivalListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ed on 6/24/13.
 */
public class HitStats implements BulletHitListener, SurvivalListener {

    int totalHits = 0;

    Map<String, Integer> hitBy = new HashMap<>();
    Map<String, Integer> hitRatioBy = new HashMap<>();

    public HitStats(RobotProxy robotProxy, RobotEvents robotEvents) {
        robotEvents.addBulletHitListener(this);
        robotEvents.addSurvivalListener(this);
    }

    public int getHitRatioBy(String aRobot) {
        Integer res = hitRatioBy.get(aRobot);
        return res == null ? 1 : res;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        int nOthers = Resources.getRobotProxy().getOthers();
        String enemyBot = event.getBullet().getName();
        Integer curCount = hitBy.get(enemyBot);
        if (curCount == null) curCount = 0; // first hit by him
        hitBy.put(enemyBot, curCount++);
        totalHits++;
        // Update hit ratios
        for (String otherBot : hitBy.keySet()) {
            hitRatioBy.put(otherBot, hitBy.get(otherBot)/totalHits*nOthers);
        }
    }

    @Override
    public void onWin(WinEvent event) {
    }

    @Override
    public void onDeath(DeathEvent event) {
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        hitBy.remove(event.getName());
        hitRatioBy.remove(event.getName());
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent event) {
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
    }

}
