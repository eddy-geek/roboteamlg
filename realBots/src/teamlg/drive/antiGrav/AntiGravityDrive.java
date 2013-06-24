package teamlg.drive.antiGrav;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.drive.Drive;
import xander.core.drive.DriveController;
import xander.core.event.PaintListener;
import xander.core.log.Logger;
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
public class AntiGravityDrive implements Drive, PaintListener {

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
    
    private static final int CONFORT_SUBDIVISION = 100;
    private double[][] confortMatrix;
    

    public AntiGravityDrive(double mapXlength, double mapYLength) {
        //Initialize robot proxy and gravmap
        robot = Resources.getRobotProxy();
        aGravMap = new HashMap<>();
        this.mapXLength = mapXlength;
        this.mapYLength = mapYLength;
        
        
        this.confortMatrix = new double[(int) (Resources.getRobotProxy().getBattleFieldHeight() / CONFORT_SUBDIVISION)]
        		[(int) (Resources.getRobotProxy().getBattleFieldWidth() / CONFORT_SUBDIVISION)];
        
        Resources.getRobotEvents().addPainter(this);
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
            
            //computeConfortMatrix();
            
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
    
    private void computeConfortMatrix() {
    	// Compute confort of my position
    	double aMyRepulse = 0;
    	{	    	
	        // Add all the gravity force from other robots
	        for (GravityPoint p : aGravMap.values()) {
	        	double d2 = Math.pow(p.x - myX, 2) + Math.pow(p.y - myY, 2);
	        	aMyRepulse -= REPULSE_FACTOR * p.power * (1 / Math.pow(d2, 1.5));
	        }
	        
            // Compute wall threats
//	        aMyRepulse -= REPULSE_FACTOR/12 * 1 / Math.pow(myX, 2);
//	        aMyRepulse -= REPULSE_FACTOR/12 * 1 / Math.pow(mapXLength - myX, 2);
//	        aMyRepulse -= REPULSE_FACTOR/12 * 1 / Math.pow(myY, 2);
//	        aMyRepulse -= REPULSE_FACTOR/12 * 1 / Math.pow(mapYLength - myY, 2);
	        
	        aMyRepulse = Math.round(aMyRepulse * 10000);
    	}
    	
    	double aMinRepulse = aMyRepulse;
    	double aMinRepulseX = 0, aMinRepulseY = 0;
    	
    	for (int i = 1; i < confortMatrix.length - 1; i++) {
			for (int j = 1; j < confortMatrix[i].length - 1; j++) {
				double aRepulse = 0;
				double iX = j * CONFORT_SUBDIVISION;
				double jY = i * CONFORT_SUBDIVISION;
				
	            // Add all the gravity force from other robots
	            for (GravityPoint p : aGravMap.values()) {
	            	double d2 = Math.pow(p.x - iX, 2) + Math.pow(p.y - jY, 2);
	            	aRepulse -= REPULSE_FACTOR * p.power * (1 / Math.pow(d2, 1.5));
	            }
	            
	            // Compute wall threats
//	            aRepulse -= REPULSE_FACTOR/12 * 1 / Math.pow(iX, 2);
//	            aRepulse -= REPULSE_FACTOR/12 * 1 / Math.pow(mapXLength - iX, 2);
//	            aRepulse -= REPULSE_FACTOR/12 * 1 / Math.pow(jY, 2);
//	            aRepulse -= REPULSE_FACTOR/12 * 1 / Math.pow(mapYLength - jY, 2);

	            // Store results
				confortMatrix[i][j] = Math.round(aRepulse * 10000);
				if (confortMatrix[i][j] < aMinRepulse) {
					aMinRepulse = confortMatrix[i][j];
					aMinRepulseX = iX;
					aMinRepulseY = jY;
				}
			}
		}
    	Logger.getLog(getClass()).info("My confort: " + aMyRepulse);
    	Logger.getLog(getClass()).info("Max confort: " + aMinRepulse);
    	Logger.getLog(getClass()).info(Arrays.toString(confortMatrix[6]));
    	Logger.getLog(getClass()).info(Arrays.toString(confortMatrix[5]));
    	Logger.getLog(getClass()).info(Arrays.toString(confortMatrix[4]));
    	Logger.getLog(getClass()).info(Arrays.toString(confortMatrix[3]));
    	Logger.getLog(getClass()).info(Arrays.toString(confortMatrix[2]));
    	Logger.getLog(getClass()).info(Arrays.toString(confortMatrix[1]));
    	
    	double aRepulseGain = aMyRepulse / aMinRepulse;
    	if (aRepulseGain > 3) {
    		Logger.getLog(getClass()).info("Going to : (" + aMinRepulseX + ", " + aMinRepulseY + ")");
    		// Let's add a heavy attraction point in the most confortable area
            double d2 = Math.pow(aMinRepulseX - myX, 2) + Math.pow(aMinRepulseY - myY, 2);
            targetX += REPULSE_FACTOR * 3 * aGravMap.size() * (1 / Math.pow(d2, 1.5)) * (aMinRepulseX - myX);
            targetY += REPULSE_FACTOR * 3 * aGravMap.size() * (1 / Math.pow(d2, 1.5)) * (aMinRepulseY - myY);
    	}
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

    @Override
	public void onPaint(Graphics2D g) {
    	// Set the paint color to a red half transparent color
        g.setColor(new Color(0xff, 0x00, 0x00, 0x80));
    
        // Draw a line from our robot to the scanned robot
        g.drawLine((int) targetX, (int) targetY, (int) myX, (int) myY);
    
//        // Draw a filled square on top of the scanned robot that covers it
//        g.fillRect(scannedX - 20, scannedY - 20, 40, 40);
    }
    
}
