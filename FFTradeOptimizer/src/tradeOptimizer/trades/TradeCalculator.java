package tradeOptimizer.trades;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tradeOptimizer.calc.WeekCalculator;
import tradeOptimizer.league.FantasyLeague;
import tradeOptimizer.league.Team;
import tradeOptimizer.projections.WeekProjections;

public class TradeCalculator {

	private Team team1;
	private Team team2;
	private List<List<Integer>> team1Combinations;
	private List<List<Integer>> team2Combinations;
	private Double team1BaseTotal;
	private Double team2BaseTotal;
	
	public TradeCalculator(Team firstTeam, Team secondTeam) {
		team1 = firstTeam;
		team2 = secondTeam;
		team1Combinations = firstTeam.getTradeCombinations();
		team2Combinations = secondTeam.getTradeCombinations();
		team1BaseTotal = firstTeam.getBaseProjectedPoints();
		team2BaseTotal = secondTeam.getBaseProjectedPoints();
	}
	
	public void getTrades() {
		Map<List<Integer>, TradeSide> team2BestTrades = new HashMap<List<Integer>, TradeSide>();
		Map<List<Integer>, TradeSide> team1BestTrades = new HashMap<List<Integer>, TradeSide>();
		RosterUpdater team1Updater = new RosterUpdater(team1.getCurrentPlayers());
		RosterUpdater team2Updater = new RosterUpdater(team2.getCurrentPlayers());
		for (List<Integer> tradePlayers : team1Combinations) {
			for (List<Integer> otherPlayers : team2Combinations) {
				Double currentTeam1Total = 0.0;
				Double currentTeam2Total = 0.0;				
				Integer[] team1Roster = team1Updater.getUpdatedRoster(tradePlayers, otherPlayers);
				Integer[] team2Roster = team2Updater.getUpdatedRoster(otherPlayers, tradePlayers);
				int team1Counter = 0;
				int team2Counter = 0;
				for (WeekProjections week : FantasyLeague.getWeeks()) {
					WeekCalculator calculator = new WeekCalculator(week.getPlayersToUse(team1Roster), week.getTopWaiverForPositions(), tradePlayers, week.getWeekNum());
					currentTeam1Total += calculator.getOptimizedProjectedPoints();
					team1Counter += calculator.getTimesNewPlayerUsed();
					calculator = new WeekCalculator(week.getPlayersToUse(team2Roster), week.getTopWaiverForPositions(), otherPlayers, week.getWeekNum());
					currentTeam2Total += calculator.getOptimizedProjectedPoints();
					team2Counter += calculator.getTimesNewPlayerUsed();
				}
                /*Set minimum requirements for player(s) received in trade contributing to calculated projection
				  this allows filtering of projections where the player(s) received in the trade didn't contribute to increasing
				  projected points for the team */
				int minTimesPlayerUsed = (int)Math.ceil(((double)((16 - FantasyLeague.getCurrentWeek())*2)/3));
				
				if (tradePlayers.size() > 1) {
					if (team1Counter < (tradePlayers.size()*minTimesPlayerUsed)) {
						continue;
					}
				} else {
					if (team1Counter < minTimesPlayerUsed) {
						continue;
					}
				}
				if (otherPlayers.size() > 1) {
					if (team2Counter <(otherPlayers.size()*minTimesPlayerUsed)) {
						continue;
					}
				} else {
					if (team2Counter < minTimesPlayerUsed) {
						continue;
					}
				}
				Double projection1Difference = currentTeam1Total - team1BaseTotal;
				Double projection2Difference = currentTeam2Total - team2BaseTotal;
				//Account for possible negative projection differences
				Double projectionDifference = Math.abs(projection1Difference - projection2Difference);
				//below should be configurable
				if (projection1Difference > 0.0 && projection2Difference > 0.0 && projection2Difference < 60.0 && projection1Difference < 60.0 &&
						(projection1Difference + projection2Difference) > 5.0 && projectionDifference < 10.0) {

					if (team2BestTrades.containsKey(tradePlayers) &&
							(team2BestTrades.get(tradePlayers).getPointDifference() > projectionDifference)) {
						if (team1BestTrades.containsKey(team2BestTrades.get(tradePlayers).getThisTeamPlayers())) {
							team1BestTrades.remove(team2BestTrades.get(tradePlayers).getThisTeamPlayers());
						}
							team1BestTrades.put(otherPlayers, new TradeSide(tradePlayers, projectionDifference, projection1Difference, projection2Difference));
							team2BestTrades.put(tradePlayers, new TradeSide(otherPlayers, projectionDifference, projection2Difference, projection1Difference));
					} else if (!team2BestTrades.containsKey(tradePlayers)){
						if (!team1BestTrades.containsKey(otherPlayers)) {
							team1BestTrades.put(otherPlayers, new TradeSide(tradePlayers, projectionDifference, projection1Difference, projection2Difference));
						    team2BestTrades.put(tradePlayers, new TradeSide(otherPlayers, projectionDifference, projection2Difference, projection1Difference));
						} else if (team1BestTrades.containsKey(otherPlayers) &&
								(team1BestTrades.get(otherPlayers).getPointDifference() > projectionDifference)) {
							if (team2BestTrades.containsKey(team1BestTrades.get(otherPlayers).getThisTeamPlayers())) {
							    team2BestTrades.remove(team1BestTrades.get(otherPlayers).getThisTeamPlayers());	
							}
							    team1BestTrades.put(otherPlayers, new TradeSide(tradePlayers, projectionDifference, projection1Difference, projection2Difference));
							    team2BestTrades.put(tradePlayers, new TradeSide(otherPlayers, projectionDifference, projection2Difference, projection1Difference));
						}
					}
				}
			}
		}
		for (Map.Entry<List<Integer>, TradeSide> trade : team2BestTrades.entrySet()) {
			Trade newTrade = new Trade(trade.getValue().getThisTeamPlayers(), team1.getTeamName(), trade.getKey(), trade.getValue().getThisTeamPointIncrease(), trade.getValue().getOtherTeamPointIncrease());
			team2.addTrade(newTrade);
		}
		for (Map.Entry<List<Integer>, TradeSide> trade : team1BestTrades.entrySet()) {
			Trade newTrade = new Trade(trade.getValue().getThisTeamPlayers(), team2.getTeamName(), trade.getKey(), trade.getValue().getThisTeamPointIncrease(), trade.getValue().getOtherTeamPointIncrease());
			team1.addTrade(newTrade);
		
		}
		
	}

}
