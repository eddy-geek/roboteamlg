/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleBots;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

/**
 *
 * @author FHEMERY
 */
public class ElephantInCorridorsSniper extends AdvancedRobot {

    @Override
    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        while (true) {
            ahead(100);
            turnRadarRight(360);
            back(100);
            turnRadarRight(360);
        }
    }
    
    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Turn the gun so as to get it right.
        double gunAngle = getGunHeading() - getHeading();
        double relativeEnemyAngle = e.getBearing();
        System.out.println("Gun angle is:"+gunAngle);
        System.out.println("bearing is: "+relativeEnemyAngle);
       
        turnGunRight(relativeEnemyAngle - gunAngle);
        fire(1);
    }
}
