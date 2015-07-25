package qlearning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

 
/**
 * Project of Learning in Autonomous Systems
 * @author Federico Lombardi
 */
public class QLearning {
	
	static final Logger logger = LogManager.getLogger("midlab");
	
    final DecimalFormat df = new DecimalFormat("#.##");
 
    // path finding
    final double alpha = 0.1;
    final double gamma = 0.9;
 
    int maxCpu = 30;
    int cpu;
    int penalizationFactor;
    int maxReward;
    int penalty;
    
    List<State> states;
    List<State> goalStates;
    int statesCount;
    
    Map<State, List<State>> actions;
    Map<Integer, List<State>> cpuValuesStateMap;
    Map<State, Boolean> policyUpdated;
    
    int[][] R; // reward lookup
    double[][] Q;  // Q(s,a) = Q(s,a) + alpha * (R(s,a) + gamma * Max(next state, all actions) - Q(s,a)) 
  
    
    /**
     * 
     * @param states
     * @param cpuValuesStateMap
     * @param actions
     * @param cpu
     * @param penalizationFactor
     */
    public QLearning(List<State> states, Map<Integer, List<State>> cpuValuesStateMap, Map<State, List<State>> actions,
    		int cpu, int penalizationFactor, boolean onlyGoalStates) {
    	logger.info("Creating QLearning Oject ..");
    	this.states = states;
    	this.cpuValuesStateMap = cpuValuesStateMap;
    	this.goalStates = cpuValuesStateMap.get(cpu);
    	this.actions = actions;
    	this.statesCount = states.size();
    	this.cpu = cpu;
    	this.penalizationFactor = penalizationFactor;
    	this.maxReward = (cpu - 1) * penalizationFactor;
    	this.penalty = (int) Math.round(Math.sqrt(penalizationFactor));
    	R = new int[statesCount][statesCount];
    	Q = new double[statesCount][statesCount];
    	this.policyUpdated = new HashMap<State, Boolean>();
    	for (State s : this.states) {
    		if (goalStates.contains(s))
    			this.policyUpdated.put(s, true);
    		else
    			this.policyUpdated.put(s, false);
    	}
        init(onlyGoalStates);
    }
 
    
    /**
     * initialization method for Reward matrix
     */
    public void init(boolean onlyGoalStates) {  
    	
    	if (onlyGoalStates) {
    		for (State currentState : goalStates) {
        		List<State> stateNearToCurrentState = actions.get(currentState);
        		for (State s : stateNearToCurrentState) {
        			R[s.getStateId()-1][currentState.getStateId()-1] = this.maxReward;
        		}
        	}
    	}
    	
    	else {
    		for (State currentState : states) {
        		List<State> stateNearToCurrentState = actions.get(currentState);
        		for (State s : stateNearToCurrentState) {
        			R[currentState.getStateId()-1][s.getStateId()-1] = this.maxReward - (this.maxReward/this.penalty)*Math.abs(this.cpu - s.getCpu());
        			
        			logger.info(s.getStateId() + " " + currentState.getStateId() + " R=" +
        					R[currentState.getStateId()-1][s.getStateId()-1]);
        		}
        	}
    	}
    	
    }
    
 
    /**
     * @throws FileNotFoundException 
     * 
     */
    void run() throws FileNotFoundException {
        /*
         1. Set parameter , and environment reward matrix R 
         2. Initialize matrix Q as zero matrix 
         3. For each episode: Select random initial state 
            Do while not reach goal state o 
                Select one among all possible actions for the current state o 
                Using this possible action, consider to go to the next state o 
                Get maximum Q value of this next state based on all possible actions o 
                Compute o Set the next state as the current state
         */
 
        // For each episode
    	
    	logger.info("Q-Learning is running..");
        Random rand = new Random();
        State startState = null;
        PrintWriter w = new PrintWriter(new File("res.csv"));
        for (int i = 0; i < 2000; i++) { // train episodes
            
        	// Select random initial state
            int stateId = rand.nextInt(statesCount);
            State state = states.get(stateId);
            if (startState == null)
            	startState = state;
             
            while (!goalStates.contains(state)) { // goal state
            	logger.debug("\nITERATION "+i+": selected state "+state.getStateId());
                           
            	 if (!policyUpdated.get(state))
              		policyUpdated.put(state, true);
            	
            	// Select one among all possible actions for the current state
                List<State> actionsFromState = actions.get(state);
                                
                // Selection strategy is random in this example
                int index = rand.nextInt(actionsFromState.size());
                State action = actionsFromState.get(index);
                logger.debug("selected action "+action+" with random index "+index);
                
                // Action outcome is set to deterministic in this example
                // Transition probability is 1
                State nextState = action; // data structure
                logger.debug("next state: "+nextState.getStateId());
                
                // Using this possible action, consider to go to the next state
                double q = getQ(state, action);
                double maxQ = getMaxQ(nextState);
                int r = getR(state, action);
                
                double value = q + alpha * (r + gamma * maxQ - q);
                setQ(state, action, value);
 
                logger.debug("\n\tq="+q+"\n\tmax_q="+maxQ+"\n\tr="+r+"\n\tvalue="+value);
                                
                /*
                try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
                */
                
                // Set the next state as the current state
                state = nextState;
            }
            
            // Compute avg step to reach a goal state
            double sumStep = 0D;
            for (State s : states) {
            	int step = stepToNearGoal(null, s, 0);
            	if (step == 0) step = 1;
            	logger.debug(s.toString() + " - step to goal = " + step);
            	sumStep += step;
            }
            double avg = sumStep / states.size();
            w.println(i + "\t" + avg);
            w.flush();
            
            /*
            showPolicy();
            System.out.println("found policy in " +count+ " iteration.");
            */
            
            state = null;
        }
        w.close();
    }
 
   
    /**
     * 
     * @param s
     * @param a
     * @return
     */
    int getR(State s, State a) {
        return R[s.getStateId()-1][a.getStateId()-1];
    }
  
    
    /**
     * 
     * @param s
     * @param a
     * @return
     */
    double getQ(State s, State a) {
        return Q[s.getStateId()-1][a.getStateId()-1];
    }
 
    
    /*
     * 
     */
    void setQ(State s, State a, double value) {
        Q[s.getStateId()-1][a.getStateId()-1] = value;
    }
 

