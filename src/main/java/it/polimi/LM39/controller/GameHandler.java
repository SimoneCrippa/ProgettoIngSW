package it.polimi.LM39.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.*;

import it.polimi.LM39.exception.CardNotFoundException;
import it.polimi.LM39.exception.FailedToReadFileException;
import it.polimi.LM39.exception.FailedToRegisterEffectException;
import it.polimi.LM39.exception.InvalidActionTypeException;
import it.polimi.LM39.exception.NotEnoughPointsException;
import it.polimi.LM39.exception.NotEnoughResourcesException;
import it.polimi.LM39.model.*;
import it.polimi.LM39.model.Character;
import it.polimi.LM39.model.characterpermanenteffect.CharacterPermanentEffect;
import it.polimi.LM39.model.excommunicationpermanenteffect.NoVictoryForCard;
import it.polimi.LM39.server.NetworkPlayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
/**
 * 
 */
public class GameHandler {
	//TODO fix long throws with logger
	private Logger logger = Logger.getLogger(GameHandler.class.getName());

	public Integer marketSize;
	
	public Integer harvestAndProductionSize;
    
	private Integer period;

    private Integer round;
    
    public MainBoard mainBoard = new MainBoard();
     
    private ArrayList<String> playerActionOrder = new ArrayList<String>();
    /*
     * probably useless attributes
     */
 /* public BuildingHandler buildingHandler = new BuildingHandler();		
    
    public TerritoryHandler territoryHandler = new TerritoryHandler();

    public VentureHandler ventureHandler = new VentureHandler();

    public CharacterHandler characterHandler = new CharacterHandler();

    public LeaderHandler leaderHandler = new LeaderHandler();

    public ExcommunicationHandler excommunicationHandler = new ExcommunicationHandler(); */	

    public PersonalBoardHandler personalBoardHandler = new PersonalBoardHandler();
    
    public CouncilHandler councilHandler = new CouncilHandler(); 	
    
    public GsonReader gsonReader = new GsonReader();

    public void setPeriod(Integer period){
    	this.period=period;
    }
    public void setRound(Integer round){
    	this.round=round;
    }
    
    
    public void rollTheDices() {
    	Integer[] diceValues = new Integer[4];
    	for(int i=0;i<3;i++){
    	Random rand = new Random();
    	diceValues[i] = (rand.nextInt(6) + 1);
    	//uncoloredFamilyMember
    	diceValues[3] = 0;
    	// There is a + 1 because rand.nextInt(6) generates number from 0 to 5 but we need from 1 to 6
    	}
    	mainBoard.setDiceValues(diceValues);    
    }

