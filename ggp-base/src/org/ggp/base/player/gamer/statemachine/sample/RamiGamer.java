package org.ggp.base.player.gamer.statemachine.sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.exception.StoppingException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.GdlTerm;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

/**
 * Currently Based off of:"
 * SampleSearchLightGamer is a simple state-machine-based Gamer. It will,
 * to the best of its ability, never pick a move which will give its opponent
 * a possible one-move win. It will also spend up to two seconds looking for
 * one-move wins it can take. This makes it slightly more challenging than the
 * RandomGamer, while still playing reasonably fast.
 *
 * Essentially, it has a one-move search-light that it shines out, allowing it
 * to avoid moves that are immediately terrible, and also choose moves that are
 * immediately excellent.
 *
 * This approach implicitly assumes that it is playing an alternating-play game,
 * which is not always true. It will play simultaneous-action games less well.
 * It also assumes that it is playing a zero-sum game, where its opponent will
 * always force it to lose if given that option.
 *
 * This player is fairly good at games like Tic-Tac-Toe, Knight Fight, and Connect Four.
 * This player is pretty terrible at most games."
 * Originally Written by: S. Schrieber
 *
 * Future iterations will see the addition of Monte-Carlo RBBSTrees for +2 player games, a
 * time-bounded sequential planner for puzzle type games, addition of a database for storing
 * and analyzing match data, and additional support for Weka Data Mining algorithms.
 *
 * @author Daniel Reilly
 */
public final class RamiGamer extends StateMachineGamer
{

	private Connection con;

	//Database Stuff
	private static String TABLE_PREFIX = "rami_ggp_";
	private String url = "jdbc:mysql://localhost:3306/core_db";
	private String user = "root";
	private String password = "pirates";

	//Match Variables
	private int numberPlayers = 0;
	private boolean fixedSum = true;
	private boolean zeroSum = true;
	private String matchId = "";
	private int turnOrder = 0;//In sequential play, what turn number is the player.
	private int turnNumber = 0; //How many turns have passed.

