package tradeOptimizer.projections;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import tradeOptimizer.league.Player;
import tradeOptimizer.league.Position;

import com.opencsv.CSVReader;

public class ReadProjectionsCSV implements ProjectionDataSource {

	private CSVReader reader;
	private boolean readerCreated = false;
	private HashMap<Position, Player> bestAvailablePlayersByPosition;
	
	public ReadProjectionsCSV(String fileName) {
		try {
			reader = new CSVReader(new FileReader(fileName));
			readerCreated = true;
			this.bestAvailablePlayersByPosition = new HashMap<Position, Player>();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public HashMap<Position, Player> getBestAvailablePlayersByPosition() {
		return this.bestAvailablePlayersByPosition;
	}
	
	public boolean isCreated() {
		return this.readerCreated;
	}
	
	public List<WeekProjections> getWeekProjections() {
		List<WeekProjections> projectionData = new ArrayList<WeekProjections>();
		List<Integer> weeksAdded = new ArrayList<Integer>();
		String loadDataControl = "";
		List<String> dataControlsUsed = Arrays.asList("BESTWAIVERPLAYERS", "WEEK", "WEEKWAIVERS");
		int currentWeek = 0;
		WeekProjections currentWeekProjections = new WeekProjections(0);
		HashMap<Integer,Double> weekProjectionsMap = new HashMap<Integer,Double>();
		String[] nextLine;
		try {
			while ((nextLine = reader.readNext()) != null) {
				if (dataControlsUsed.contains(nextLine[0])) {
					loadDataControl = nextLine[0];
					if (loadDataControl.equals("WEEK")) {
						//if this isn't first week being loaded then previous week projection data must be stored
						if (weekProjectionsMap.size() > 0 && !currentWeekProjections.getProjectionsSet()) {
						    currentWeekProjections.addProjectionsForWeek(weekProjectionsMap);
						}
						if (!weeksAdded.contains(currentWeek) && currentWeek != 0 && currentWeekProjections.getProjectionsSet()) {
							projectionData.add(currentWeekProjections);
							weeksAdded.add(currentWeek);
						}
						currentWeek = Integer.valueOf(nextLine[1]);
						currentWeekProjections = new WeekProjections(currentWeek);
						weekProjectionsMap = new HashMap<Integer,Double>();
					} else if (loadDataControl.equals("WEEKWAIVERS")) {
						if (Integer.valueOf(nextLine[1]) != currentWeek) {
							loadDataControl = "";
							System.out.println("Warning, unexpected week for waiver player data");
						}
					}
				} else {
					switch (loadDataControl) {
					case "BESTWAIVERPLAYERS":
						//format should be: position, name, byeweek, player ID
						//must confirm ID field is a valid integer
						try {
						    int playerId = Integer.valueOf(nextLine[3]);
						    String positionText = nextLine[0];
						    if (Position.isValidPosition(positionText)) {
						    	Position position = Position.valueOf(positionText);
						        this.bestAvailablePlayersByPosition.put(position, new Player(nextLine[1], position, playerId));
						    }

						} catch (NumberFormatException ex) {
							System.out.println("Warning, unable to read player ID for top waiver player: " + nextLine[1]);
						}
						break;
					case "WEEK":
						//need to parse both player ID and projected point value
						try {
							int playerId = Integer.valueOf(nextLine[0]);
							double projectedPoints = Double.valueOf(nextLine[1]);
							weekProjectionsMap.put(playerId, projectedPoints);
						} catch (NumberFormatException ex) {
							System.out.println("Warning, failed to parse projected points for player: " + nextLine[0] + " in week: " + String.valueOf(currentWeek));
						}
						break;
					case "WEEKWAIVERS":
						try {
							double projectedPoints = Double.valueOf(nextLine[1]);
							String positionText = nextLine[0];
							if (Position.isValidPosition(positionText)) {
						    	Position position = Position.valueOf(positionText);
							    currentWeekProjections.tryAddTopWaiverPositionValue(projectedPoints, position);
							}
						} catch (NumberFormatException ex) {
							System.out.println("Warning, failed to parse top waiver points for: " + nextLine[0] + " in week: " + String.valueOf(currentWeek));
						}
						break;
					default:
						//if no valid control is set can't trust data being parsed
						break;
					} 
				}
			}
			//add last week after end of file has been parsed
			if (weekProjectionsMap.size() > 0 && !currentWeekProjections.getProjectionsSet()) {
			    currentWeekProjections.addProjectionsForWeek(weekProjectionsMap);
			}
			if (!weeksAdded.contains(currentWeek) && currentWeek != 0) {
				projectionData.add(currentWeekProjections);
				weeksAdded.add(currentWeek);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return projectionData;
	}
}
