package tradeOptimizer;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import tradeOptimizer.calc.OptimalLineupCalculator;
import tradeOptimizer.calc.PositionsFillableChecker;
import tradeOptimizer.league.LeaguePosition;
import tradeOptimizer.league.Player;
import tradeOptimizer.league.Position;
import tradeOptimizer.league.FantasyLeague;
import tradeOptimizer.league.Team;
import tradeOptimizer.league.data.LeagueDataSource;
import tradeOptimizer.projections.PlayerProjection;
import tradeOptimizer.projections.ProjectionDataSource;
import tradeOptimizer.projections.WeekProjections;

public class TestWeekCalculator {
	
	List<PlayerProjection> testWeekProjections1;
	List<Player> testPlayers;
	
	@Test
	public void test() {
        TestDataSource testData = new TestDataSource();
        ProjectionTestData testProjs = new ProjectionTestData();
        setupPlayers(testData, testProjs);
		FantasyLeague.setupLeague(testData, testProjs);
		List<LeaguePosition> positions = testData.getLeaguePositions();
		PositionsFillableChecker checker = new PositionsFillableChecker(testWeekProjections1, positions);
		boolean positionsFilled = checker.allPositionsFilled();
		assertTrue(positionsFilled);
		OptimalLineupCalculator calc = new OptimalLineupCalculator(testWeekProjections1,positions);
		Double projectedPoints = calc.getOptimalProjPoints();
        assertEquals(projectedPoints,(Double)265.5);
        assertFalse(calc.getPlayersUsed().contains(testWeekProjections1.get(7).getPlayerId()));
        assertFalse(calc.getPlayersUsed().contains(testWeekProjections1.get(1).getPlayerId()));
        assertTrue(calc.getPlayersUsed().contains(testWeekProjections1.get(12).getPlayerId()));
		//HashMap<Integer,List<Position>> positionMap = new HashMap<Integer,List<Position>>();
		//positionMap.put(testPlayer.getPlayerId(), testPlayer.getFootballPositions());
		
		
		//fail("Not yet implemented");
	}
	
	void setupPlayers(TestDataSource data, ProjectionTestData projData) {
		testWeekProjections1 = new ArrayList<PlayerProjection>();
		testPlayers = new ArrayList();
		testPlayers.add(new Player("Tom Brady", Position.QB, 1));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(0).getPlayerId(), 30.0));
		testPlayers.add(new Player("Aaron Rodgers", Position.QB, 2));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(1).getPlayerId(), 5.5));
		testPlayers.add(new Player("Antonio Brown", Position.WR, 3));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(2).getPlayerId(), 28.5));
		testPlayers.add(new Player("Dez Bryant", Position.WR, 4));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(3).getPlayerId(), 25.5));
		testPlayers.add(new Player("Jordan Howard", Position.RB, 5));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(4).getPlayerId(), 24.5));
		testPlayers.add(new Player("Gronk", Position.TE, 6));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(5).getPlayerId(), 27.5));
		testPlayers.add(new Player("Leveon Bell", Position.RB, 7));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(6).getPlayerId(), 28.0));
		testPlayers.add(new Player("Pierre Garcon", Position.WR, 8));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(7).getPlayerId(), 21.0));
		testPlayers.add(new Player("Demarco Murray", Position.RB, 9));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(8).getPlayerId(), 26.0));
		testPlayers.add(new Player("Jimmy Graham", Position.TE, 10));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(9).getPlayerId(), 27.0));
		testPlayers.add(new Player("Eddie Lacy", Position.RB, 11));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(10).getPlayerId(), 2.0));
		testPlayers.add(new Player("Deandre Hopkins", Position.WR, 12));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(11).getPlayerId(), 26.5));
		testPlayers.add(new Player("Matt Forte", Position.RB, 13));
		testWeekProjections1.add(new PlayerProjection(testPlayers.get(12).getPlayerId(), 22.0));
		for (Player player : testPlayers) {
			data.addPlayerData(player.getPlayerId(), player);
		}
		WeekProjections week = new WeekProjections(1);
		Map<Integer,Double> projections = new HashMap<Integer,Double>();
		for (PlayerProjection proj : testWeekProjections1) {
			projections.put(proj.getPlayerId(), proj.getProjection());
		}
		week.addProjectionsForWeek(projections);
		projData.addWeek(week);
	}
	
	class TestDataSource implements LeagueDataSource {
		
		List<LeaguePosition> positions;
		Map<Integer,Player> playerIdMap = new HashMap();
		
		TestDataSource() {
			addPositionData();
		}

		
		private void addPositionData() {
			positions = new ArrayList<LeaguePosition>();
			positions.add(new LeaguePosition("QB1", Arrays.asList(Position.QB)));
			positions.add(new LeaguePosition("RB1", Arrays.asList(Position.RB)));
			positions.add(new LeaguePosition("RB2", Arrays.asList(Position.RB)));
			positions.add(new LeaguePosition("WR1", Arrays.asList(Position.WR)));
			positions.add(new LeaguePosition("WR2", Arrays.asList(Position.WR)));
			positions.add(new LeaguePosition("WR3", Arrays.asList(Position.WR)));
			positions.add(new LeaguePosition("TE1", Arrays.asList(Position.TE)));
			positions.add(new LeaguePosition("FLEX1", Arrays.asList(Position.RB,Position.WR,Position.TE)));
			positions.add(new LeaguePosition("FLEX2", Arrays.asList(Position.RB,Position.WR)));
			positions.add(new LeaguePosition("FLEX3", Arrays.asList(Position.WR,Position.TE)));
		}
		
		void addPlayerData(int playerId, Player player) {
			playerIdMap.put(playerId, player);
		}
		

		@Override
		public List<Team> getTeams() {
			// TODO Auto-generated method stub
			return new ArrayList<Team>();
		}

		@Override
		public List<LeaguePosition> getLeaguePositions() {
			// TODO Auto-generated method stub
			return positions;
		}

		@Override
		public Map<Integer, Integer> getNumPlayersOnByeForWeek() {
			// TODO Auto-generated method stub
			return new HashMap<Integer,Integer>();
		}

		@Override
		public Map<Integer, Player> getPlayersById() {
			// TODO Auto-generated method stub
			return playerIdMap;
		}

		@Override
		public int getCurrentWeek() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		public List<LeaguePosition> getPositions() {
			return positions;
		}


		@Override
		public String getLeagueName() {
			// TODO Auto-generated method stub
			return "TestLeague";
		}
	}
	
	class ProjectionTestData implements ProjectionDataSource {

		List<WeekProjections> weeks = new ArrayList();
		
		Map<Position,Player> bestWaiverPlayers = new HashMap<Position,Player>();
		
		ProjectionTestData() {
			
		}
		
		void addWeek(WeekProjections week) {
			weeks.add(week);
		}
		
		@Override
		public List<WeekProjections> getWeekProjections() {
			// TODO Auto-generated method stub
			return weeks;
		}

		@Override
		public Map<Position, Player> getBestAvailablePlayersByPosition() {
			// TODO Auto-generated method stub
			return bestWaiverPlayers;
		}
		
	}

}
