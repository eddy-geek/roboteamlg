package xander.cat.group.mirror;

import xander.core.ComponentChain;
import xander.core.Resources;
import xander.core.drive.Drive;
import xander.core.drive.OrbitalDrivePredictor;
import xander.core.gun.Gun;
import xander.core.gun.XanderGun;
import xander.core.gun.power.FixedPowerSelector;
import xander.core.gun.power.PowerSelector;

public class MirrorFactory {

	/**
	 * And Anti-Mirror components.  
	 * 
	 * @param componentChain   component chain
	 * @param scanDepth        number of consecutive ticks to record mirror hits for
	 * @param scannedTicks     number of ticks into the past to scan for mirroring (the copy delay of the opponent)
	 * @param reversalTickRange  determines how long robot might continue in same direction before reversing direction
	 */
	public static void addAntiMirrorComponents(ComponentChain componentChain, int scanDepth, int scannedTicks, int reversalTickRange) {
		MirrorPlan mirrorPlan = new MirrorPlan(Resources.getRobotEvents(), reversalTickRange);
		OrbitalDrivePredictor orbitalDrivePredictor = new OrbitalDrivePredictor();
		MirrorDetector mirrorDetector = new MirrorDetector(scanDepth, scannedTicks);
		MirrorScenario mirrorScenario = new MirrorScenario(mirrorDetector);
		Drive antiMirrorDrive = new AntiMirrorDrive(mirrorPlan, orbitalDrivePredictor);
		PowerSelector powerSelector = new FixedPowerSelector(1.95);
		((FixedPowerSelector)powerSelector).setAllowAutoAdjust(false);
		AntiMirrorTargeter targeter = new AntiMirrorTargeter(mirrorPlan, mirrorDetector, orbitalDrivePredictor);
		Gun antiMirrorGun = new XanderGun(targeter, powerSelector);
		componentChain.addComponents(mirrorScenario, antiMirrorDrive, antiMirrorGun);
	}
}
