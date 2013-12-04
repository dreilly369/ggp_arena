package org.ggp.base.player.gamer.statemachine.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javassist.tools.rmi.ObjectNotFoundException;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;


public class GameGraphBFS {
	private ArrayList<Integer> visited;//key is the hash value of a state and true means that state has been visited
	private HashMap<Integer, Integer> edgeTo;//Key is States hash value is hash of preceding state.
	private GameGraph base;
	private List<Integer> frontier;//Hash to explore next
	private HashMap<Integer,MachineState> stateHashMap;//Look-up table for reversing the hash
	private StateMachine theMachine;
	private ArrayList<Integer> terminalStates = new ArrayList<Integer>();
	private Role playingAs;
	private long finishBy;


	public GameGraphBFS(StateMachine machine, Role role, long finish){
		base = new GameGraph(0);
		frontier = new ArrayList<Integer>();
		visited = new ArrayList<Integer>();
		edgeTo = new HashMap<Integer,Integer>();
		theMachine = machine;
		playingAs = role;
		finishBy = finish;
		try {
			this.buildGraph();
		} catch (MoveDefinitionException | TransitionDefinitionException | GoalDefinitionException e) {
			e.printStackTrace();
		}
	}

	private void buildGraph() throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		System.out.println("Building Graph");
		MachineState start = theMachine.getInitialState();
		frontier.add(start.hashCode());
		stateHashMap = new HashMap<Integer,MachineState>();
		stateHashMap.put(start.hashCode(), start);
		base.addVertex(start.hashCode());
		while(!frontier.isEmpty() && System.currentTimeMillis() < finishBy){
			int at = frontier.get(0);
			visited.add(at);
			if(theMachine.isTerminal(stateHashMap.get(at))){
				System.out.println("Found Terminal");
				HashMap<Integer, ArrayList<Integer>> termStateToVal = new HashMap<Integer,ArrayList<Integer>>();
				ArrayList<Integer> val = (ArrayList<Integer>) theMachine.getGoals(stateHashMap.get(at));
				termStateToVal.put(at, val);
			}
			List<MachineState> nexts = theMachine.getNextStates(stateHashMap.get(at));
			System.out.println(nexts.size() + " connected states");
			for(MachineState v : nexts){
				stateHashMap.put(v.hashCode(), v);
				frontier.add(v.hashCode());
				edgeTo.put(v.hashCode(), at);
				base.addEdge(v.hashCode(), at);
			}
			frontier.remove(0);
			System.out.println("Finshed Expanding nodes");
		}
	}

	public ArrayList<MachineState> knownTerminalStates(boolean onlyWins) throws ObjectNotFoundException{
		ArrayList<MachineState> states = new ArrayList<MachineState>();
		Iterator<Integer> list = terminalStates.iterator();
		while(list.hasNext()){
			int n = list.next();
			MachineState st = stateHashMap.get(n);
			if(onlyWins){
				//calculate if playingAs has the highest utility in this state
				if(!theMachine.isTerminal(st)){
					System.out.println("Something went wrong captain.");
					throw new ObjectNotFoundException(st.toString());
				}
				try {
					if(theMachine.getGoal(st, playingAs) == maxGoal(st)){
						states.add(st);
					}
				} catch (GoalDefinitionException e) {
					System.out.println("POORLY DEFINED GOAL \n" + st.toString());
				}
			}else{
				states.add(st);
			}
		}
		return null;
	}

	public ArrayList<Move> pathTo(int start, int goal){
		ArrayList<Integer> currFrontier = new ArrayList<Integer>();
		ArrayList<Integer> vstd = new ArrayList<Integer>();
		ArrayList<Move> pathToFollow = new ArrayList<Move>();
		currFrontier.add(start);

		while(!currFrontier.isEmpty()){
			int at = currFrontier.get(0);
			ArrayList<Integer> adj = base.getAdj(at);
			if(adj.contains(goal)){

			}
			for(int next : adj){
				if(!currFrontier.contains(next)){
					currFrontier.add(next);
				}
			}
		}

		return null;
	}

	private int maxGoal(MachineState terminalState) throws GoalDefinitionException {
		StateMachine sm = theMachine;
		List<Integer> goals = sm.getGoals(terminalState);
		int max = 0;
		for(int score : goals){
			if(score > max){
				max = score;
			}
		}
		return max;
	}

}
