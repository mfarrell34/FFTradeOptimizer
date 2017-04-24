package tradeOptimizer.yahoo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import tradeOptimizer.league.Player;
import tradeOptimizer.league.Position;
import tradeOptimizer.league.FantasyLeague;
import tradeOptimizer.projections.ProjectionCSVWriter;
import tradeOptimizer.projections.ProjectionDataSource;
import tradeOptimizer.projections.WeekProjections;

public class QueryProjections implements ProjectionDataSource {
    
	private int startWeekProjections = 1;
	private int numPlayersInLeague;
	private Map<Integer, Integer> playersOnByeForWeekProjections;
	private int numDefenses;
	private ProjectionCSVWriter writer;
	private int leagueId;
	private String baseURL;
	private boolean writeToCsv = false;
	private HashMap<Position, Player> bestAvailablePlayersByPosition;
	
	public QueryProjections(int startWeekProjections, boolean writeToCsv, int leagueId) {
		this.startWeekProjections = startWeekProjections;
		this.bestAvailablePlayersByPosition = new HashMap<Position, Player>();
		this.leagueId = leagueId;
		baseURL = "http://football.fantasysports.yahoo.com/f1/" + leagueId + "/players";
		if (writeToCsv) {
            writer = new ProjectionCSVWriter();
            if (!writer.isCreated()) {
            	System.out.println("Warning, failed to create csv file to write");
            } else {
            	this.writeToCsv = true;
            }
		}
	}
	
	public void setPlayersOnBye(Map<Integer,Integer> playersByeWeekProjectionsMap) {
		this.playersOnByeForWeekProjections = playersByeWeekProjectionsMap;
	}
	
	public void setNumDefenses(int number) {
		this.numDefenses = number;
	}
	
	public HashMap<Position, Player> getBestAvailablePlayersByPosition() {
		return this.bestAvailablePlayersByPosition;
	}
	
	public List<WeekProjections> getWeekProjections() {
		List<WeekProjections> weeks = new ArrayList<WeekProjections>();
		getBestAvailablePlayers();
        numPlayersInLeague = FantasyLeague.getPlayerCount();
		for (int i = this.startWeekProjections; i < 17; i++) {
			WeekProjections thisWeekProjections = queryProjectionsForWeek(i,0);
			weeks.add(thisWeekProjections);
		}
        return weeks;
	}		
			
