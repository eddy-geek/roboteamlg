package xander.core.track;

public enum WaveState {

	LEADING,   // wave has not reached target 
	HIT,       // wave passed leading edge of target
	PASSING,   // wave passed center of target
	PASSED;    // wave passed trailing edge of target
}
