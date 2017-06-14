package it.polimi.LM39.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;

import it.polimi.LM39.exception.CardNotFoundException;
import it.polimi.LM39.exception.FailedToReadFileException;
import it.polimi.LM39.exception.FailedToRegisterEffectException;
import it.polimi.LM39.exception.InvalidActionTypeException;
import it.polimi.LM39.exception.NotEnoughPointsException;
import it.polimi.LM39.exception.NotEnoughResourcesException;
import it.polimi.LM39.model.ActionBonus;
import it.polimi.LM39.model.FamilyMember;
import it.polimi.LM39.model.MainBoard;
import it.polimi.LM39.model.PersonalBonusTile;
import it.polimi.LM39.model.Player;
import it.polimi.LM39.model.PlayerRank;
import it.polimi.LM39.server.NetworkPlayer;
import it.polimi.LM39.server.Room;

/**
 * 
 */
public class Game implements Runnable{

    /**
     * Default constructor
     */
    public Game(Integer playerNumber, ArrayList<NetworkPlayer> players) {
    	this.playerNumber = playerNumber;
    	this.players = players;
    	gameHandler = new GameHandler();
    }
    
    private GameHandler gameHandler;
    private int playerNumber;

    /**
     * 
     */
    private ArrayList<NetworkPlayer> players = new ArrayList<NetworkPlayer>();

    /**
     * 
     */
    public Integer timeOutStart;

    /**
     * 
     */
    public Integer timeOutMove;

    private void initialize() throws FailedToReadFileException, FailedToRegisterEffectException, IOException{
    	if(playerNumber > 2)
    		//unreachable value for harvestAndProductionSize
    		gameHandler.mainBoard.harvestAndProductionSize = 9;
    	else
    		gameHandler.mainBoard.harvestAndProductionSize = 1;
    	if(playerNumber > 3)
    		gameHandler.mainBoard.marketSize = 4;
    	else
    		gameHandler.mainBoard.marketSize = 2;
    	
    	gameHandler.initializeTheGame();
    	
    	//load the Rankings
    	loadRankings();
    	
    	//set players action order
    	Collections.shuffle(players);
    	ArrayList<String> order = new ArrayList<String>();
    	setPlayersColor(players);
    	for(NetworkPlayer player : players){
    		order.add(player.playerColor);
    	}
    		
    	gameHandler.setPlayersActionOrder(order);
    	
    	for(int i=0;i<order.size();i++){
			try {
				gameHandler.setFirstRoundBonuses(playerColorToNetworkPlayer(order.get(i)),i+1);
			} catch (NotEnoughResourcesException e) {
				e.printStackTrace();
			}
			
			
    }
    	updatePersonalMainBoards();
    
    }
    private void setPlayersColor(ArrayList<NetworkPlayer> players){
    	String[] colors = {"green","blue","red","yellow"};
    	for(int i = 0; i < players.size(); i++){
    		players.get(i).playerColor = colors[i];
    	}
    }
    
