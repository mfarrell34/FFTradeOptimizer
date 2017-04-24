package tradeOptimizer.projections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tradeOptimizer.league.Position;
/*
 * Class representing a week in the NFL season, holds data containing projected point values for players playing in the given week
 */
public class WeekProjections {

	Map<Integer,Double> playerProjections; //maps player Id to the player's projected point total for the week
	//List<Player> playersToUse;
	//List<PlayerProjection> projectionsToUse;
	Map<Position,Double> topWaiverValueForPosition; //maps each position to the point value of the highest projected player available on waivers
	//Map<Integer,String> playerPositionMap;
	private int weekNumber; //week number in NFL season for each instance of class
	private boolean projectionsSet = false; //indicates whether projection data has been set for the WeekProjections instance
	
	/*
	 * Constructor for WeekProjections, only week number is stored initially as data will be added when projections are parsed
	 */
	public WeekProjections(int weekNum) {
		topWaiverValueForPosition = new HashMap<Position,Double>();
		this.weekNumber = weekNum;
		//playersToUse = new ArrayList<Player>();
	}
	
	/*
	 * Accessor methods
	 */
	public int getWeekNum() {
		return this.weekNumber;
	}

	public boolean getProjectionsSet() {
		return this.projectionsSet;
	}
	
	public Map<Position,Double> getTopWaiverForPositions() {
		return this.topWaiverValueForPosition;
	}
	
	/*
	 * Method to populate the playerId:projected points map for the given week
	 */
	public void addProjectionsForWeek(Map<Integer, Double> projections) {
		this.playerProjections = projections;
		projectionsSet = true;
	}
	
	/*
	 * Adds top projected point total for a given position if none exists yet for the current week
	 * Projection parsing will read projections from highest value to lowest and may not know whether
	 * a given position has already been populated for the week
	 */
	public void tryAddTopWaiverPositionValue(Double value, Position position) {
		if (!topWaiverValueForPosition.containsKey(position)) {
			topWaiverValueForPosition.put(position,value);
			//System.out.println("Adding top " + position + " " + String.valueOf(value));
		}
	}
	
	
	/*public void addPlayerPositionsMap(HashMap<Integer,String> playerPositions) {
		this.playerPositionMap = playerPositions;
	} */
	
	/*
	 * Method getPlayersToUse returns a sorted list of PlayerProjections
	 * List is sorted highest to lowest by each Player's projected points for the WeekProjections instance
	 * Argument is a list of Players that represents the players in an instance of a Team, this could be before or after
	 * a team's players have been updated by a Trade
	 */
	public List<PlayerProjection> getPlayersToUse(List<Integer> players) {
		List<PlayerProjection> addedPlayers = new ArrayList<PlayerProjection>();
		// Iterate through list of players and add PlayerProjection to list
		// If playerProjections map contains the playerId then we have a projected points value, use this to create the PlayerProjection
		// if playerId isn't in map then we don't have a projected point value for the player (could be injured or on bye week etc.) so use 0.0
		for (int playerId : players) {
			Double value;
			if (playerProjections.containsKey(playerId)) {
			    value = playerProjections.get(playerId);
			} else {
				value = 0.0;
			}
			addedPlayers.add(new PlayerProjection(playerId, value));
		}
		Collections.sort(addedPlayers);
		return addedPlayers;
	}

}
