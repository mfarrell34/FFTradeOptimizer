package tradeOptimizer.league;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;

import tradeOptimizer.league.FantasyLeague;
import tradeOptimizer.trades.Trade;

/*
 * Team class representing a fantasy football team that competes in a fantasy league
 * Team objects can be cloned and the list of Players for the Team instance can be updated on the cloned Team
 * to represent the new Team's players after a trade
 */
public class Team {

	public int largestTrade = 2; //represents maximum number of players in a Trade with this Team, used to format output file
	private List<Integer> currentPlayers; //refer to players by Id
	private String teamName;
	private ArrayList<Trade> trades; //only used for initial Team instances, not for Cloned instances
	private Double currentBaseProjectedPoints = 0.0; //can only be set once, represents projected points for remainder of season with current team
	private List<List<Integer>> playerCombos;
	
	/*
	 * Constructor method used when creating initial Team instance
	 * for each Team in the fantasy league, name of Team is only argument
	 * ArrayList fields also initialized when this constructor is used
	 */
	
	public Team(String teamName) {
		this.teamName = teamName;
		this.currentPlayers = new ArrayList<Integer>();
		this.trades = new ArrayList<Trade>();
	}
	
	/*
	 * Cloning constructor, creates new instance of Team that allows Players on Team
	 * to be updated by Trades without modifying players on original Team instance
	 */
/*    public Team(Team team) {
    	this.teamName = team.getTeamName();
    	List<Integer> currentPlayers = new ArrayList<Integer>();
    	//Clone each Player on Team so current Players list on cloned team won't reference same Player objects as initial Team instance
    	for (int playerId : team.getCurrentPlayers()) {
    		currentPlayers.add(playerId);
    	}
    	this.currentPlayers = currentPlayers;
    	this.receivedPlayers = new ArrayList<Integer>();
    } */
    
    /*
     * Accessor methods
     */
	
	public String getTeamName() {
		return this.teamName;
	}
	
	public List<Trade> getTrades() {
		return this.trades;
	}
	
	public Integer[] getCurrentPlayers() {
		return currentPlayers.toArray(new Integer[currentPlayers.size()]);
	}
	
/*	public List<Integer> getNewPlayers() {
		return this.receivedPlayers;
	} */
	
	public Double getBaseProjectedPoints() {
		return this.currentBaseProjectedPoints;
	}
	
	public void setBaseProjectedPoints(Double points) {
		//should only be set once before any trades are calculated to have a base projection for comparisons
		if (this.currentBaseProjectedPoints == 0.0 && points != 0.0) {
			this.currentBaseProjectedPoints = points;
		}
	}
	
	/*
	 * Method used by league roster data parser to add Player object to the Team
	 */
	public void addPlayer(int player) {
		currentPlayers.add(player);
	}
	
	/*
	 * Method to add Trade to list of optimal Trades to be stored for this team 
	 */
	
	public synchronized void addTrade(Trade trade) {
		int getPlayerSize = trade.getOtherTeamPlayers().size();
		//if number of players received from other Team greater than current largestTrade value, update largestTrade
		if (getPlayerSize > this.largestTrade) {
			this.largestTrade = getPlayerSize;
		}
		this.trades.add(trade);
	}
	
	/*
	 * getTradeCombinations method returns ArrayList containing all possible combinations of Players from Team that can be sent to other Team in a Trade
	 * combination of Players represented by ArrayList of Players
	 */
	
	public List<List<Integer>> getTradeCombinations() {
		synchronized (this) {
		    if (playerCombos == null) {
		        List<List<Integer>> tradeCombos = new ArrayList<List<Integer>>();
		        for (int i = 0; i < currentPlayers.size(); i++ ) {
			        for (int j = i + 1; j < currentPlayers.size(); j++) {
				        tradeCombos.add(new ArrayList<Integer>(Arrays.asList(currentPlayers.get(i), currentPlayers.get(j))));
			        }
			        tradeCombos.add(new ArrayList<Integer>(Arrays.asList(currentPlayers.get(i))));
		        }
		        playerCombos = new ImmutableList.Builder().addAll(tradeCombos).build();
		    }
		}
		return playerCombos;
	}
	
}
