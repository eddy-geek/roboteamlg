package teamlg.drive.antiGrav;

/**
 * Gravity point is the representation of a force position and strength.
 * @author FHEMERY
 */
public class GravityPoint {
    
    public double x,y,power;
    public String name;
    public GravityPoint(double pX,double pY,double pPower, String pName) {
        x = pX;
        y = pY;
        power = pPower;
        name = pName;
    }
    
}