    private void playerAction(NetworkPlayer player){
    	player.setMessage("What action do you want to perform?");
    	String response = player.sendMessage();
    	//flag is false by default
    	boolean flag;
    	if(("get card").equals(response)){
    		player.setMessage("What card do you want?");
    		response = player.sendMessage();
    		try {
				gameHandler.cardNameToInteger(response);
			} catch (CardNotFoundException e) {
				player.setMessage("This card is not on the Towers");
				playerAction(player);
				return;
			}
    		FamilyMember familyMember = handleFamilyMember(player);
    		try {
				flag = gameHandler.addFamilyMemberToTheTower(familyMember, response, player);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | IOException | NotEnoughResourcesException | NotEnoughPointsException
					| CardNotFoundException e) {
				e.printStackTrace();
				//give the servants back to the player if the action failed
				try {
					player.resources.setServants(familyMember.getServants());
				} catch (NotEnoughResourcesException e1) {
					e1.printStackTrace();
				}
				playerAction(player);
				return;
			}
    		if(flag==false){
				//give the servants back to the player if the action failed
				try {
					player.resources.setServants(familyMember.getServants());
				} catch (NotEnoughResourcesException e1) {
					e1.printStackTrace();
				}
				playerAction(player);
				return;
    		}
    		//ad the played family member to the played family fembers list
    		player.setPlayedFamilyMember(familyMember.color);
    	}
    	else if (("activate production").equals(response)){
    		FamilyMember familyMember = handleFamilyMember(player);
    		try {
    			flag = gameHandler.addFamilyMemberToProductionOrHarvest(familyMember,gameHandler.mainBoard.familyMembersLocation.getFamilyMembersOnProductionOrHarvest("Production"),"Production",player);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | IOException | NotEnoughResourcesException | NotEnoughPointsException
					| InvalidActionTypeException e) {
				e.printStackTrace();
				//give the servants back to the player if the action failed
				try {
					player.resources.setServants(familyMember.getServants());
				} catch (NotEnoughResourcesException e1) {
					e1.printStackTrace();
				}
				playerAction(player);
				return;
			}
    		if (flag == false){
    			playerAction(player);
    			return;}
    	}
    	else if (("activate harvest").equals(response)){
    		FamilyMember familyMember = handleFamilyMember(player);
			try {
				flag = gameHandler.addFamilyMemberToProductionOrHarvest(familyMember,gameHandler.mainBoard.familyMembersLocation.getFamilyMembersOnProductionOrHarvest("Harvest"),"Harvest",player);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | IOException | NotEnoughResourcesException | NotEnoughPointsException
					| InvalidActionTypeException e) {
					e.printStackTrace();
					//give the servants back to the player if the action failed
					try {
						player.resources.setServants(familyMember.getServants());
					} catch (NotEnoughResourcesException e1) {
						e1.printStackTrace();
					}
					playerAction(player);
					return;
			}
			if (flag == false){
    			playerAction(player);
    			return;}
			
    	}
    	else if (("discard leader").equals(response)){
    		player.setMessage("Which leader card do you want to discard?");
    		response = player.sendMessage();
    		for(String card : player.personalBoard.getPossessedLeaders())
    			if((card).equals(response))
					try {
						gameHandler.discardLeader(player, card);
					} catch (NotEnoughResourcesException | NotEnoughPointsException e) {
						e.printStackTrace();
						playerAction(player);
						return;
					}
    	}
    	else if (("activate leader").equals(response)){
    		player.setMessage("Which leader do you want to activate?");
    		response = player.sendMessage();
    		flag = false;
    		for(String card : player.personalBoard.getPossessedLeaders())
    			if((card).equals(response)){
    				CardHandler cardHandler = new CardHandler(gameHandler);
    				try {
						flag = cardHandler.checkLeaderRequestedObject(gameHandler.mainBoard.leaderMap.get(card).requestedObjects, player);
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
    				if(flag == false){
    					player.setMessage("You don't have the requirements to activate this leader!");
						playerAction(player);
						return;
    				}
    				else {
    					try {
							cardHandler.activateLeader(gameHandler.mainBoard.leaderMap.get(card).effect, player, gameHandler.mainBoard.leaderMap.get(card).cardName);
						} catch (SecurityException | IllegalAccessException | IllegalArgumentException
								| InvocationTargetException | NoSuchMethodException e) {
							e.printStackTrace();
						}
    				}
    			}
    	}
    	
    	else if (("go to the market").equals(response)){
    		CardHandler cardHandler = new CardHandler(gameHandler);
    		for(int i =0;i<4;i++){
    			ActionBonus bonus = gameHandler.mainBoard.marketBonuses[i];
    			player.setMessage("Market position " + (i+1) + " gives you:");
    			cardHandler.printCardResources(bonus.resources, player);
    			cardHandler.printCardPoints(bonus.points, player);
    		}
    		FamilyMember familyMember = handleFamilyMember(player);
    		player.setMessage("In which position do you want to go? From 1 to 4");
    		flag = false;
    		response = player.sendMessage();
    		if(!("4").equals(response) && !("3").equals(response) && !("2").equals(response) && !("1").equals(response)){
    			player.setMessage("You must choose between 1 and 4");
    			//give the servants back to the player if the action failed
				try {
					player.resources.setServants(familyMember.getServants());
				} catch (NotEnoughResourcesException e) {
					e.printStackTrace();
				}
				playerAction(player);
				return;
    		}
    		else{
    			try {
					flag = gameHandler.addFamilyMemberToTheMarket(familyMember, Integer.parseInt(response), player);
				} catch (NumberFormatException | IOException | NotEnoughResourcesException
						| NotEnoughPointsException e) {
					e.printStackTrace();
				}
    			if(flag==false){
    				//give the servants back to the player if the action failed
    				try {
    					player.resources.setServants(familyMember.getServants());
    				} catch (NotEnoughResourcesException e) {
    					e.printStackTrace();
    				}
    				playerAction(player);
    				return;
    			}
    		}
    	}
    	
    	else if (("go to the council palace").equals(response)){
    		flag = false;
    		FamilyMember familyMember = handleFamilyMember(player);
    		try {
				flag = gameHandler.addFamilyMemberToTheCouncilPalace(familyMember, player);
			} catch (IOException | NotEnoughResourcesException | NotEnoughPointsException e) {
				e.printStackTrace();
			}
    		if(flag==false){
				//give the servants back to the player if the action failed
				try {
					player.resources.setServants(familyMember.getServants());
				} catch (NotEnoughResourcesException e) {
					e.printStackTrace();
				}
				playerAction(player);
				return;
    		}
    	}
    	
    	else if (("get card info").equals(response)){
    		player.setMessage("What card are you interested in ?");
    		String cardName = player.sendMessage();
    		try {
				searchCard(cardName,player);
				playerAction(player);
				return;
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException e) {
				e.printStackTrace();
			}
    		catch (CardNotFoundException e) {
    			playerAction(player);
    			return;
			}
    	}
    	else if (("skip action").equals(response)){
    		//do nothing
    	}
    	else{
    		player.setMessage("Invalid action");
    		playerAction(player);
			return;
    	}
    }
    
    private void searchCard(String cardName,NetworkPlayer player) throws SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, CardNotFoundException{
    	CardHandler cardHandler = new CardHandler(gameHandler);
    	int p = 0; int k = 0; int j = 0; int i;
    	Integer cardNumber = -1;
    	String cardType = "";
    	for(i=0;i<4;i++)
    		for(j=0;j<4;j++)
    			if(gameHandler.mainBoard.getCardNamesOnTheTowers()[i][j].compareToIgnoreCase(cardName) == 0){
    				p = i;
    				k = j;
    				i=4;
    				j=4;
    			}
    	if(j == 5 && i == 5){
    		//the card was found on the towers
    		cardNumber = gameHandler.mainBoard.getCardsOnTheTowers()[p][k];
    		switch(k) {
    		case 0: cardType = "Territory";
    		break;
    		case 1: cardType = "Character";
    		break;
    		case 2: cardType = "Building";
    		break;
    		case 3: cardType = "Venture";
    		break;
    		}	
    	}
    	else{
    		for(Integer number : player.personalBoard.getPossessions("Territory"))
    			if(gameHandler.mainBoard.territoryMap.get(number).cardName.compareToIgnoreCase(cardName) == 0){
    				cardNumber = number;
    				cardType = "Territory";
    			}
    		if(cardNumber==-1){
    			for(Integer number : player.personalBoard.getPossessions("Character"))
        			if(gameHandler.mainBoard.characterMap.get(number).cardName.compareToIgnoreCase(cardName) == 0){
        				cardNumber = number;
        				cardType = "Character";
        			}
    		}
    		if(cardNumber==-1){
    			for(Integer number : player.personalBoard.getPossessions("Building"))
        			if(gameHandler.mainBoard.buildingMap.get(number).cardName.compareToIgnoreCase(cardName) == 0){
        				cardNumber = number;
        				cardType = "Building";
        			}
    		}
    		if(cardNumber==-1){
    			for(Integer number : player.personalBoard.getPossessions("Venture"))
        			if(gameHandler.mainBoard.ventureMap.get(number).cardName.compareToIgnoreCase(cardName) == 0){
        				cardNumber = number;
        				cardType = "Venture";
        			}
    		}
    		if(cardNumber==-1){
    			for(String leader : player.personalBoard.getPossessedLeaders())
    				if((leader).compareToIgnoreCase(cardName) == 0){
    					cardHandler.getInfo(gameHandler.mainBoard.leaderMap.get(leader).requestedObjects,player);
    					cardHandler.getInfo(gameHandler.mainBoard.leaderMap.get(leader).effect,player);
    					return;
    				}
    		}
    	}
    		
    	if(cardNumber == -1){
    		player.setMessage("This card is not on the Towers or in your Personal Board");
    		throw new CardNotFoundException("Card not found!");
    	}
    	else{
    		switch(cardType){
    		case("Territory"): cardHandler.getInfo(gameHandler.mainBoard.territoryMap.get(cardNumber).instantBonuses,player);
    						player.setMessage("Harvest Cost " + gameHandler.mainBoard.territoryMap.get(cardNumber).activationCost);
    						player.setMessage("When activated this card gives you:");
    						cardHandler.getInfo(gameHandler.mainBoard.territoryMap.get(cardNumber).activationReward,player);
    						break;
    		case("Character"): player.setMessage("Coins cost " + gameHandler.mainBoard.characterMap.get(cardNumber).costCoins);
    						cardHandler.getInfo(gameHandler.mainBoard.characterMap.get(cardNumber).instantBonuses,player);
    						cardHandler.getInfo(gameHandler.mainBoard.characterMap.get(cardNumber).permanentEffect,player);
    						break;
    		case("Building"): player.setMessage("This card cost in resources:");
			   				cardHandler.printCardResources(gameHandler.mainBoard.buildingMap.get(cardNumber).costResources,player);
			   				player.setMessage("This card gives you:");
			   				cardHandler.printCardPoints(gameHandler.mainBoard.buildingMap.get(cardNumber).instantBonuses,player);
			   				player.setMessage("Production Cost " + gameHandler.mainBoard.buildingMap.get(cardNumber).activationCost);
			   				player.setMessage("When activated this card gives you:");
			   				cardHandler.getInfo(gameHandler.mainBoard.buildingMap.get(cardNumber).activationEffect,player);
			   				break;
    		case("Venture"): if(gameHandler.mainBoard.ventureMap.get(cardNumber).neededMilitary>0){
    						player.setMessage("To get this card you need " + gameHandler.mainBoard.ventureMap.get(cardNumber).neededMilitary + " military points");
    						}
    						if(gameHandler.mainBoard.ventureMap.get(cardNumber).costMilitary > 0){
			    			player.setMessage("This card costs " + gameHandler.mainBoard.ventureMap.get(cardNumber).costMilitary + " militarypoints");
    						}
			    			player.setMessage("This card cost in resources:");
			    			cardHandler.printCardResources(gameHandler.mainBoard.ventureMap.get(cardNumber).costResources,player);
			    			player.setMessage("This card gives you " + gameHandler.mainBoard.ventureMap.get(cardNumber).finalVictory + " victory points at the end of the game");
			    			player.setMessage("This card gives you:");
				   			cardHandler.getInfo(gameHandler.mainBoard.ventureMap.get(cardNumber).instant,player);
			    			break;
    		}
    	}
    }


    
    private FamilyMember handleFamilyMember(NetworkPlayer player){
    	FamilyMember familyMember = gameHandler.chooseFamilyMember(player);
		try {
			familyMember.setServants(gameHandler.addServants(player));
		} catch (IOException | NotEnoughResourcesException e) {
			player.setMessage("You don't have enough servants!");
			player.setMessage("You can use another Family Member or do another action, respond change family member or do another action");
			String response = player.sendMessage();
			if(("do another action").equals(response))
				playerAction(player);
			else
				return handleFamilyMember(player);
		}
		return familyMember;
    }

    private void loadRankings(){
    	ArrayList<PlayerRank> faithRanking = new ArrayList<PlayerRank>();
    	ArrayList<PlayerRank> victoryRanking = new ArrayList<PlayerRank>();
    	ArrayList<PlayerRank> militaryRanking = new ArrayList<PlayerRank>();
    	for (NetworkPlayer player : players){
    		PlayerRank playerRankFaith = new PlayerRank();
    		playerRankFaith.playerNickName = player.nickname;
    		playerRankFaith.setPlayerPoints(0);
    		faithRanking.add(playerRankFaith);
    		
    		PlayerRank playerRankVictory = new PlayerRank();
    		playerRankVictory.playerNickName = player.nickname;
    		playerRankVictory.setPlayerPoints(0);
    		victoryRanking.add(playerRankVictory);
    		
    		PlayerRank playerRankMilitary = new PlayerRank();
    		playerRankMilitary.playerNickName = player.nickname;
    		playerRankMilitary.setPlayerPoints(0);
    		militaryRanking.add(playerRankMilitary);
    	}
    		
    	gameHandler.mainBoard.rankings.setFaithRanking(faithRanking);
    	gameHandler.mainBoard.rankings.setVictoryRanking(victoryRanking);
    	gameHandler.mainBoard.rankings.setMilitaryRanking(militaryRanking);
    }
    
    
    //TODO handle SkipFirstTurn Excommunication
    public void run() {
    	//initialize the game loading parameters and cards
    	try {
			initialize();
		} catch (FailedToReadFileException | FailedToRegisterEffectException | IOException e) {
			e.printStackTrace();
		}
    	//make the players choose a their four leader cards
    	//TODO uncomment the lines blow in the final version
    	//chooseLeaderCard();
    	chooseBonusTile();
    	//the array list where the players actions order is stored
    	ArrayList <String> order;
    	for(int period=0;period<3;period++){
    		gameHandler.setPeriod(period+1);
    		for(int round=0;round<2;round++){
    			order = gameHandler.getPlayersActionOrder();
    			gameHandler.setRound(round+1);
    			try {
					gameHandler.loadCardsOnTheMainBoard();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
    			gameHandler.rollTheDices();
    			
    			for(int action=0;action<4;action++){
    				for(int move=0;move<playerNumber;move++){
    					//update the personalMainBoards of all players
        	    		updatePersonalMainBoards();
    					NetworkPlayer player = playerColorToNetworkPlayer(order.get(move));
    					player.setMessage(gameHandler.mainBoard);
    					playerAction(player);
    					gameHandler.updateRankings(player);
    				}
    			}
	    		gameHandler.setPlayerActionOrder(playerNumber);
	    		
	    		//clean all the action spaces for a new round
	    		try {
					gameHandler.cleanActionSpaces();
				} catch (InvalidActionTypeException e1) {
					e1.printStackTrace();
				}
	    		
	    		//give the played family members back to the players
	    		giveFamilyMembersBack();
	    		
    		}
    		//support the church at the end of a period
    		for(NetworkPlayer player : players)
    			try {
					gameHandler.supportTheChurch(player);
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NotEnoughResourcesException | NotEnoughPointsException e) {
					e.printStackTrace();
				}
    	
    	}
    	//calculate and send the final points made by every player to every player
    	sendFinalPoints(gameHandler.calculateFinalPoints(players));
    	
    }

    private void sendFinalPoints (ArrayList<PlayerRank> finalScores){
    	for(NetworkPlayer player : players)
    		for(PlayerRank playerRank : finalScores){
    			player.setMessage(playerRank.playerNickName + " made " + playerRank.getPlayerPoints());
    		}
    }
    
    private void updatePersonalMainBoards(){
    	for(NetworkPlayer player : players){
    		gameHandler.resetPlayerPersonalMainBoard(player);
    		try {
				gameHandler.activatePermanentEffects(player);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				e.printStackTrace();
			}
    	}
    		
    }
    
    private NetworkPlayer playerColorToNetworkPlayer (String color){
    	for(NetworkPlayer player: players)
    		if(player.playerColor.equals(color))
    			return(player);
    	return null;
    }
    
    private void giveFamilyMembersBack(){
    	for(NetworkPlayer player : players)
    		player.setPlayedFamilyMembers(new ArrayList<String>());
    }

    private void chooseLeaderCard(){
    	//creating an array list of leaders names randomly ordinated
    	ArrayList<String> leaders;
    	leaders = gameHandler.mainBoard.leaderName;
    	Collections.shuffle(leaders);
    	int j;
    	String response;
    	for(int i=4,n=0;i>0;i--,n++)
    		//send to the players the cards he should choose one every time 
    		for(int playerNumber=0,k=0;playerNumber<players.size();){
    			if(playerNumber+n<players.size()){
    				System.out.println("playerNumber " + playerNumber + " " + n);
    				players.get(playerNumber + n).setMessage("Choose a leader card between:");
    				}
    			else if (((playerNumber + n)-players.size())<players.size()){
    				System.out.println("playerNumber " + playerNumber + " " + n + " " + (-players.size()));
    				players.get((playerNumber + n)-players.size()).setMessage("Choose a leader card between:");}
    			else
    				players.get((playerNumber + n)-players.size()-players.size()).setMessage("Choose a leader card between:");
    			//send to the player the list of leader card in which he must choose one card
    			System.out.println("da " + (i+k) + " a " + k);
    			for(j=i+k;j>0+k;j--){
    				if(playerNumber+n<players.size())
    					players.get(playerNumber + n).setMessage(leaders.get(j));
    				else if (((playerNumber + n)-players.size())<players.size())
    					players.get((playerNumber + n)-players.size()).setMessage(leaders.get(j));
    				else
        				players.get((playerNumber + n)-players.size()-players.size()).setMessage(leaders.get(j));
    			}
    			if(playerNumber+n<players.size())
    				response = players.get((playerNumber + n)).sendMessage();
    			else if (((playerNumber + n)-players.size())<players.size())
    				response = players.get((playerNumber + n)-players.size()).sendMessage();
    			else
    				response = players.get((playerNumber + n)-players.size()-players.size()).sendMessage();
	    		// if the player chose a leader card between the ones he could choose
	    		if(leaders.contains(response)){
	    			//give to the player the card
	    			players.get(playerNumber).personalBoard.setLeader(response);
	    			//remove the card from the array list so that no other players can get this same card
	 				leaders.remove(response);
	 				//go to the next player
	 				playerNumber++;
	 				//updating k this way ensures that going through the cicles the players send the cards they discarded to the next player like in the game rules
	 				k+=i;
	    		}
	    		//if the player response is not a leader card in between the ones he could choose keep sending the same list of cards
	   		}
    }
    
    private void chooseBonusTile(){
    	int i;
    	CardHandler cardHandler = new CardHandler(gameHandler);
    	for(int playerNumber = 0; playerNumber < players.size();){
    		NetworkPlayer player = players.get(playerNumber);
    		player.setMessage("Choose a tile between: (You must answer with the tile number)");
    		//send the list of choosable Bonus Tiles to every player
    		for(i=0; i < gameHandler.mainBoard.personalBonusTiles.size(); i++){
    			PersonalBonusTile tile = gameHandler.mainBoard.personalBonusTiles.get(i);
    			player.setMessage("Tile " + (i+1));
    			player.setMessage("Harvest Bonuses:");
    			cardHandler.printCardPoints(tile.harvestBonus.points, player);
    			cardHandler.printCardResources(tile.harvestBonus.resources, player);
    			player.setMessage("Production Bonuses:");
    			cardHandler.printCardPoints(tile.productionBonus.points, player);
    			cardHandler.printCardResources(tile.productionBonus.resources, player);
    		}
    		String response = player.sendMessage();
    		//if the player responded with a valid Bonus Tile Number
    		if (Integer.parseInt(response) <= gameHandler.mainBoard.personalBonusTiles.size() && Integer.parseInt(response) >= 0){
    			//give the tile to the player
    			player.personalBoard.personalBonusTile = gameHandler.mainBoard.personalBonusTiles.get(Integer.parseInt(response)-1);
    			//remove the tile from the Main Board
    			gameHandler.mainBoard.personalBonusTiles.remove(gameHandler.mainBoard.personalBonusTiles.get(Integer.parseInt(response)-1));
    			//go to the next player
    			playerNumber++;
    		}
    		//if the player responded with an invalid Bonus Tile number keep sending him the list until he will choose a valid one
    		else
    			player.setMessage("You must answer with a valid Tile number");
    	}
    }

    
    /*
    public static void main(String[] args) throws FailedToReadFileException, FailedToRegisterEffectException, IOException {
        
    	//code to test the method loadCardsOnTheMainBoard();
    	
    	MainBoard mainBoard = new MainBoard();
        GameHandler gameHandler = new GameHandler();
        gameHandler.mainBoard = mainBoard;
        //calls the  to populate the hashmaps
        gameHandler.setPeriod(1);
        gameHandler.setRound(1);
        gameHandler.initializeTheGame();
        gameHandler.loadCardsOnTheMainBoard();
        String[][] cards = gameHandler.mainBoard.getCardNamesOnTheTowers();
        for(int i=0;i<4;i++){
            for (int j=0; j<4; j++){
            	System.out.println(cards[j][i]);
            }
            }
        System.out.println();
        gameHandler.setRound(2);
        gameHandler.loadCardsOnTheMainBoard();
        cards = gameHandler.mainBoard.getCardNamesOnTheTowers();
        for(int i=0;i<4;i++){
            for (int j=0; j<4; j++){
            	System.out.println(cards[j][i]);
            }
            } 
    } 
    */
    
    

}