package tradeOptimizer.trades;

import java.util.ArrayList;
import java.util.List;

/*
 * Trade class representing a trade between two teams in a league, used to store information
 * on players exchanged in the trade and projected point change resulting from the trade for each team.
 * Each Team will store Trade objects, so the Team holding the reference to a Trade instance is known as "this" Team
 */
public class Trade implements Comparable {

	private List<Integer> thisTeamsPlayers;
	private String otherTeamName;
	private List<Integer> otherTeamsPlayers;
	private Double thisTeamPointIncrease;
	private Double otherTeamPointIncrease;
	
	/*
	 * Constructor method, arguments:
	 * thisTeamsPlayers: ArrayList of players being given away by this team in Trade
	 * otherTeamName: Name of other team in trade
	 * otherTeamsPlayers: ArrayList of players being given away by other team (received by this team)
	 * thisTeamPointIncrease: Projected total point increase for this team resulting from the trade
	 * otherTeamPointIncrease: Projected total point increase for other team resulting from the trade
	 */
	public Trade(List<Integer> thisTeamsPlayers, String otherTeamName, List<Integer> otherTeamsPlayers, Double thisPoints, Double otherPoints) {
		this.thisTeamsPlayers = thisTeamsPlayers;
		this.otherTeamName = otherTeamName;
		this.otherTeamsPlayers = otherTeamsPlayers;
		this.thisTeamPointIncrease = thisPoints;
		this.otherTeamPointIncrease = otherPoints;
	}
	
/*	public Trade getReverseTrade(String teamName) {
		Trade reverseTrade = new Trade(this.otherTeamsPlayers, teamName, this.thisTeamsPlayers, this.otherTeamPointIncrease, this.thisTeamPointIncrease);
		return reverseTrade;
	} */

	/*
	 * compareTo override sorts trades by thisTeam's projected point change to allow returning of optimal trades rather than all possible trades
	 */
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		if (arg0 instanceof Trade) {
			Trade otherTrade = (Trade) arg0;
			return Double.compare(otherTrade.thisTeamPointIncrease, this.thisTeamPointIncrease);
		} else {
		   return 0;
		}
	}

	public List<Integer> getOtherTeamPlayers() {
		return otherTeamsPlayers;
	}
	
	public Double getOtherTeamPointIncrease() {
		return this.otherTeamPointIncrease;
	}

	public Double getThisTeamPointIncrease() {
		return this.thisTeamPointIncrease;
	}
	
	public String getOtherTeamName() {
		return this.otherTeamName;
	}
	
	public List<Integer> getThisTeamPlayers() {
		return this.thisTeamsPlayers;
	}
}
