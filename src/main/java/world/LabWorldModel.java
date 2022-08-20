package world;

import java.io.Serializable;
import java.util.*;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.environments.W3DEnvironment;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Environment;

public class LabWorldModel extends WorldModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public int health ;
	public int score ;
	public int scoreGained ;
	public int healthLost ;
	public String mood ;
	public boolean gameover;

	/**
	 * Describing the part of the static world that the agent currently sees.
	 * Here, it is described as a set of nodes in the world's navigation graph
	 * that the agent sees. Each node is represented by a single integer which
	 * is an index to where the node is stored in the world's navigation graph.
	 * The graph itself is not represented in this object; though it is assumed
	 * that the agent has a way to access it.
	 */
	public int[] visibleNavigationNodes ;

	public boolean didNothingPreviousGameTurn ;

	// Lab Recruits so far only have one interaction-type with items in the game;
	// let's just call it "interact".
	public static final String INTERACT = "interact" ;

	final Map<String,Set<String>> availableInteractionTypes_ = new HashMap<>() ;

	public LabWorldModel() { super() ;
	   // specify which interaction type is possible on which entity types:
	   Set<String> justSwitch = new HashSet<>() ;
	   justSwitch.add(LabEntity.SWITCH) ;
	   availableInteractionTypes_.put(INTERACT,justSwitch) ;
	}

	@Override
	public List<WorldEntity> mergeNewObservation(WorldModel observation) {
		LabWorldModel observation_ = (LabWorldModel) observation ;
		this.healthLost  = this.health -  observation_.health ;
		this.health = observation_.health ;
		this.scoreGained = observation_.score - this.score ;
		this.score  = observation_.score ;
		this.mood = observation_.mood ;
		this.gameover = observation_.gameover ;
		this.visibleNavigationNodes = observation_.visibleNavigationNodes ;
		return super.mergeNewObservation(observation) ;
	}


	@Override
	public LabEntity getElement(String id) {
		return (LabEntity) super.getElement(id) ;
	}

	/**
	 * Return the center position of the agent, with the y-position shifted to the floor level.
	 */
	public Vec3 getFloorPosition() {
		return new Vec3(position.x,position.y -  extent.y, position.z) ;
	}

    public Map<String,Set<String>> availableInteractionTypes() {
		return availableInteractionTypes_ ;
	}

	@Override
	public boolean isBlocking(WorldEntity e) {
		switch(e.type) {
		   case LabEntity.DOOR : return ! e.getBooleanProperty("isOpen") ;
		   case LabEntity.COLORSCREEN : return true ;
		   case LabEntity.GOAL : return true ;
		   default : return false ;
		}
	}

}
