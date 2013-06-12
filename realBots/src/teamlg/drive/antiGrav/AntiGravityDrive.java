package teamlg.drive.antiGrav;

import java.util.ArrayList;
import java.util.HashMap;
import xander.core.Resources;

import xander.core.RobotProxy;
import xander.core.drive.Drive;
import xander.core.drive.DriveController;
import xander.core.math.RCPhysics;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;

/**
 * This drive is used to move around the different targets, avoiding the walls.
 *
 * The anti-gravity patterns look at all the enemies and set up the danger they
 * represent.
 * 
 *  TODO: Take walls into account
 *  TODO: Improve the repulsive force so that it takes into account target health
 *  TODO: Remove printLn and perform proper logging.
 *
 * @author Frederic Hemery
 */
public class AntiGravityDrive implements Drive {

    private RobotProxy robot;
    private HashMap<String, GravityPoint> aGravMap;
    private ArrayList<String> robotNames;

    public AntiGravityDrive() {
        //Initialize robot proxy and gravmap
        robot = Resources.getRobotProxy();
        aGravMap = new HashMap<>();
        this.robotNames = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "Anti-gravity Drive";
    }

    @Override
    public void onRoundBegin() {
        
    }

    @Override
    public void drive(DriveController driveController) {
        // Update the data concerning the robots
        SnapshotHistory aHistory = Resources.getSnapshotHistory();
        
        // List the robots
        if (robotNames.size() != robot.getOthers())
        {
            robotNames.clear();
            
            for ( Snapshot aRobotSnap : aHistory.getLastOpponentsScanned())
                if (! aRobotSnap.getName().equals(robot.getName()))
                    robotNames.add(aRobotSnap.getName());
        }
        
        for (String aRobot : robotNames) {
            Snapshot aSnapshot = aHistory.getSnapshot(aRobot);
            if (aSnapshot != null) {
                // Todo : determine Robot dangerousness.
                GravityPoint aPoint = new GravityPoint(aSnapshot.getX(), aSnapshot.getY(), -1);
                aGravMap.put(aRobot, aPoint);
            }
        }
        
        System.out.println("Number of robots: "+robot.getOthers());
        System.out.println("Number of robots found: "+robotNames.size());

        for (GravityPoint p : aGravMap.values())
            System.out.println("Gravity: "+p.x+";"+p.y+" -- "+p.power);
        
        if (robot.getOthers() == 0 || robotNames.isEmpty()) {
            driveController.drive(0, 0);
        } else {
            double myX = robot.getX(), myY = robot.getY();
            double targetX = robot.getX();
            double targetY = robot.getY();

            System.out.println ("Current position: "+myX+";"+myY);
            
            // Add all the gravity force from other robots
            for (GravityPoint p : aGravMap.values()) {
                
                double d2 = Math.pow(p.x - myX, 2) + Math.pow(p.y - myY, 2);
                System.out.println("Distance to robot "+Math.sqrt(d2));
                targetX += 1000000 * p.power * (1 / Math.pow(d2, 1.5)) * (p.x - myX);
                targetY += 1000000 * p.power * (1 / Math.pow(d2, 1.5)) * (p.y - myY);
 
            }

            System.out.println ("Move from: ["+myX+";"+myY+"] to ["+targetX+";"+targetY+"]");
            double aRadius = Math.sqrt( Math.pow(myX-targetX, 2) + Math.pow(myY-targetY, 2) );
            
            
            double diffX = targetX-myX, diffY=targetY-myY;
            double angleToTurn = Math.acos( Math.abs(diffY)/ Math.sqrt( Math.pow(diffX, 2) + Math.pow(diffY,2)));
            if (diffY < 0)
            {
                angleToTurn = Math.PI - angleToTurn;
            }
            if (diffX < 0)
            {
                angleToTurn *= -1;
            }
            
            System.out.println("Robot should turn by "+ Math.toDegrees(angleToTurn)+" degrees");
            
            driveController.drive(Math.toDegrees(angleToTurn), RCPhysics.MAX_SPEED);
            
        }
    }

    @Override
    public void driveTo(Snapshot opponentSnapshot, DriveController driveController) {
        drive(driveController);
    }
}
