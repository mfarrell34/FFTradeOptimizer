package tradeOptimizer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import tradeOptimizer.league.FantasyLeague;
import tradeOptimizer.league.Team;
import tradeOptimizer.trades.Trade;

public class ExcelFileGenerator {
	List<Team> teams;
	
	public ExcelFileGenerator(List<Team> teamsList) {
		teams = teamsList;
	}
	
	public void writeFile() {
    XSSFWorkbook workbook = new XSSFWorkbook();
	
	for (Team team : teams) {

        XSSFSheet sheet = workbook.createSheet(team.getTeamName());

		int rowNum = 0;
		int cellNum = 0;
		int largestTeamTrade = team.largestTrade;
		Row row = sheet.createRow(rowNum++);
		Cell cell = row.createCell(cellNum++);
		cell.setCellValue("Other Team");
		cell = row.createCell(cellNum++);
		cell.setCellValue("Your Proj Point Increase");
		for (int i = 0; i < largestTeamTrade; i++) {
		cell = row.createCell(cellNum++);
		cell.setCellValue("Player to Get");
		}
		cell = row.createCell(cellNum++);
		cell.setCellValue("Player to Send");
		cell = row.createCell(cellNum++);
		cell.setCellValue("Player To Send");
		cell = row.createCell(cellNum++);
		cell.setCellValue("Other Team Proj Point Increase");
		List<Trade> teamTrades = team.getTrades();
    	Collections.sort(teamTrades);
        for (Trade trade : teamTrades) {
        	try {
		    cellNum = 0;
		    row = sheet.createRow(rowNum++);
			cell = row.createCell(cellNum++);
			cell.setCellValue(trade.getOtherTeamName());
			cell = row.createCell(cellNum++);
			cell.setCellValue(trade.getThisTeamPointIncrease());
			List<Integer> tradePlayers = trade.getOtherTeamPlayers();
			for (int i = 0; i < largestTeamTrade; i++) {
			    cell = row.createCell(cellNum++);
			if (i < tradePlayers.size()) {
			    cell.setCellValue(FantasyLeague.getPlayerById(tradePlayers.get(i)).getName());
			} else {
				cell.setCellType(Cell.CELL_TYPE_BLANK);
			}
			}
			cell = row.createCell(cellNum++);
			tradePlayers = trade.getThisTeamPlayers();
			cell.setCellValue(FantasyLeague.getPlayerById(tradePlayers.get(0)).getName());
			cell = row.createCell(cellNum++);
			if (tradePlayers.size() > 1) {
				String playerName = FantasyLeague.getPlayerById(tradePlayers.get(1)).getName();
				if (!playerName.substring(0,4).equals("Add:")) {
					cell.setCellValue(playerName);
				} else {
					cell.setCellType(Cell.CELL_TYPE_BLANK);
				}
			} else {
				cell.setCellType(Cell.CELL_TYPE_BLANK);
			}
			cell = row.createCell(cellNum++);
			cell.setCellValue(trade.getOtherTeamPointIncrease());
        } catch (Exception ex) {
        	//don't print trade if player Id isn't found in league map
		}	
	}
	
    try
    {
        //Write the workbook in file system
        FileOutputStream out = new FileOutputStream(new File(FantasyLeague.getLeagueName() + "_fantasy_trades.xlsx"));
        workbook.write(out);
        out.close();
        workbook.close();
        
    } 
    catch (Exception e) 
    {
        e.printStackTrace();
    }
	}
	}
}
