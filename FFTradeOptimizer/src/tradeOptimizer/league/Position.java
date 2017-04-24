package tradeOptimizer.league;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum Position {

	/*
	 * Position contains football positions
	 */
	QB(-1),
	RB(-2),
	WR(-3),
	TE(-4),
	K(-5),
	DEF(-6),
	D(-7),
	BN(-8),
	IR(-9);
		
	private int waiverPlayerId;
	
	Position(int waiverPlayerId) {
		this.waiverPlayerId = waiverPlayerId;
	}
	
	public int getWaiverId() {
		return waiverPlayerId;
	}
	private static Map<String,Position> stringToPos = new HashMap<>();
		
	static Set<Position> ignorePositions = new HashSet<Position>(Arrays.asList(Position.K, Position.D, Position.BN, Position.IR));
		
	static {
		for (Position position : Position.values()) {
			stringToPos.put(position.name(), position);
		}
	}
		
	public static boolean isValidPosition(String pos) {
		if (stringToPos.containsKey(pos) && !ignorePositions.contains(pos)) {
			return true;
		}
		return false;
	}
	
	//assumes pos has already been validated as a valid position, will return
	//null if invalid string is passed
	public static Position fromString(String pos) {
		return stringToPos.get(pos.toUpperCase());
	}
	
}
