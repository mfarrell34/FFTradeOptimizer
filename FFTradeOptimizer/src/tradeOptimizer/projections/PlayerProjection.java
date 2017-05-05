package tradeOptimizer.projections;

import java.util.ArrayList;
import java.util.List;

import tradeOptimizer.league.Player;
import tradeOptimizer.league.Position;
import tradeOptimizer.league.FantasyLeague;
import tradeOptimizer.projections.PlayerProjection;

/*
 * Helper class for sorting players by projected points for the week
 */

public class PlayerProjection implements Comparable<Object>{
	private int thisPlayerId;
	private Double projection;
	
	/*
	 * Constructor method to initialize PlayerProjection, arguments are Player and projected amount of points for the week
	 */
	public PlayerProjection(int playerId, Double projection) {
		this.thisPlayerId = playerId;
		this.projection = projection;
	}
	
	/*
	 * Accessor methods
	 */
	public Player getPlayer() {
		return FantasyLeague.getPlayerById(thisPlayerId);
	}
	
	public Double getProjection() {
		return projection;
	}
	
	public int getPlayerId() {
		return thisPlayerId;
	}
	
	public List<Position> getFootballPosition() {
		if (FantasyLeague.isValidPlayer(thisPlayerId)) {
			return FantasyLeague.getPlayerById(thisPlayerId).getFootballPositions();
		} else {
			return new ArrayList<Position>();
		}
	}
	
	/*
	 * Override of compareTo to allow sorting Players by projected points for the week
	 */
	
	@Override
	public int compareTo(Object arg0) {
		if (arg0 instanceof PlayerProjection) {
			PlayerProjection otherPlayer = (PlayerProjection) arg0;
			return Double.compare(otherPlayer.projection, this.projection);
		} else {
		   return 0;
		}
	}
}
