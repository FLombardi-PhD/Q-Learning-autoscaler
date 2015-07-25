package qlearning;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;


/**
 * Project of Learning in Autonomous Systems
 * 
 * @author Federico Lombardi
 */
public class Starter {

	static final Logger logger = LogManager.getLogger("midlab");
	
	/**
	 * Build
	 * @return
	 */
	public static List<State> buildStates(String[] types, int[] maxMachinePerType){
						
		Map<String, Machine> mapMachineNamesMachine = new HashMap<String, Machine>();
		ArrayList<String> machineNames = new ArrayList<String>();
		
		// create map and machine list
		for (int i=0; i<types.length; ++i) {
			for (int j=0; j<maxMachinePerType[i]; ++j) {
				Machine machine = new Machine(types[i],i+1);
				mapMachineNamesMachine.put(types[i], machine);
				machineNames.add(types[i]);
				logger.trace("Added machine "+machine.toString());
			}
		}
				
		// create subsets
		ICombinatoricsVector<String> initialSet = Factory.createVector(machineNames);
		Generator<String> gen = Factory.createSubSetGenerator(initialSet);

		// create configurations
		int configId = 0;
		ArrayList<Configuration> configurations = new ArrayList<Configuration>();
		Map<Machine, Integer> mapMachineNumber = null;
		
		for (ICombinatoricsVector<String> subSet : gen) {
			logger.trace(subSet);
			
			List<String> vector = subSet.getVector();
			mapMachineNumber = new HashMap<Machine, Integer>();
			
			for (String machineName : vector) {
				Machine machine = mapMachineNamesMachine.get(machineName);
				if (mapMachineNumber.containsKey(machine)) {
					int count = mapMachineNumber.get(machine) + 1;
					mapMachineNumber.put(machine, count);
				}
				else
					mapMachineNumber.put(machine, 1);
			}
			
			for (Machine m : mapMachineNumber.keySet()) {
				logger.trace(m.getType()+": "+mapMachineNumber.get(m));
			}
			
			// skip the configuration 0
			if (configId > 0) {
				Configuration conf = new Configuration(configId, mapMachineNumber);
				configurations.add(conf);
			}
			
			++configId;
		}
		
		// build state for each configuration
		ArrayList<State> states = new ArrayList<State>();
		for (Configuration conf : configurations) {
			State state = new State(conf.getConfigurationId(), conf);
			states.add(state);
			logger.debug(state.toString());
		}
		
		return states;
	}
	
	/**
	 * Check whether 2 states are near or not
	 * @param state1
	 * @param state2
	 * @return true if the state are near; false otherwise
	 */
	public static boolean areStateNear(State state1, State state2) {
		
		int distance = 0;
		Map<Machine, Integer> map1 = state1.getConfiguration().getMachineMap();
		Map<Machine, Integer> map2 = state2.getConfiguration().getMachineMap();
		
		Set<Machine> set1 = map1.keySet();
		Set<Machine> set2 = map2.keySet();
		
		for (Machine m : set1) {
			if (set2.contains(m))
				distance += Math.abs(map1.get(m) - map2.get(m));
			else
				distance += map1.get(m);
			if (distance > 1)
				return false;
		}
		
		for (Machine m : set2) {
			if (!set1.contains(m))
				distance += map2.get(m);
			if (distance > 1)
				return false;
		}
		
		return true;
	}
	
