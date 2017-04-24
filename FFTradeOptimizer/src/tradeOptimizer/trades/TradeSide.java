package tradeOptimizer.trades;

import java.util.ArrayList;
import java.util.List;

/*
 * TradeSide class holds projected point calculations for a given List of players given away by a Team in a Trade
 * TradeSide objects will be stored for each Team when calculating Trades
 * Used to compare calculated Trade results with previously calculated Trades for the same combination of players given away by the Team
 */
public class TradeSide {
	private List<Integer> thisTeamsPlayers;
	private Double thisTeamPointIncrease;
	private Double otherTeamPointIncrease;
	private Double pointDifference;
	
	/*
	 * Constructor method, arguments:
	 * thisPlayers: ArrayList of Players from this side of the trade, represents combination of players being sent to other Team in a Trade
	 * thisTeamPointIncrease: Projected point increase for Team on this side of the Trade, assumed to be positive as TradeSide objects won't created when projected points expected to decrease for either side of Trade
	 * otherTeamPointIncrease: Projected point increase for other Team involved in Trade, assumed to be positive as TradeSide objects won't created when projected points expected to decrease for either side of Trade
	 * pointDifference: Difference in projected point increases for this Team and other Team, minimum pointDifference value represents Trade that is most fair
	 */
	public TradeSide(List<Integer> thisPlayers, Double thisTeamPointIncrease, Double otherTeamPointIncrease, Double pointDifference) {
		this.thisTeamsPlayers = thisPlayers;
		this.otherTeamPointIncrease = otherTeamPointIncrease;
		this.thisTeamPointIncrease = thisTeamPointIncrease;
		this.pointDifference = pointDifference;
	}
	
	public List<Integer> getThisTeamPlayers() {
		return this.thisTeamsPlayers;
	}
	
	public Double getThisTeamPointIncrease() {
		return this.thisTeamPointIncrease;
	}
	
	public Double getOtherTeamPointIncrease() {
		return this.otherTeamPointIncrease;
	}
	
	public Double getPointDifference() {
		return this.pointDifference;
	}
}