	private WeekProjections queryProjectionsForWeek(int weekNum, int numRetries) {
		if (numRetries != 0) {
			System.out.println("Retrying projection queries for week: " + String.valueOf(weekNum));
		}
			int numPlayersToLoad = numPlayersInLeague - playersOnByeForWeekProjections.get(weekNum) - numDefenses;
			int playersLoaded = 0;
			WeekProjections thisWeekProjections = new WeekProjections(weekNum);
			HashMap<Integer, Double> projectionsForPlayers = new HashMap<Integer, Double>();

			try {
				for (int j = 0; j < 13; j++) {
					Document doc;
					if (j == 0) {
				        doc = Jsoup.connect(baseURL)
						        .data("status", "ALL")
							    .data("pos", "O")
							    //.data("cut_type", "9")
							    .data("sdir", "1")
							    .data("stat1", "S_PW_" + String.valueOf(weekNum))
							    .data("sort", "PTS")
							    .timeout(15000)
							    .get();
					}
					else {
						doc = Jsoup.connect(baseURL)
							    .data("status", "ALL")
								.data("pos", "O")
								//.data("cut_type", "9")
								.data("sdir", "1")
								.data("stat1", "S_PW_" + String.valueOf(weekNum))
								.data("sort", "PTS")
								.data("count", String.valueOf(25*j))
								.timeout(15000)
								.get();
					}
				Element playersTableElement = doc.select("div#players-table").first();
				Element playersElement = playersTableElement.select("div.players").first();
				Element playersDataTable = playersElement.select("TABLE").first().select("TBODY").first();
				Elements playerRows = playersDataTable.select("TR");
				
				for (Element element : playerRows) {
					boolean addPlayerProjection = false;
					Double projectedPointsValue = 0.0;
					int playerID = -1;
					String waiverPlayerPosition = "";
					Element playerStart = element.select(".player.Ta-start.Bdrend").first();
					Elements aElements = playerStart.getElementsByTag("A");
					for (Element aElement : aElements) {
						if (aElement.hasAttr("data-ys-playerid")) {
							String playerId = aElement.attr("data-ys-playerid");
							playerID = Integer.valueOf(playerId);
							if (FantasyLeague.isValidPlayer(playerID)) {
								addPlayerProjection = true;
								break;
							}
							
						}
					}
					if (!addPlayerProjection) {
						Elements spanElements = playerStart.getElementsByTag("SPAN");
						if (spanElements.hasClass("Fz-xxs")) {
							Element positionText = spanElements.select(".Fz-xxs").first();
							String teamAndPosition = positionText.ownText();
							waiverPlayerPosition = teamAndPosition.substring(teamAndPosition.length() - 2, teamAndPosition.length());
						}
					}
					Elements elements = element.select("TD.Alt.Ta-end.Nowrap");
					for (Element e : elements) {
						Elements childElements = e.getElementsByTag("SPAN");
						if (!childElements.isEmpty() && childElements.hasClass("Fw-b")) {
							Element projectedPoints = childElements.select(".Fw-b").first();
							String pointsTextVal = projectedPoints.ownText();
							projectedPointsValue = Double.valueOf(pointsTextVal);
							break;
						}
					}
					
					if (addPlayerProjection) {
						if (projectedPointsValue == 0.0) {
							System.out.println("WARNING: Couldnt get projection for: " + String.valueOf(playerID));
						}
						projectionsForPlayers.put(playerID, projectedPointsValue);
						playersLoaded++;
						if (playersLoaded == numPlayersToLoad) {
							break;
						}
					} else if (projectedPointsValue != 0.0 && playerID != 0 && !waiverPlayerPosition.equals("")) {
						if (Position.isValidPosition(waiverPlayerPosition)) {
							Position position = Position.valueOf(waiverPlayerPosition);
						    thisWeekProjections.tryAddTopWaiverPositionValue(projectedPointsValue, position);
						}
					}
				}
				
				}
				
				//Query Defenses separately
				Document doc = Jsoup.connect(baseURL)
					    .data("status", "ALL")
						.data("pos", "DEF")
						//.data("cut_type", "9")
						.data("sdir", "1")
						.data("stat1", "S_PW_" + String.valueOf(weekNum))
						.data("sort", "PTS")
						.timeout(15000)
						.get();
				Element playersTableElement = doc.select("div#players-table").first();
				Element playersElement = playersTableElement.select("div.players").first();
				Element playersDataTable = playersElement.select("TABLE").first().select("TBODY").first();
				Elements playerRows = playersDataTable.select("TR");
				
				for (Element element : playerRows) {
					boolean addPlayerProjection = false;
					Double projectedPointsValue = 0.0;
					int defPlayerID = -1;
					Element playerStart = element.select(".player.Ta-start.Bdrend").first();
					Elements aElements = playerStart.getElementsByTag("A");
					for (Element aElement : aElements) {
						if (aElement.hasAttr("data-ys-playerid")) {
							String defPlayerId = aElement.attr("data-ys-playerid");
							defPlayerID = Integer.valueOf(defPlayerId);
							if (FantasyLeague.isValidPlayer(defPlayerID)) {
								addPlayerProjection = true;
								break;
							}
							
						}
					}
					Elements elements = element.select("TD.Alt.Ta-end.Nowrap");
					for (Element e : elements) {
						Elements childElements = e.getElementsByTag("SPAN");
						if (!childElements.isEmpty() && childElements.hasClass("Fw-b")) {
							Element projectedPoints = childElements.select(".Fw-b").first();
							String pointsTextVal = projectedPoints.ownText();
							projectedPointsValue = Double.valueOf(pointsTextVal);
							break;
						}
					}
					if (addPlayerProjection) {
						if (projectedPointsValue == 0.0) {
							System.out.println("WARNING: Couldnt get projection for: " + String.valueOf(defPlayerID));
						}
						projectionsForPlayers.put(defPlayerID, projectedPointsValue);
						playersLoaded++;
					} else if (projectedPointsValue != 0.0 && defPlayerID != 0) {
						thisWeekProjections.tryAddTopWaiverPositionValue(projectedPointsValue, Position.DEF);
					}
					
				}
				
				int totalExpected = numPlayersToLoad + numDefenses;
				System.out.println("Total projections loaded for week: " + String.valueOf(weekNum) + " " + String.valueOf(playersLoaded) + " / " + String.valueOf(totalExpected));
			} catch (IOException e) {
				e.printStackTrace();
				return queryProjectionsForWeek(weekNum, ++numRetries);
			} catch (NullPointerException e) {
				e.printStackTrace();
				return queryProjectionsForWeek(weekNum, ++numRetries);
			}
			thisWeekProjections.addProjectionsForWeek(projectionsForPlayers);
			if (writeToCsv) {
			    writer.writeWeekProjections(weekNum, projectionsForPlayers, thisWeekProjections.getTopWaiverForPositions());
			}
			return thisWeekProjections;
		}
	
