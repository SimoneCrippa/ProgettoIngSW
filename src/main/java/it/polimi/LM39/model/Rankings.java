package it.polimi.LM39.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Rankings implements Serializable{

	private static final long serialVersionUID = 3477030534526157937L;

	private ArrayList<FamilyMemberRank> victoryRanking = new ArrayList<FamilyMemberRank>();

    private ArrayList<FamilyMemberRank> faithRanking = new ArrayList<FamilyMemberRank>();

    private ArrayList<FamilyMemberRank> militaryRanking = new ArrayList<FamilyMemberRank>();
    
    
    public void setVictoryRanking(ArrayList<FamilyMemberRank> victoryRanking) {
        this.victoryRanking=victoryRanking;
    }

    public ArrayList<FamilyMemberRank> getVictoryRanking() {
        return this.victoryRanking;
    }

    public void setFaithRanking(ArrayList<FamilyMemberRank> faithRanking) {
    	this.faithRanking=faithRanking;
    }

    public ArrayList<FamilyMemberRank> getFaithRanking() {
        return this.faithRanking;
    }

    public void setMilitaryRanking(ArrayList<FamilyMemberRank> militaryRanking) {
    	this.militaryRanking=militaryRanking;
    }

    public ArrayList<FamilyMemberRank> getMilitaryRanking() {
    	return this.militaryRanking;
    }

}