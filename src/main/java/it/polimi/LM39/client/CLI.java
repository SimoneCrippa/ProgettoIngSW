package it.polimi.LM39.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.polimi.LM39.exception.InvalidActionTypeException;
import it.polimi.LM39.model.ActionBonus;
import it.polimi.LM39.model.Excommunication;
import it.polimi.LM39.model.FamilyMember;
import it.polimi.LM39.model.FamilyMembersLocation;
import it.polimi.LM39.model.MainBoard;
import it.polimi.LM39.model.PersonalBoard;
import it.polimi.LM39.server.NetworkPlayer;

/*
 * COMMAND LINE INTERFACE
 */
public class CLI extends UserInterface {

	/*
	 * input scanner
	 */
	BufferedReader userInput;
    /**
     * Default constructor: initialize the inputstream
     */
    public CLI() {
    	userInput = new BufferedReader(new InputStreamReader(System.in));
    }


    
    /*
     * print the mainboard (only towers)
     * 
     */
	@Override
	public void printMainBoard(MainBoard mainBoard) {
		String[][] cardsOnTowers = mainBoard.getCardNamesOnTheTowers();
//		FamilyMembersLocation familyMemberPositions = mainBoard.getFamilyMembersLocation();
//		FamilyMember[][] familyOnTowers = familyMemberPositions.getFamilyMembersOnTheTowers();
		FamilyMembersLocation familyMemberPositions = new FamilyMembersLocation();
		FamilyMember[][] familyOnTowers = familyMemberPositions.getFamilyMembersOnTheTowers();
		System.out.println("╔══════════════════════╦═══════════════╦══════════════════════╦═══════════════╦══════════════════════╦═══════════════╦══════════════════════╦═══════════════╗%n");
		System.out.printf("║%-22s║%-15s║%-22s║%-15s║%-22s║%-15s║%-22s║%-15s║%n"
		,this.getCardOnTower(cardsOnTowers[0][0],mainBoard,0,0),this.getPlayerColor(familyOnTowers[0][0]),this.getCardOnTower(cardsOnTowers[0][1],mainBoard,0,1)
		,this.getPlayerColor(familyOnTowers[0][1]),this.getCardOnTower(cardsOnTowers[0][2],mainBoard,0,2),this.getPlayerColor(familyOnTowers[0][2])
		,this.getCardOnTower(cardsOnTowers[0][3],mainBoard,0,3),this.getPlayerColor(familyOnTowers[0][3]));
		System.out.printf("║                      ║%-15s║                      ║%-15s║                      ║%-15s║                      ║%-15s║%n"
		,this.getFamilyMemberColor(familyOnTowers[0][0]),this.getFamilyMemberColor(familyOnTowers[0][1])
		,this.getFamilyMemberColor(familyOnTowers[0][2]),this.getFamilyMemberColor(familyOnTowers[0][3]));
		System.out.println("╠══════════════════════╬═══════════════╬══════════════════════╬═══════════════╬══════════════════════╬═══════════════╬══════════════════════╬═══════════════╣%n");
		System.out.printf("║%-22s║%-15s║%-22s║%-15s║%-22s║%-15s║%-22s║%-15s║%n"
		,this.getCardOnTower(cardsOnTowers[1][0],mainBoard,1,0),this.getPlayerColor(familyOnTowers[1][0]),this.getCardOnTower(cardsOnTowers[1][1],mainBoard,1,1)
		,this.getPlayerColor(familyOnTowers[1][1]),this.getCardOnTower(cardsOnTowers[1][2],mainBoard,1,2),this.getPlayerColor(familyOnTowers[1][2])
		,this.getCardOnTower(cardsOnTowers[1][3],mainBoard,1,3),this.getPlayerColor(familyOnTowers[1][3]));
		System.out.printf("║                      ║%-15s║                      ║%-15s║                      ║%-15s║                      ║%-15s║%n"
		,this.getFamilyMemberColor(familyOnTowers[1][0]),this.getFamilyMemberColor(familyOnTowers[1][1])
		,this.getFamilyMemberColor(familyOnTowers[1][2]),this.getFamilyMemberColor(familyOnTowers[1][3]));
		System.out.println("╠══════════════════════╬═══════════════╬══════════════════════╬═══════════════╬══════════════════════╬═══════════════╬══════════════════════╬═══════════════╣%n");
		System.out.printf("║%-22s║%-15s║%-22s║%-15s║%-22s║%-15s║%-22s║%-15s║%n"
		,this.getCardOnTower(cardsOnTowers[2][0],mainBoard,2,0),this.getPlayerColor(familyOnTowers[2][0]),this.getCardOnTower(cardsOnTowers[2][1],mainBoard,2,1)
		,this.getPlayerColor(familyOnTowers[2][1]),this.getCardOnTower(cardsOnTowers[2][2],mainBoard,2,2),this.getPlayerColor(familyOnTowers[2][2])
		,this.getCardOnTower(cardsOnTowers[2][3],mainBoard,2,3),this.getPlayerColor(familyOnTowers[2][3]));
		System.out.printf("║                      ║%-15s║                      ║%-15s║                      ║%-15s║                      ║%-15s║%n"
		,this.getFamilyMemberColor(familyOnTowers[2][0]),this.getFamilyMemberColor(familyOnTowers[2][1])
		,this.getFamilyMemberColor(familyOnTowers[2][2]),this.getFamilyMemberColor(familyOnTowers[2][3]));
		System.out.println("╠══════════════════════╬═══════════════╬══════════════════════╬═══════════════╬══════════════════════╬═══════════════╬══════════════════════╬═══════════════╣%n");
		System.out.printf("║%-22s║%-15s║%-22s║%-15s║%-22s║%-15s║%-22s║%-15s║%n"
		,this.getCardOnTower(cardsOnTowers[3][0],mainBoard,3,0),this.getPlayerColor(familyOnTowers[3][0]),this.getCardOnTower(cardsOnTowers[3][1],mainBoard,3,1)
		,this.getPlayerColor(familyOnTowers[3][1]),this.getCardOnTower(cardsOnTowers[3][2],mainBoard,3,2),this.getPlayerColor(familyOnTowers[3][2])
		,this.getCardOnTower(cardsOnTowers[3][3],mainBoard,3,3),this.getPlayerColor(familyOnTowers[3][3]));
		System.out.printf("║                      ║%-15s║                      ║%-15s║                      ║%-15s║                      ║%-15s║%n"
		,this.getFamilyMemberColor(familyOnTowers[3][0]),this.getFamilyMemberColor(familyOnTowers[3][1])
		,this.getFamilyMemberColor(familyOnTowers[3][2]),this.getFamilyMemberColor(familyOnTowers[3][3]));
		System.out.println("╚══════════════════════╩═══════════════╩══════════════════════╩═══════════════╩══════════════════════╩═══════════════╩══════════════════════╩═══════════════╝%n");
		
	}
	/*
	 * print the market zone, if a space is empty print the space bonuses
	 */
	public void printMarket(MainBoard mainBoard){
		int marketSize = 4;					//TODO choose a class to put marketSize value (possibly room)
		FamilyMembersLocation location = mainBoard.getFamilyMembersLocation();
		FamilyMember[] market = location.getFamilyMembersOnTheMarket();
		int i = 0;
		System.out.printf("╔═══════════════╦═══════════════╗%n");
		while(i < marketSize){
			System.out.printf("║%-15s║%-15s║%n",this.marketTilePrinter(market[i], mainBoard, i),this.getFamilyMemberColor(market[i]));
		}
		System.out.printf("╚═══════════════╩═══════════════╝%n");
	}
	/*
	 * support method for printmarket, print the family member on the market or the bonus if there is no family member
	 */
	public String marketTilePrinter(FamilyMember familyMember, MainBoard mainBoard, int index){
		ActionBonus bonus;
		if(familyMember == null){
			 bonus = mainBoard.marketBonuses[index];
			 if(bonus.resources.coins != 0)
				 return "coins" + bonus.resources.coins;
			 else if(bonus.resources.council != 0)
				 return "council" + bonus.resources.council;
			 else if(bonus.resources.servants != 0)
				 return "servants" + bonus.resources.servants;
			 else if(bonus.resources.woods != 0)
				 return "woods:" + bonus.resources.woods;
			 else if(bonus.resources.stones != 0)
				 return "stones:" + bonus.resources.stones;
			 else if(bonus.points.faith != 0)
				 return "faith" + bonus.points.faith;
			 else if(bonus.points.victory != 0)
				 return "victory" + bonus.points.victory;
			 else if(bonus.points.military != 0)
				 return "military" + bonus.points.military;
		}
		else
			return "player" + familyMember.playerColor;
		return "";
	}
	/*
	 * print the council palace with relative bonuses
	 */
	public void printCouncilPalace(MainBoard mainBoard){
		FamilyMembersLocation location = mainBoard.getFamilyMembersLocation();
		ArrayList<FamilyMember> councilPalace = location.getFamilyMembersAtTheCouncilPalace();
		int i = 0;
		ActionBonus palaceBonus = mainBoard.councilPalaceBonus;
		System.out.printf("╔════════════════════════════════════╗%n");
		while(i < councilPalace.size()){
			System.out.printf("║%-15s%n",this.getPlayerColor(councilPalace.get(i)));
			System.out.printf("║%-15s%n",this.getFamilyMemberColor(councilPalace.get(i)));
			i++;
		}
		System.out.printf("╚════════════════════════════════════╝%n");
		if(palaceBonus.resources.coins != 0)
			System.out.println("Coins Bonus:" + palaceBonus.resources.coins);
		if(palaceBonus.resources.council != 0)
			System.out.println("Council Privilege Bonus:" + palaceBonus.resources.council);
		if(palaceBonus.resources.woods != 0)
			System.out.println("Woods Bonus:" + palaceBonus.resources.woods);
		if(palaceBonus.resources.stones != 0)
			System.out.println("Stones Bonus:" + palaceBonus.resources.stones);
		if(palaceBonus.resources.servants != 0)
			System.out.println("Servants Bonus:" + palaceBonus.resources.servants);
		if(palaceBonus.points.faith != 0)
			System.out.println("Faith Bonus:" + palaceBonus.points.faith);
		if(palaceBonus.points.military != 0)
			System.out.println("Military Bonus:" + palaceBonus.points.military);
		if(palaceBonus.points.victory != 0)
			System.out.println("Victory Bonus:" + palaceBonus.points.victory);
	}
	/*
	 * print harvest and production area
	 */
	public void printHarvestAndProduction(MainBoard mainBoard) throws InvalidActionTypeException{
		FamilyMembersLocation location = mainBoard.getFamilyMembersLocation();
		ArrayList<FamilyMember> harvestArea = location.getFamilyMembersOnProductionOrHarvest("harvest");
		ArrayList<FamilyMember> productionArea = location.getFamilyMembersOnProductionOrHarvest("production");
		int harvestProductionSize = 4;
		int i = 0;
		System.out.printf("╔══════════════════════════════════════╗%n");	//TODO ask if also production/harvest space can have bonuses
		while(i < harvestProductionSize){
			System.out.printf("║%-15s║%n",this.getPlayerColor(harvestArea.get(i)));
			System.out.printf("║%-15s║%n",this.getFamilyMemberColor(harvestArea.get(i)));
			i++;
		}
		System.out.printf("╚══════════════════════════════════════╝%n");
		i = 0;
		System.out.printf("╔══════════════════════════════════════╗%n");	
		while(i < harvestProductionSize){
			System.out.printf("║%-15s║%n",this.getPlayerColor(productionArea.get(i)));
			System.out.printf("║%-15s║%n",this.getFamilyMemberColor(productionArea.get(i)));
			i++;
		}
		System.out.printf("╚══════════════════════════════════════╝%n");
		
	}