	private void getBestAvailablePlayers() {
		try {
			List<String> positionsToFind = new ArrayList<String>(Arrays.asList("QB", "WR", "RB", "TE"));
			boolean foundAllPositions = false;
			for (int j = 0; j < 15; j++) {
				Document doc;
				if (j == 0) {
			        doc = Jsoup.connect(baseURL)
					        .data("status", "ALL")
						    .data("pos", "O")
						    .data("sdir", "1")
						    .data("stat1", "S_PSR_2016")
						    .data("sort", "PTS")
						    .timeout(15000)
						    .get();
				}
				else {
					doc = Jsoup.connect(baseURL)
						    .data("status", "ALL")
							.data("pos", "O")
							.data("sdir", "1")
							.data("stat1", "S_PSR_2016")
							.data("sort", "PTS")
							.data("count", String.valueOf(25*j))
							.timeout(15000)
							.get();
				}
				Element playersTableElement = doc.select("div#players-table").first();
				Element playersElement = playersTableElement.select("div.players").first();
				Element playersDataTable = playersElement.select("TABLE").first().select("TBODY").first();
				Elements playerRows = playersDataTable.select("TR");
				
				for (Element element : playerRows) {
					boolean addPlayerProjection = false;
					Double projectedPointsValue = 0.0;
					int playerID = -1;
					String waiverPlayerPosition = "";
					String playerName = "";
					int byeWeek = 0;
					boolean foundIdAndPosition = false;
					boolean getPoints = false;
					Element playerStart = element.select(".player.Ta-start.Bdrend").first();
					Elements aElements = playerStart.getElementsByTag("A");
					for (Element aElement : aElements) {
						if (aElement.hasAttr("data-ys-playerid")) {
							String playerId = aElement.attr("data-ys-playerid");
							//System.out.println("Found ID: " + playerId);
							playerID = Integer.valueOf(playerId);
							if (!FantasyLeague.isValidPlayer(playerID)) {
								//addPlayerProjection = true;
								Elements spanElements = playerStart.getElementsByTag("SPAN");
								if (spanElements.hasClass("Fz-xxs")) {
									Element positionText = spanElements.select(".Fz-xxs").first();
									String teamAndPosition = positionText.ownText();
									//System.out.println("Found Team and Position: " + teamAndPosition);
									waiverPlayerPosition = teamAndPosition.substring(teamAndPosition.length() - 2, teamAndPosition.length());
									if (Position.isValidPosition(waiverPlayerPosition)) {
										Position position = Position.valueOf(waiverPlayerPosition);
										foundIdAndPosition = true;
										if (!this.bestAvailablePlayersByPosition.containsKey(position)) {
											getPoints = true;
											if (!playerName.equals("")) {
												break;
											}
										}
									}
								}
								
							} else {
								break;
							}
						} else if (aElement.hasClass("Nowrap")) {
							playerName = aElement.ownText();
							if (foundIdAndPosition) {
								break;
							}
						}
					}
					if (getPoints) {
						this.bestAvailablePlayersByPosition.put(Position.valueOf(waiverPlayerPosition), new Player("Add: " + playerName, Position.valueOf(waiverPlayerPosition), byeWeek, playerID));
					    if (positionsToFind.size() == FantasyLeague.getAvailablePlayersCount()) {
							foundAllPositions = true;
							break;
						}
					}
				}
				if (foundAllPositions) {
					if (writeToCsv) {
					    writer.addBestWaiverPlayers();
					}
					break;
				}
			}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
	}

}
