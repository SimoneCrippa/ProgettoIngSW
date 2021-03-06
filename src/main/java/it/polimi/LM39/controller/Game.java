package it.polimi.LM39.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import it.polimi.LM39.exception.CardNotFoundException;
import it.polimi.LM39.exception.DoAnotherActionException;
import it.polimi.LM39.exception.InvalidActionTypeException;
import it.polimi.LM39.exception.NotEnoughPointsException;
import it.polimi.LM39.exception.NotEnoughResourcesException;
import it.polimi.LM39.model.ActionBonus;
import it.polimi.LM39.model.FamilyMember;
import it.polimi.LM39.model.PersonalBonusTile;
import it.polimi.LM39.model.PlayerRank;
import it.polimi.LM39.model.excommunicationpermanenteffect.SkipFirstTurn;
import it.polimi.LM39.server.NetworkPlayer;
import it.polimi.LM39.server.Server;

/**
 * this class is the core of the game there is one for every room
 */
public class Game implements Runnable{

   /**
    * the constructor
    * @param playerNumber the number of players in the room
    * @param players the array list of players in the room
    */
    public Game(Integer playerNumber, ArrayList<NetworkPlayer> players) {
    	this.playerNumber = playerNumber;
    	this.players = players;
    	gameHandler = new GameHandler();
    }
    
	private Logger logger = Logger.getLogger(Game.class.getName());
    private GameHandler gameHandler;
    private int playerNumber;

    private ArrayList<NetworkPlayer> players = new ArrayList<NetworkPlayer>();

    /**
     * to initialize the game, prepare the MainBoard and the players
     */
    private void initialize(){
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
    
    	//set players action order
    	Collections.shuffle(players);
    	
    	//send timeout to players
    	for(NetworkPlayer player:players){
    		try {
				player.setMessage(GsonReader.timeoutLoader(1));
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Can't read json file", e);
			}
    	}
    	//set player colors
    	setPlayersColor(players);

    	//load the Rankings
    	loadRankings();
    	
    	ArrayList<String> order = new ArrayList<String>();
    	for(NetworkPlayer player : players){
    		order.add(player.playerColor);
    	}
    		
    	gameHandler.setPlayersActionOrder(order);
    	
    	for(int i=0;i<order.size();i++){
			try {
				gameHandler.setFirstRoundBonuses(playerColorToNetworkPlayer(order.get(i)),i+1);
			} catch (NotEnoughResourcesException e) {
				logger.log(Level.WARNING,"Not enough resources",e);
			}
    	}
    	for(NetworkPlayer player : players)
    		updatePersonalMainBoards(player);
    }
    
    /**
     * to give a color to the players
     * @param players
     */
    private void setPlayersColor(ArrayList<NetworkPlayer> players){
    	String[] colors = {"green","blue","red","yellow"};
    	for(int i = 0; i < players.size(); i++){
    		players.get(i).playerColor = colors[i];
    		players.get(i).setMessage("\nYour color is " + colors[i] + "\n");
    	}
    }
    
    /**
     * to get a card on the Towers
     * @param player
     */
    private void actionGetCard (NetworkPlayer player){
    	boolean flag;
		player.setMessage("What card do you want?");
		String response = player.sendMessage();
		try {
			gameHandler.cardNameToInteger(response);
		} catch (CardNotFoundException e) {
			player.setMessage("This card is not on the Towers");
			doPlayerAction(player);
			return;
		}
		FamilyMember familyMember;
		try {
			familyMember = handleFamilyMember(player);
		} catch (DoAnotherActionException e2) {
			doPlayerAction(player);
			return;
		}
		try {
			flag = gameHandler.addFamilyMemberToTheTower(familyMember, response, player);
		} catch (ReflectiveOperationException | NotEnoughResourcesException | NotEnoughPointsException
				| CardNotFoundException e) {
			logger.log(Level.SEVERE, "Reflection error", e);
			//give the servants back to the player if the action failed
			try {
				player.resources.setServants(familyMember.getServants());
			} catch (NotEnoughResourcesException e1) {
				logger.log(Level.WARNING, "Not enough servants", e1);
			}
			doPlayerAction(player);
			return;
		}
		if(flag==false){
			//give the servants back to the player if the action failed
			try {
				player.resources.setServants(familyMember.getServants());
			} catch (NotEnoughResourcesException e1) {
				logger.log(Level.WARNING, "Not enough servants", e1);
			}
			doPlayerAction(player);
			return;
		}
		//add the played family member to the played family members list
		player.setPlayedFamilyMember(familyMember.color);
    	
    }
    
