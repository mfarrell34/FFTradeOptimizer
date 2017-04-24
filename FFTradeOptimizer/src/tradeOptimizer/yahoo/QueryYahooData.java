package tradeOptimizer.yahoo;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.CharacterData;

import tradeOptimizer.league.LeaguePosition;
import tradeOptimizer.league.Player;
import tradeOptimizer.league.Position;
import tradeOptimizer.league.Team;
import tradeOptimizer.league.data.LeagueDataSource;

import com.github.scribejava.apis.YahooApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;


public class QueryYahooData implements LeagueDataSource {
	
	static OAuth1AccessToken accessToken;
	static OAuth10aService service;
	private List<Team> teamList = new ArrayList<Team>();
	private List<LeaguePosition> leaguePositions;
	private Map<Integer,Player> playersById = new HashMap<Integer,Player>();
	private Map<Integer, Integer> numPlayersOnByeForWeek = new HashMap<Integer, Integer>();
	private QueryProjections projectionsQuery;
	private int numDefenses;
	private int yahooLeagueId;
	private int currentWeek;
	private int numTeams;
	private String oaApiKey;
	private String oaApiSecret;
	private String leagueName;
	
	
	public QueryYahooData(int leagueId, String oaApiKey, String oaApiSecret) {
		this.yahooLeagueId = leagueId;
		this.oaApiKey = oaApiKey;
		this.oaApiSecret = oaApiSecret;
	}
	
	/*
	 * Accessor methods
	 */
	
	public List<Team> getTeams() {
		return teamList;
	}
	
	public List<LeaguePosition> getLeaguePositions() {
		return leaguePositions;
	}
	public Map<Integer,Integer> getNumPlayersOnByeForWeek() {
		return numPlayersOnByeForWeek;
	}
	
	public Map<Integer, Player> getPlayersById() {
		return playersById;
	}
	
	public int getCurrentWeek() {
		return currentWeek;
	}
	
	public String getLeagueName() {
		return leagueName;
	}
	
	public void OAuthentication() {
		service = new ServiceBuilder()
		//move to constructor 
		.apiKey(oaApiKey)
		.apiSecret(oaApiSecret)
		.build(YahooApi.instance());
		
		final Scanner in = new Scanner(System.in);
		
		try {
			final OAuth1RequestToken requestToken = service.getRequestToken();
			String authUrl = service.getAuthorizationUrl(requestToken);
			System.out.println("Url to use: ");
			System.out.println(authUrl);
			
			System.out.println("And paste the verifier here");
	        System.out.print(">>");
	        final String oauthVerifier = in.nextLine();
	        System.out.println();
			
			accessToken = service.getAccessToken(requestToken, oauthVerifier);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			in.close();
		}
		
	}
		
