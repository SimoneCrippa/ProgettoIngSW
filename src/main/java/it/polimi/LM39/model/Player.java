package it.polimi.LM39.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * this class contains all player informations and possessed cards
 */
public class Player implements Serializable{

	private static final long serialVersionUID = 216892385150280280L;

    public String playerColor = "";

    public PersonalBoard personalBoard = new PersonalBoard();

    public PlayerPoints points = new PlayerPoints();

    public PlayerResources resources = new PlayerResources();

    private ArrayList<Integer> excommunications = new ArrayList<Integer>();

    // this is a modified version of the MainBoard, should be updated every time the board gets modified,
    public MainBoard personalMainBoard = new MainBoard(); 

    private ArrayList<String> playerPlayedLeaderCards = new ArrayList<String>();
    
    private ArrayList<String> playedFamilyMembers = new ArrayList<String>();
    
    private ArrayList<String> playerInstantLeaderCards = new ArrayList<String>();
    
    //here is stored the name of the copied leader card if the player used Lorenzo De'Medici
    public String copiedLeaderCard = "";
    
    public void setPlayerPlayedLeaderCard(String cardName) {
    	playerPlayedLeaderCards.add(cardName);
    }

    public ArrayList<String> getPlayerPlayedLeaderCards() {
        return this.playerPlayedLeaderCards;
    }

    public void setExcommunications(Integer excommunicationNumber) {
        this.excommunications.add(excommunicationNumber);
    }

    public ArrayList<Integer> getExcommunications() {
    	return this.excommunications;
    }

	public ArrayList<String> getPlayedFamilyMembers() {
		return playedFamilyMembers;
	}

	public void setPlayedFamilyMember(String playedFamilyMember) {
		this.playedFamilyMembers.add(playedFamilyMember);
	}
	
	public void setPlayedFamilyMembers(ArrayList<String> playedFamilyMembers) {
		this.playedFamilyMembers = playedFamilyMembers;
	}

	public ArrayList<String> getPlayerInstantLeaderCards() {
		return playerInstantLeaderCards;
	}

	public void setPlayerInstantLeaderCard(String playerInstantLeaderCard) {
		this.playerInstantLeaderCards.add(playerInstantLeaderCard);
	}
	public void setPlayerInstantLeaderCards(ArrayList<String> playerInstantLeaderCards){
		this.playerInstantLeaderCards = playerInstantLeaderCards;
	}

	

}