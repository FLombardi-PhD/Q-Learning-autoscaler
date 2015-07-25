package qlearning;


/**
 * Project of Learning in Autonomous Systems
 * @author Federico Lombardi
 */
public class Machine {

	private String type;
	private int cpu;
	
	public Machine(String type, int cpu) {
		this.type = type;
		this.cpu = cpu;
	}
	
	public String getType() {
		return this.type;
	}
	
	public int getCpu() {
		return this.cpu;
	}
	
	@Override
	public String toString() {
		return "Machine " + type + ": cpu=" + cpu;
	}
	
}
