package it.polimi.LM39.model.leaderobject;


import java.io.Serializable;

import it.polimi.LM39.model.CardResources;

/**
 * To activate this leader you need *resources* resources
 */
public class RequestedResources extends LeaderRequestedObjects implements Serializable{

	private static final long serialVersionUID = -6898111787196988487L;

    public CardResources resources;

}