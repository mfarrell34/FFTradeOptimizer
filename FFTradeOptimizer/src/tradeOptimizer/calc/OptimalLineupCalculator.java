package tradeOptimizer.calc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tradeOptimizer.league.LeaguePosition;
import tradeOptimizer.projections.PlayerProjection;

/* Class for calculating the optimal projected points for a given list of player projections and positions to be filled,
 * generally performed on projections for a single week. Uses Kuhn Munkres algorithm (aka Hungarian algorithm) to determine
 * maximum matching in bipartite graph that is built with a set of vertices representing positions and the other set representing
 * player projections. Algorithm complexity is N^3 where N is the maximum between the number of players and positions.
 */
public class OptimalLineupCalculator {

	private double[][] projCostMatrix; //matrix holding weights of bipartite graph edges, rows are positions and columns are player projections
	private double[] labelForPosition; //label values for position vertices
	private double[] labelForPlayer; //label values for player vertices
	private int[] positionForPlayer; //contains position # matched with a player #
	private int[] playerForPosition; //contains player # matched with a position #
    private boolean[] playerInTree; //indicates whether a player has been added to the alternating tree for the current phase of algorithm
    private boolean[] positionInTree; //indicates whether a position has been added to the alternating tree for the current phase
	private double[] slack;  //holds slack values for edges by player # (eg. slack[i] represents the slack for edge between player i and slackPosition[i])
	private int[] slackPosition;  //holds position vertex that is other end of edge represented in slack[] 
	private int[] prevPositionInTree; //holds most recent position added to alternating tree, used to update matchings when alternating path is found
	
	private int matrixDimension; //dimension of square matrix, represents maximum of player projections size and positions size
	private int numPositions;
	private int[] playerIds; //maps player id to projections added to cost matrix for determining which players were used
	
	private final double INF = Double.MAX_VALUE;
	
	public OptimalLineupCalculator(List<PlayerProjection> players, List<LeaguePosition> positions) {
		playerIds = new int[players.size()];
		for (int i = 0; i < players.size(); i++) {
			playerIds[i] = players.get(i).getPlayerId();
		}
		buildCostMatrix(players,positions);
	}
	
	//returns set containing Player Ids of all players that were matched with a position in the
	//optimal week calculation which indicates the player contributed to the projected total
	public Set<Integer> getPlayersUsed() {
		Set<Integer> playersUsed = new HashSet<Integer>();
		//if number of players > number of positions then unused players
		//will be matched with "dummy" positions created ( numPositions <= dummy < matrixDimension )
		for (int i = 0; i < playerIds.length; i++) {
			if (positionForPlayer[i] < numPositions) {
				playersUsed.add(playerIds[i]);
			}
		}
		return playersUsed;
	}
	
	/*
	 * Populates cost matrix for player projections and positions, if a player can be used in a 
	 * position then weight of matrix[position][player] is that player's projected points value,
	 * otherwise weight is set to 0. If number of players and positions isn't equal (this will
	 * generally be the case as typically number of players will be more than number of positions)
	 * then extra "dummy" players or positions are created in order to create a square matrix. These
	 * dummy rows or columns are populated with 0.0 weights, these will still be matched but any player 
	 * or position matched with a dummy player or position isn't being used in the optimal projection.
	 */
	private void buildCostMatrix(List<PlayerProjection> players, List<LeaguePosition> positions) {
		numPositions = positions.size();
		matrixDimension = Math.max(players.size(), numPositions);
		projCostMatrix = new double[matrixDimension][matrixDimension];
		for (int x = 0; x < matrixDimension; x++) {
			for (int y = 0; y < matrixDimension; y++) {
				projCostMatrix[x][y] = getPlayerPositionCost(players,positions,x,y);
			}
		}
	}
	
	/*
	 * Helper method that returns the weight to use in cost matrix for a given (position,player) edge
	 */
	private Double getPlayerPositionCost(List<PlayerProjection> players, List<LeaguePosition> positions, int positionNum, int playerNum) {
		//since there will likely be more players than positions the number of rows and columns won't be equal
		//we add empty "dummy" rows (or columns if necessary) so the matrix will be balanced
		if (positionNum >= positions.size() || playerNum >= players.size()) {
			return 0.0;
		}
		if (players.get(playerNum).getPlayer().canBeUsedInPosition(positions.get(positionNum))) {
			return players.get(playerNum).getProjection();
		} else {
			return 0.0;
		}
	}
	/*
	 * Initialize fields, setup labels, then run algorithm until maximum matching is calculated.
	 * When maximum matching is found, add up projected point values for all filled positions and
	 * return this value.
	 */
	public Double getOptimalProjPoints() {
		positionForPlayer = new int[matrixDimension];
		playerForPosition = new int[matrixDimension];
		Arrays.fill(positionForPlayer, -1);
		Arrays.fill(playerForPosition, -1);
		slack = new double[matrixDimension];
		slackPosition = new int[matrixDimension];
		playerInTree = new boolean[matrixDimension];
		positionInTree = new boolean[matrixDimension];
		prevPositionInTree = new int[matrixDimension];
		
		setupLabels();

		getInitialMatching();

		runHungarian();
		
		Double projPoints = 0.0;
		for (int x = 0; x < numPositions; x++) {
			projPoints += projCostMatrix[x][playerForPosition[x]];
		}
		return projPoints;
	}
	
	/*
	 * Calculate initial feasible labeling, player labels set to 0.0 and position
	 * labels are set to the value of the maximum edge connected to the position.
	 */
	private void setupLabels() {
		labelForPosition = new double[matrixDimension];
		labelForPlayer = new double[matrixDimension];
		Arrays.fill(labelForPlayer, 0.0);
		for (int x = 0; x < matrixDimension; x++) {
			for (int y = 0; y < matrixDimension; y++) {
				labelForPosition[x] = Math.max(labelForPosition[x], projCostMatrix[x][y]);
			}
		}
	}
	
