package tradeOptimizer.trades;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tradeOptimizer.calc.PositionsFillableChecker;
import tradeOptimizer.league.FantasyLeague;
import tradeOptimizer.league.LeaguePosition;
import tradeOptimizer.league.Position;
import tradeOptimizer.projections.PlayerProjection;

import com.google.common.collect.ImmutableList;


public class RosterUpdater {

	private final Integer[] originalPlayers;
	
	public RosterUpdater(Integer[] originalPlayers) {
		this.originalPlayers = Arrays.copyOf(originalPlayers, originalPlayers.length);
	}
	
	public Integer[] getUpdatedRoster(List<Integer> playersSent, List<Integer> playersReceived) {
        List<Integer> newRosterPlayers = new ArrayList<Integer>();
        List<PlayerProjection> dummyProjections = new ArrayList<PlayerProjection>();
        for (int addedPlayerId : playersReceived) {
        	newRosterPlayers.add(addedPlayerId);
        	dummyProjections.add(new PlayerProjection(addedPlayerId, 1.0));
        }
        for (int playerId : originalPlayers) {
        	if (!playersSent.contains(playerId)) {
        		newRosterPlayers.add(playerId);
        		dummyProjections.add(new PlayerProjection(playerId, 1.0));
        	}
        }
        //check if any positions are currently unfilled for all weeks
        PositionsFillableChecker positionChecker = new PositionsFillableChecker(dummyProjections);
        if (!positionChecker.allPositionsFilled()) {
        	Set<Position> addedPositions = new HashSet<Position>();
        	List<LeaguePosition> emptyPositions = positionChecker.getUnfilledPositions();
        	//greedy add for empty positions, TODO update this to optimize who is added
        	for (LeaguePosition emptyPosition : emptyPositions) {
        		for (Position position : emptyPosition.getPossiblePositions()) {
        			if (FantasyLeague.hasAvailablePlayerForPosition(position) && !addedPositions.contains(position)) {
        				addedPositions.add(position);
        				newRosterPlayers.add(FantasyLeague.getBestAvailablePlayer(position).getPlayerId());
        			}
        		}
        	}
        }
        return newRosterPlayers.toArray(new Integer[newRosterPlayers.size()]);
	}
}
