package it.polimi.LM39.controller.decorator;

import java.io.IOException;

import it.polimi.LM39.controller.DecoratedMethods;
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

public class BuildingResourcesDiscountDecorator extends DecoratedMethods{

	private DecoratedMethods decoratedMethods;
	private GameHandler gameHandler;
	private CardResources resourcesDiscount;
	private NetworkPlayer player;
	
	public BuildingResourcesDiscountDecorator (DecoratedMethods decoratedMethods,GameHandler gameHandler, CardResources resourcesDiscount, NetworkPlayer player) {
		this.decoratedMethods = decoratedMethods;
		this.gameHandler = gameHandler;
		this.resourcesDiscount = resourcesDiscount;
		this.player = player;
	}
	
	
	@Override
	public void resourcesForBuilding(NetworkPlayer player ,Building building) throws NotEnoughResourcesException{
		if(this.player == player){
			//creating a CardResources object that is the result of the card costs - the bonus  
			CardResources resources = new CardResources();
			if(resourcesDiscount.stones > 0 && resourcesDiscount.woods > 0){
				player.setMessage("Do you want a discount of " + resourcesDiscount.stones + " stones or " + resourcesDiscount.woods + " woods?\n Respond woods or stones" );
	    		String response = player.sendMessage();
	    		if(("stones").equals(response)){
	    			if(building.costResources.stones>=resourcesDiscount.stones)
	        			resources.stones= building.costResources.stones - resourcesDiscount.stones;
	        		else
	        			resources.stones = 0;
	    			resources.woods= building.costResources.woods;
	    		}
	    		else if (("woods").equals(response)){	
		    		if(building.costResources.woods>=resourcesDiscount.woods)
		    			resources.woods= building.costResources.woods - resourcesDiscount.woods;
		    		else
		    			resources.woods = 0;
		    		resources.stones= building.costResources.stones;
	    		}
	    		else{
	    			player.setMessage("You must choose woods or stones");
	    			decoratedMethods.resourcesForBuilding(player,building);
	    			return;
	    		}
			}
			else{
				if(building.costResources.woods>=resourcesDiscount.woods)
	    			resources.woods= building.costResources.woods - resourcesDiscount.woods;
	    		else
	    			resources.woods = 0;
				if(building.costResources.stones>=resourcesDiscount.stones)
	    			resources.stones= building.costResources.stones - resourcesDiscount.stones;
	    		else
	    			resources.stones = 0;
				}
			if(building.costResources.coins>=resourcesDiscount.coins)
    			resources.coins= building.costResources.coins - resourcesDiscount.coins;
    		else
    			resources.coins = 0;
    		
    		if(building.costResources.servants>=resourcesDiscount.servants)
    			resources.servants= building.costResources.servants - resourcesDiscount.servants;
    		else
    			resources.servants=0;
			Building building2 = new Building();
			building2.costResources = resources;
			decoratedMethods.resourcesForBuilding(player,building2);
		}
		else
			//if the bonus is not for the player that is now using this method
			decoratedMethods.resourcesForBuilding(player,building);
	}
	 
	 
	@Override
	public void coinsForCharacter(NetworkPlayer player ,Character character) throws NotEnoughResourcesException{
		decoratedMethods.coinsForCharacter(player,character);
	}
	
	@Override
	 public void resourcesForVenture(NetworkPlayer player ,Venture venture) throws NotEnoughResourcesException{
		decoratedMethods.resourcesForVenture(player,venture);
	}
	
	@Override
	public void addCardResources (CardResources resources, NetworkPlayer player) throws NotEnoughResourcesException, NotEnoughPointsException{
		decoratedMethods.addCardResources (resources,player);
	}
	
	@Override
	public boolean addFamilyMemberToTheMarket(FamilyMember familyMember, Integer position, NetworkPlayer player) throws IOException, NotEnoughResourcesException, NotEnoughPointsException {
		return decoratedMethods.addFamilyMemberToTheMarket(familyMember, position, player);
	}
	
	@Override
	public void addCardPoints (CardPoints points, NetworkPlayer player) throws NotEnoughPointsException{
		decoratedMethods.addCardPoints(points, player);
	}
	
	@Override
	public Integer addServants(NetworkPlayer player) throws IOException, NotEnoughResourcesException{
		return decoratedMethods.addServants(player);
	}
	
}

