package tradeOptimizer.projections;

import java.util.Map;
import java.util.List;

import tradeOptimizer.league.Player;
import tradeOptimizer.league.Position;

public interface ProjectionDataSource {

	public List<WeekProjections> getWeekProjections();
	public Map<Position, Player> getBestAvailablePlayersByPosition();
}
