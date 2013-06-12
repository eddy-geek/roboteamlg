package teamlg.radar;


import xander.core.radar.Radar;
import xander.core.radar.RadarController;
import xander.core.track.Snapshot;

/**
 *
 * @author FHEMERY
 */
public class SpinningRadar implements Radar{

    private double _sweepAngle;
    public SpinningRadar(double sweepAngle)
    {
        _sweepAngle = sweepAngle;
    }
    
    @Override
    public Snapshot search(RadarController radarController) {
        radarController.setTurnRadarLeftRadians(_sweepAngle);
        return null;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return "Spinning Radar";
    }

    @Override
    public void onRoundBegin() {
        // Do nothing.
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
