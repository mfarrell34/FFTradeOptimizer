package tradeOptimizer.league;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/* 
 * LeaguePosition class, represents position in fantasy league rather than football position
 * as some leagues have FLEX positions that allow use of multiple possible player positions.
 * LeaguePosition is sortable by number of possible football positions that can be used, as it can be useful
 * to fill the most restrictive positions first (such as positions only allowing a single NFL position)
 * when calculating optimal lineups.
 */
public class LeaguePosition implements Comparable {

	private String positionName; //name of position in FF league, not necessarily matching a single football position
	private Set<Position> possiblePositions; //list of football positions allowed to be used in position, ie. RB, WR, TE in a standard flex positon
	
	public LeaguePosition(String name, List<Position> possiblePositions) {
		this.positionName = name;
		this.possiblePositions = new HashSet<Position>();
		this.possiblePositions.addAll(possiblePositions);
	}
	
	//cloning constructor
	public LeaguePosition(LeaguePosition position) {
		this.positionName = position.positionName;
		this.possiblePositions = position.possiblePositions;
	}
	
	/*
	 * Accessor methods
	 */
	
	public String getPositionName() {
		return this.positionName;
	}
	
	public Set<Position> getPossiblePositions() {
		return this.possiblePositions;
	}
	
	/*
	 * compareTo override used to sort positions by number of football positions allowed
	 */
	@Override
	public int compareTo(Object arg0) {
		if (arg0 instanceof LeaguePosition) {
			LeaguePosition otherPosition = (LeaguePosition) arg0;
			return Integer.compare(this.possiblePositions.size(), otherPosition.possiblePositions.size());
		} else {
		   return 0;
		}
	}
	
	/*
	 * Equality for fantasy position objects checks if position name and list of allowed positions is identical.
	 * Positions should have unique names but this may not be strictly enforced.
	 */
	@Override
	public boolean equals(Object position) {
		if (position instanceof LeaguePosition) {
			if (this.positionName.equals(((LeaguePosition) position).getPositionName()) &&
					this.possiblePositions.equals(((LeaguePosition) position).getPossiblePositions())) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result *= 31 * positionName.hashCode();
		for (Position pos : possiblePositions) {
			result *= 31 * pos.hashCode();
		}
		return result;
	}
	
}
