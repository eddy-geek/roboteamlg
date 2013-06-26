package xander.core;

import robocode.*;
import xander.core.event.BulletHitListener;
import xander.core.event.SurvivalListener;
import xander.core.log.Log;
import xander.core.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ed on 6/24/13.
 */
public class HitStats implements BulletHitListener, SurvivalListener {

    private static final Log log = Logger.getLog(HitStats.class);

    protected final static double MAX_NORMALIZED = 2.5;

    int totalHits = 0;
    int nOthers = 1;

    Map<String, Integer> hitBy = new HashMap<String, Integer>();
    Map<String, Double> hitRatioBy = new HashMap<String, Double>();

    public HitStats(RobotProxy robotProxy, RobotEvents robotEvents) {
        log.info("Initialize HitStats !");
        robotEvents.addBulletHitListener(this);
        robotEvents.addSurvivalListener(this);
    }

    /**
     * Same as below, but between 0.5 (not dangerous) and 3 (very dangerous)
     * Average will be 0.5+2.5/nRobot¹
     */
    public double getHitRatioBy(String aRobot) {
        return 0.5 + getNormalizedHitRatioBy(aRobot) * MAX_NORMALIZED;
    }

    /**
     * Returns a number between 0 and 1
     * > 1/nOthers if robot is responsible for more than his "share" of total hits
     * (i.e. more than the average), else < 1/nOthers
     * @param aRobot
     * @return
     */
    public double getNormalizedHitRatioBy(String aRobot) {
        Double res = hitRatioBy.get(aRobot);
        return res == null ?
                (totalHits < nOthers ? 1/MAX_NORMALIZED : 0) : res;
        // (Wait for n hits before saying a bit is not dangerous)
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        totalHits++;
        nOthers = Resources.getRobotProxy().getOthers();
        String enemyBot = event.getBullet().getName();
        Integer curCount = hitBy.get(enemyBot);
        if (curCount == null) curCount = 0; // first hit by him
        curCount++;
        hitBy.put(enemyBot, curCount);
        // Update hit ratios
        for (String otherBot : hitBy.keySet()) {
            hitRatioBy.put(otherBot, hitBy.get(otherBot)/(double)totalHits);
        }
        log.info("TotalHits: "+totalHits);
        log.info("Hits: "+hitBy);
        log.info("HitRatios: "+hitRatioBy);
    }

    @Override
    public void onWin(WinEvent event) {
    }

    @Override
    public void onDeath(DeathEvent event) {
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        Integer hitsByDead = hitBy.get(event.getName());
        if (hitsByDead != null) {
            totalHits -= hitsByDead;
        }
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