    /**
     *  
     * @param s
     * @return
     */
     double getMaxQ(State s) {
         List<State> actionsFromState = actions.get(s);
         double maxValue = Double.MIN_VALUE;
         for (int i = 0; i < actionsFromState.size(); i++) {
             State nextState = actionsFromState.get(i);
             double value = Q[s.getStateId()-1][nextState.getStateId()-1];
  
             if (value > maxValue) {
             	maxValue = value;
             }
                 
         }
         return maxValue;
     }
     
     
    /**
     * 
     * @param state
     * @return
     */
    // get policy from state
    State getPolicy(State state) {
        List<State> actionsFromState = actions.get(state);
        double maxValue = Double.MIN_VALUE;
        State policyGotoState = state; // default goto self if not found
        for (int i = 0; i < actionsFromState.size(); i++) {
            State nextState = actionsFromState.get(i);
            double value = Q[state.getStateId()-1][nextState.getStateId()-1];
 
            if (value > maxValue) {
                maxValue = value;
                policyGotoState = nextState;
            }
        }
        return policyGotoState;
    }
    
    /**
     * 
     * @param state
     * @return
     */
    public int stepToGoal(State from, State state, int count) {
    	
    	// to avoid infinite loop
    	if (count > 50) 
    		return 1000;
    	
    	// base case 1
    	if (state.equals(from))
    		return 0;
    	
    	// base case 2
    	if (goalStates.contains(state))
    		return 0;
    	
    	// recursive step
    	else {
    		State to = getPolicy(state);
    		while (to.equals(from) || state.equals(to)) {
        			Random rand = new Random();
        			List<State> stateList = actions.get(state);
        			int stateIndex = rand.nextInt(stateList.size());
                    to = stateList.get(stateIndex);
    		}
    		return 1+stepToGoal(state, to, count+1);
    	}
    }
    
    /**
     * 
     * @param state
     * @return
     */
    public int stepToNearGoal(State from, State state, int count) {
    	
    	// base case 1
    	if (Math.abs(state.getCpu()-this.cpu) < 3)
    		return 0;
    	
    	// tp avoid infinite loop
    	if (count > 50) 
    		return 1000;
    	
    	// base case 2
    	if (state.equals(from))
    		return 0;
    	
    	// base case 3
    	if (goalStates.contains(state))
    		return 0;
    	
    	// recursive step
    	else {
    		State to = getPolicy(state);
    		while (to.equals(from) || state.equals(to)) {
        			Random rand = new Random();
        			List<State> stateList = actions.get(state);
        			int stateIndex = rand.nextInt(stateList.size());
                    to = stateList.get(stateIndex);
    		}
    		return 1+stepToNearGoal(state, to, count+1);
    	}
    }
    
    
    /**
     * 
     */
    void printResult() {
        logger.info("Print result");
        for (int i = 0; i < Q.length; i++) {
            System.out.print("out from " + states.get(i) + ":  ");
            for (int j = 0; j < Q[i].length; j++) {
                System.out.print(df.format(Q[i][j]) + " ");
            }
            System.out.println();
        }
    }
 
    
    /**
     * 
     */
    // policy is maxQ(states)
    void showPolicy() {
        logger.info("showPolicy");
        for (int i = 0; i < states.size(); i++) {
            State from = states.get(i);
            if (this.policyUpdated.get(from)) {
            	State to = getPolicy(from);
                logger.info("from "+states.get(from.getStateId()-1)+" goto "+states.get(to.getStateId()-1));
            }
        }           
    }
}