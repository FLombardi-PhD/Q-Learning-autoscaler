package qlearning;

/**
 * Project of Learning in Autonomous Systems
 * @author Federico Lombardi
 */
public class Action {

	private State srcState;
	private State dstState;
	
	public Action(State srcState, State dstState) {
		this.srcState = srcState;
		this.dstState = dstState;
	}
	
	public State getSrcState() {
		return this.srcState;
	}
	
	public State getDstState() {
		return this.dstState;
	}
	
	@Override
	public String toString() {
		return "Action " + this.srcState.getStateId() + " -> " + this.dstState.getStateId();
	}
	
}
