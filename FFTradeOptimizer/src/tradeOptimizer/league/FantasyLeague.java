package tradeOptimizer.league;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import tradeOptimizer.league.data.LeagueDataSource;
import tradeOptimizer.projections.ProjectionDataSource;
import tradeOptimizer.projections.WeekProjections;

//Static class for holding global data related to the fantasy league including
//teams and players in the league, projection data, and available waiver players

public class FantasyLeague {

	private static List<LeaguePosition> positions;
	private static Map<Position, Player> bestAvailablePlayersByPosition;
	private static Map<Integer,Player> playersById;
	private static List<Team> teams;
	private static List<WeekProjections> weeks;
	private static String leagueName;
	private static int currentWeek;
	public static int tradesPrinted = 0;
	public static boolean printTrades = false;
	public static PrintWriter tradesfile;
	private static boolean leagueInitialized = false;
	
	private FantasyLeague(){}
	
	public static void setupLeague(LeagueDataSource leagueData, ProjectionDataSource projData) {
		if (!leagueInitialized) {
			Map<Integer,Player> playersMap = new HashMap<Integer,Player>();
	    	currentWeek = leagueData.getCurrentWeek();
	    	putPlayersInMap(leagueData.getPlayersById(), playersMap);
	    	positions = new ImmutableList.Builder().addAll(leagueData.getLeaguePositions()).build();
		    teams = new ImmutableList.Builder().addAll(leagueData.getTeams()).build();
		    leagueName = leagueData.getLeagueName();
		    weeks = new ImmutableList.Builder().addAll(projData.getWeekProjections()).build();
		    addBestAvailablePlayers(projData.getBestAvailablePlayersByPosition(), playersMap);
	    	//add "dummy" players that will be used to represent
	    	//players added from waiver based on their position
	    	//these have negative Player Ids defined in the Position enum
	    	//to prevent clashing with Player Ids of actual players
	    	for (Position pos : Position.values()) {
		    	Player waiverPlayer = new Player("Waiver add:" + pos.name(), Arrays.asList(pos), pos.getWaiverId());
		    	putPlayerInMap(pos.getWaiverId(), waiverPlayer, playersMap);
		    }
	    	playersById = new ImmutableMap.Builder().putAll(playersMap).build();
	    	leagueInitialized = true;
		}
	}
	
	public static int getPlayerCount() {
		return playersById.size();
	}
	
	public static int getAvailablePlayersCount() {
		return bestAvailablePlayersByPosition.size();
	}
	
	public static List<LeaguePosition> getPositions() {
		return positions;
	}
	
	public static int getCurrentWeek() {
		return currentWeek;
	}
	
	public static List<Team> getTeams() {
		return teams;
	}
	
	public static List<WeekProjections> getWeeks() {
		return weeks;
	}
	
	public static boolean isValidPlayer(int playerId) {
		return playersById.containsKey(playerId);
	}
	
	public static Player getPlayerById(int playerId) {
		return playersById.get(playerId);
	}
	
	public static String getLeagueName() {
		return leagueName;
	}
	
	private static void putPlayersInMap(Map<Integer,Player> playersToAdd, Map<Integer,Player> targetMap) {
		for (int playerId : playersToAdd.keySet()) {
		    if (!targetMap.containsKey(playerId)) {
		    	targetMap.put(playerId, playersToAdd.get(playerId));
		    } else {
			    System.out.println("Warning, attempted to add player with Id already in use. Player name: " + playersToAdd.get(playerId).getName() + " Id: " + String.valueOf(playersToAdd.get(playerId).getPlayerId()));
		    }
		}
	}
	
	private static void putPlayerInMap(int playerId, Player player, Map<Integer,Player> targetMap) {
	    if (!targetMap.containsKey(playerId)) {
	        targetMap.put(playerId, player);
	    } else {
		    System.out.println("Warning, attempted to add player with Id already in use. Player name: " + player.getName() + " Id: " + String.valueOf(player.getPlayerId()));
	    }
	}
	
	public static boolean hasAvailablePlayerForPosition(Position position) {
		return bestAvailablePlayersByPosition.containsKey(position);
	}
	
	public static Player getBestAvailablePlayer(Position position) {
		return bestAvailablePlayersByPosition.get(position);
	}
	
	private static void addBestAvailablePlayers(Map<Position, Player> bestWaiverPlayers, Map<Integer,Player> targetMap) {
		if (!leagueInitialized) {
			Map<Position,Player> bestPlayers = new HashMap<Position,Player>();
	    	for (Position position : bestWaiverPlayers.keySet()) {
			    if (!bestPlayers.containsKey(position)) {
			    	Player player = bestWaiverPlayers.get(position);
			    	putPlayerInMap(player.getPlayerId(), player, targetMap);
			    	//check if player was successfully added to league map, if Id was duplicate then don't use this player
			    	if (targetMap.get(player.getPlayerId()).equals(player)) {
			    		bestPlayers.put(position, player);
			    	}	
		    	} else {
		    		//add warning
		    	}
	    	}
	    	bestAvailablePlayersByPosition = new ImmutableMap.Builder().putAll(bestPlayers).build();
		}
	}
}