    public boolean getCard(Integer cardNumber,NetworkPlayer player, Integer towerNumber) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	boolean cardGotten=false;
    	    		switch(towerNumber){
    		    		case 0: Territory territory=mainBoard.territoryMap.get(cardNumber);
    		    			cardGotten=getTerritoryCard(territory,player,cardNumber);
    		    			break;
    		    		case 1: Character character=mainBoard.characterMap.get(cardNumber);
    		    			cardGotten=getCharacterCard(character,player,cardNumber);
    		    			break;
    		    		case 2: Building building=mainBoard.buildingMap.get(cardNumber);
    		    			cardGotten=getBuildingCard(building,player,cardNumber);
    		    			break;
    		    		case 3: Venture venture=mainBoard.ventureMap.get(cardNumber);
    		    			cardGotten=getVentureCard(venture,player,cardNumber);
    		    			break;
    		    		default: player.setMessage("This tower doesn't exist!");
    		    			break;
    	    		}
    	    		return cardGotten;
    }
    
    
    public boolean getTerritoryCard(Territory territory,NetworkPlayer player,Integer cardNumber) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    	ArrayList<Integer> possessedTerritories = player.personalBoard.getPossessions("Territory");
    	int militaryPoints = player.points.getMilitary();
    	if (possessedTerritories.size()<6){
    		//if there is a place for the territory
    		if(possessedTerritories.size()<2 || militaryPoints >= player.personalMainBoard.militaryForTerritory[possessedTerritories.size() -2]) {
    			//add the territory to PersonalBoard
    			player.personalBoard.setPossessions(cardNumber,"Territory");
    			//get the instant effect
    			CardHandler cardHandler = new CardHandler(this);
    			cardHandler.doInstantEffect(territory.instantBonuses, player);
    			return true;
    			}
    		else
        		player.setMessage("You don't have enough military points");
    	}
    	else 
    		player.setMessage("You can't have more than 6 territories! ");
    	return false;
    }

    public boolean getCharacterCard(Character character,NetworkPlayer player,Integer cardNumber) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    	
    	ArrayList<Integer> possessedCharacters = player.personalBoard.getPossessions("Character");
		if (possessedCharacters.size()<6){
	    		try {
	    			coinsForCharacter(player,character);
				} catch (NotEnoughResourcesException e) {
					player.setMessage("You don't have enough coins!");
					logger.log(Level.INFO, "Not enough coins", e);
					return false;
				}
	    		CardHandler cardHandler = new CardHandler(this);
    			player.personalBoard.setPossessions(cardNumber,"Character");
    			cardHandler.doInstantEffect(character.instantBonuses, player);
    			cardHandler.activateCharacter(character.permanentEffect, player);
    			return true;
    	}
		else
			player.setMessage("You can't have more than 6 characters!");
    	return false;
    }
    
    public void coinsForCharacter(NetworkPlayer player ,Character character) throws NotEnoughResourcesException{
    	player.resources.setCoins(-character.costCoins);
    }

    public boolean getBuildingCard(Building building,NetworkPlayer player,Integer cardNumber) throws IOException{
    	ArrayList<Integer> possessedBuildings = player.personalBoard.getPossessions("Building");
		if (possessedBuildings.size()<6){
	    		try {
	    			resourcesForBuilding(player ,building);
					addCardPoints(building.instantBonuses,player);
				} catch (NotEnoughResourcesException | NotEnoughPointsException e) {
					player.setMessage("You don't have enough resources or points!");
					logger.log(Level.INFO, "Not enough resources or points", e);
					return false;
				}
	    		player.personalBoard.setPossessions(cardNumber,"Building");
	    		return true;
	    	
    	}
		else
			player.setMessage("You can't have more than 6 buildings!");
    	return false;
    }
    
    public void resourcesForBuilding(NetworkPlayer player ,Building building) throws NotEnoughResourcesException{
    	subCardResources(building.costResources,player);
    }
    
    public boolean getVentureCard(Venture venture,NetworkPlayer player,Integer cardNumber) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    	ArrayList<Integer> possessedVentures = player.personalBoard.getPossessions("Venture");
    	Integer choice = 0;
		if (possessedVentures.size()<6){
	    	if(venture.costMilitary!=0 && (venture.costResources.coins!=0 || venture.costResources.woods!=0 || venture.costResources.stones!=0 || venture.costResources.servants!=0)) {
	    		//ask to the player what payment he wants to do
	    		player.setMessage("What payment do you want to do? 1 or 2");
	    		//get the player response
	    		choice = Integer.parseInt(player.sendMessage());
	    	}
	    	if(venture.costMilitary==0 || choice == 2){
	    		try {
	    			resourcesForVenture(player,venture);
	    		} catch (NotEnoughResourcesException e) {
	    			player.setMessage("You don't have enough resources!");
	    			logger.log(Level.INFO, "Not enough resources", e);
	    			return false;
				}
	    	}
	    
	    	else if((venture.costMilitary!=0 || choice ==1) && (player.points.getMilitary() >= venture.neededMilitary)){
	    		try {
	    			player.points.setMilitary(-venture.costMilitary);
	    		} catch (NotEnoughPointsException e) {
	    			player.setMessage("You don't have enough military points!");
	    			logger.log(Level.INFO, "Not enough military points", e);
	    			return false;
	    		}
	    	}
	    	else{
	    		player.setMessage("You don't have enough military points!");
    			return false;
    			}
	    
	    	player.personalBoard.setPossessions(cardNumber,"Venture");
	    	player.points.setFinalVictory(venture.finalVictory);
	    	CardHandler cardHandler = new CardHandler(this);
	    	cardHandler.doInstantEffect(venture.instant, player);
	    	return true;
	   }
		else
			player.setMessage("You can't have more than 6 ventures!");
    	return false;
    }
    
    public void resourcesForVenture(NetworkPlayer player ,Venture venture) throws NotEnoughResourcesException{
    	subCardResources(venture.costResources,player);
    }
    
    public Integer familyMemberColorToDiceValue(String familyMemberColor,NetworkPlayer player) throws IOException{
    	//The order followed is the one on the Game Board for the dices positions
    	Integer value = -1;
    	Integer[] diceValues = player.personalMainBoard.getDiceValues();
    	switch(familyMemberColor){
	    	case "black": value = diceValues[0];
	    		break;
	    	case "white": value = diceValues[1];
	    		break;
	    	case "orange": value = diceValues[2];
	    		break;
	    	case "uncolored": value = diceValues[3];
	    		break;
	    	default: player.setMessage("Invalid familyMemberColor");
	    		break;
    	}
    	return value;
    }
    
    public boolean addFamilyMemberToTheTower(FamilyMember familyMember , String cardName, NetworkPlayer player) throws IOException, NotEnoughResourcesException, NotEnoughPointsException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CardNotFoundException {
        int i,j = 0;
        boolean coloredFamilyMemberOnTheTower = false;
        boolean uncoloredFamilyMemberOnTheTower = false;
        String[][] CardNamesOnTheTowers = mainBoard.getCardNamesOnTheTowers();
        FamilyMember[][] familyMembersOnTheTowers = player.personalMainBoard.familyMembersLocation.getFamilyMembersOnTheTowers(); // we use the player Personal MainBaord
      //search the coordinates of the card in the board
        int p = -1;
        int k = -1;
        for(i=0;i<4;i++)
        	for(j=0; j<4;j++){
        		if((CardNamesOnTheTowers[i][j]).compareToIgnoreCase(cardName) == 0){
        			 //store the values
        			 p=i;
        		     k=j;
        		     //exit the cicles
        		     j=4;
        		     i=4;
        		}
        			
        	}
        System.out.println(p + " " + k);
        //to store i and j as the coordinates of the position interested, the if check if the player can get the card with a specific family member
        if(familyMembersOnTheTowers[p][k].color.equals("") && (familyMemberValue(familyMember,player) >= (mainBoard.getTowersValue())[p][k])){
        	//if the place is free and the family member has an high enough value, ((i+1)*2)-1 is to convert the value i of the matrix to the value of the floor in dice
        	for(i=0;i<4;i++){
        		if((familyMembersOnTheTowers[i][k].playerColor).equals(familyMember.playerColor)){
        			//if there is one of my family members on the tower
        			if (("uncolored").equals(familyMembersOnTheTowers[i][k].color))
        				//if this family member is uncolored
        				uncoloredFamilyMemberOnTheTower=true;
        			else
        				//if this family member is colored
        				coloredFamilyMemberOnTheTower=true;
        		}
        	}
        	if ((uncoloredFamilyMemberOnTheTower==true && coloredFamilyMemberOnTheTower==false) || (coloredFamilyMemberOnTheTower==true && ("uncolored").equals(familyMember.color))){
        	//TODO delete
        		System.out.println("here1");
        	//if there is an uncolored family member on the tower or there is a colored one but the player uses an uncolored family member
        		if(player.resources.getCoins()>=3 && getCard(cardNameToInteger(cardName),player,k)){
        			//TODO delete
            		System.out.println("here2");
	        		try {
						player.resources.setCoins(player.personalMainBoard.occupiedTowerCost);
					} catch (NotEnoughResourcesException e) {
						player.setMessage("You don't have enough resources!");
						logger.log(Level.INFO, "Not enough resources", e);
						return false;
					}
	        		setActionBonus((mainBoard.getTowersBonuses())[p][k],player);
	        		(mainBoard.familyMembersLocation.getFamilyMembersOnTheTowers()[p][k].playerColor)=(familyMember.playerColor);
	        		(mainBoard.familyMembersLocation.getFamilyMembersOnTheTowers()[p][k].color)=(familyMember.color);
	        		removeCard(p,k);
	        		return true;
	        		}
        		else
        			player.setMessage("You don't have the necessary resources!");
	        	}
        	if (uncoloredFamilyMemberOnTheTower==false && coloredFamilyMemberOnTheTower==false){
        		//TODO delete
        		System.out.println("here3");
        		//if there is none of my family members
        		for(i=0;i<4 && familyMembersOnTheTowers[i][k].playerColor.equals("");i++){}
        		if(i==4 && getCard(cardNameToInteger(cardName),player,k)){
        			//TODO delete
            		System.out.println("here4");
        			//if the tower is free
        			(mainBoard.familyMembersLocation.getFamilyMembersOnTheTowers()[p][k].playerColor)=(familyMember.playerColor);
        			(mainBoard.familyMembersLocation.getFamilyMembersOnTheTowers()[p][k].color)=(familyMember.color);
        			System.out.println(familyMembersOnTheTowers[p][k].playerColor + " " + familyMembersOnTheTowers[p][k].color);
        			setActionBonus((mainBoard.getTowersBonuses())[p][k],player);
        			removeCard(p,k);
	        		return true;
        		}
        		else{
        			//TODO delete
            		System.out.println("here5");
        			//if the tower is occupied
        			if(player.resources.getCoins()>=3 && getCard(cardNameToInteger(cardName),player,k)){
        				//TODO delete
                		System.out.println("here6");
    	        		try {
							player.resources.setCoins(player.personalMainBoard.occupiedTowerCost);
						} catch (NotEnoughResourcesException e) {
							player.setMessage("You don't have enough resources");
							logger.log(Level.INFO, "Not enough resources", e);
							return false;
						}
    	        		setActionBonus((mainBoard.getTowersBonuses())[p][k],player);
    	        		(mainBoard.familyMembersLocation.getFamilyMembersOnTheTowers()[p][k].playerColor)=(familyMember.playerColor);
            			(mainBoard.familyMembersLocation.getFamilyMembersOnTheTowers()[p][k].color)=(familyMember.color);
            			removeCard(p,k);
    	        		return true;
        			}
        			else{
        				player.setMessage("You don't have the necessary resources!");
        				return false;
        				}
        		}
        	}
        }
        else{
        	player.setMessage("This position is occupied or your family member hasn't a value high enough!");	
        	return false;
        }
       return false; 
    }
    
    private void removeCard(Integer p, Integer k){
		String[][] cards = mainBoard.getCardNamesOnTheTowers();
		cards[p][k] = "";
		mainBoard.setCardNamesOnTheTowers(cards);
    }
    
    public boolean addFamilyMemberToTheCouncilPalace(FamilyMember familyMember, NetworkPlayer player) throws IOException, NotEnoughResourcesException, NotEnoughPointsException{
    	if (familyMemberValue(familyMember,player)>=1){
	    	mainBoard.familyMembersLocation.setFamilyMemberAtTheCouncilPalace(familyMember);
	    	setActionBonus(player.personalMainBoard.councilPalaceBonus, player);
    	}
    	else {
    		player.setMessage("Your Family Member must have a value of at least 1");
    		return false;
    	}
    	return true;
    }
    
    public void supportTheChurch (NetworkPlayer player) throws NotEnoughResourcesException, NotEnoughPointsException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    	//period + 2 is the minimum amount of faith points needed to support to the church for every period
    	CardHandler cardHandler = new CardHandler(this);
    	cardHandler.getInfo((mainBoard.excommunicationMap.get(player.personalMainBoard.excommunicationsOnTheBoard[period-1])).effect, player);
    	if(player.points.getFaith()>=(period+2)){
    		player.setMessage("Do you want to support the Church? yes or no");
    		String response = player.sendMessage();
    		response = checkResponse(response, player);
    		if(("yes").equals(response)){
    			setActionBonus(player.personalMainBoard.faithBonuses[player.points.getFaith()],player);
    			player.points.setFaith(0);
    		}
    	}
    	//if the player doesn't have enough faith points to support the Church or he decided not to support the Church
    	else{
    		player.setMessage("You don't have enough faith points or you answered no so you get the Excommunication");
    		player.setExcommunications(player.personalMainBoard.excommunicationsOnTheBoard[period-1]);
			cardHandler.activateExcommunication((mainBoard.excommunicationMap.get(player.personalMainBoard.excommunicationsOnTheBoard[period-1])).effect, player);
    	}
    }
    
    public void setActionBonus(ActionBonus actionBonus,NetworkPlayer player) throws NotEnoughResourcesException, NotEnoughPointsException{
    			addCardResources(actionBonus.resources,player);
    			addCardPoints(actionBonus.points,player);
		}
    
    //probably useless method
    /*
    public void addPlayerResources (PlayerResources resources, NetworkPlayer player) throws NotEnoughResourcesException, NotEnoughPointsException{
    	PlayerResources playerResources = player.resources;
    	playerResources.setCoins(resources.getCoins());
    	playerResources.setWoods(resources.getWoods());
    	playerResources.setStones(resources.getStones());
    	playerResources.setServants(resources.getServants());
    	ArrayList<Integer> list = new ArrayList<Integer>();
    	councilHandler.getCouncil(resources.getCouncil(),player,this,list);
    	player.resources=playerResources;
    }
    */
    
    //probably useless method
    /*
    public void addPlayerPoints (PlayerPoints points, NetworkPlayer player) throws NotEnoughPointsException{
    	PlayerPoints playerPoints = player.points;
    	playerPoints.setFaith(points.getFaith());
    	playerPoints.setFaith(points.getVictory());
    	playerPoints.setFinalVictory(points.getFinalVictory());
    	playerPoints.setMilitary(points.getMilitary());
    	player.points=playerPoints;
    }
    */
    
    
    public void subCardResources (CardResources resources, NetworkPlayer player) throws NotEnoughResourcesException{
    	PlayerResources playerResources = player.resources;
    	playerResources.setCoins(-resources.coins);
    	playerResources.setWoods(-resources.woods);
    	playerResources.setStones(-resources.stones);
    	playerResources.setServants(-resources.servants);
    	//if any of the set above fails this line of code is never reached
    	player.resources=playerResources;
    }
    
    public void addCardResources (CardResources resources, NetworkPlayer player) throws NotEnoughResourcesException, NotEnoughPointsException{
    	PlayerResources playerResources = player.resources;
    	playerResources.setCoins(resources.coins);
    	playerResources.setWoods(resources.woods);
    	playerResources.setStones(resources.stones);
    	playerResources.setServants(resources.servants);
    	councilHandler.getCouncil(resources.council,player,this,new ArrayList<Integer>());
    	player.resources=playerResources;
    }
    
    public void subCardPoints (CardPoints points, NetworkPlayer player) throws NotEnoughPointsException{
    	PlayerPoints playerPoints = player.points;
    	playerPoints.setFaith(-points.faith);
    	playerPoints.setFaith(-points.victory);
    	playerPoints.setMilitary(-points.military);
    	//if any of the set above fails this line of code is never reached
    	player.points=playerPoints;
    }
    
    public void addCardPoints (CardPoints points, NetworkPlayer player) throws NotEnoughPointsException{
    	PlayerPoints playerPoints = player.points;
    	playerPoints.setFaith(points.faith);
    	playerPoints.setFaith(points.victory);
    	playerPoints.setMilitary(points.military);
    	player.points=playerPoints;
    }
    
    public Integer familyMemberValue (FamilyMember familyMember, NetworkPlayer player) throws IOException{
    	Integer diceValue = familyMemberColorToDiceValue(familyMember.color,player);
    	return (diceValue+familyMember.getServants());
    }

    public boolean addFamilyMemberToTheMarket(FamilyMember familyMember, Integer position, NetworkPlayer player) throws IOException, NotEnoughResourcesException, NotEnoughPointsException {
    	FamilyMember[] familyMembersAtTheMarket = player.personalMainBoard.familyMembersLocation.getFamilyMembersOnTheMarket(); // we use the player Personal MainBaord
        if(familyMembersAtTheMarket[position].color.equals("") && position<=marketSize){ 
        	if(familyMemberValue(familyMember,player)>=1){
        	if(position==1 || position==2 || position==3 || position==4)
        		setActionBonus(player.personalMainBoard.marketBonuses[position-1],player);
        	else{
        		player.setMessage("Invalid position! the position must be between 1 and 4");
	        	return false;
	        	}
        	(mainBoard.familyMembersLocation.getFamilyMembersOnTheMarket()[position].color) = (familyMember.color);
        	(mainBoard.familyMembersLocation.getFamilyMembersOnTheMarket()[position].playerColor) = (familyMember.playerColor);
        	}
        	else {
            	player.setMessage("Your Family Member must have a value of at least 1");
            	return false;
            }
        }
        else {
        	player.setMessage("This place is occupied or not usable if two player game");
        	return false;
        }
        return true;
    }
    
    public boolean addFamilyMemberToProductionOrHarvest(FamilyMember familyMember, ArrayList<FamilyMember> familyMembersAtProductionOrHarvest, String actionType,NetworkPlayer player) throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NotEnoughResourcesException, NotEnoughPointsException {
    	int i;
    	//doAction is false by default
    	boolean doAction;
    	//to know if the action Harvest or Production can be done
    	Integer penalty=3;
    	//penalty in case of first slot already occupied
    		for(i=0;i<harvestAndProductionSize && i<familyMembersAtProductionOrHarvest.size() && !(familyMembersAtProductionOrHarvest.get(i).playerColor).equals(familyMember.playerColor);i++){}
    		if(i==familyMembersAtProductionOrHarvest.size()){
    			//if there isn't any of my family Members
    			for(i=0;i<harvestAndProductionSize && i<familyMembersAtProductionOrHarvest.size() && !familyMembersAtProductionOrHarvest.get(i).equals("");i++){}
    			//move i to the first free slot
    			if(i<familyMembersAtProductionOrHarvest.size()){
    				//if there is place in the Production Area
    				if(i==0){
    					//if there is no one
    					familyMembersAtProductionOrHarvest.get(i).color=familyMember.color;
    		    		familyMembersAtProductionOrHarvest.get(i).playerColor=familyMember.playerColor;
    		    		doAction=true;
    		    		penalty=0;
    		    	}
    				else{
    					//if there is someone but not any of my Family Members
    					familyMembersAtProductionOrHarvest.get(i).color=familyMember.color;
    		    		familyMembersAtProductionOrHarvest.get(i).playerColor=familyMember.playerColor;
        		    		doAction=true;
    					}
    				}
    			else {
    				//this happens only in matches of 2 players
    				player.setMessage("The production area is full!");
    				return false;
    			}
    		}
    		
    		else{
    			//if there is already one of my family members
    			int j=0;
    			for(i=0;i<harvestAndProductionSize && i<familyMembersAtProductionOrHarvest.size() ;i++){
    				if((familyMembersAtProductionOrHarvest.get(i).playerColor).equals(familyMember.playerColor)){
    					if(("uncolored").equals(familyMembersAtProductionOrHarvest.get(i).color))
    						j++;
    					else
    						j--;
    					}
    			}
    			if(j==0){
    				//if there are already two of my family members
    				player.setMessage("You can't place another family member");
    				return false;
    			}	
    			for(i=0;i<harvestAndProductionSize && i<familyMembersAtProductionOrHarvest.size() && !familyMembersAtProductionOrHarvest.get(i).playerColor.equals("");i++){}
    			//move i to the first free slot
    			if(j==-1){
    				//if there is a colored family member
    				if(familyMember.color=="uncolored"){
    					familyMembersAtProductionOrHarvest.get(i).color=familyMember.color;
    		    		familyMembersAtProductionOrHarvest.get(i).playerColor=familyMember.playerColor;
		    			doAction=true;}
    				else{
    					player.setMessage("You can place just one uncolored family member");
    					return false;
    				}
    			}
    			else {
    				//if there is an uncolored family member
    				familyMembersAtProductionOrHarvest.get(i).color=familyMember.color;
		    		familyMembersAtProductionOrHarvest.get(i).playerColor=familyMember.playerColor;
	    			doAction=true;
    			}	
    		}
    		if (doAction==true){
    			if(familyMemberValue(familyMember,player)>=1){
	    			if (actionType=="Production"){
		    			personalBoardHandler.activateProduction(familyMemberValue(familyMember,player)-penalty,player); // we use the player Personal MainBaord
	    			}
	    			else if(actionType=="Harvest"){
	    				personalBoardHandler.activateHarvest(familyMemberValue(familyMember,player)-penalty,player); // we use the player Personal MainBaord
	    			}
		    		else {
		    			player.setMessage("Invalid action it must be Production or Harvest");
		    			return false;
		    		}
    			}
    			else {
	    			player.setMessage("Your Family Member must have a value of at least 1");
	    			return false;
	    		}
    		}
    		return true;
    	}

    public void initializeTheGame() throws FailedToReadFileException, FailedToRegisterEffectException, IOException {
    	personalBoardHandler.setGameHandler(this);
    	//initialize the value of an action space on the Towers
    	Integer [] towerValue = {1,3,5,7};
    	Integer[][] towersValue = new Integer[4][4];
    	for(int i=0;i<4;i++)
    		for(int j=0,k=3;j<4;j++,k--)
    			towersValue[j][i]=towerValue[k];
    	mainBoard.setTowersValue(towersValue);
        //read the files
		gsonReader.fileToCard(mainBoard);
		//load excommunications
		loadExcommunications ();
		for(int i=0;i<4;i++){
			mainBoard.familyMembersLocation.setFamilyMemberOnTheMarket(new FamilyMember(), i);
			for(int j=0;j<4;j++)
				mainBoard.familyMembersLocation.setFamilyMemberOnTheTower(new FamilyMember(), i, j);
		}
    }
    
    public void loadCardsOnTheMainBoard() throws IOException{
    	int j;
    	Integer[][] cardsOnTheTowers = mainBoard.getCardsOnTheTowers();
    	//checking the period to load the correct cards, we are using integers because
    	//the cards on the hashmaps use an integer as a key
    	//keys 1-8 first period, 9-16 second period, 17-24 third period
    	if(period==1){
    			j=1;
    			}
    		else if(period==2){
    			j=9;
    		}
    		else{
    			j=17;
    		}
    	//if it is the first round of a period it loads four random cards of the correct period
    	if(round==1){
    		ArrayList<Integer> list = new ArrayList<Integer>();
	    	for(int i=0;i<4;i++,list.clear()){
	            for (int r=0+j; r<8+j; r++) {
	                list.add(r);
	            }
	            //ordering randomly the numbers on the list
	            Collections.shuffle(list);
		    	for(int k=0;k<4;k++){
			    	cardsOnTheTowers[k][i] = list.get(k);
			    }
	    	}
    	}
    	//if it is the second round of a period, we load the four remaining cards for this period
    	else{
    		int l,p,k;
    		//alreadyOnTheBoard is false by default
    		boolean alreadyOnTheBoard ;
    		ArrayList<Integer> list = new ArrayList<Integer>();
    		for(int i=0;i<4;i++,list.clear()){
    			//checking if a number l generated in the range of possible values 
    			//for the specific period is present or not
    			for(p=0,l=0;l<8 && p<4; l++){
		    	    for(alreadyOnTheBoard=false, k=0;k<4;k++){
		    	    	if(cardsOnTheTowers[k][i]==(l+j)){
		    	    		alreadyOnTheBoard=true;}
		    	    }
		    	    //if l is not present 
	    	    	if(alreadyOnTheBoard==false){
	    	    		list.add(l+j);
	    	    		p++;
	    	    	}
    			}	
    			//ordering randomly the numbers on the list
    			Collections.shuffle(list);
    		    for(k=0;k<4;k++){
    			    cardsOnTheTowers[k][i] = list.get(k);
    		    }		
    		}
    	}
    	this.mainBoard.setCardsOnTheTowers(cardsOnTheTowers);
    	//populate the matrix of the card names on the towers
    	loadCardNamesOnTheMainBoard();
    	
    }
    
    private void loadExcommunications (){
    	Integer[] excommunications = new Integer[3];
    	//generating three random numbers from 1 to 7, 8 to 14 , 15 to 21 ,to choose the excommunications that are ordered by period in their hashmap
    	for(int i=0,j=1;i<3;i++,j+=7){
    	Random rand = new Random();
    	excommunications[i] = (rand.nextInt(7) + j);}
    	// There is a + 1 because rand.nextInt(7) generates number from 0 to 6 but we need from 1 to 7
    	mainBoard.excommunicationsOnTheBoard = excommunications;
    }
   
    public void loadCardNamesOnTheMainBoard() throws IOException{
    	Integer[][] cardsOnTheTowers = mainBoard.getCardsOnTheTowers();
    	String[][] cardNamesOnTheTowers = new String[4][4]; 
    	for(int i=0;i<4;i++){
            for (int j=0; j<4; j++){
            	if(cardsOnTheTowers[j][i] == -1)
            		cardNamesOnTheTowers[j][i] = "";
            	else{
            		switch(i){
		        	case 0: cardNamesOnTheTowers[j][i] = mainBoard.territoryMap.get(cardsOnTheTowers[j][i]).cardName;
		        		break;
		        	case 1: cardNamesOnTheTowers[j][i] = mainBoard.characterMap.get(cardsOnTheTowers[j][i]).cardName;
		        		break;
		        	case 2: cardNamesOnTheTowers[j][i] = mainBoard.buildingMap.get(cardsOnTheTowers[j][i]).cardName;
		    			break;
		        	case 3: cardNamesOnTheTowers[j][i] = mainBoard.ventureMap.get(cardsOnTheTowers[j][i]).cardName;
		        		break;
	            	}
            	}
            }
        }
    	mainBoard.setCardNamesOnTheTowers(cardNamesOnTheTowers);
    }

    public Integer cardNameToInteger (String card) throws CardNotFoundException{
    	for(int i=0;i<4;i++)
    		for(int j=0;j<4;j++){
    			if(mainBoard.getCardNamesOnTheTowers()[i][j].compareToIgnoreCase(card) == 0)
    				return mainBoard.getCardsOnTheTowers()[i][j];}
    	throw new CardNotFoundException("Card not found!"); //card not found
    			
    }
    
    
    public ArrayList<PlayerRank> calculateFinalPoints(ArrayList<NetworkPlayer> players) {
        Integer finalPoints;
        ArrayList<PlayerRank> list = new ArrayList<PlayerRank>();
        boolean flag = true;
        //use the method calculateMilitaryStrenght(); to create the arraylist of the first and second position of the military strenght
        ArrayList<PlayerRank> militaryStrenght = calculateMilitaryStrenght();
    	
        for(NetworkPlayer player : players){
        	finalPoints = player.points.getVictory();
        	
        	for(PlayerRank playerRank : militaryStrenght)
        		if(player.nickname.equals(playerRank.playerNickName)){
        			if(playerRank.getPlayerPoints() == 1)
        				finalPoints+=5;
        			else
        				finalPoints+=2;
        		}
        	switch (player.personalBoard.getPossessions("Territory").size()){
	        	case(3): finalPoints += 1;
	        	case(4): finalPoints += 4;
	        	case(5): finalPoints += 10;
	        	case(6): finalPoints += 20;
        	}
        	switch (player.personalBoard.getPossessions("Character").size()){
        		case(1): finalPoints += 1;
        		case(2): finalPoints += 3;
        		case(3): finalPoints += 6;
	        	case(4): finalPoints += 10;
	        	case(5): finalPoints += 15;
	        	case(6): finalPoints += 21;
        	}
        	flag=true;
        	for(Integer excommunicationNumber : player.getExcommunications())
        		if((mainBoard.excommunicationMap.get(excommunicationNumber).effect.getClass()).equals(NoVictoryForCard.class))
        			flag=false;
        	if(flag==true){
        		finalPoints += player.points.getFinalVictory();
        	}
        	finalPoints += ((player.resources.getCoins()+player.resources.getWoods()+player.resources.getStones()+player.resources.getServants())/5);
        	PlayerRank playerRank = new PlayerRank();
        	playerRank.setPlayerPoints(finalPoints);
        	playerRank.playerNickName=player.nickname;
        list.add(playerRank);		
    	}
    	return list;
    }
    
    public ArrayList<PlayerRank> calculateMilitaryStrenght (){
    	int max = 0;
    	ArrayList<PlayerRank> list = new ArrayList<PlayerRank>();
    	//finding how many points have the first
    	for(int i =0; i< mainBoard.rankings.getMilitaryRanking().size();i++)
    		if(mainBoard.rankings.getMilitaryRanking().get(i).getPlayerPoints()>max)
    			max=mainBoard.rankings.getMilitaryRanking().get(i).getPlayerPoints();
    	//looking for first/firsts
    	for(int i =0; i< mainBoard.rankings.getMilitaryRanking().size();i++)
    		if(mainBoard.rankings.getMilitaryRanking().get(i).getPlayerPoints()==max){
    			PlayerRank playerRank = new PlayerRank();
    			playerRank.playerNickName = mainBoard.rankings.getMilitaryRanking().get(i).playerNickName;
    			//setting points to 1 for indicating that this player is first
    			playerRank.setPlayerPoints(1);
    			list.add(playerRank);
    		}
    	//if the are more than one first		
    	if(list.size()>1)
    		return list;
    	//looking for the second/seconds
    	else{
    		int max2 = 0;
    		for(int i =0; i< mainBoard.rankings.getMilitaryRanking().size();i++)
        		if(mainBoard.rankings.getMilitaryRanking().get(i).getPlayerPoints()<max && mainBoard.rankings.getMilitaryRanking().get(i).getPlayerPoints()>max2)
        			max2 = mainBoard.rankings.getMilitaryRanking().get(i).getPlayerPoints();
    		
        	for(int i =0; i< mainBoard.rankings.getMilitaryRanking().size();i++)
        		if(mainBoard.rankings.getMilitaryRanking().get(i).getPlayerPoints()==max2){
        			PlayerRank playerRank = new PlayerRank();
        			playerRank.playerNickName = mainBoard.rankings.getMilitaryRanking().get(i).playerNickName;
        			//setting points to 2 for indicating that this player is second
        			playerRank.setPlayerPoints(2);
        			list.add(playerRank);
        		}
        }
    	return list;
    }

    //ask to the player if he wants to add servants to the action
    public Integer addServants(NetworkPlayer player) throws IOException, NotEnoughResourcesException{
    	player.setMessage("Do you want to add servants? yes or no");
    	String response = player.sendMessage();
    	response = checkResponse(response, player);
    	if(("yes").equals(response)){
    		player.setMessage("How many?");
    		Integer qty = Integer.parseInt(player.sendMessage());
    		player.resources.setServants(-qty);
    		return qty;
    	}
    	else
    		return 0;
    }
    
    public FamilyMember chooseFamilyMember (NetworkPlayer player){
    	player.setMessage("Which Family Memeber do you want to use? Choose a color between orange,black,white,uncolored");
		String response = player.sendMessage();
		if(!("orange").equals(response) && !("black").equals(response) && !("white").equals(response) && !("uncolored").equals(response)){
			player.setMessage("You must choose a color between between orange,black,white,uncolored");
			return chooseFamilyMember (player);
		}
		else
			for(String color : player.getPlayedFamilyMembers())
				if ((color).equals(response)){
					player.setMessage("You have already played this Family Member");
					return chooseFamilyMember (player);
				}
		FamilyMember familyMember = new FamilyMember();
		familyMember.color = response;
		familyMember.playerColor = player.playerColor;
		return familyMember;
	}
    
    public void discardLeader (NetworkPlayer player, String leader) throws NotEnoughResourcesException, NotEnoughPointsException{
    	ArrayList<String> playedLeader = player.getPlayerPlayedLeaderCards();
    	for(String name : player.personalBoard.getPossessedLeaders())
    		if((name).equals(leader)){
    			for(String playedName : playedLeader)
    				if(playedName.equals(leader))
    					return;
    			ArrayList<String> leaders = player.personalBoard.getPossessedLeaders();
    			leaders.remove(leader);
    			player.personalBoard.setPossessedLeaders(leaders);
	    		councilHandler.getCouncil(1, player, this, new ArrayList<Integer>());
	    	}
    }
    
    public static String checkResponse (String response,NetworkPlayer player){
    	while(!("no").equals(response) && !("yes").equals(response)){
			 player.setMessage("You must answer yes or no");
			 response = player.sendMessage();}
		return response;
    }
    
    public void updateRankings (NetworkPlayer player){
    	//set faith points
    	for(PlayerRank playerFaithRank : mainBoard.rankings.getFaithRanking())
    		if((playerFaithRank.playerNickName).equals(player.nickname))
    			playerFaithRank.setPlayerPoints(player.points.getFaith());
    	//set military points
    	for(PlayerRank playerMilitaryRank : mainBoard.rankings.getMilitaryRanking())
    		if((playerMilitaryRank.playerNickName).equals(player.nickname))
    			playerMilitaryRank.setPlayerPoints(player.points.getMilitary());
    	//set victory points
    	for(PlayerRank playerVictoryRank : mainBoard.rankings.getVictoryRanking())
    		if((playerVictoryRank.playerNickName).equals(player.nickname))
    			playerVictoryRank.setPlayerPoints(player.points.getVictory());
    }
    
    
    public void setPlayerActionOrder (Integer playersQty){
    	ArrayList<String> order = new ArrayList<String>();
    	//firstly set the order places by the order of the family members at the Council Palace
    	for(int i =0; i< mainBoard.familyMembersLocation.getFamilyMembersAtTheCouncilPalace().size();i++)
    		if(!order.contains(mainBoard.familyMembersLocation.getFamilyMembersAtTheCouncilPalace().get(i).playerColor))
    			order.add(mainBoard.familyMembersLocation.getFamilyMembersAtTheCouncilPalace().get(i).playerColor);
    	//then for players that haven't set any family member at the Council Palace follow the order of the previous round
    	for(int i =0;i<playersQty;i++)
    		if(!order.contains(this.playerActionOrder.get(i)))
    			order.add(this.playerActionOrder.get(i));
    	this.playerActionOrder = order;
    }
    
    public void cleanActionSpaces() throws InvalidActionTypeException{
    	//empty the market
    	for (int i=0;i<4;i++)
    		mainBoard.familyMembersLocation.setFamilyMemberOnTheMarket(new FamilyMember(),i);
    	//empty the production and the harvest area
    	mainBoard.familyMembersLocation.changeFamilyMemberOnProductionOrHarvest(new ArrayList<FamilyMember>(), "Harvest");
    	mainBoard.familyMembersLocation.changeFamilyMemberOnProductionOrHarvest(new ArrayList<FamilyMember>(), "Production");
    	mainBoard.familyMembersLocation.setFamilyMembersAtTheCouncilPalace(new ArrayList<FamilyMember>());
    	//empty the towers
    	for(int x=0;x<4;x++)
    		for(int y=0;y<4;y++)
    				mainBoard.familyMembersLocation.setFamilyMemberOnTheTower(new FamilyMember(), x, y);
    }
    
    public void setPlayersActionOrder (ArrayList<String> playerActionOrder){
    	//to be used only for the first round of the first period
    	this.playerActionOrder = playerActionOrder;
    }
    
    public ArrayList<String> getPlayersActionOrder (){
    	return this.playerActionOrder;
    }
    
    public void activatePermanentEffects(NetworkPlayer player) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    	CardHandler cardHandler = new CardHandler (this);
    	for(Integer cardNumber : player.personalBoard.getPossessions("Character"))
    		cardHandler.activateCharacter(mainBoard.characterMap.get(cardNumber).permanentEffect, player);
    	for(String leader : player.personalBoard.getPossessedLeaders())
    		cardHandler.activateLeader(mainBoard.leaderMap.get(leader).effect, player,leader);
    	for(Integer excommunicationNumber : player.getExcommunications())
    		cardHandler.activateExcommunication(mainBoard.excommunicationMap.get(excommunicationNumber).effect, player);
    }
    
    public void resetPlayerPersonalMainBoard (NetworkPlayer player)
    {
    	//clone the MainBoard into the player personal mainboard
    	Gson gson =new Gson();
    	player.personalMainBoard = gson.fromJson(gson.toJson(mainBoard),mainBoard.getClass());
    }
    public void setFirstRoundBonuses(NetworkPlayer player,Integer position) throws NotEnoughResourcesException{
    	switch (position){
	    	case(1): player.resources.setCoins(5);
	    		break;
	    	case(2): player.resources.setCoins(6);
	    		break;
	    	case(3): player.resources.setCoins(7);
	    		break;
	    	case(4): player.resources.setCoins(8);
	    		break;
    	}
    }
    
    
    
    //probably useless code
    /*
    public void removeDecoration(Class toRemove, NetworkPlayer player){
		//if it is not a decorator
    	if (this.getClass() == toRemove)
			return;
	}
	*/
}