package tradeOptimizer.projections;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import tradeOptimizer.league.Player;
import tradeOptimizer.league.Position;
import tradeOptimizer.league.FantasyLeague;

import com.opencsv.CSVWriter;

public class ProjectionCSVWriter {
	
	private CSVWriter writer;
	private boolean writerCreated = false;

	public ProjectionCSVWriter() {
		try {
			writer = new CSVWriter(new FileWriter("projections.csv"));
			writerCreated = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isCreated() {
		return this.writerCreated;
	}
	
	public void addBestWaiverPlayers() {
		writer.writeNext("BESTWAIVERPLAYERS".split(" "));
		for (Position position : Position.values()) {
			if (FantasyLeague.hasAvailablePlayerForPosition(position)) {
			    Player waiverPlayer = FantasyLeague.getBestAvailablePlayer(position);
			    String line = position + "#" + waiverPlayer.getName() + "#" + String.valueOf(waiverPlayer.getByeWeek()) + "#" + String.valueOf(waiverPlayer.getPlayerId()); 
			    writer.writeNext(line.split("#"));
			}
		}
	}
	
	public void writeWeekProjections(int weekNum, HashMap<Integer,Double> projectionsMap, Map<Position,Double> bestWaiverPositionsMap) {
		String[] weekHeader = {"WEEK", String.valueOf(weekNum)};
		writer.writeNext(weekHeader);
		for (int playerId : projectionsMap.keySet()) {
			String[] writeLine = {String.valueOf(playerId), String.valueOf(projectionsMap.get(playerId))};
			writer.writeNext(writeLine);
		}
		String[] waiverHeader = {"WEEKWAIVERS", String.valueOf(weekNum)};
		writer.writeNext(waiverHeader);
		for (Position position : bestWaiverPositionsMap.keySet()) {
			String[] writeWaiverLine = {String.valueOf(position), String.valueOf(bestWaiverPositionsMap.get(position))};
			writer.writeNext(writeWaiverLine);
		}
	}
}
