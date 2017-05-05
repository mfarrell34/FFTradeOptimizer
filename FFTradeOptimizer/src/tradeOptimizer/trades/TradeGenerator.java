package tradeOptimizer.trades;

import java.util.ArrayList;
import java.util.List;

import tradeOptimizer.ExcelFileGenerator;
import tradeOptimizer.calc.WeekCalculator;
import tradeOptimizer.league.Team;
import tradeOptimizer.league.FantasyLeague;
import tradeOptimizer.league.data.LeagueDataSource;
import tradeOptimizer.projections.ProjectionDataSource;
import tradeOptimizer.projections.WeekProjections;

public class TradeGenerator {
	
	private List<Team> leagueTeams;

	
	public TradeGenerator(LeagueDataSource dataSource, ProjectionDataSource projectionSource) {
	    FantasyLeague.setupLeague(dataSource, projectionSource);
	}
	

	public void generateTrades() {
		leagueTeams = FantasyLeague.getTeams();
		for (Team team : leagueTeams) {
			Double currentRosterTotal = 0.0;
			for (WeekProjections week : FantasyLeague.getWeeks()) {
				WeekCalculator calculator = new WeekCalculator(week.getPlayersToUse(team.getCurrentPlayers()), week.getTopWaiverForPositions(), new ArrayList<Integer>(), week.getWeekNum());
				Double currentWeekTotal = calculator.getOptimizedProjectedPoints();
				currentRosterTotal += currentWeekTotal;
			}
			team.setBaseProjectedPoints(currentRosterTotal);

		}
		
		for (int i = 0; i < leagueTeams.size() - 1; i++) {
	        for (int j = i + 1; j < leagueTeams.size(); j++) {
		    	Team thisTeam = leagueTeams.get(i);
		    	Team otherTeam = leagueTeams.get(j);
		    	TradeCalculator calculator = new TradeCalculator(thisTeam, otherTeam);
		    	calculator.getTrades();
		    }
		}
		ExcelFileGenerator generator = new ExcelFileGenerator(leagueTeams);
		generator.writeFile();

	}

}