	/*
	 * Initialize a new phase in the algorithm for the unmatched position pos, which
	 * is also the root vertex of the alternating tree that will be built. 
	 */
	private void setupPhase(int pos) {
		Arrays.fill(positionInTree, false);
		Arrays.fill(playerInTree, false);
		Arrays.fill(prevPositionInTree, -1);
		positionInTree[pos] = true;
		prevPositionInTree[pos] = -2;
		for (int y = 0; y < matrixDimension; y++) {
			slack[y] = edgeSlack(pos,y);
			slackPosition[y] = pos;
		}
	}
	
	/*
	 * Generates initial matching by greedily matching positions with the maximum weighted player for the position
	 * if the player hasn't already been matched to another position. Improves performance by reducing the number
	 * of positions that need to be matched by finding augmenting paths in the algorithm.
	 */
	private void getInitialMatching() {
		for (int x = 0; x < matrixDimension; x++) {
			for (int y = 0; y < matrixDimension; y++) {
				if (playerForPosition[x] == -1 && positionForPlayer[y] == -1 &&
						edgeSlack(x,y) == 0) {
					playerForPosition[x] = y;
					positionForPlayer[y] = x;
				}
			}
		}
	}
	
	/*
	 * Update labels with alpha, which is equal to the minimum edge slack for edges where
	 * the player vertex is not currently in the alternating tree.
	 */
	private void updateLabels() {
		int x;
		double alpha = INF;
		for (x = 0; x < matrixDimension; x++) {
			if (!playerInTree[x]) {
				alpha = Math.min(alpha, slack[x]);
			}
		}
		for (x = 0; x < matrixDimension; x++) {
			if (positionInTree[x]) {
				labelForPosition[x] -= alpha;
			}
			if (playerInTree[x]) {
				//player is vertex in T so update label
				labelForPlayer[x] += alpha;
			} else {
				slack[x] -= alpha;
			}
		}
	}
	
	/*
	 * Helper method to return slack value for a (position,player) edge.
	 */
	private double edgeSlack(int position, int player) {
		return labelForPosition[position] + labelForPlayer[player] - projCostMatrix[position][player];
	}
	
	/*
	 * Run a single phase of the algorithm. If all positions are matched then we have a maximum matching so return, else
	 * find an unmatched position and setup the phase with this position as the root vertex of the alternating tree. Each phase
	 * will add edges to alternating tree until an augmenting path is found, if none is found after a single iteration over the
	 * edges in equality subgraph then we update the labels and repeat until alternating path is found.
	 */
	private void runHungarian() {
		int pos = getUnmatchedPosition();
		if (pos == matrixDimension) {
			return;
		}
		setupPhase(pos);
		List<Integer> queue = new ArrayList<Integer>();
		int readPos = 0;
		queue.add(pos);
		int x = 0;
		int y = 0;
		while (true) {
			while (readPos < queue.size()) {
			    x = queue.get(readPos++);
			    for (y = 0; y < matrixDimension; y++) {
			    	if (!playerInTree[y] && edgeSlack(x,y) == 0) {
			    		int position = positionForPlayer[y];
			    		if ( position == -1) {
			    			break;
			    		} else {
			    		    playerInTree[y] = true;
			    		    queue.add(position);
			    		    positionInTree[position] = true;
			    		    prevPositionInTree[position] = x;
			    		    for (int p = 0; p < matrixDimension; p++) {
			    		    	if (edgeSlack(position,p) < slack[p]) {
			    		    		slack[p] = edgeSlack(position,p);
			    		    		slackPosition[p] = position;
			    		    	}
			    		    }
			    		}
			    	}
			    }
			    if (y < matrixDimension) {
			    	break;
			    }
			}
			if (y < matrixDimension) {
				break;
			}
			//no augmenting path found, update labels and check slack for edges again
			updateLabels();
			
			readPos = 0;
			queue = new ArrayList<Integer>();
			
			for (y = 0; y < matrixDimension; y++) {
				if (!playerInTree[y] && slack[y] == 0) {
					int position = positionForPlayer[y];
					if ( position == -1) {
						x = slackPosition[y];
						break;
					} else {
						playerInTree[y] = true;
						if (!positionInTree[position]) {
							queue.add(position);
							readPos++;
							positionInTree[position] = true;
							prevPositionInTree[position] = slackPosition[y];
							for (int p = 0; p < matrixDimension; p++) {
								if (edgeSlack(position,p) < slack[p]) {
									slack[p] = edgeSlack(position,p);
									slackPosition[p] = position;
								}
							}
						}
					}
				}
			}
			if ( y < matrixDimension) {
				break;
			}
		}
		//augmenting path has been found, now update matchings by reversing the edges in augmenting path
		if (y < matrixDimension) {
		    for (int currentPos = x, currentPlayer = y, tempPlayer; currentPos != -2; currentPos = prevPositionInTree[currentPos], currentPlayer = tempPlayer)
		    {
		         tempPlayer = playerForPosition[currentPos];
		         positionForPlayer[currentPlayer] = currentPos;
		         playerForPosition[currentPos] = currentPlayer;
		    }
		    //after matchings updated using augmenting path, run another phase of algorithm
		    runHungarian();
		}
	}

	/*
	 * Helper method to return next position that isn't currently matched
	 * with a player.
	 */
	private int getUnmatchedPosition() {
		int x;
		for (x = 0; x < matrixDimension; x++) {
			if (playerForPosition[x] == -1) {
				return x;
			}
		}
		return x;
	}

}
