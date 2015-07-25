package qlearning;

import java.util.Map;

/**
 * Project of Learning in Autonomous Systems
 * @author Federico Lombardi
 */
public class Configuration {

	private int configurationId;
	private Map<Machine, Integer> machineMap;
	private int cpu;
	
	public Configuration(int configurtionId, Map<Machine, Integer> machineMap) {
		this.configurationId = configurtionId;
		this.machineMap = machineMap;
		for (Machine m : this.machineMap.keySet()) {
			this.cpu += m.getCpu() * machineMap.get(m);
		}
	}
	
	public int getConfigurationId() {
		return this.configurationId;
	}
	
	public Map<Machine, Integer> getMachineMap() {
		return this.machineMap;
	}
	
	public int getCpu() {
		return this.cpu;
	}
	
	@Override
	public String toString() {
		String configuration = "Config. " + configurationId;
		for (Machine m : this.machineMap.keySet()) {
			if (configuration.equals("Config. " + configurationId))
				configuration += ": [" + m.getType() + "*" + this.machineMap.get(m);
			else
				configuration += ", " + m.getType() + "*" + this.machineMap.get(m);
				
		}
		configuration += "]";
		return configuration;
	}
	
}
