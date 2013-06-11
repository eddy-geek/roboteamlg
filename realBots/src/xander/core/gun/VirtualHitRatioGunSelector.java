package xander.core.gun;

import java.util.HashMap;
import java.util.Map;

import xander.core.Resources;
import xander.core.math.ConstantValueFunction;
import xander.core.math.Function;
import xander.core.math.RCRoundFunction;
import xander.core.track.GunStats;
import xander.core.track.Snapshot;

public class VirtualHitRatioGunSelector implements GunSelector {

	private GunStats gunStats;
	private Map<Gun, Double> biasMap = new HashMap<Gun, Double>();
	private RCRoundFunction rollingRatioWeightFunction = new RCRoundFunction(new ConstantValueFunction(0.6));
	private Gun preferredGun;
	private double preferredHitRatio;
	private int usePreferredGunOnlyUntilRound;
	
	public VirtualHitRatioGunSelector() {
		gunStats = Resources.getGunStats();
	}
	
	/**
	 * Sets the rolling ratio weight to a constant percentage.  Value should be in range 0 to 1.
	 * 
	 * @param rollingRatioWeight    percentage of rolling ratio weight
	 */
	public void setRollingRatioWeight(double rollingRatioWeight) {
		this.rollingRatioWeightFunction = new RCRoundFunction(new ConstantValueFunction(rollingRatioWeight));
	}
	
	/**
	 * Sets a function for the rolling ratio weight.  Function values should be in range 0 to 1.
	 * 
	 * @param rollingRatioWeightFunction   function for computing rolling ratio weight
	 */
	public void setRollingRatioWeightFunction(Function rollingRatioWeightFunction) {
		this.rollingRatioWeightFunction = new RCRoundFunction(rollingRatioWeightFunction);
	}
	
	/**
	 * Sets a function for the rolling ratio weight.  Function values should be in range 0 to 1.
	 * 
	 * @param rollingRatioWeightFunction   function for computing rolling ratio weight
	 */
	public void setRollingRatioWeightFunction(RCRoundFunction rollingRatioWeightFunction) {
		this.rollingRatioWeightFunction = rollingRatioWeightFunction;
	}
	
	public void addBias(Gun gun, double bias) {
		biasMap.put(gun, Double.valueOf(bias));
	}	
	
	public void setPreferredGun(Gun gun, double hitRatio, int usePreferredGunOnlyUntilRound) {
		this.preferredGun = gun;
		this.preferredHitRatio = hitRatio;
		this.usePreferredGunOnlyUntilRound = usePreferredGunOnlyUntilRound;
	}
	
	@Override
	public int selectGun(Gun[] guns, Snapshot target) {
		int sGunIndex = 0;
		double rollingRatioWeight = rollingRatioWeightFunction.getValueForRound();
		double sRatio = Double.NEGATIVE_INFINITY;
		//StringBuilder sb = new StringBuilder();
		for (int i=0; i<guns.length; i++) {
			Gun gun = guns[i];
			double ratio = gunStats.getVirtualHitRatio(gun.getName());
			double rollingRatio = gunStats.getRollingVirtualHitRatio(gun.getName());
			ratio = ratio * (1-rollingRatioWeight) + rollingRatio * rollingRatioWeight;
			Double bias = biasMap.get(gun);
			if (bias != null) {
				ratio += bias.doubleValue();
			}
			//sb.append("[" + gun.getName() + ":" + gunController.getVirtualBulletsFired(gun.getName()) + ":" + Logger.format(100*ratio) + "%]");
			if ((ratio > sRatio && guns[i].canFireAt(target)) || (gun == preferredGun && (ratio > preferredHitRatio || Resources.getRobotProxy().getRoundNum() < usePreferredGunOnlyUntilRound))) {
				sGunIndex = i;
				sRatio = ratio;
			}
		}
		//System.out.println(sb.toString());
		return sGunIndex;
	}
}
