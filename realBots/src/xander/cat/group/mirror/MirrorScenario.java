package xander.cat.group.mirror;

import xander.core.Scenario;

public class MirrorScenario implements Scenario {

	private MirrorDetector mirrorDetector;
	
	public MirrorScenario(MirrorDetector mirrorDetector) {
		this.mirrorDetector = mirrorDetector;
	}
	
	@Override
	public boolean applies() {
		return mirrorDetector.isMirrorDetected();
	}
}
