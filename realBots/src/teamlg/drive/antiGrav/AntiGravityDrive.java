package teamlg.drive.antiGrav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
 * The anti-gravity patterns looks at all the enemies and set up the danger they
 * represent.
 * 
 *  TODO: Improve the repulsive force so that it takes into account target health
 *
 * @author Frederic Hemery
 */
public class AntiGravityDrive implements Drive {

    private static final int REPULSE_FACTOR = 100000;
    private static final int ESCAPE_ANGLE = 15;
    
    private RobotProxy robot;
    private HashMap<String, GravityPoint> aGravMap;
    private double mapXLength, mapYLength;
    
    private double targetX, targetY;
    private double myX;
    private double myY;
    
    private double repulseX =-1;
    private double repulseY =-1;
    private int turnToRenewRepulse;
    private static int NB_OF_TURNS_PER_REPULSE = 5;
    

    public AntiGravityDrive(double mapXlength, double mapYLength) {
        //Initialize robot proxy and gravmap
        robot = Resources.getRobotProxy();
        aGravMap = new HashMap<>();
        this.mapXLength = mapXlength;
        this.mapYLength = mapYLength;
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
        
        HashSet<String> aRobotList = Resources.getOtherRobots().getRobotList(); 
        for (String aRobot : aRobotList ) {
            Snapshot aSnapshot = aHistory.getSnapshot(aRobot);
            if (aSnapshot != null) {
                // Todo : determine Robot dangerousness.
                GravityPoint aPoint = new GravityPoint(aSnapshot.getX(), aSnapshot.getY(), -1);
                aGravMap.put(aRobot, aPoint);
            }
        }
        
        if (robot.getOthers() == 0 || aRobotList.isEmpty()) {
            driveController.drive(0, 0);
        } else {
            InitializePositions();
           
            // Add all the gravity force from other robots
            for (GravityPoint p : aGravMap.values()) {
                ComputeRobotThreat(p);
            }

            // Add the gravity from the walls.
            computeWallThreat();

            // Add the repulse from last position.
            handleOwnRepulse();
            
            // Tweak the turn angle as to avoid in bearing.
            double turnAngle = computeTurnAngle(targetX, targetY);
            boolean recompute = true;
            while (recompute)
            {
                recompute = false;
                for (String aRobot: aRobotList)
                {
                    Snapshot aSnapshot = aHistory.getSnapshot(aRobot);
                    if (aSnapshot != null) {
                        double aRobotAngle = computeTurnAngle(aSnapshot.getX(), aSnapshot.getY());
                        if (Math.abs(turnAngle - aRobotAngle) % 180 <= ESCAPE_ANGLE)
                        {
                            turnAngle+=ESCAPE_ANGLE;
                            recompute = true;
                            System.out.println("We're in the area of "+aSnapshot.getName()+", let's move out.");
                            break;
                        }

                    }
                }
            }
            // Finished computing the danger, start computing the Move
            driveController.drive(turnAngle, RCPhysics.MAX_SPEED);
            
        }
    }

    @Override
    public void driveTo(Snapshot opponentSnapshot, DriveController driveController) {
        drive(driveController);
    }

    /**
     * Retrieves the positions and the target position from the proxy.
     */
    private void InitializePositions() {
        myX = robot.getX();
        myY = robot.getY();
        targetX = robot.getX();
        targetY = robot.getY();
    }

    /**
     * Computes the wall threat. Modifies the targetX and Y in consequence.
     * 
     * Force driving the robot out of the wall is a 1/d^3 one, d being the distance
     * between the robot of the wall.
     */
    private void computeWallThreat() {
        // compute wall threat. 
        targetX += REPULSE_FACTOR/6* 1 / Math.pow(myX, 2);
        targetX -= REPULSE_FACTOR/6 * 1 / Math.pow(mapXLength - myX, 2);
        targetY += REPULSE_FACTOR/6 * 1 / Math.pow(myY, 2);
        targetY -= REPULSE_FACTOR/6 * 1 / Math.pow(mapYLength - myY, 2);

    }

    private void handleOwnRepulse() {
        // First init. Just record the positions, the other robots are going to do the job.
        if (robot.getTime() < 10)
            return;
        
        if (turnToRenewRepulse == 0)
        {
            repulseX = myX;
            repulseY = myY;
            turnToRenewRepulse--;
        }
        else if (turnToRenewRepulse > 0)
            --turnToRenewRepulse;
        
        if (robot.getVelocity() ==0 && turnToRenewRepulse < 0)
        {
            turnToRenewRepulse = NB_OF_TURNS_PER_REPULSE;
        }
        
        // Compute target X and Y
        double d2 = Math.pow(repulseX - myX, 2) + Math.pow(repulseY - myY,2);        
        if (d2 < 0.1)
            return;
        targetX += -1* REPULSE_FACTOR * robot.getOthers() * (1/Math.pow(d2, 2))*(repulseX - myX);
        targetY += -1* REPULSE_FACTOR * robot.getOthers() * (1/Math.pow(d2, 2))*(repulseY - myY);
        
    }
    
    private void ComputeRobotThreat(GravityPoint p) {
        double d2 = Math.pow(p.x - myX, 2) + Math.pow(p.y - myY, 2);
        targetX += REPULSE_FACTOR * p.power * (1 / Math.pow(d2, 1.5)) * (p.x - myX);
        targetY += REPULSE_FACTOR * p.power * (1 / Math.pow(d2, 1.5)) * (p.y - myY);

    }

    private double computeTurnAngle( double targetX, double targetY) {
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

        return Math.toDegrees(angleToTurn);
    }

    
}
