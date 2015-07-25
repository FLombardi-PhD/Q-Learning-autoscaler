package qlearning;


/**
 * Project of Learning in Autonomous Systems
 * @author Federico Lombardi
 */
public class State {

	private int stateId;
	private Configuration configuration;
	private int cpu;
	
	public State(int stateId, Configuration configuration) {
		this.stateId = stateId;
		this.configuration = configuration;
		this.cpu = configuration.getCpu();
	}
	
	public int getStateId() {
		return this.stateId;
	}
	
	public Configuration getConfiguration() {
		return this.configuration;
	}
	
	public int getCpu() {
		return this.cpu;
	}
	
	@Override
	public String toString() {
		return "State " + stateId + ": cpu=" + this.cpu + " " + this.configuration.toString();
		
	}
}
