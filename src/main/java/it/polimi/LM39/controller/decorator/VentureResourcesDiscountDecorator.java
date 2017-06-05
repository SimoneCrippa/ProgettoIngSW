package it.polimi.LM39.controller.decorator;

import java.io.IOException;

import it.polimi.LM39.controller.GameHandler;
import it.polimi.LM39.exception.NotEnoughPointsException;
import it.polimi.LM39.exception.NotEnoughResourcesException;
import it.polimi.LM39.model.Building;
import it.polimi.LM39.model.CardPoints;
import it.polimi.LM39.model.CardResources;
import it.polimi.LM39.model.Character;
import it.polimi.LM39.model.FamilyMember;
import it.polimi.LM39.model.Venture;
import it.polimi.LM39.server.NetworkPlayer;

public class VentureResourcesDiscountDecorator extends GameHandler{

	private GameHandler decoratedGameHandler;
	private CardResources resourcesDiscount;
	private NetworkPlayer player;
	
	public VentureResourcesDiscountDecorator (GameHandler decoratedGameHandler, CardResources resourcesDiscount, NetworkPlayer player) {
		this.decoratedGameHandler = decoratedGameHandler;
		this.resourcesDiscount = resourcesDiscount;
		this.player = player;
	}

	
	@Override
	public void resourcesForVenture(NetworkPlayer player ,Venture venture) throws NotEnoughResourcesException{
		if(this.player == player){
			//creating a CardResources object that is the result of the card costs - the bonus  
			CardResources resources = new CardResources();
			if(resourcesDiscount.stones > 0 && resourcesDiscount.woods > 0){
				player.setMessage("Do you want a discount of " + resourcesDiscount.stones + " stones or " + resourcesDiscount.woods + " woods?\n Respond woods or stones" );
	    		String response = player.sendMessage();
	    		if(("stones").equals(response)){
	    			if(venture.costResources.stones>=resourcesDiscount.stones)
	        			resources.stones= venture.costResources.stones - resourcesDiscount.stones;
	        		else
	        			resources.stones = 0;
	    			resources.woods= venture.costResources.woods;
	    		}
	    		else if (("woods").equals(response)){	
		    		if(venture.costResources.woods>=resourcesDiscount.woods)
		    			resources.woods= venture.costResources.woods - resourcesDiscount.woods;
		    		else
		    			resources.woods = 0;
		    		resources.stones= venture.costResources.stones;
	    		}
	    		else{
	    			player.setMessage("You must choose woods or stones");
	    			decoratedGameHandler.resourcesForVenture(player,venture);
	    			return;
	    		}
			}
			else{
				if(venture.costResources.woods>=resourcesDiscount.woods)
	    			resources.woods= venture.costResources.woods - resourcesDiscount.woods;
	    		else
	    			resources.woods = 0;
				if(venture.costResources.stones>=resourcesDiscount.stones)
	    			resources.stones= venture.costResources.stones - resourcesDiscount.stones;
	    		else
	    			resources.stones = 0;
				}
    		
    		if(venture.costResources.coins>=resourcesDiscount.coins)
    			resources.coins= venture.costResources.coins - resourcesDiscount.coins;
    		else
    			resources.coins=0;
    		if(venture.costResources.servants>=resourcesDiscount.servants)
    			resources.servants= venture.costResources.servants - resourcesDiscount.servants;
    		else
    			resources.servants=0;
			Venture venture2 = new Venture();
			venture2.costResources = resources;
			decoratedGameHandler.resourcesForVenture(player,venture2);
		}
		//if the bonus is not for the player that is now using this method
		decoratedGameHandler.resourcesForVenture(player,venture);
	}
	 
	 
	@Override
	public void coinsForCharacter(NetworkPlayer player ,Character character) throws NotEnoughResourcesException{
		decoratedGameHandler.coinsForCharacter(player,character);
	}
	
	@Override
	public void resourcesForBuilding(NetworkPlayer player, Building building) throws NotEnoughResourcesException{
		decoratedGameHandler.resourcesForBuilding(player,building);
	}
	
	@Override
	public void addCardResources (CardResources resources, NetworkPlayer player) throws NotEnoughResourcesException{
		decoratedGameHandler.addCardResources (resources,player);
	}
	
	@Override
	public boolean addFamilyMemberToTheMarket(FamilyMember familyMember, Integer position, NetworkPlayer player) throws IOException, NotEnoughResourcesException, NotEnoughPointsException {
		return decoratedGameHandler.addFamilyMemberToTheMarket(familyMember, position, player);
	}
	
	@Override
	public void addCardPoints (CardPoints points, NetworkPlayer player) throws NotEnoughPointsException{
		decoratedGameHandler.addCardPoints(points, player);
	}
	
	@Override
	public Integer addServants(NetworkPlayer player) throws IOException, NotEnoughResourcesException{
		return decoratedGameHandler.addServants(player);
	}
	
}
