package tradeOptimizer.league;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tradeOptimizer.league.data.LeagueDataSource;
import tradeOptimizer.projections.ProjectionDataSource;
import tradeOptimizer.projections.WeekProjections;

//Static class for holding global data related to the fantasy league including
//teams and players in the league, projection data, and available waiver players

public class FantasyLeague {

	private static List<LeaguePosition> positions = new ArrayList<LeaguePosition>();
	private static Map<Position, Player> bestAvailablePlayersByPosition = new HashMap<Position, Player>();
	private static Map<Integer,Player> playersById = new HashMap<Integer, Player>();
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
	    	currentWeek = leagueData.getCurrentWeek();
	    	putPlayersInMap(leagueData.getPlayersById());
	    	positions = leagueData.getLeaguePositions();
		    teams = leagueData.getTeams();
		    leagueName = leagueData.getLeagueName();
		    weeks = projData.getWeekProjections();
		    addBestAvailablePlayers(projData.getBestAvailablePlayersByPosition());
	    	//add "dummy" players that will be used to represent
	    	//players added from waiver based on their position
	    	//these have negative Player Ids defined in the Position enum
	    	//to prevent clashing with Player Ids of actual players
	    	for (Position pos : Position.values()) {
		    	Player waiverPlayer = new Player("Waiver add:" + pos.name(), Arrays.asList(pos), pos.getWaiverId());
		    	putPlayerInMap(pos.getWaiverId(), waiverPlayer);
		    }
	    	leagueInitialized = true;
		}
	}
	
	public static int getPlayerCount() {
		return playersById.keySet().size();
	}
	
	public static int getAvailablePlayersCount() {
		return bestAvailablePlayersByPosition.keySet().size();
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
	
	public static void putPlayersInMap(Map<Integer,Player> playerIdMap) {
		for (int playerId : playerIdMap.keySet()) {
		    if (!playersById.containsKey(playerId)) {
			    playersById.put(playerId, playerIdMap.get(playerId));
		    } else {
			    System.out.println("Warning, attempted to add player with Id already in use. Player name: " + playerIdMap.get(playerId).getName() + " Id: " + String.valueOf(playerIdMap.get(playerId).getPlayerId()));
		    }
		}
	}
	
	public static void putPlayerInMap(int playerId, Player player) {
	    if (!playersById.containsKey(playerId)) {
		    playersById.put(playerId, player);
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
	
	public static void addBestAvailablePlayers(Map<Position, Player> bestWaiverPlayers) {
		for (Position position : bestWaiverPlayers.keySet()) {
			Player player = bestWaiverPlayers.get(position);
			if (!bestAvailablePlayersByPosition.containsKey(position)) {
				putPlayerInMap(player.getPlayerId(), player);
				//check if player was successfully added to league map, if Id was duplicate then don't use this player
				if (getPlayerById(player.getPlayerId()).equals(player)) {
					bestAvailablePlayersByPosition.put(position, player);
				}
				
			} else {
				//add warning
			}
		}
	}
}
