package teamlg.radar;


import xander.core.Resources;
import xander.core.RobotProxy;
import xander.core.radar.Radar;
import xander.core.radar.RadarController;
import xander.core.track.Snapshot;
import xander.core.track.SnapshotHistory;

/**
 * TODO: implement a method to avoid scanning when there is nothing.
 * @author FHEMERY
 */
public class SpinningRadar implements Radar{
    
    private RobotProxy robot;
    private Snapshot lastSnapshot;
    
    private double _sweepAngle;
    public SpinningRadar(double sweepAngle)
    {
        _sweepAngle = sweepAngle;
        robot = Resources.getRobotProxy();
        lastSnapshot = null;
    }
    
    @Override
    public Snapshot search(RadarController radarController) {
        if (robot.getRadarTurnRemainingDegrees() == 0)
        {
            radarController.setTurnRadarLeftRadians(_sweepAngle);
            lastSnapshot = seekForTarget();
        }
        else if (lastSnapshot != null)
            lastSnapshot = Resources.getSnapshotHistory().getSnapshot(lastSnapshot.getName());

        
        return lastSnapshot;
    }

    @Override
    public String getName() {
        return "Spinning Radar";
    }

    @Override
    public void onRoundBegin() {        
    }

    private Snapshot seekForTarget() {
        double aRefDistance = 200000000;
        Snapshot aClosestTarget = null;
        SnapshotHistory aHistory =  Resources.getSnapshotHistory();
        for (String aRobot : Resources.getOtherRobots().getRobotList())
        {
            Snapshot aSnapshot = aHistory.getSnapshot(aRobot);
            if (aSnapshot == null)
                continue;
            double aRobotDistance = Math.pow(aSnapshot.getX() - robot.getX(),2)+Math.pow(aSnapshot.getY() - robot.getY(),2);
            if (aRobotDistance < aRefDistance){
                aRefDistance = aRobotDistance;
                aClosestTarget = aSnapshot;
            }
        }
        return aClosestTarget;
    }

}