    /**
     * to activate the production
     * @param player
     */
    private void actionActivateProduction(NetworkPlayer player){
    	FamilyMember familyMember;
    	boolean flag;
		try {
			familyMember = handleFamilyMember(player);
		} catch (DoAnotherActionException e2) {
			doPlayerAction(player);
			return;
		}
		try {
			flag = gameHandler.addFamilyMemberToProductionOrHarvest(familyMember,player.personalMainBoard.familyMembersLocation.getFamilyMembersOnProductionOrHarvest("Production"),"Production",player);
		} catch (ReflectiveOperationException | NotEnoughResourcesException | NotEnoughPointsException
				| InvalidActionTypeException e) {
			logger.log(Level.SEVERE, "Reflection error", e);
			//give the servants back to the player if the action failed
			try {
				player.resources.setServants(familyMember.getServants());
			} catch (NotEnoughResourcesException e1) {
				logger.log(Level.WARNING, "Not enough servants", e1);
			}
			doPlayerAction(player);
			return;
		}
		if (flag == false){
			doPlayerAction(player);
			return;}
    }
    
    /**
     * to activate the harvest
     * @param player
     */
    private void actionActivateHarvest(NetworkPlayer player){
    	boolean flag;
		FamilyMember familyMember;
		try {
			familyMember = handleFamilyMember(player);
		} catch (DoAnotherActionException e2) {
			doPlayerAction(player);
			return;
		}
		try {
			flag = gameHandler.addFamilyMemberToProductionOrHarvest(familyMember,player.personalMainBoard.familyMembersLocation.getFamilyMembersOnProductionOrHarvest("Harvest"),"Harvest",player);
		} catch (ReflectiveOperationException | NotEnoughResourcesException | NotEnoughPointsException
				| InvalidActionTypeException e) {
				logger.log(Level.SEVERE, "Reflection error", e);
				//give the servants back to the player if the action failed
				try {
					player.resources.setServants(familyMember.getServants());
				} catch (NotEnoughResourcesException e1) {
					logger.log(Level.WARNING, "Not enough servants", e1);
				}
				doPlayerAction(player);
				return;
		}
		if (flag == false){
			doPlayerAction(player);
			return;}
    }
    
    /**
     * to discard a leader
     * @param player
     */
    private void actionDiscardLeader(NetworkPlayer player){
		Integer cardNumber = -1;
		player.setMessage("Which leader card do you want to discard?");
		String response = player.sendMessage();
		if(player.personalBoard.getPossessedLeaders().contains(response)){
			cardNumber = player.personalBoard.getPossessedLeaders().indexOf(response);
				try {	
					gameHandler.discardLeader(player, player.personalBoard.getPossessedLeaders().get(cardNumber));
				} catch (NotEnoughResourcesException | NotEnoughPointsException e) {
					logger.log(Level.WARNING, "Not enough points or resources", e);
					doPlayerAction(player);
					return;
				}
		}
		if(cardNumber == -1)
			player.setMessage("You don't have this leader");
		doPlayerAction(player);
    }
    