	/*
	 * print player's possessed cards, including leader and excommunications
	 */
	public void printPossesedCards(NetworkPlayer player, MainBoard mainBoard){
		PersonalBoard board = player.personalBoard;
		ArrayList<Integer> buildings = board.possessedBuildings;
		ArrayList<Integer> territories = board.possessedTerritories;
		ArrayList<Integer> ventures = board.possessedVentures;
		ArrayList<Integer> characters = board.possessedCharacters;
		ArrayList<String> leaders = board.possessedLeaders;
		System.out.println("Possesed buildings:%n");
		printCardType("Building",buildings);
		System.out.println("Possesed territories:%n");
		printCardType("Territory",territories);
		System.out.println("Possesed ventures:%n");
		printCardType("Venture",ventures);
		System.out.println("Possesed characters:%n");
		printCardType("Character",characters);
		System.out.println("Possesed leaders:%n");
		printCardType(leaders);
		System.out.println("Possesed excommunications:%n");
		//TODO print excommunication in some way
	}
	/*
	 * support method for printpossessedcards
	 */
	public void printCardType(String cardType, ArrayList<Integer> cardMap){
		int i = 0;
		while (cardMap.size() < i){
			if(("Territory").equals(cardType))
				System.out.println(MainBoard.territoryMap.get(cardMap.get(i)));
			else if(("Building").equals(cardType))
				System.out.println(MainBoard.buildingMap.get(cardMap.get(i)));
			else if(("Venture").equals(cardType))
				System.out.println(MainBoard.ventureMap.get(cardMap.get(i)));
			else if(("Character").equals(cardType))
				System.out.println(MainBoard.characterMap.get(cardMap.get(i)));
			i++;
			System.out.println("%n");
		}
	}
	/*
	 * support method for printpossessedcards
	 */
	public void printCardType(ArrayList<String> cardMap){
		Iterator<String> iterator = cardMap.iterator();
		while(iterator.hasNext()){
			System.out.println(iterator.next() + "%n");
		}
	}
	/*
	 * print dices values
	 */
	public void printDicesValues(MainBoard mainBoard){
		Integer[] diceValues = mainBoard.getDiceValues();
		System.out.println("Black dice:"+ diceValues[0] + "%nWhite dice:"+ diceValues[1] + "%nOrange dice:"+ diceValues[2]);
	}
	/*
	 * print excommunications on the board
	 */
	public void printExcommunication(MainBoard mainBoard){
		Integer[] excommunications = mainBoard.excommunicationsOnTheBoard;
		System.out.println("First period:"+ excommunications[0] + "%nSecond period:"+ excommunications[1] + "%nThird period:"+ excommunications[2]);
	}
	/*
	 * support method: return free if there is no family member on the space, otherwise it returns the player's color
	 */
	public String getPlayerColor(FamilyMember familyMember){
		if(familyMember == null)
			return "free";
		return "Player:" + familyMember.playerColor;
	}
	/*
	 * support method: return free if there is no family member on the space, otherwise it returns the family member's color
	 */
	public String getFamilyMemberColor(FamilyMember familyMember){
		if(familyMember == null)
			return "";
		return "Family:" + familyMember.color;
	}
	/*
	 * support method: return no card if there is no card on the selected space otherwise it returns the specific bonus
	 */
	public String getCardOnTower(String cardOnTower, MainBoard mainBoard, int i, int j){
		ActionBonus[][] bonus;
		if(cardOnTower == null){
			bonus = mainBoard.getTowersBonuses();
			 if(bonus[i][j].resources.coins != 0)
				 return "coins" + bonus[i][j].resources.coins;
			 else if(bonus[i][j].resources.council != 0)
				 return "council" + bonus[i][j].resources.council;
			 else if(bonus[i][j].resources.servants != 0)
				 return "servants" + bonus[i][j].resources.servants;
			 else if(bonus[i][j].resources.woods != 0)
				 return "woods:" + bonus[i][j].resources.woods;
			 else if(bonus[i][j].resources.stones != 0)
				 return "stones:" + bonus[i][j].resources.stones;
			 else if(bonus[i][j].points.faith != 0)
				 return "faith" + bonus[i][j].points.faith;
			 else if(bonus[i][j].points.victory != 0)
				 return "victory" + bonus[i][j].points.victory;
			 else if(bonus[i][j].points.military != 0)
				 return "military" + bonus[i][j].points.military;
		}
		else
			return cardOnTower;
		return "";
	}
	/*
	 * print the message to the client
	 * 
	 */
	@Override
	public String printMessage(String message) {
		Logger logger = Logger.getLogger(CLI.class.getName());
		String response = "";
		System.out.println(message + "%n");
		try{
			response = userInput.readLine();
		}catch(IOException e){
			logger.log(Level.INFO, "Invalid Input", e);
			this.printMessage(message);
		}
		return response;
	}
}