	private int[] depth;
	private GameTree<Integer, Integer> tree;
	private GameGraphBFS graph;

	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		println("Meta-gaming");
		StateMachine sm = getStateMachine();
		this.numberPlayers = sm.getRoleIndices().size();
		this.matchId = getMatch().getMatchId();
		this.turnOrder = sm.getRoleIndices().get(getRole());
		this.insertNewMatch();
		this.tree = new GameTree<Integer, Integer>();
		this.tree.put(getCurrentState().hashCode(), 1);//Add the initial state to the tree
		this.graph = new GameGraphBFS(sm, getRole(), timeout-100);
	}

	private void insertNewMatch() {
		Connection con = null;
		Statement st = null;
		int rs = 0;

		try {
			this.con = DriverManager.getConnection(url, user, password);
			st = this.con.createStatement();
			String desc = getMatch().getGame().getDescription();
			String stmnt = "INSERT INTO " + TABLE_PREFIX + "matches (match_id, player_count, description, playing_as) VALUES ('"+this.matchId +"','"+this.numberPlayers+"','" + desc + "','" + this.turnOrder + "');";
			println("SQLing:\n" + stmnt);
			rs = st.executeUpdate(stmnt);
			println("Done adding Match\n");
		} catch (SQLException ex) {
			println("FAILED CONNECTION: \n");
			ex.printStackTrace();
		} finally {
			//Nothing Yet
		}
	}

	private void insertNewMove(Move theMove ){
		StateMachine theMachine = getStateMachine();
		List<GdlTerm> lastMoves = getMatch().getMostRecentMoves();
		lastMoves.add(theMove.getContents());
		Statement st = null;
		int rs = 0;

		try {
			this.con = DriverManager.getConnection(url, user, password);
			st = this.con.createStatement();
			String stmnt = "INSERT INTO " + TABLE_PREFIX + "moves (match_id, player_id, starting_state, move) VALUES ('"+this.matchId+"','"+this.numberPlayers+"','nfn');";
			println("SQLing:\n" + stmnt);
			rs = st.executeUpdate(stmnt);
			println("Done adding Match\n");
		} catch (SQLException ex) {
			println("FAILED CONNECTION: \n");
			ex.printStackTrace();
		} finally {
			//Nothing Yet
		}
	}

	public void addStep(List<Move> moves){
		//Nothing yet
	}

	private Random theRandom = new Random();

	/**
	 * Employs a simple sample "Search Light" algorithm.  First selects a default legal move.
	 * It then iterates through all of the legal moves in random order, updating the current move selection
	 * using the following criteria.
	 * <ol>
	 * 	<li> If a move produces a 1 step victory (given a random joint action) select it </li>
	 * 	<li> If a move produces a 1 step loss avoid it </li>
	 * 	<li> If a move allows a 2 step forced loss avoid it </li>
	 * 	<li> Otherwise select the move </li>
	 * </ol>
	 */
	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		long startedAt = time();
		println("Selecting Move");
		StateMachine sm = getStateMachine();
		List<Move> moves = sm.getLegalMoves(getCurrentState(), getRole());
		Move choice = null;
		if(moves.size()>1){
			//TODO Expand this section to choose different play styles based on game type
			//and match variables. Example. Play a puzzle like a tree, and multiplayer games like monte-carlo
			if(numberPlayers > 1){
				choice = monteCarloForMove(timeout-250, getRole());
			}else{
				//Add a different move selector here for each type of play supported
				choice = monteCarloForMove(timeout-250, getRole());
			}
		}else{
			println("One Move: " + moves.get(0).toString());
			choice = moves.get(0);
		}
		long stop = time();
		notifyObservers(new GamerSelectedMoveEvent(moves, choice, stop - startedAt));
		return choice;
	}

	public Move monteCarloForMove(long timeout, Role role) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		long start = time();
		Move selection = monteCarloFromState(timeout-100, role, getCurrentState());
		long stop = time();

		long took = (stop - start)/1000;
		println("Chose: \n\t" + selection.toString() + "\n in " + took +" seconds\n");
		return selection;
	}

	private int evalStateCompetitive(MachineState theState, long timeout) {
		int ret = 0;
		try {
			ret = maxscore(getRole(), theState, 0, 100, timeout);
		} catch (MoveDefinitionException | TransitionDefinitionException
				| GoalDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		println("Eval of state: " + ret);
		return ret;
	}

	public MachineState depthLimitedCharge(MachineState state, int maxDepth, final int[] theDepth) throws TransitionDefinitionException, MoveDefinitionException {
		int nDepth = 0;
		StateMachine sm = getStateMachine();
		while(!sm.isTerminal(state) && nDepth <= maxDepth) {
			nDepth++;
			state = sm.getNextStateDestructively(state, sm.getRandomJointMove(state));
		}
		if(theDepth != null){
			theDepth[0] = nDepth;
		}

		return state;
	}

	public Move monteCarloFromState(long timeout, Role role, MachineState state) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		StateMachine theMachine = getStateMachine();
		long finishBy = timeout-100;

		List<Move> moves = theMachine.getLegalMoves(state, role);
		List<Move> cleanMoves = winLoseOrDraw(moves);
		if(cleanMoves.size()>0){
			moves = cleanMoves;
		}

		Move selection = moves.get(0);
		if (moves.size() > 1) {
			// Perform depth charges for each candidate move, and keep track
			// of the total score and total attempts accumulated for each move.
			Move currBest = moves.get(0);
			MachineState theState = theMachine.getRandomNextState(state,role, currBest);//Boo random selection
			long finshBy = timeout - 500;
			double bestScore = evalStateCompetitive(theState,finshBy-500);

			for (int i = 1; i < moves.size();i++) {
				if (time() >= finishBy){
					break;
				}
				int total = 0;
				int tries = 5;

				for(int j = 0; j < tries; j++){
					if (time() >= finishBy){
						break;
					}
					MachineState nextState = theMachine.getRandomNextState(state,role, moves.get(i));//Boo random selection
					total += evalStateCompetitive(nextState,finishBy-500);
				}
				double avg = total/tries;
				if(avg >= bestScore){
					bestScore = avg;
					currBest = moves.get(i);
					println("New best move with score: " + bestScore);
				}
			}

			println(bestScore + " Estimated Utility for selected move");
			selection = currBest;
		}
		return selection;
	}

	//Removes moves which leave an immediate loss on the table and looks for moves with wins
	//in all proceeding states.
	private List<Move> winLoseOrDraw(List<Move> moves) throws GoalDefinitionException, MoveDefinitionException, TransitionDefinitionException {
		List<Move> safetied = moves.subList(0, 0);
		StateMachine theMachine = getStateMachine();
		Map<Move, List<MachineState>> states_if_taken = theMachine.getNextStates(getCurrentState(), getRole());

		for(int i = 0; i<moves.size();i++){
			//println("Considering move number: " + i);
			Move trying = moves.get(i);
			//Pretend we took this move with random opponents
			List<MachineState> alt_play_model = states_if_taken.get(moves.get(i));

			int winCount = 0;
			int numStates = alt_play_model.size();
			println( alt_play_model.size() + " states possible");

			for(MachineState possible : alt_play_model){
				if(theMachine.isTerminal(possible)){
					if(theMachine.getGoal(possible, getRole()) < maxGoal(possible)){
						println("Removed hazardous move from list.");
						break;
					}
					if(theMachine.getGoal(possible, getRole()) == maxGoal(possible)){
						println("Found a winning state.");
						winCount++;
					}
					safetied.add(trying);
				}
			}

			if(winCount == numStates){
				safetied.clear();
				safetied.add(trying);
				return safetied;
			}
		}
		println(safetied.size() + " safe moves");
		return safetied;
	}

	private void println(String string) {
		System.out.println(string);
	}

	private int maxGoal(MachineState terminalState) throws GoalDefinitionException {
		StateMachine sm = getStateMachine();
		List<Integer> goals = sm.getGoals(terminalState);
		int max = 0;
		for(int score : goals){
			if(score > max){
				max = score;
			}
		}
		//println("Max Goal: " + max + " for state " + terminalState.toString());
		return max;
	}

	private int minscore (Role role, Move action, MachineState state, int alpha, int beta, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		StateMachine sm = getStateMachine();
		List<List<Move>> actions = sm.getLegalJointMoves(state, role, action);
		for(int i=0;i<actions.size();i++){
			if(time() >= timeout){
				println("ran out of time in minscore");
				break;
			}
			List<Move> moves = actions.get(i);
			MachineState newstate = sm.getNextState(state, moves);
			beta = min(beta,maxscore(role,newstate,alpha,beta,timeout));
			println("min(" + beta+", maxscore("+role+",...,"+alpha+","+beta+","+timeout+"))");
			if(beta<=alpha){
				println("\tpruning because " + alpha + " <= " + beta);
				return alpha;
			}
		}
		println("minscore returning: " + beta);
		return beta;
	}

	private int maxscore (Role role, MachineState state, int alpha, int beta, long timeout) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException{
		StateMachine sm = getStateMachine();

		if (sm.isTerminal(state)) {
			println("maxscore reached terminal");
			return sm.getGoal(state, role);
		}
		List<Move> actions = sm.getLegalMoves(state, role);
		for (int i=0; i<actions.size(); i++){
			if(time() >= timeout){
				println("ran out of time in maxscore");
				break;
			}
			println("max(" + alpha+", minscore("+role+",...,"+alpha+","+beta+","+timeout+"))");
			alpha = max(alpha,minscore(role,actions.get(i),state,alpha,beta,timeout));
			if (alpha>=beta){
	        	println("\tpruning because " + alpha + " >= " + beta);
	        	return beta;
	        }
		}
		println("maxscore returning " + alpha);
		return alpha;
	}

	private long time() {
		// TODO Auto-generated method stub
		return System.currentTimeMillis();
	}


	private int min(int alpha, int beta) {
		// Returns the minimum of two values
		if(alpha<=beta){
			return alpha;
		}
		return beta;
	}

	private int max(int alpha, int beta) {
		// Returns the max of two values
		if(alpha >= beta){
			return alpha;
		}
		return beta;
	}

	@Override
	public void stateMachineStop() {

		if (this.con != null) {
			try {
				this.con.close();
				this.con = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Uses a CachedProverStateMachine
	 */
	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public String getName() {
		return "Rami";
	}

	@Override
	public DetailPanel getDetailPanel() {
		return new SimpleDetailPanel();
	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		println("Previewing");
	}

	@Override
	public void stateMachineAbort() {
		try {
			this.stop();
		} catch (StoppingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}