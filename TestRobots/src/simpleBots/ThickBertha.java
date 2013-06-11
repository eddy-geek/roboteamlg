/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleBots;

import java.awt.Color;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

/**
 *
 * @author FHEMERY
 */
public class ThickBertha extends AdvancedRobot {

    private void linearShoot(ScannedRobotEvent event) {
        // First get info about the other robot
        double aEnemyDistance = event.getDistance();
        double aEnemyBearing = event.getBearing();
        double aEnemyHeading = event.getHeading();
        double aEnemySpeed = event.getVelocity();
        
        // Depending on the distance, we decide the bullet strength
        double aBulletStrength = 0.1;
        if (aEnemyDistance < 200)
            aBulletStrength = 3;
        else if (aEnemyDistance < 400)
            aBulletStrength = 2;
        else if (aEnemyDistance < 600)
            aBulletStrength = 1;
        double aBulletSpeed = 20.0 - 3.0 * aBulletStrength;
        
        // Now, the question is, where is the enemy going to be ?
        int aTickDelta=0;
        int predictedX, predictedY;
        while (++aTickDelta > 0)
        {
            //predictedX +=
        }
    }
    
    enum CurrentMove { AHEAD, TURN }
    CurrentMove _myMove = CurrentMove.TURN;
    
    @Override
    public void run()
    {
        setBodyColor(Color.yellow);
        setBulletColor(Color.yellow);
           
        while (true)
        {
            applySquareMove();
            setTurnRadarLeft(360);
            execute();
        }
    }

    private void applySquareMove() {
        
        if (_myMove == CurrentMove.TURN && Math.abs(getTurnRemaining()) < 0.1)
        {
            _myMove = CurrentMove.AHEAD;
            setAhead(100);
        }
        else if (_myMove == CurrentMove.AHEAD && Math.abs(getDistanceRemaining()) < 0.1)
        {
            setTurnLeft(90);
            _myMove = CurrentMove.TURN;
        }
        
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        
        long eX, eY;
        
        double aAngle = robocode.util.Utils.normalAbsoluteAngle(getHeadingRadians()+ event.getBearingRadians());
        
        eX = Math.round( event.getDistance()*Math.sin( aAngle ) +getX());
        eY = Math.round( event.getDistance()*Math.cos( aAngle ) +getY());
       
        System.out.println("Detected "+event.getName()+" ["+eX+";"+eY+"]");
        System.out.println("Enemy Bearing: "+event.getBearing());
        System.out.println("Enemy Heading: "+event.getHeading());
            
        System.out.println("My Heading: "+getHeading());
        if (getGunHeat() == 0)
        {
            stop();
            
            //linearShoot(event);
            resume();
        }
    }
    
    
    
}