	public void getLeagueData() {
		
		OAuthentication();
		
		String LeagueURL = "http://fantasysports.yahooapis.com/fantasy/v2/league/nfl.l." + String.valueOf(yahooLeagueId) + "/settings";
		final OAuthRequest requestTeams = new OAuthRequest(Verb.GET, LeagueURL, service);
		service.signRequest(accessToken, requestTeams); // the access token from step 4
		final Response leagueResponse = requestTeams.send();
		
		try {
			InputSource input = new InputSource();
			String teamsXML = leagueResponse.getBody();
			input.setCharacterStream(new StringReader(teamsXML));
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document league = builder.parse(input);
			NodeList leagueNodes = league.getElementsByTagName("league");
			Element leagueNode = (Element) leagueNodes.item(0);
			leagueName = getCharacterDataFromElement((Element)leagueNode.getElementsByTagName("name").item(0));
			numTeams = Integer.valueOf(getCharacterDataFromElement((Element) leagueNode.getElementsByTagName("num_teams").item(0)));
			currentWeek = Integer.valueOf(getCharacterDataFromElement((Element) leagueNode.getElementsByTagName("current_week").item(0)));
			Element positions = (Element) leagueNode.getElementsByTagName("roster_positions").item(0);
			NodeList rosterPositions = positions.getElementsByTagName("roster_position");
			for (int i = 0; i < rosterPositions.getLength(); i++) {
				Element position = (Element) rosterPositions.item(i);
				String positionName = getCharacterDataFromElement((Element) position.getElementsByTagName("position").item(0));
				if (Position.isValidPosition(positionName)) {
					String count = getCharacterDataFromElement((Element) position.getElementsByTagName("count").item(0));
					if (Integer.valueOf(count) > 1) {
						for (int j = 1; j <= Integer.valueOf(count); j++) {
							this.leaguePositions.add(new LeaguePosition(positionName + j, Arrays.asList(Position.valueOf(positionName))));
						}
					} else {
					    this.leaguePositions.add(new LeaguePosition(positionName + count, Arrays.asList(Position.valueOf(positionName))));
					}
				} else {
					String count = getCharacterDataFromElement((Element) position.getElementsByTagName("count").item(0));
					List<Position> possiblePositions = new ArrayList<Position>();
					for (String posssiblePosition : positionName.split("/")) {
						if (posssiblePosition.equals("W")) {
							possiblePositions.add(Position.WR);
						} else if (posssiblePosition.equals("R")) {
							possiblePositions.add(Position.RB);
						} else if (posssiblePosition.equals("T")) {
							possiblePositions.add(Position.TE);
						} else if (posssiblePosition.equals("Q")) {
							possiblePositions.add(Position.QB);
						}
					}if (Integer.valueOf(count) > 1) {
						for (int j = 1; j <= Integer.valueOf(count); j++) {
							this.leaguePositions.add(new LeaguePosition(positionName + j, possiblePositions));
						}
					} else {
						this.leaguePositions.add(new LeaguePosition(positionName + count, possiblePositions));
					}
					
				}
			}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String PlayersURLbase = "http://fantasysports.yahooapis.com/fantasy/v2/team/nfl.l." + String.valueOf(this.yahooLeagueId) + ".t.";
		
		
		for (int i = 4; i < 17; i++) {
			numPlayersOnByeForWeek.put(i, 0);
		}
		
		int numberDefenses = 0;
		
		for (int i = 1; i <= numTeams; i++) {
		
		String PlayersURL = PlayersURLbase + String.valueOf(i) + "/roster";
		final OAuthRequest requestRosters = new OAuthRequest(Verb.GET, PlayersURL, service);
		service.signRequest(accessToken, requestRosters);
		final Response playersResponse = requestRosters.send();
		try {
			InputSource input = new InputSource();
			String teamsXML = playersResponse.getBody();
			input.setCharacterStream(new StringReader(teamsXML));
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document teams = builder.parse(input);
			NodeList teamNodes = teams.getElementsByTagName("team");
			
			
		    for (int j = 0; j < teamNodes.getLength(); j++) {
		        Element team = (Element) teamNodes.item(j);
		        
		        NodeList name = team.getElementsByTagName("name");
		        String teamName = getCharacterDataFromElement((Element) name.item(0));
		        Team thisTeam = new Team(teamName);
		        
		        NodeList rosterNode = team.getElementsByTagName("roster");
		        Element roster = (Element) rosterNode.item(0);
		        
		        NodeList playersNode = roster.getElementsByTagName("players");
		        NodeList playerNodes = ((Element) playersNode.item(0)).getElementsByTagName("player");
		        for (int k = 0; k < playerNodes.getLength(); k++) {
		        	Element player = (Element) playerNodes.item(k);
		        	
			        NodeList playerIdNode = player.getElementsByTagName("player_id");
			        String playerIdText = getCharacterDataFromElement((Element) playerIdNode.item(0));
			        int playerId = Integer.valueOf(playerIdText);
		        	
		        	NodeList playerInfo = player.getElementsByTagName("name");
		        	NodeList fullName = ((Element) playerInfo.item(0)).getElementsByTagName("full");
		        	String playerName = getCharacterDataFromElement(((Element) fullName.item(0)));
		        	
		        	NodeList positionNode = player.getElementsByTagName("display_position");
		        	String playerPosition = getCharacterDataFromElement(((Element) positionNode.item(0)));
		        	
		        	NodeList byeWeekProjectionssNode = player.getElementsByTagName("bye_weeks");
		        	NodeList byeWeekProjectionsNode = ((Element) byeWeekProjectionssNode.item(0)).getElementsByTagName("week");
		        	String week = getCharacterDataFromElement(((Element) byeWeekProjectionsNode.item(0)));
		        	if (playerPosition.equals("DEF")) {
		        		int byeWeekProjectionsNumber = Integer.valueOf(week);
		        		Player thisPlayer = new Player(playerName, Position.DEF, byeWeekProjectionsNumber, playerId);
		        		this.playersById.put(playerId, thisPlayer);
		        		thisTeam.addPlayer(playerId);
		        		numberDefenses++;
		        	} else if (Position.isValidPosition(playerPosition)) {
		        	    int byeWeekProjectionsNumber = Integer.valueOf(week);
		            	int currentNumPlayersOnBye = numPlayersOnByeForWeek.get(byeWeekProjectionsNumber);
		            	numPlayersOnByeForWeek.put(byeWeekProjectionsNumber, currentNumPlayersOnBye + 1);
		        	    Player thisPlayer = new Player(playerName, Position.valueOf(playerPosition), byeWeekProjectionsNumber, playerId);
		        	    this.playersById.put(playerId, thisPlayer);
		        	    thisTeam.addPlayer(playerId);
		        	}
		        }
		        teamList.add(thisTeam);
		    }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		}
		numDefenses = numberDefenses;
	}
	
	  public static String getCharacterDataFromElement(Element e) {
		    Node child = e.getFirstChild();
		    if (child instanceof CharacterData) {
		        CharacterData cd = (CharacterData) child;
		        return cd.getData();
		    } else {
		        return "";
		    }
	  }

}
