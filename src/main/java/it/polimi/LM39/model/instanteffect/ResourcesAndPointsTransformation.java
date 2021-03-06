package it.polimi.LM39.model.instanteffect;

import java.io.Serializable;

import it.polimi.LM39.model.CardPoints;

/**
 * Used by Buildings
 * instant effect, when activated gives to the player resources and points in return for resources
 */
public class ResourcesAndPointsTransformation extends ResourcesAndPoints implements Serializable{

	private static final long serialVersionUID = -1951798817008039490L;
	
	public CardPoints requestedForTransformation;
}