    /**
     * to activate a leader
     * @param player
     */
    private void actionActivateLeader(NetworkPlayer player){
    	player.setMessage("Which leader do you want to activate?");
		String response = player.sendMessage();
		boolean flag = false;
		if(player.personalBoard.getPossessedLeaders().contains(response) && !player.getPlayerInstantLeaderCards().contains(response) 
		&& !player.getPlayerPlayedLeaderCards().contains(response)){
				CardHandler cardHandler = new CardHandler(gameHandler,gameHandler.decoratedMethods);
				try {
					flag = cardHandler.checkLeaderRequestedObject(gameHandler.mainBoard.leaderMap.get(response).requestedObjects, player);
				} catch (ReflectiveOperationException e) {
					logger.log(Level.SEVERE, "Reflection error", e);
				}
				if(flag == false){
					player.setMessage("You do not have the requirements to activate this leader!");
					doPlayerAction(player);
					return;
				}
				else {
					try {
						gameHandler.decoratedMethods = cardHandler.activateLeader(gameHandler.mainBoard.leaderMap.get(response).effect, player, gameHandler.mainBoard.leaderMap.get(response).cardName);
						doPlayerAction(player);
						return;
					} catch (ReflectiveOperationException e) {
						logger.log(Level.SEVERE, "Reflection error", e);
					}
				}	
		}
		else{
				player.setMessage("You do not have this card or this card is active");
				doPlayerAction(player);
				return;
		}
    }
    
    /**
     * to go to the market
     * @param player
     */
    private void actionGoToTheMarket(NetworkPlayer player){
		CardHandler cardHandler = new CardHandler(gameHandler,gameHandler.decoratedMethods);
		for(int i =0;i<gameHandler.mainBoard.marketSize;i++){
			ActionBonus bonus = gameHandler.mainBoard.marketBonuses[i];
			player.setMessage("Market position " + (i+1) + " gives you:");
			cardHandler.printCardResources(bonus.resources, player);
			cardHandler.printCardPoints(bonus.points, player);
		}
		FamilyMember familyMember;
		try {
			familyMember = handleFamilyMember(player);
		} catch (DoAnotherActionException e1) {
			doPlayerAction(player);
			return;
		}
		player.setMessage("In which position do you want to go? From 1 to " + gameHandler.mainBoard.marketSize);
		boolean flag = false;
		String response = player.sendMessage();
		if(!("4").equals(response) && !("3").equals(response) && !("2").equals(response) && !("1").equals(response)){
			player.setMessage("You must choose between 1 and " + gameHandler.mainBoard.marketSize);
			//give the servants back to the player if the action failed
			try {
				player.resources.setServants(familyMember.getServants());
			} catch (NotEnoughResourcesException e) {
				logger.log(Level.WARNING, "Not enough resources", e);
			}
			doPlayerAction(player);
			return;
		}
		else{
			try {
				flag = gameHandler.decoratedMethods.addFamilyMemberToTheMarket(familyMember, Integer.parseInt(response), player);
			} catch (NumberFormatException | NotEnoughResourcesException
					| NotEnoughPointsException e) {
				logger.log(Level.SEVERE,"Not enough resources or points", e);
			}
			if(flag==false){
				//give the servants back to the player if the action failed
				try {
					player.resources.setServants(familyMember.getServants());
				} catch (NotEnoughResourcesException e) {
					logger.log(Level.WARNING,"Not enough resources or points", e);
				}
				doPlayerAction(player);
				return;
			}
		}
    }
    
    /**
     * to go to the council palace
     * @param player
     */
    private void actionGoToTheCouncilPalace(NetworkPlayer player){
    	boolean flag = false;
		FamilyMember familyMember;
		try {
			familyMember = handleFamilyMember(player);
		} catch (DoAnotherActionException e1) {
			doPlayerAction(player);
			return;
		}
		try {
			flag = gameHandler.addFamilyMemberToTheCouncilPalace(familyMember, player);
		} catch (NotEnoughResourcesException | NotEnoughPointsException e) {
			logger.log(Level.WARNING,"Not enough resources or points", e);
		}
		if(flag==false){
			//give the servants back to the player if the action failed
			try {
				player.resources.setServants(familyMember.getServants());
			} catch (NotEnoughResourcesException e) {
				logger.log(Level.WARNING,"Not enough servants", e);
			}
			doPlayerAction(player);
			return;
		}
    }
    
    /**
     * to get informations about a card costs and bonuses
     * @param player
     */
    private void actionGetCardInfo(NetworkPlayer player){
    	player.setMessage("What card are you interested in ?");
		String cardName = player.sendMessage();
		try {
			searchCard(cardName,player);
			doPlayerAction(player);
			return;
		} catch (ReflectiveOperationException e) {
			logger.log(Level.SEVERE,"Reflection error", e);
		}
		catch (CardNotFoundException e) {
			doPlayerAction(player);
			return;
		}
    }
    
