package tradeOptimizer.calc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import tradeOptimizer.league.FantasyLeague;
import tradeOptimizer.league.LeaguePosition;
import tradeOptimizer.projections.PlayerProjection;



/*
 * Class to check if all positions can be filled by a player with a non-zero projection for a given week. Even
 * if a player is eligible to play a given position the player may not necessarily be able to be used in the position
 * (the projection value for the week could be 0.0 for the player due to being on a bye week or injury etc). Uses
 * Hopcroft-Karp algorithm to find maximum number of positions that can be matched to players.
 */
public class PositionsFillableChecker {

	private final List<LeaguePosition> positions;
	private final List<PlayerProjection> players;
	private final int numPlayers;
	private final int numPositions;
	
	private final int NIL = 0;
	private final int INF = Integer.MAX_VALUE;
	int[] playerForPosition;
	int[] positionForPlayer;
	int[] dist;
	List<Integer>[] edges;
	
	public PositionsFillableChecker(List<PlayerProjection> players) {
		this.positions = FantasyLeague.getPositions();
		this.players = players;
		numPlayers = players.size();
		numPositions = positions.size();
		playerForPosition = new int[numPositions + 1];
		positionForPlayer = new int[numPlayers + 1];
		dist = new int[numPositions + 1];
		Arrays.fill(playerForPosition, NIL);
		Arrays.fill(positionForPlayer, NIL);
		edges = new List[numPositions + 1];
		for (int u = 1; u <= numPositions; u++) {
			edges[u] = new ArrayList<Integer>();
			for (int v = 0; v < numPlayers; v++) {
				if ( players.get(v).getProjection() > 0.0 &&
					players.get(v).getPlayer().canBeUsedInPosition(positions.get(u - 1))) {
					edges[u].add(v + 1);
				}
			}
		}
	}
	
	/*
	 * Main method for class, runs algorithm and returns true if all positions have been
	 * filled, false otherwise
	 */
	public boolean allPositionsFilled() {
		int numFilled = hopcroftKarp();
		//null vertex position is added which is filled as well so 
		//all positions are filled if numFilled is equal to numPositions + 1
		return (numFilled == numPositions + 1);
	}
	
	/*
	 * Method that returns a list of all positions that were unable to be filled, allows calling
	 * class to add additional players (generally available players from the waiver) in order to fill
	 * empty positions. Should only be called after allPositionsFilled() is called, while it should
	 * only be needed when allPositionsFilled() returns false, will return an empty list when all positions
	 * have been filled which indicates there are no empty positions (same as allPositionsFilled() == true).
	 */
	public List<LeaguePosition> getUnfilledPositions() {
		List<LeaguePosition> unfilledPositions = new ArrayList<LeaguePosition>();
		for (int i = 1; i <= numPositions ; i++) {
			if (playerForPosition[i] == NIL) {
				unfilledPositions.add(positions.get(i - 1));
			}
		}
		return unfilledPositions;
	}
	
	//Hopcroft-Karp algorithm implementation to find maximum matching, left side vertices represent
	///combination positions to be matched with players that can play the position. If every combination
	//position can have one player with a non-zero projection assigned to it, then 
	//the number of matchings will equal the number of combination positions.
	private int hopcroftKarp() {

		int numPositionsFilled = 0;
		
		while (bfs()) {
			for (int u = 0; u <= numPositions; u++) {
				if (playerForPosition[u] == NIL && dfs(u)) {
					numPositionsFilled++;
				}
			}
		}
		
		return numPositionsFilled;
	}
	
	/*
	 * Breadth first search 
	 */
	private boolean bfs() {
		Queue<Integer> queue = new LinkedList();
		//0 is null vertex so ignore
		for (int u = 1; u <=positions.size(); u++ ) {
			if (playerForPosition[u] == NIL) {
				dist[u] = 0;
				queue.add(u);
			} else {
				dist[u] = INF;
			}
		}
		dist[NIL] = INF;
		
		while (!queue.isEmpty()) {
			int u = queue.poll();
			if (dist[u] < dist[NIL]) {
				for (int v : edges[u]) {
					if (dist[positionForPlayer[v]] == INF) {
						dist[positionForPlayer[v]] = dist[u] + 1;
						queue.add(positionForPlayer[v]);
					}
				}
			}
		}
		
		return dist[NIL] != INF;
	}
	
	/*
	 * Depth first search starting with root vertex u.
	 */
	private boolean dfs(int u) {
		if (u != NIL) {
			for (int v : edges[u]) {
				if (dist[positionForPlayer[v]] == dist[u] + 1) {
					if (dfs(positionForPlayer[v]) == true) {
						positionForPlayer[v] = u;
						playerForPosition[u] = v;
						return true;
					}
				}
			}
			dist[u] = INF;
			return false;
		}
		return true;
	}
	
}
