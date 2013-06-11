package xander.core.event;

public interface GunListener {

	public void gunFired(GunFiredEvent event);
	
	public void virtualGunFired(GunFiredEvent event);
}