    /**
     * to show the excommunications on the Board
     * @param player
     */
    private void actionDisplayExcommunications(NetworkPlayer player){
    	CardHandler cardHandler = new CardHandler(gameHandler,gameHandler.decoratedMethods);
		for(int i=0;i<3;i++){
			player.setMessage("\nExcommunication " + (i+1));
			try {
				cardHandler.getInfo(gameHandler.mainBoard.excommunicationMap.get(gameHandler.mainBoard.excommunicationsOnTheBoard[i]).effect,player);
			} catch (ReflectiveOperationException e) {
				logger.log(Level.SEVERE,"Reflection error", e);
			}
		}
		doPlayerAction(player);
		return;
    }
    
    /**
     * to call the various actions that a player can do
     * @param player
     */
    private void doPlayerAction(NetworkPlayer player){
    	player.setMessage("What action do you want to perform?");
    	String response = player.sendMessage();
    	switch(response){
    	case "get card": actionGetCard (player);
    		break;
    	case "activate production": actionActivateProduction(player);
    		break;
    	case "activate harvest": actionActivateHarvest(player);
    		break;
    	case  "discard leader": actionDiscardLeader(player);
    		break;
    	case "activate leader": actionActivateLeader(player);
    		break;
    	case "go to the market": actionGoToTheMarket(player);
    		break;
    	case "go to the council palace": actionGoToTheCouncilPalace(player);
    		break;
    	case "get card info": actionGetCardInfo(player);
    		break;
    	case "display excommunications": actionDisplayExcommunications(player);
    		break;
    	case "skip action": // do nothing
    		break;
    	case "timeout": //do nothing
    		break;
    	default: player.setMessage("Invalid action");
				 doPlayerAction(player);
			break;
    	}
    }
    
