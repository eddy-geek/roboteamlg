package teamlg.bot;

import java.util.HashSet;

/**
 *
 * @author FHEMERY
 */
public class RobotList {
    private HashSet<String> robotNames;
    
    public RobotList ()
    {
        robotNames = new HashSet<>();
    }
    
    public void addRobot(String robot)
    {
        robotNames.add(robot);
    }
    
    public void clearRobotList()
    {
        robotNames.clear();
    }
    
    public HashSet<String> getRobotList()
    {
        return robotNames;
    }
    
}
