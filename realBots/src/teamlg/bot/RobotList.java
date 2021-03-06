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
        robotNames = new HashSet<String>();
    }
    
    public void addRobot(String robot)
    {
        robotNames.add(robot);
    }
    
    public void removeRobot(String robot){
        robotNames.remove(robot);
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