    /**
     * to search a card on the MainBoard and on the player PersonalBoard
     * @param cardName
     * @param player
     * @throws ReflectiveOperationException
     * @throws NoSuchMethodException
     * @throws CardNotFoundException
     */
    private void searchCard(String cardName,NetworkPlayer player) throws ReflectiveOperationException, CardNotFoundException{
    	CardHandler cardHandler = new CardHandler(gameHandler,gameHandler.decoratedMethods);
    	int p = 0; int k = 0; int j = 0; int i;
    	Integer cardNumber = -1;
    	String cardType = "";
    	boolean flag = false;
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
    		default: break;
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
    		case"Territory": cardHandler.getInfo(gameHandler.mainBoard.territoryMap.get(cardNumber).instantBonuses,player);
    						player.setMessage("Harvest Cost " + gameHandler.mainBoard.territoryMap.get(cardNumber).activationCost);
    						player.setMessage("When activated this card gives you:");
    						cardHandler.getInfo(gameHandler.mainBoard.territoryMap.get(cardNumber).activationReward,player);
    						break;
    		case"Character": player.setMessage("Coins cost " + gameHandler.mainBoard.characterMap.get(cardNumber).costCoins);
    						cardHandler.getInfo(gameHandler.mainBoard.characterMap.get(cardNumber).instantBonuses,player);
    						cardHandler.getInfo(gameHandler.mainBoard.characterMap.get(cardNumber).permanentEffect,player);
    						break;
    		case"Building": player.setMessage("This card costs in resources:");
			   				cardHandler.printCardResources(gameHandler.mainBoard.buildingMap.get(cardNumber).costResources,player);
			   				player.setMessage("This card gives you:");
			   				cardHandler.printCardPoints(gameHandler.mainBoard.buildingMap.get(cardNumber).instantBonuses,player);
			   				player.setMessage("Production Cost " + gameHandler.mainBoard.buildingMap.get(cardNumber).activationCost);
			   				player.setMessage("When activated this card gives you:");
			   				cardHandler.getInfo(gameHandler.mainBoard.buildingMap.get(cardNumber).activationEffect,player);
			   				break;
    		case"Venture": if(gameHandler.mainBoard.ventureMap.get(cardNumber).neededMilitary>0){
    						player.setMessage("To get this card you need " + gameHandler.mainBoard.ventureMap.get(cardNumber).neededMilitary + " military points");
    						}
    						if(gameHandler.mainBoard.ventureMap.get(cardNumber).costMilitary > 0){
    							player.setMessage("This card costs " + gameHandler.mainBoard.ventureMap.get(cardNumber).costMilitary + " militarypoints");
    							flag = true;
    						}
    						if(gameHandler.mainBoard.ventureMap.get(cardNumber).costResources.coins > 0 || gameHandler.mainBoard.ventureMap.get(cardNumber).costResources.woods > 0
    						|| gameHandler.mainBoard.ventureMap.get(cardNumber).costResources.stones > 0 || gameHandler.mainBoard.ventureMap.get(cardNumber).costResources.servants > 0){
    							if(flag)
    								player.setMessage("or");
    							player.setMessage("This card costs in resources:");
    							cardHandler.printCardResources(gameHandler.mainBoard.ventureMap.get(cardNumber).costResources,player);
    						}
			    			player.setMessage("This card gives you " + gameHandler.mainBoard.ventureMap.get(cardNumber).finalVictory + " victory points at the end of the game");
			    			player.setMessage("This card gives you:");
				   			cardHandler.getInfo(gameHandler.mainBoard.ventureMap.get(cardNumber).instant,player);
			    			break;
    		default: break;
    		}
    	}
    }

    /**
     * to handle the family members
     * @param player
     * @return
     * @throws DoAnotherActionException
     */
    private FamilyMember handleFamilyMember(NetworkPlayer player) throws DoAnotherActionException{
    	FamilyMember familyMember = gameHandler.chooseFamilyMember(player);
		try {
			familyMember.setServants(gameHandler.decoratedMethods.addServants(player));
		} catch (NotEnoughResourcesException e) {
			player.setMessage("You do not have enough servants!");
			player.setMessage("You can use another Family Member or do another action, respond change family member or do another action");
			String response = player.sendMessage();
			if(("do another action").equals(response))
				throw new DoAnotherActionException("do another action");
			else
				return handleFamilyMember(player);
		}
		return familyMember;
    }

  /**
   * to initialize the rankings
   */
    private void loadRankings(){
    	ArrayList<PlayerRank> faithRanking = new ArrayList<PlayerRank>();
    	ArrayList<PlayerRank> victoryRanking = new ArrayList<PlayerRank>();
    	ArrayList<PlayerRank> militaryRanking = new ArrayList<PlayerRank>();
    	for (NetworkPlayer player : players){
    		PlayerRank playerRankFaith = new PlayerRank();
    		playerRankFaith.playerColor = player.playerColor;
    		playerRankFaith.setPlayerPoints(0);
    		faithRanking.add(playerRankFaith);
    		
    		PlayerRank playerRankVictory = new PlayerRank();
    		playerRankVictory.playerColor = player.playerColor;
    		playerRankVictory.setPlayerPoints(0);
    		victoryRanking.add(playerRankVictory);
    		
    		PlayerRank playerRankMilitary = new PlayerRank();
    		playerRankMilitary.playerColor = player.playerColor;
    		playerRankMilitary.setPlayerPoints(0);
    		militaryRanking.add(playerRankMilitary);
    	}
    		
    	gameHandler.mainBoard.rankings.setFaithRanking(faithRanking);
    	gameHandler.mainBoard.rankings.setVictoryRanking(victoryRanking);
    	gameHandler.mainBoard.rankings.setMilitaryRanking(militaryRanking);
    }
    
    /**
     * the core of the game, handles periods, rounds and player moves
     */
    @Override
    public void run() {
    	//initialize the game loading parameters and cards
		initialize();
    	//make the players choose four leader cards
    	chooseLeaderCard();
    	//make the player choose one bonus tile
    	chooseBonusTile();
    	//the array list where the players actions order is stored
    	ArrayList <String> order;
    	
    	for(int period=0;period<3;period++){
    		gameHandler.setPeriod(period+1);
    		
    		for(int round=0;round<2;round++){
    			order = gameHandler.getPlayersActionOrder();
    			gameHandler.setRound(round+1);
				gameHandler.loadCardsOnTheMainBoard();
    			gameHandler.rollTheDices();
    			
    			for(int turn=0;turn<4;turn++){
    				for(int move=0;move<playerNumber;move++){
    					//update the personalMainBoards of all players
    					NetworkPlayer player = playerColorToNetworkPlayer(order.get(move));
        	    		updatePersonalMainBoards(player);
        	    		if((turn == 0 && !skipFirstTurn(player)) || turn != 0){
	    					player.setMessage(gameHandler.mainBoard);
	    					doPlayerAction(player);
	    					player.setMessage("End of your turn");
	    					gameHandler.updateRankings(player);
	    				}
        	    		//else skip the first turn
        	    		
        	    		//if it is the last turn and the last move, give to the player with the SkipFirstTurn excommunication the chance do place his latest family member
        	    		if(turn==3 && move==3){
        	    			for(int i=0;i<playerNumber;i++){
        	    				player = playerColorToNetworkPlayer(order.get(i));
        	    				if(skipFirstTurn(player)){
        	    					player.setMessage(gameHandler.mainBoard);
        	    					doPlayerAction(player);
        	    					player.setMessage("End of your turn");
        	    					gameHandler.updateRankings(player);
        	    				}
        	    			}
        	    		}
    				}
    			}
    			emptyPlayerInstantLeaderCards();
	    		gameHandler.setPlayerActionOrder(playerNumber);
	    		
	    		//clean all the action spaces for a new round
	    		try {
					gameHandler.cleanActionSpaces();
				} catch (InvalidActionTypeException e1) {
					logger.log(Level.INFO,"Invalid Action Type", e1);
				}
	    		
	    		//give the played family members back to the players
	    		giveFamilyMembersBack();
    		}
    		//support the church at the end of a period
    		for(NetworkPlayer player : players){
    			try {
					gameHandler.supportTheChurch(player);
				} catch (ReflectiveOperationException| NotEnoughResourcesException | NotEnoughPointsException e) {
					logger.log(Level.SEVERE,"Reflection error", e);
				}
    		}
    	
    	}
    	//calculate and send the final points made by every player to every player
    	sendFinalPoints(gameHandler.calculateFinalPoints(players));
    	//logout players at the end of the game
    	Server.logoutPlayers(players);
    }
    
    /**
     * clean the list of played leaders that gives an instant bonus
     */
    private void emptyPlayerInstantLeaderCards(){
    	for(NetworkPlayer player : players){
    		player.setPlayerInstantLeaderCards(new ArrayList<String>());
    	}
    }
    
    /**
     * check if a player has the SkipFirstTurn excommunication
     * @param player
     * @return
     */
    private boolean skipFirstTurn(NetworkPlayer player){
    	for(Integer excommunicationNumber : player.getExcommunications())
    		if((gameHandler.mainBoard.excommunicationMap.get(excommunicationNumber).effect.getClass()).equals(SkipFirstTurn.class))
    			return true;
    	return false;
    }

    /**
     * send the final scores to the players
     * @param finalScores
     */
    private void sendFinalPoints (ArrayList<PlayerRank> finalScores){
    	for(NetworkPlayer player : players)
    		for(PlayerRank playerRank : finalScores){
    			player.setMessage(playerColorToNetworkPlayer(playerRank.playerColor).getNickName() + " made " + playerRank.getPlayerPoints());
    		}
    }
    
    /**
     * updates the player personal MainBoard
     * @param player
     */
    private void updatePersonalMainBoards(NetworkPlayer player){
    	gameHandler.resetPlayerPersonalMainBoard(player);
    		try {
				gameHandler.activatePermanentEffects(player);
			} catch (ReflectiveOperationException e) {
				logger.log(Level.SEVERE,"Reflection error", e);
			}
    		
    }
    
    /**
     * return a NetworkPlayer from his color
     * @param color
     */
    private NetworkPlayer playerColorToNetworkPlayer (String color){
    	for(NetworkPlayer player: players)
    		if(player.playerColor.equals(color))
    			return player;
    	return null;
    }
    
    /**
     * give back the family members to all players
     */
    private void giveFamilyMembersBack(){
    	for(NetworkPlayer player : players)
    		player.setPlayedFamilyMembers(new ArrayList<String>());
    }

    /**
     * to choose the leader cards at the start of the game
     */
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
    				players.get(playerNumber + n).setMessage("\nChoose a leader card between:");
    				}
    			else if (((playerNumber + n)-players.size())<players.size()){
    				players.get((playerNumber + n)-players.size()).setMessage("\nChoose a leader card between:");}
    			else
    				players.get((playerNumber + n)-players.size()-players.size()).setMessage("\nChoose a leader card between:");
    			//send to the player the list of leader card in which he must choose one card
    			for(j=i+k;j>0+k;j--){
    				if(playerNumber+n<players.size())
    					players.get(playerNumber + n).setMessage(leaders.get(j));
    				else if (((playerNumber + n)-players.size())<players.size())
    					players.get((playerNumber + n)-players.size()).setMessage(leaders.get(j));
    				else
        				players.get((playerNumber + n)-players.size()-players.size()).setMessage(leaders.get(j));
    			}
    			Integer playerNumber2;
    			if(playerNumber+n<players.size()){
    				response = players.get(playerNumber + n).sendMessage();
    				playerNumber2 = playerNumber + n;
    			}
    			else if ((playerNumber + n -players.size())<players.size()){
    				response = players.get((playerNumber + n)-players.size()).sendMessage();
    				playerNumber2 = (playerNumber + n) -players.size();
    			}
    			else{
    				response = players.get((playerNumber + n)-players.size()-players.size()).sendMessage();
    				playerNumber2 = (playerNumber + n)-players.size()-players.size();
    			}
	    		// if the player chose a leader card between the ones he could choose
	    		if(leaders.contains(response)){
	    			//give to the player the card
	    			players.get(playerNumber2).personalBoard.setLeader(response);
	    			//remove the card from the array list so that no other players can get this same card
	 				leaders.remove(response);
	 				//go to the next player
	 				playerNumber++;
	 				//updating k this way ensures that going through the cycles the players send the cards they discarded to the next player like in the game rules
	 				k+=i;
	    		}
	    		//if the player response is not a leader card in between the ones he could choose keep sending the same list of cards
	   		}
    }
    
    /**
     * to choose the bonus tiles at the start of the game
     */
    private void chooseBonusTile(){
    	int i;
    	CardHandler cardHandler = new CardHandler(gameHandler,gameHandler.decoratedMethods);
    	for(int playerNumber = 0; playerNumber < players.size();){
    		NetworkPlayer player = players.get(playerNumber);
    		player.setMessage("Choose a tile between: (You must answer with the tile number)");
    		//send the list of choosable Bonus Tiles to every player
    		for(i=0; i < gameHandler.mainBoard.personalBonusTiles.size(); i++){
    			PersonalBonusTile tile = gameHandler.mainBoard.personalBonusTiles.get(i);
    			player.setMessage(" ");
    			player.setMessage("Tile " + (i+1));
    			player.setMessage("Harvest Bonuses:");
    			cardHandler.printCardPoints(tile.harvestBonus.points, player);
    			cardHandler.printCardResources(tile.harvestBonus.resources, player);
    			player.setMessage("Production Bonuses:");
    			cardHandler.printCardPoints(tile.productionBonus.points, player);
    			cardHandler.printCardResources(tile.productionBonus.resources, player);
    		}
    		Integer response = 0;
    		try{
    			//get the player response
    			response = Integer.parseInt(player.sendMessage());
    		}
    		catch(NumberFormatException e){
    			response = -1;
    		}
    		if (response <= gameHandler.mainBoard.personalBonusTiles.size() && response >= 0){
    			//give the tile to the player
    			player.personalBoard.personalBonusTile = gameHandler.mainBoard.personalBonusTiles.get(response-1);
    			//remove the tile from the Main Board
    			gameHandler.mainBoard.personalBonusTiles.remove(gameHandler.mainBoard.personalBonusTiles.get(response-1));
    			//go to the next player
    			playerNumber++;
    		}
    		//if the player responded with an invalid Bonus Tile number keep sending him the list until he will choose a valid one
    		else
    			player.setMessage("You must answer with a valid Tile number");
    	}
    }
    
}