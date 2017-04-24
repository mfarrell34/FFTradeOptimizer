package tradeOptimizer.league.data;

import java.util.List;
import java.util.Map;

import tradeOptimizer.league.LeaguePosition;
import tradeOptimizer.league.Player;
import tradeOptimizer.league.Team;

public interface LeagueDataSource {
		
	public List<Team> getTeams();
	public List<LeaguePosition> getLeaguePositions();
	public Map<Integer,Integer> getNumPlayersOnByeForWeek();
	public Map<Integer, Player> getPlayersById();
	public int getCurrentWeek();
	public String getLeagueName();
	
}