	public static String arrayToString(Object[] arr) {
		String s = "{";
		for (int i=0; i<arr.length; ++i) {
			if (i==0) s += arr[i];
			else s+= ", "+arr[i];
		}
		return s+= "}";
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		
		// default input parameters
		String[] types = {"M1", "M2", "M3", "M4"};
		int[] maxMachinePerType = {3, 3, 3, 3};
		int goalCpu = 20;
		int penalizationFactor = 10;
		boolean onlyGoalState = false;

		// parameters pased as args
		int parameters = args.length;
		for (int i=0; i<parameters; ++i) {
			if (args[i].startsWith("types=")) {
				try {
					types = args[i].replaceAll("types=", "").split(",");
				}
				catch(Exception e) {
					logger.error("Illegal argument passed for type. It must be a string "
							+ "with machine names values separated by comma (e.g. M1,M2,M3,M4) and must have the same "
							+ "dimension of maxmachine array.");
					System.exit(1);
				}
			}
			if (args[i].startsWith("maxmachine=")) {
				try {
					String[] newMaxMachinePerTypeToCast = args[i].replaceAll("maxmachine=", "").split(",");
					maxMachinePerType = new int[newMaxMachinePerTypeToCast.length];
					for (int j=0; j<newMaxMachinePerTypeToCast.length; ++j) {
						maxMachinePerType[j] = Integer.parseInt(newMaxMachinePerTypeToCast[j]);
					}
				}
				catch(Exception e) {
					logger.error(e.getMessage() + ". Illegal argument passed for maxmachine. It must be a string "
							+ "with integer values separated by comma (e.g. 3,2,4,5) and must have the same "
							+ "dimension of type array.");
					System.exit(1);
				}
			}
			if (args[i].startsWith("goalcpu=")){
				try {
					goalCpu = Integer.parseInt(args[i].replaceFirst("goalcpu=",""));
				}
				catch(NumberFormatException e) {
					System.err.println(e.getMessage() + ". Illegal argument passed for goalcpu. It must be an integer. ");
					System.exit(1);
				}
			}
			if (args[i].startsWith("penalization=")){
				try {
					penalizationFactor = Integer.parseInt(args[i].replaceFirst("penalization=",""));
				}
				catch(NumberFormatException e) {
					logger.error(e.getMessage() + ". Illegal argument passed for penalization. It must be an integer. ");
					System.exit(1);
				}
			}
			if (args[i].startsWith("onlygoal=")){
				try {
					onlyGoalState = Boolean.parseBoolean(args[i].replaceFirst("onlygoal=",""));
				}
				catch(Exception e) {
					logger.error(e.getMessage() + ". Illegal argument passed for onlygoal. It must be a boolean.");
					System.exit(1);
				}
			}	
		}
		
		// check if the type array and maxMachinePerType has the same length
		if (types.length != maxMachinePerType.length) {
			logger.error("The array types and maxMachinePerTypes must have the same length!");
			System.exit(1);
		}
		
		// parameters visualization
		System.out.println("Starting with the following parameters:\n"
				+ "- types = " + arrayToString(types) + "\n"
				+ "- goalcpu = " + goalCpu + "\n"
				+ "- penalization = " + penalizationFactor + "\n"
				+ "- onlygoal = " + onlyGoalState);
		
		// build the states with the given types and maxMachinePerTypes arrays
		List<State> states = buildStates(types, maxMachinePerType);
		logger.info("Built " + states.size() +" states.");
		
		// build a map that have a computational power (CPU) associated with the list of all state with that CPU
		Map<Integer, List<State>> cpuValuesStateMap = new HashMap<Integer, List<State>>();
		
		// build a map that gieven a State has a List of all possible outgoing state, i.e the actions
		Map<State, List<State>> actions = new HashMap<State, List<State>>();
		
		// populate the maps
		for (int i=0; i<states.size(); ++i) {
			
			State currentState = states.get(i);
			int currentCpu = currentState.getCpu();
			actions.put(currentState, new ArrayList<State>());
			
			// insert current state in the map <CPU, List<State>>
			if ( !cpuValuesStateMap.containsKey(currentCpu) ) {
				List<State> currentStateList = new ArrayList<State>();
				currentStateList.add(currentState);
				cpuValuesStateMap.put(currentCpu, currentStateList);
				logger.debug("found new cpu key: " + currentCpu);
			}
			else {
				List<State> currentStateList = cpuValuesStateMap.get(currentCpu);
				currentStateList.add(currentState);
				cpuValuesStateMap.put(currentCpu, currentStateList);
			}
			
			// insert actions for each near state
			for (int j=0; j<states.size(); ++j) {
				// link 2 states only if they are nears
				if (areStateNear(states.get(i), states.get(j))) {
					List<State> currentStateActionsList = actions.get(currentState);
					currentStateActionsList.add(states.get(j));
					actions.put(currentState, currentStateActionsList);
					logger.debug("Inserted Action: "+states.get(i).getStateId() + "->" + states.get(j));
				}	
			}
		}
		
		// print for each cpu values the list of the states
		for (Integer i : cpuValuesStateMap.keySet()) {
			List<State> iState = cpuValuesStateMap.get(i);
			logger.debug("States for cpu " + i);
			for (State s : iState) {
				logger.debug("State " + s.getStateId());
			}
		}
		
		// create qlearning and execute
		long begin = System.currentTimeMillis();
	    
		QLearning obj = new QLearning(states, cpuValuesStateMap, actions, goalCpu, penalizationFactor, onlyGoalState);
	    obj.run();
	    obj.printResult();
	    obj.showPolicy();
	    
	    long end = System.currentTimeMillis();
	    logger.info("Time: " + (end - begin) / 1000.0 + " sec.");
	    
	}	
	
}
