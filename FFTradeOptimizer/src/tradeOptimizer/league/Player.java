package tradeOptimizer.league;

import java.util.Arrays;
import java.util.List;
import java.util.Set;


/*
 * Player class representing an NFL player, will be identified by a unique player Id
 */

public class Player {

	private String fullName;
	private List<Position> footballPositions;
	private int byeWeek = 0; //not currently used for player class but could be useful for other projections or implementations

	/* In Yahoo FF leagues this will represent the yahoo Player ID, 
	 * ID must be unique as it will be used for equality as some players can have identical names (and possibly positions)
	 */
	private Integer playerId; 
	
	/*
	 * Constructor method, arguments:
	 * fullName: String containing First and Last name for Player
	 * position: Position for player
	 * byeWeek: Week number the player has a bye
	 * playerId: Unique integer identifier for player
	 */
	public Player(String fullName, Position position, int playerId, int byeWeek) {
		this(fullName, Arrays.asList(position), playerId, byeWeek);
	}
	
	/*
	 * Alternate constructor without byeWeek argument
	 */
	public Player(String fullName, Position position, int playerId) {
		this(fullName, position, playerId, 0);
	}
	
	/*
	 * Most players only have 1 football position so above 2 constructors will be used
	 * These alternate constructors allow creation of player objects where the player is listed as playing multiple
	 * positions, ie. player is listed as WR/RB because they play both
	 */
	public Player(String fullName, List<Position> positions, int playerId, int byeWeek) {
		this.fullName = fullName;
		this.footballPositions = positions;
		this.byeWeek = byeWeek;
		this.playerId = playerId;
	}
	
	/*
	 * Alternate constructor without byeWeek argument
	 */
	public Player(String fullName, List<Position> positions, int playerId) {
		this(fullName, positions, playerId, 0);
	}
	
	//Cloning constructor, bye week not currently used for player class equality because it won't always be populated
	public Player(Player player) {
		this.fullName = player.getName();
		this.playerId = player.getPlayerId();
		this.footballPositions = player.getFootballPositions();
	}
	
	/* Accessor methods */
	public int getPlayerId() {
		return this.playerId;
	}
	
	public String getName() {
		return this.fullName;
	}

	public List<Position> getFootballPositions() {
		return this.footballPositions;
	}
	
	public int getByeWeek() {
		return this.byeWeek;
	}
	
	/*
	 * Method to determine if Player is able to be used in a LeaguePosition
	 * Checks if Player's Position(s) is allowed to be used in the LeaguePosition
	 */
	public boolean canBeUsedInPosition(LeaguePosition leaguePosition) {
	    for (Position playerPosition : this.footballPositions) {
		    if (leaguePosition.getPossiblePositions().contains(playerPosition)){
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Override equals method to determine player equality using player ID which must be unique
	 */
	@Override
	public boolean equals(Object player) {
		if (player instanceof Player) {
			if (this.playerId == ((Player) player).getPlayerId()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	/*
	 * As with equals method above, player Id is sufficient to use for hashCode as it will be unique
	 */
	@Override
	public int hashCode() {
		return (this.playerId.hashCode());
	}
	
}
