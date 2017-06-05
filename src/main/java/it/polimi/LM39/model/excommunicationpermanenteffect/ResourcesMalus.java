package it.polimi.LM39.model.excommunicationpermanenteffect;

import java.io.Serializable;

import it.polimi.LM39.model.CardResources;

/**
 * 
 */
public class ResourcesMalus extends ExcommunicationPermanentEffect implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -2864370630853896656L;

	/**
     * Each time you receive wood or stone (from action spaces or from your Cards), you receive *resources* fewer wood or stone.
     * Each time you receive servants (from action spaces or from your Cards), you receive *resources* fewer servant.
     * Each time you receive coins (from action spaces or from your Cards),you receive *resources* fewer coin.
     */
    public ResourcesMalus() {
    }

    /**
     * 
     */
    public CardResources resources;

}