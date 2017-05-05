package tradeOptimizer.calc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tradeOptimizer.league.LeaguePosition;
import tradeOptimizer.league.Position;
import tradeOptimizer.league.FantasyLeague;
import tradeOptimizer.projections.PlayerProjection;

/*
 * WeekCalculator class used for calculating the projected point total using the optimal lineup for a list
 * of PlayerProjections
 */
public class WeekCalculator {

	private Map<Position,Double> topWaiverValueForPosition;
	private Map<Position,Boolean> waiverPositionUsed;
	private List<PlayerProjection> projectionsToUse;
	private List<Integer> addedPlayers;
	private int timesNewPlayerUsed;
	private int thisWeekNum;
	
	/*
	 * Constructor method, Arguments:
	 * projections: List of PlayerProjections sorted highest to lowest by projected points
	 * waiverValues: Map containing maximum number of projected points for a given position on the waiver wire
	 * addedPlayers: List of Players added to a Team instance from a Trade, used to determine if the Players received in a trade would be used this week
	 * weekNum: Represents week number of the NFL season for a WeekCalculator instance
	 */
	public WeekCalculator(List<PlayerProjection> projections, Map<Position,Double> waiverValues, List<Integer> addedPlayers, int weekNum) {
		this.topWaiverValueForPosition = waiverValues;
		this.waiverPositionUsed = new HashMap<Position,Boolean>(this.topWaiverValueForPosition.keySet().size());
		for (Position position : this.topWaiverValueForPosition.keySet()) {
			this.waiverPositionUsed.put(position,  false);
		}
		//filter out players with 0 points (they won't be used)
		this.projectionsToUse = projections;
		this.addedPlayers = addedPlayers;
		this.timesNewPlayerUsed = 0;
		this.thisWeekNum = weekNum;
	}
	/*
	 * Method to return number of player(s) added to a Team in a Trade were used in a WeekCalculator instance,
	 * allows determining whether Players obtained in a trade were used or not in the optimal lineup for the WeekCalculator instance
	 */
	public int getTimesNewPlayerUsed() {
		return timesNewPlayerUsed;
	}
	
	public Double getOptimizedProjectedPoints() {
		Double totalPoints = 0.0;
		checkNeedWaiverPlayers();
		OptimalLineupCalculator calc = new OptimalLineupCalculator(projectionsToUse, FantasyLeague.getPositions());
		totalPoints = calc.getOptimalProjPoints();
		
		//must also set timesNewPlayerUsed field to determine if players added in trade were used in the optimal projected lineup
		Set<Integer> playerIdsUsed = calc.getPlayersUsed();
		for (Integer addedPlayerId : addedPlayers) {
			if (playerIdsUsed.contains(addedPlayerId)) {
				timesNewPlayerUsed++;
			}
		}
		return totalPoints;
	}
	
	/*
	 * Method checks if all positions can be filled, if there are unfilled positions then populate them
	 * with the best available player from waiver.
	 */
	private void checkNeedWaiverPlayers() {
		PositionsFillableChecker posChecker = new PositionsFillableChecker(projectionsToUse);
		if (!posChecker.allPositionsFilled()) {
			List<LeaguePosition> unfilledPositions = posChecker.getUnfilledPositions();
			for (LeaguePosition emptyPosition : unfilledPositions) {
				addTopWaiverForPosition(emptyPosition);
			}
		}
	}
	
	private void addTopWaiverForPosition(LeaguePosition position) {
		Double waiverPlayerPoints = 0.0;
		
		if (this.topWaiverValueForPosition.keySet().size() > 0) {
			Position positionToUse = null;
			//get unused waiver player with highest projected point total for the week
			//that can also play this position
			for (Position pos : position.getPossiblePositions()) {
				if (this.waiverPositionUsed.containsKey(pos) && !this.waiverPositionUsed.get(pos) && 
						this.topWaiverValueForPosition.get(pos) > waiverPlayerPoints) {
					positionToUse = pos;
					waiverPlayerPoints = this.topWaiverValueForPosition.get(pos);
				}
			}
			if (waiverPlayerPoints > 0.0 && positionToUse != null) {
				waiverPositionUsed.put(positionToUse, true);
				projectionsToUse.add(new PlayerProjection(positionToUse.getWaiverId(), waiverPlayerPoints));
			}
		}
	}
}
