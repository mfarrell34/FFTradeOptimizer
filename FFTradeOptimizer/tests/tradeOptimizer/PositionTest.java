package tradeOptimizer;

import static org.junit.Assert.*;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import tradeOptimizer.league.LeaguePosition;
import tradeOptimizer.league.Player;
import tradeOptimizer.league.Position;

public class PositionTest {

	@Test
	public void test() {
		LeaguePosition QB = new LeaguePosition("QB1", Arrays.asList(Position.QB));
		LeaguePosition RB = new LeaguePosition("RB1", Arrays.asList(Position.RB));
		LeaguePosition WR = new LeaguePosition("WR1", Arrays.asList(Position.WR));
		LeaguePosition TE = new LeaguePosition("TE1", Arrays.asList(Position.TE));
		LeaguePosition FLEX = new LeaguePosition("FLEX", Arrays.asList(Position.WR,Position.TE));
		Player testPlayer1 = new Player("Test1", Arrays.asList(Position.WR, Position.RB), 1);
		Player testPlayer2 = new Player("Test2", Position.RB, 2);
		Assert.assertTrue(testPlayer1.canBeUsedInPosition(WR));
		Assert.assertTrue(testPlayer1.canBeUsedInPosition(FLEX));
		Assert.assertFalse(testPlayer1.canBeUsedInPosition(QB));
		Assert.assertFalse(testPlayer2.canBeUsedInPosition(WR));
		Assert.assertFalse(testPlayer2.canBeUsedInPosition(FLEX));
		Assert.assertTrue(testPlayer2.canBeUsedInPosition(RB));
	}

}
