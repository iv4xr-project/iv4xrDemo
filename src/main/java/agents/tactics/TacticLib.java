/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents.tactics;

import static nl.uu.cs.aplib.AplibEDSL.*;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import nl.uu.cs.aplib.multiAgentSupport.Acknowledgement;
import nl.uu.cs.aplib.multiAgentSupport.Message;
import nl.uu.cs.aplib.agents.MiniMemory;
import nl.uu.cs.aplib.utils.Pair;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Matrix;
import eu.iv4xr.framework.spatial.Vec3;
import eu.iv4xr.framework.spatial.meshes.Face;

import world.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provide a set of standard tactics to interact with the Lab
 * Recruits game. While {@link world.LabWorldModel} (and likewise the underlying
 * {@link environments.LabRecruitsEnvironment}) provides the primitive
 * methods/actions to control the Lab Recruit game, the tactics provided by this
 * class TacticLib provide higher-level actions, and are used to solve goals for aplib
 * agents. Most tactics provided here incorporate automatic path finding over
 * the Lab Recruits game world. This path finding will in principle allow an
 * agent to auto navigate from its current position to some position p,
 * provided: (1) p is physically reachable from the agent's current position,
 * and (2) the agent believes that p is reachable. For example, if p is located
 * in a room that the agent never sees, it would then believe that it has no way
 * to reach p. So, before it is able to auto-navigate to p, we may need to first
 * convince the agent of the reachability of p, e.g. by making it see that the
 * areas in-between are reachable.
 */
public class TacticLib {

	/**
	 * Distance used to unstuck an agent.
	 */
	public static float UNSTUCK_DELTA = 0.5f ;

	/**
	 * When the agent comes to this distance to the current exploration target,
	 * the target is considered as achieved (and the agent may then move to
	 * the next exploration target).
	 */
	public static float EXPLORATION_TARGET_DIST_THRESHOLD = 0.5f ;


	/**
	 * Threshold on the distance between a point to a surface to determine when
	 * the point is on the surface. This is used by the unstuck tactic to check
	 * that an unstucking proposal is still on the navigable surface,
	 */
	public static float DIST_SURFACE_THRESHOLD_STUCK = 0.045f;


	/**
	 * The same as {@link rawNavigateTo}, but the constructed tactic will also try
	 * to get the agent unstucked if it thinks it has become stuck. So, the method
	 * constructs a tactic T that will drive the agent to move towards a given
	 * position using a path planning algorithm. The tactic T is enabled (more
	 * precisely: the action that underlies T) if the agent BELIEVES that there is a
	 * path (through the navigation-graph maintained by the agent) to the entity.
	 * Note that agents' view are limited, so what they believe may not be what
	 * reality is. In other words, the position might actually be unreachable. As
	 * the agent tries to reach it, eventually it will discover that it is
	 * unreachable though, after which the T will no longer be enabled.
	 *
	 * Similarly, the position might actually be reachable, but the agent believes
	 * it is not, and therefore T will not be enabled. In this case you will first
	 * need to make the agent explore so that it can update itself with a more
	 * recent information from which the agent can infer that the position is
	 * reachable.
	 *
	 * This tactic will try to detect if the agent has become stuck and try to
	 * unstuck it.
	 */
	public static Tactic navigateTo(Vec3 position) {
		return FIRSTof(
				 forceReplanPath(),
				 tryToUnstuck(),
				 rawNavigateTo(position)
			   )  ;
	}


	/**
	 * To navigate to the location of an in-game element. Be mindful that the destination
	 * location is literally the location of the given game element. E.g. if it is a
	 * closed door, trying to literally get to its position won't work, since that position
	 * is blocked.
	 */
	public static Tactic navigateTo(String id) {
		return FIRSTof(
				 forceReplanPath(),
				 tryToUnstuck(),
				 rawNavigateTo(id)
			   )  ;
	}
	
	static Vec3 getDoorCenterPosition(LabEntity door) {
		var entity_location = door.getFloorPosition() ;
	    // Calculate the center of the square on which the target entity is located.
	    // Note: the bottom-left position of the bottom-left corner is (0.5,-,0.5) so this need to be taken into
	    // account.
	    // First, substract 0.5 from (x,z) ... then round it down. Add 0.5 to get the center position.
	    // Then add another 0.5 to compensate the 0.5 that we substracted earlier.
	    var entity_sqcenter = new Vec3((float) Math.floor((double) entity_location.x - 0.5f) + 1f,
	    		entity_location.y,
	    		(float) Math.floor((double) entity_location.z - 0.5f) + 1f) ;
	    return entity_sqcenter ; 
	}
	
	
	public static Pair<Vec3,List<Vec3>> calculatePathToDoor(BeliefState belief, LabEntity door, float delta) {
		
		boolean originalBlockingState = belief.pathfinder().getBlockingStatus(door) ;
    	belief.pathfinder().setBlockingState(door,false) ;
    	var door_center = getDoorCenterPosition(door) ;
    	//var p = e.getFloorPosition() ;
    	// System.out.println(">>> door " + id + " center:" + p) ;
    	Pair<Vec3,List<Vec3>> path = belief.findPathTo(door_center,true) ; 
    	
    	if (path == null) {
    		List<Vec3> candidates = new LinkedList<>() ;
    		// adding North and south candidates
    		candidates.add(Vec3.add(door_center, new Vec3(0,0,delta))) ;
    		candidates.add(Vec3.add(door_center, new Vec3(0,0,-delta))) ;
    		// adding east and west candidates:
    		candidates.add(Vec3.add(door_center, new Vec3(delta,0,0))) ;
    		candidates.add(Vec3.add(door_center, new Vec3(-delta,0,0))) ;

    		// iterate over the candidates, if one would be reachable:
    		for (var c : candidates) {
    			// if c (a candidate point near the entity) is on the navigable,
    			// we should ignore it:
    			if (getCoveringFaces(belief,c) == null) continue ;
    			path = belief.findPathTo(c, true) ; 
    			if (path != null) {
    				// found our target
    				break ;
    			}
    		}
    	}
    	
    	belief.pathfinder().setBlockingState(door,originalBlockingState) ;	 
    	return path ;
	}
	
	/**
	 * Guiding to an entity. If it is a door, we assume the door is open.
	 */
	public static Tactic optimisticNavigateToEntity(String id) {
		
		MiniMemory memory = new MiniMemory("S0") ;
		
		Action move = unguardedNavigateTo("Navigate to " + id)
			      // replacing its guard with this new one:
	              . on((BeliefState belief) -> {
	                	var e = (LabEntity) belief.worldmodel.getElement(id) ;
	    			    if (e==null) return null ;
	    			    
	    			    Vec3 thisTacticMemorizedGoalLocation = null ;
						if (!memory.memorized.isEmpty()) {
							thisTacticMemorizedGoalLocation = (Vec3) memory.memorized.get(0) ;
						}
						Vec3 currentGoalLocation = belief.getGoalLocation() ;
						
						if (thisTacticMemorizedGoalLocation == null
							    || currentGoalLocation == null
							    || Vec3.dist(thisTacticMemorizedGoalLocation,currentGoalLocation) >= 0.05) {
							
							Pair<Vec3,List<Vec3>> path = null ;
		    			    if (e.type.equals(LabEntity.DOOR)) {
		    			    	boolean originalBlockingState = belief.pathfinder().getBlockingStatus(e) ;
		    			    	belief.pathfinder().setBlockingState(e,true) ;
		    			    	var p = getDoorCenterPosition(e) ;
		    			    	//var p = e.getFloorPosition() ;
		    			    	// System.out.println(">>> door " + id + " center:" + p) ;
		    			    	path = calculatePathToDoor(belief,e,0.9f) ;
		    			    	belief.pathfinder().setBlockingState(e,originalBlockingState) ;	 
		    			    }
		    			    else {
		    			    	path = belief.findPathTo(e.getFloorPosition(),true) ; 
		    			    }
		    			    //System.out.println(">>> path:" + path) ;
		    			    memory.memorized.clear();
	    			        if (path != null) memory.memorize(path.fst);
		    			    return path ;
						}
						else {
							return new Pair (thisTacticMemorizedGoalLocation,null) ;
						}
						
	    			    
	                }) ;
		
		return FIRSTof(
				 forceReplanPath(),
				 tryToUnstuck(),
				 move.lift()
			   )  ;
	}
	
	static Tactic navigateToCloseByPosition(String id) {
		return navigateToCloseByPosition(id,0.5f) ;
	}

	/**
	 * Navigate to a location, nearby the given entity, if the location is reachable.
	 * Locations east/west/south/north of the entity of distance 0.7 will be tried.
	 */
	static Tactic navigateToCloseByPosition(String id, float delta) {

		MiniMemory memory = new MiniMemory("S0") ;

		Action move =
				unguardedNavigateTo("Navigate to a position nearby " + id)

				. on((BeliefState belief) -> {

					var e = (LabEntity) belief.worldmodel.getElement(id) ;
    			    if (e==null) return null ;

					Vec3 closeByLocation = null ;
					if (!memory.memorized.isEmpty()) {
						// if the position has been calculated before, retrieve it from memory:
						closeByLocation = (Vec3) memory.memorized.get(0) ;
					}
					Vec3 currentGoalLocation = belief.getGoalLocation() ;

					if (closeByLocation == null
					    || currentGoalLocation == null
					    || Vec3.dist(closeByLocation,currentGoalLocation) >= 0.05
					    || belief.getMemorizedPath() == null) {
						// in all these cases we need to calculate the location to go
                        
						/*
						//var agent_location = belief.worldmodel.getFloorPosition() ;
	    			    var entity_location = e.getFloorPosition() ;
	    			    // Calculate the center of the square on which the target entity is located.
	    			    // Note: the bottom-left position of the bottom-left corner is (0.5,-,0.5) so this need to be taken into
	    			    // account.
	    			    // First, substract 0.5 from (x,z) ... then round it down. Add 0.5 to get the center position.
	    			    // Then add another 0.5 to compensate the 0.5 that we substracted earlier.
	    			    var entity_sqcenter = new Vec3((float) Math.floor((double) entity_location.x - 0.5f) + 1f,
	    			    		entity_location.y,
	    			    		(float) Math.floor((double) entity_location.z - 0.5f) + 1f) ;
	    			    */
						var entity_sqcenter = getDoorCenterPosition(e) ;
						
 	    			    List<Vec3> candidates = new LinkedList<>() ;
	    			    // adding North and south candidates
	    			    candidates.add(Vec3.add(entity_sqcenter, new Vec3(0,0,delta))) ;
	    			    candidates.add(Vec3.add(entity_sqcenter, new Vec3(0,0,-delta))) ;
	    			    // adding east and west candidates:
	    			    candidates.add(Vec3.add(entity_sqcenter, new Vec3(delta,0,0))) ;
	    			    candidates.add(Vec3.add(entity_sqcenter, new Vec3(-delta,0,0))) ;

	    			    // iterate over the candidates, if one would be reachable:
	    			    for (var c : candidates) {
	    			    	// if c (a candidate point near the entity) is on the navigable,
	    			    	// we should ignore it:
	    			    	if (getCoveringFaces(belief,c) == null) continue ;
	    			    	var result = belief.findPathTo(c, true) ; 
	    			    	if (result != null) {
	    			    		// found our target
	    			    		System.out.println(">>> a reachable closeby position found :" + c + ", path: " + result.snd) ;
	    			    		memory.memorized.clear();
	    			    		memory.memorize(c);
	    			    		return result ;
	    			    	}
	    			    }
	    			    System.out.println(">>> i tried few nearby locations, but none are reachable :|") ;
	    			    // no reachable node can be found. We will clear the memory, and declare the tactic as disabled
	    			    memory.memorized.clear() ;
	    			    return null ;
					}
					else {
						// else the memorized location and the current goal-location coincide. No need to
						// recalculate the path, so we will just return the pair (memorized-loc,null)
						return new Pair (closeByLocation,null) ;
					}
				}) ;

		return  FIRSTof(
				 forceReplanPath(),
				 tryToUnstuck(),
				 move.lift()
			   ) ;
	}
	
	
	/**
	 * Return the face in the nav-mesh maintained by a BeliefState, 
	 * that covers a given point. Return null if there is none.
	 */
    static Face getCoveringFaces(BeliefState S, Vec3 p) {
    	for(Face face : S.pathfinder().faces) {
			if (face.distFromPoint(p, S.pathfinder().vertices) <= 0.12) {
				// found it
				return face ;
			}
    	}
    	return null ;
    }
	

	/**
	 * Navigate to a navigation node closest to the given entity, and is moreover
	 * reachable by the agent.
	 */
	static Tactic navigateToClosestReachableNode(String id) {

		MiniMemory memory = new MiniMemory("S0") ;

		Action move =
				unguardedNavigateTo("Navigate to a navigation vertex nearby " + id)

				. on((BeliefState belief) -> {

					LabEntity e = belief.worldmodel().getElement(id) ;
					
    			    if (e==null) return null ;

					Vec3 nodeLocation = null ;
					if (!memory.memorized.isEmpty()) {
						nodeLocation = (Vec3) memory.memorized.get(0) ;
					}
					Vec3 currentGoalLocation = belief.getGoalLocation() ;

					if (nodeLocation == null
					    || currentGoalLocation == null
					    || Vec3.dist(nodeLocation,currentGoalLocation) >= 0.05) {
						// in all these cases we need to calculate the node to go

    			        var entity_location = e.getFloorPosition() ;
	    			    List<Pair<Vec3,Float>> candidates = new LinkedList<>() ;
	    			    int k=0 ;
	    			    for (Vec3 v : belief.pathfinder().vertices) {
	    			    	if (belief.pathfinder().seenVertices.get(k)) {
	    			    		// v has been seen:
	    			    		candidates.add(new Pair(v, Vec3.dist(entity_location, v))) ;
	    			    	}
	    			    	k++ ;
	    			    }
	    			  
		    		    if (candidates.isEmpty()) return null ;
		    		    // sort the candidates according to how close they are to the entity e (closest first)
		    		    candidates.sort((c1,c2) -> c1.snd.compareTo(c2.snd));
		    		    // now find the first one that is reachable:
		    		    System.out.println(">>> #candidates closest reachable neighbor nodes = " + candidates.size()) ;
		    		    Pair<Vec3,List<Vec3>> result = null ;
		    		    for(var c : candidates) {
		    			    result = belief.findPathTo(c.fst,true) ;
		    			    if (result != null) {
		    			        // found a reachable candidate!
		    			        System.out.println(">>> a reachable nearby node found :" + c.fst + ", path: " + result.snd) ;
		    			        memory.memorized.clear();
		    			        memory.memorize(result.fst);
		    			    	return result ;
		    			    }
		    		    }
		    			System.out.println(">>> no reachable nearby nodes :|") ;
		    			// no reachable node can be found. We will clear the memory, and declare the tactic as disabled
		    			memory.memorized.clear() ;
		    			return null ;
					}
					else {
						// else the memorized location and the current goal-location coincide. No need to
						// recalculate the path, so we will just return the pair (memorized-loc,null)
						return new Pair (nodeLocation,null) ;
					}
				}) ;

		return FIRSTof(
				 forceReplanPath(),
				 tryToUnstuck(),
				 move.lift()
			   )  ;
	}


    /**
     * A tactic to navigate to the given entity's location. The tactic is enabled if
     * the agent believes the entity exists and is reachable. Else the tactic is NOT
     * enabled.
     */
    public static Tactic rawNavigateTo(String id) {

    	// let's just reuse rawNavigateTo_(..), and then we replace its guard:

    	Action move = unguardedNavigateTo("Navigate to " + id)
    			      // replacing its guard with this new one:
		              . on((BeliefState belief) -> {
		                	var e = (LabEntity) belief.worldmodel.getElement(id) ;
		    			    if (e==null) return null ;
		    			    var p = e.getFloorPosition() ;
		    			    
		    			    //System.out.println(">>> target: " + id + " @" + e.position + ", floor: " + p) ;
		    			    //System.out.println(">>> extent: " + e.extent) ;
		    			    //System.out.println(">>> agent @" + belief.worldmodel().position + ", floor: " + belief.worldmodel().getFloorPosition()) ;
		    			    //System.out.println(">>> agent extent" + belief.worldmodel().extent) ;
		    			    
		    			    // find path to p, but don't force re-calculation
		    			    var path = belief.findPathTo(p,false) ; 
		    			    //System.out.println(">>> path:" + path) ;
		    			    		    			    
		    			    return path ;
		                }) ;
    	
    	return move.lift() ;
    }
     

	/**
	 * Construct a tactic T that will drive the agent to move towards a given
	 * position using a path planning algorithm. The tactic T is enabled (more
	 * precisely: the action that underlies T) if the agent BELIEVES that there is a
	 * path (through the navigation-graph maintained by the agent) to the entity;
	 * otherwise the tactic is NOT enabled.
	 *
	 * Note that agents' view are limited, so what they believe may not be what
	 * reality is. In other words, the position might actually be unreachable. As
	 * the agent tries to reach it, eventually it will discover that it is
	 * unreachable though, after which the T will no longer be enabled.
	 *
	 * Similarly, the position might actually be reachable, but the agent believes
	 * it is not, and therefore T will not be enabled. In this case you will first
	 * need to make the agent explore so that it can update itself with a more
	 * recent information from which the agent can infer that the position is
	 * reachable.
	 *
	 * This tactic will not try to detect if the agent has become stuck.
	 */
    public static Tactic rawNavigateTo(Vec3 position) {
    	return unguardedNavigateTo("Navigate to " + position)
    		   . on((BeliefState belief) -> {
    			    // Check if a path to the position can be found; use the flag "false"
    			    // so as not to force repeated recalculation of the reachability.
    			    // The fragment below will check if the given position is already
    			    // memorized as a goal-location; is so, no path will be calculated,
    			    // we will instead just use the path already memorized.
    			    // Ortherwise, a path is calculated, and the effect part above will
    			    // memorized it.

    			    // If no path can be found, this guard returns null... hence disabled.
    			    return belief.findPathTo(position, false) ;
    			 })
    		   . lift() ;
    }

    /**
     * This action will in principle drive the agent towards a previously memorized 
     * goal-location (a position in the world), along a previously memorized path. 
     * The exact behavior is controlled
     * by what its guard passes/propagates to it. The given guard below is a dummy guard which is
     * always enabled and simply passes the pair (null,null),which will lead to the above
     * behavior. This guard should be replaced when using this action.
     *
     * If the guard propagates (d,path) where d is a destination and p is a non-null path,
     * this pair will be memorized as the new goal-location/path pair for the agent
     * to follow.
     *
     * If the guard propagates (*,null) the action will stick to the currently memorized
     * goal-location/path.
     *
     * IMPORTANT: this action will still try to move the agent to the goal-location, even
     * when it is already there. A higher level reasoning of the agent should decide
     * that whether it wants to stop this stuttering, and how (e.g. by imposing a guard
     * that prevents the stuttering, or by clearing the memorized goal-location.
     */
    private static Action unguardedNavigateTo(String actionName) {
    	Action move = action(actionName)
                .do2((BeliefState belief) -> (Pair<Vec3,List<Vec3>> q)  -> {
                	// q is a pair of (distination,path). Passing the destination is not necessary
                	// for this tactic, but it will allows us to reuse the effect
                	// part for other similar navigation-like tactics
                	var destination = q.fst ;
                	var path = q.snd ;

                	//System.out.println("### tactic NavigateTo " + destination) ;

                	//if a new path is received, memorize it as the current path to follow:
                	if (path!= null) {
                		belief.applyPath(belief.worldmodel.timestamp, destination, path) ;
                	}
                    //move towards the next way point of whatever the current path is:
                	//System.out.println(">>> destination: " + destination) ;
                	//System.out.println(">>> path: " + path) ;
                	//System.out.println(">>> memorized dest: " + belief.getGoalLocation()) ;
                	//System.out.println(">>> memorized path: " + belief.getMemorizedPath()) ;
                	if (belief.getMemorizedPath() != null) {
                		belief.env().moveToward(belief.id, belief.worldmodel().getFloorPosition(),belief.getCurrentWayPoint());
                		return belief ;
                	}
                	else return null ;
                    })
                // a dummy guard; override this when using this action:
                .on((BeliefState belief) -> new Pair(null,null)) ;
/* ----- conflict
                    })
                .on((BeliefState belief) -> {
                	Vec3 currentDestination = belief.getGoalLocation() ;
                	if (currentDestination==null || currentDestination.distance(position) >= 0.05
                			|| !belief.mentalMap.hasActivePath()) {
                		// the agent has no current location to go to, or the new goal location
                		// is quite different from the current goal location, we will then calculate
                		// a new path:
                		var path = belief.findPathTo(position) ;
                		if (path==null || path.length==0) return null ;
                		return new Tuple(position,path) ;
                	}
                	else {
                		// the agent is already going to the specified location. So there is
                		// no need to calculate a new path. We will return a pair(position,null)
                		// to signal this.
                		return new Tuple(position,null) ;
                	}}) ;
									*/
    	return move ;
    }
    
    private void adjustPath() {
    	
    }


    /**
     * When asked to get to a certain (far away) position, the agent would need
     * multiple update rounds to get to the goal position. To do this, it first
     * ask a path to be calculated (some pathfinding algorithm is used to do this).
     * If a path can be found, the agent will store this in its memory and proceed
     * to follow this path. It may happen that this path leads to a door that
     * turns out to be closed; so, path re-calculation is needed.
     *
     * This method constructs a tactic T that tries to identify when recalculation of
     * path to the the goal position, namely when:
     *
     *    (1) the agent observes a door has a state which is different than the last time
     *    it saw it.
     *    
     *    (2) the agent observes an NPC or another agent, and its position has changed 
     *        by more than distance 2 from the last time the agent saw it.
     *        
     *        For now, we will not take enemies into account.
     *
     *  Path recalculation is forced by clearing the goal-position.
     */
    public static Tactic forceReplanPath() {
        Tactic clearTargetPositionOrBendPath = action("Force path recalculation.")
                .do2((BeliefState belief) -> (Integer flag) -> {
                	System.out.println("####Detecting some path-planning relevant state change. Forcing path recalculation @" + belief.worldmodel.position) ;
                	switch (flag) {
                	  case 0 : // replan-due to some door has changed state:
                		       belief.clearGoalLocation();
                	  		   try { 
                	  				Thread.sleep(700); // waiting for the door animation
                	  		   }
                	  		   catch(Exception e) { }
                	  		   break ;
                	  case 1 : 	belief.clearGoalLocation() ; break ;
                	  case 2 :  // monsters presence force path bending:
                		  	    if (belief.delayWhenBendingPath>0) {
                		  	    	try {
                		  	    		Thread.sleep(belief.delayWhenBendingPath);
                		  	    	}
                		  	    	catch(Exception e) { 
                		  	    		// swallow
                		  	    	}
                		  	    }
                		        boolean success = belief.bendPathToEvadeMonsters() ;
                		        System.out.println(">>> bending success: " + success) ;
                		        if (! success)
                		        	 belief.clearGoalLocation();
                	}
                	return belief ;
                })                
                .on((BeliefState belief) -> {
                	
                	if (belief.getGoalLocation() == null) 
                		return null ;
                	
                	// be careful with the threshold value (the "10" below);
                	// if this is set too low, the agent may unnecessarily do re-plan
                	// if it is set too high, the agent may get stuck longer

                    /*
                	var someDoorHasChangedState =
                			belief.knownDoors().stream()
                	        . anyMatch(door -> door.lastStutterTimestamp < 0
                	                           && door.hasPreviousState()
                	                           && door.hasChangedState())
                	        ;
                	*/
                    // replacing the above logic with this one that should be more reliable:
                	var someDoorHasChangedState = belief.changedEntities.stream().anyMatch(e -> e.type == LabEntity.DOOR) ;

                	if (someDoorHasChangedState) 
                		return 0 ;
                	// System.out.println(">>> forcereplan: no door change state") ;
                	
                	// case 2: detecting monsters/NPC/other-agents ; they might be blocking the
                	// agent's current path, or simply need to be avoided because dangerous (monsters)
                	var mobiles = belief.worldmodel.elements.values().stream() 
                			.filter(e -> e.type.equals(LabEntity.NPC)  
                						 || e.type.equals(LabEntity.ENEMY)  
                					     || (e.type.equals(LabEntity.PLAYER) && !e.id.equals(belief.id)))
                			.collect(Collectors.toList()) ;
                	for (var mob : mobiles) {
                		if (mob.timestamp == belief.worldmodel.timestamp
                				//&& mob.type.equals(LabEntity.ENEMY) 
                				) {
                			if (belief.updateCount > belief.lastTimePathReplanDueToMobile + 8) {
                				belief.lastTimePathReplanDueToMobile = belief.updateCount+1 ;
                				System.out.println(">>>> mob in vicinity, mob-replan-timeout. Mob:" + mob.id) ;
                				return 2 ;
                			}
                			
                		}
                		if (mob.timestamp == belief.worldmodel.timestamp
                				&& mob.hasPreviousState()
                				&& Vec3.distSq(mob.position, mob.getPreviousState().position) > 4f
                				) {
                			System.out.println(">>>> mob noticed, with significant diff from its last seen pos. Mob:" + mob.id) ;
                    		return 1  ;
                		}
                		if (!mob.hasPreviousState() && mob.timestamp == belief.worldmodel.timestamp) {
                			System.out.println(">>>> noticing new mob " + mob.id) ;                			
                			// the mob was seen first time:
                			return 2 ;
                		}
                	}
                	return null ;
                	/*
                	if (belief.getGoalLocation() == null
                		|| belief.worldmodel.timestamp - belief.getGoalLocationTimestamp() < 50) {
                		return false ;
                	}

                	var closeby_doors = belief.closebyDoor() ;
                	//System.out.println(">>> #close-by doors: " + closeby_doors.size()) ;
            		for (var door : closeby_doors) {
                		if (!belief.isOpen(door)) {
                			return true ;
                		}
                	}
                	return false ;
                	*/

                })
                .lift() ;
           return clearTargetPositionOrBendPath ;
    }
    /**
     * This tactic detects if the agent gets stuck (e.g. if the position turns out to be unreachable),
	 * in which case it will check again if a path to the position exists, according
	 * to the latest information it has. If so, the agent will follow this path, and
	 * else the tactic is not enabled.
     * @return
     */
    public static Tactic tryToUnstuck() {
    	Tactic unstuck = action("Trying to unstuck")
    			.do1((BeliefState belief) -> {
    				System.out.println("#### STUCK, probably cannot get past a turn-corner: @"
    			           + belief.worldmodel.position
    			           + ", current way-point: "
    			           + belief.getCurrentWayPoint()) ;
    	    		var unstuckPosition = unstuck(belief) ;
    	    		if (unstuckPosition != null) {
    	    			// no need to do this anymore; as moveToward now uses the agent's floor-pos as reference
    	    			// unstuckPosition.y += belief.worldmodel.extent.y ;
    	    			System.out.println("#### forcing a move past the corner...to " + unstuckPosition) ;
    	    			//belief.mentalMap.insertNewWayPoint(unstuckPosition);
    	    			belief.env().moveToward(belief.id, belief.worldmodel().getFloorPosition() ,unstuckPosition) ;
    	    			try {
    	    				Thread.sleep(100) ;
    	    			}
    	    			catch(Exception e) { } 
    	    			//belief.env().moveToward(belief.id, belief.worldmodel().getFloorPosition() ,unstuckPosition) ;
    	    		}
    	    		else {
    	    			// else .... for now do nothing :|
    	        		System.out.println("#### unfortunately cannot find an unstuck solution...") ;
    	    		}
    	    	    belief.clearStuckTrackingInfo();
    				return belief ;
    			})
    			.on_((BeliefState belief) -> {
    				//System.out.println(">>> stuck: " + belief.isStuck() + ", goal loc: " + belief.getGoalLocation()) ;
    				return belief.getGoalLocation() != null &&  belief.isStuck() ;
    			})
    			.lift() ;
    	return unstuck ;
    }
    
    static Matrix rotationXZ(float degree) {
       Matrix M	=  Matrix.mkM3x3(
       		(float) Math.cos(Math.toRadians(degree)), 0 , (float) - Math.sin(Math.toRadians(degree)),
       		0, 1, 0,
       		(float) Math.sin(Math.toRadians(degree)), 0, (float) Math.cos(Math.toRadians(degree))
       		);
       return M ;
    }
    
    static Matrix ROTxz75 = rotationXZ(75) ;
    static Matrix ROTxzMin75 = rotationXZ(360-75) ;
    
    static Random rnd = new Random() ;

    /**
     * If the agent gets stuck in an bending corner (because the navigation algorithm does not
     * take agent's dimension into account... :| ), this method tries to find a position close
     * to the agent, which can unstuck agent. The agent can travel to this new position, which
     * will move it past the stucking corner.
     *
     * If such a position can be found, it is returned. Else null is returned.
     *
     * Note that the method simply checks if there is a navigation polygon that contains this
     * unstuck position. It doesn't check if this position is actually reachable from the agent's
     * current position.
     */
    public static Vec3 unstuck(BeliefState belief) {
    	Vec3 p = belief.worldmodel().getFloorPosition() ;
    	var agent_current_direction = Vec3.sub(belief.getCurrentWayPoint(), p) ;

    	// let's try 90-degree to left or right:
    	try {
    		// expensive :(
    		//System.out.println(">>> trying perpendicular...") ;
    		
    		// throw an exception if the length is 0:
    		Vec3 direction =  agent_current_direction.normalized() ;
    		List<Vec3> qs = new LinkedList<>() ;
    		var v1 = Vec3.mul(Matrix.ROTxz90.apply(direction), 0.4f) ;
    		var q1 = Vec3.add(p,v1) ;
    		if (isPointInNavigableSurface(belief,q1)) {
    			qs.add(q1) ;
    		}
    		var v2 = Vec3.mul(Matrix.ROTxz270.apply(direction), 0.4f) ;
    		var q2 = Vec3.add(p,v2) ;
    		if (isPointInNavigableSurface(belief,q2)) {
    			qs.add(q2) ;
    		}
    		/*
    		if (q1 != null && (q2==null || v1.lengthSq() < v2.lengthSq())) {
    			return q1 ;
    		}
    		if (q2 != null && (q1 == null || v2.lengthSq() <= v1.lengthSq())) {
    			return q2 ;
    		}
    		*/
    		if (! qs.isEmpty()) {
    			return qs.get(rnd.nextInt(qs.size())) ;
    		}
    	}
    	catch(Exception e) {  }
    	
    	
    	// try straight vertical or straight horizontal:
    	var x_orientation = Math.signum(agent_current_direction.x) ;  // 1 if the agent is facing eastly, and -1 if westly
    	var z_orientation = Math.signum(agent_current_direction.z) ;  // 1 if the agent is facing northly, and -1 if southly
    	// System.out.println("#### calling unstuck()") ;
    	// try E/W unstuck first:
    	if (x_orientation != 0) {
    		p = p.copy() ;
    		p.x += TacticLib.UNSTUCK_DELTA * x_orientation ;
    		if (isPointInNavigableSurface(belief,p)) return p ;
        	//if (mentalMap.pathFinder.graph.vecToNode(p) != null) return p ;
    	}
    	// try N/S unstuck:
    	if (z_orientation != 0) {
    		p = p.copy() ;
    		p.z += TacticLib.UNSTUCK_DELTA * z_orientation ;
        	if (isPointInNavigableSurface(belief,p)) return p ;
        	//if (mentalMap.pathFinder.graph.vecToNode(p) != null) return p ;
    	}
    	
    	
    	// can't find an unstuck option...
    	return null ;
    }

    private static boolean isPointInNavigableSurface(BeliefState belief, Vec3 p) {
    	List<Face> faces = new LinkedList<>() ;
    	faces.addAll(belief.pathfinder().faces) ;
    	faces.sort((f1,f2) ->
    	     Float.compare(Vec3.dist(belief.pathfinder().vertices.get(f1.vertices[0]),p),
    	    		       Vec3.dist(belief.pathfinder().vertices.get(f2.vertices[0]),p))) ;
    	for (Face f : faces) {
    		if (f.distFromPoint(p, belief.pathfinder().vertices) <= DIST_SURFACE_THRESHOLD_STUCK)
    			return true ;
    	}
    	return false ;
    }


    /**
     * Send an interact command if the agent is close enough.
     * @param objectID The id of the in-game entity to interact with
     * @return A tactic in which the agent will interact with the object
     */
    public static Tactic interact(String objectID) {
        Tactic interact = action("Interact")
               . do2((BeliefState belief) -> (WorldEntity e) -> {
            	   
            	      var obs =  belief.env().interact(belief.id, e.id, LabWorldModel.INTERACT) ;
            	   
                	  // var obs = belief.worldmodel().interact(belief.env(), LabWorldModel.INTERACT, e)  ;
                	  // force update to worldmodel:
                	  //System.out.println("## interacted with " + objectID) ;
                	  
                	  // Fix
                	  // This merge cause update-steal, which causes the change-entities detection to
                	  // fail to detect. We remove it; the agent can still get the updated state, but
                	  // in the next update-cycle:
                	  // belief.mergeNewObservationIntoWOM(obs);
                	  
                	  // add a wait... not an ideal solution as it ignores thread interrupt
                	  try {
                		  // LR has 0.5s timeout before a button can be interacted again, so we need to wait: 
                		  Thread.sleep(650) ;
                	  }
                	  catch(Exception exc) {
                		  // swallowing thread exception...
                	  }
                      return belief;
                    })
               . on((BeliefState belief) -> {
                	var e = belief.worldmodel.getElement(objectID) ;
                	//System.out.println(">>>> " + objectID + ": " + e) ;
                	if (e==null) return null ;
                	// System.out.println(">>>>    dist: " + Vec3.dist(belief.worldmodel.getFloorPosition(),e.getFloorPosition())) ;

                	if (belief.canInteract(e.id)) {
                		return e ;
                	}
                	//System.out.println(">>> cannot interact with " + e.id) ;
            		//System.out.println("    Agent pos: " + belief.worldmodel.getFloorPosition()) ;
            		//System.out.println("    Entity pos:" + e.getFloorPosition()) ;
            		//System.out.println("    Entity extent:" + e.extent) ;
            		
                	return null ;
                    })
               . lift();
        return interact ;
    }

    /*
     * This method will return an observe tactic which will do nothing but receive an
     * observation and update the agent
     * @return A do nothing action
     */
    public static Tactic observe() {
        //this is a wait action which will allow the agent to retrieve an observation
        Tactic observe = action("Observe")
                .do1((BeliefState belief) -> {
                	// var obs = belief.worldmodel.observe(belief.env());
                	// force wom update:
                	// belief.mergeNewObservationIntoWOM(obs) ;

                	// agent-runtime already performs update at the start of a cyle, so we just return
                	// the resulting current belief:
                    return belief;
                }).lift();
        return observe;
    }
    
    public static Action mkScreenShot(String filename) {
    	return action("MakeScreenShot")
    	.do1((BeliefState belief) -> {
    	   belief.env().mkScreenShot(filename) ;
    	   return belief ;
    	}) ;
    }

/*
    public static Tactic observeOnce() {
        //this is a wait action which will allow the agent to retrieve an observation
        Tactic observe = action("Observe once")
                .do1((BeliefState belief) -> {
                	LabWorldModel o = belief.env().observe(belief.id);
                    belief.updateBelief(o);
                    return belief;
                }).on((BeliefState b) -> !b.worldmodel.didNothingPreviousGameTurn).lift();
        return observe;
    }
    */

    /**
     * This tactic will allow an agent to observe, and share its observation to other agents.
     */
    public static Tactic shareObservation(String id){
        return action("Share map")
                . do1((BeliefState belief)-> {
                	var obs = belief.env().observe(belief.id);
                	// force wom update:
                	belief.mergeNewObservationIntoWOM(obs) ;
                    Acknowledgement a = belief.messenger().send(id,0, Message.MsgCastType.BROADCAST, "","ObservationSharing",obs) ;
                    return belief;
                }).lift();
    }

    /**
     * This tactic cause the agent to receive an memory share if one is available and make an observation
     * @return The tactic which will receive the memory share
     */
    public static Tactic receiveObservationShare(){
        return action("Receive map sharing")
                . do1((BeliefState belief)-> {
                	//get the  messages
                	Message m = belief.messenger().retrieve(M -> M.getMsgName().equals("ObservationSharing")) ;
                    while(m != null){
                        //apply the memory share
                    	var obs = (LabWorldModel) m.getArgs()[0] ;
                    	if (obs.timestamp >= belief.worldmodel.timestamp) {
                    		// Don't do this! It would take over the agent position of the new obs.
                    		// belief.worldmodel.mergeNewObservation(obs) ;
                    		// Do this instead:
                    		for (WorldEntity e : obs.elements.values()) {
                    			belief.worldmodel.updateEntity(e) ;
                    		}
                    	}
                    	else {
                    		belief.worldmodel.mergeOldObservation(obs) ;
                    	}
                    	belief.pathfinder().markAsSeen(obs.visibleNavigationNodes);
                        m = belief.messenger().retrieve(M -> M.getMsgName().equals("ObservationSharing")) ;
                    }
                    //do an observation
                    //LabWorldModel o = belief.env().observe(belief.id);
                    //belief.updateBelief(o);
                    return belief;
                })
                .on_((BeliefState S) -> S.messenger().has(M -> M.getMsgName().equals("ObservationSharing")))//check if there is a memory share available
                .lift() ;
    }


    public static Tactic sendPing(String idFrom, String idTo){
        return action("Send ping")
                . do1((BeliefState belief)-> {
                    //do an observation
                	//LabWorldModel o = belief.env().observe(belief.id);

                    //send the message
                    Acknowledgement a = belief.messenger().send(idFrom,0, Message.MsgCastType.SINGLECAST, idTo,"Ping", "") ;
                    return belief;
                }).lift();
    }

    /**
     * This tactic cause the agent to receive a ping if one is available and make an observation
     * @return The tactic which will receive the ping returns true or false on depended on whether there was an ping yes or no
     */
    public static Tactic receivePing(){
        return action("Receive ping")
                . do1((BeliefState belief)-> {
                    //get the ping message
                    Message m = belief.messenger().retrieve(M -> M.getMsgName().equals("Ping"));

                    //update the belief state
                    if(m != null) belief.receivedPing = true;

                    //do an observation
                    //LabWorldModel o = belief.env().observe(belief.id);

                    //return whether we have received an observation yes or no
                    return belief;
                })
                .lift() ;
    }

    /**
     * This method will construct a tactic in which the agent will "explore" the world.
     * The tactic will locate the nearest reachable navigation node which the agent
     * has not discovered yet, and drive the agent to go there.
     */
    public static Tactic explore() {

    	var memo = new MiniMemory("S0") ;
    	// three states:
    	//  S0 ; initial exploration state, a new exploration target must be set
    	//  inTransit: when the agent is traveling to the set exploration target
    	//  exhausted: there is no morsetting a new exploration targete exploration target left
    	//

    	var explore_ =
    			unguardedNavigateTo("Explore: traveling to an exploration target")

    			. on((BeliefState belief) -> {
    				 if(memo.stateIs("S0")) {
    					 // in this state we must decide a new exploration target:

                         //get the location of the closest unexplored node
        				 var position = belief.worldmodel().getFloorPosition() ;
        				 //System.out.println(">>> #explored nodes:" + belief.pathfinder.numberOfSeen()) ;
        				 var path = belief.pathfinder().explore(position,BeliefState.DIST_TO_FACE_THRESHOLD) ;

        				 if (path==null || path.isEmpty()) {
        					memo.moveState("exhausted") ;
                            System.out.println("### no new and reachable navigation point found; agent is @" + belief.worldmodel.position) ;
                            return null ;
        				 }
        				 List<Vec3> explorationPath = path.stream()
        						            .map(v -> belief.pathfinder().vertices.get(v))
        						            .collect(Collectors.toList()) ;

        				 var target = explorationPath.get(explorationPath.size() - 1) ;
        				 System.out.println("### setting a new exploration target: " + target) ;
                         System.out.println("### abspath to exploration target: " + path) ;
                         //System.out.println("### path to exploration target: " + explorationPath) ;
                         memo.memorized.clear();
                         memo.memorize(target);
                         memo.moveState("inTransit") ; // move the exploration state to inTransit...
                         return new Pair(target, explorationPath);//return the path finding information
    				 }
    				 else if (memo.stateIs("inTransit")) {
    					 Vec3 exploration_target = (Vec3) memo.memorized.get(0) ;
                         // note that exploration_target won't be null because we are in the state
                         // in-Transit
                         Vec3 agentLocation = belief.worldmodel().getFloorPosition() ;
                         Vec3 currentDestination = belief.getGoalLocation() ;
                         var distToExplorationTarget = Vec3.dist(agentLocation,exploration_target) ;
                         if (distToExplorationTarget <= EXPLORATION_TARGET_DIST_THRESHOLD // current exploration target is reached
                             || currentDestination==null
                             || Vec3.dist(currentDestination,exploration_target) > 0.3) {
                        	 // in all these cases we need to select a new exploration target.
                        	 // This is done by moving back the exploration state to S0.
                        	 memo.moveState("S0");
                         }
                         if (distToExplorationTarget<=EXPLORATION_TARGET_DIST_THRESHOLD) {
                        	 System.out.println("### dist to explroration target " + distToExplorationTarget) ;
                         }
                         // System.out.println(">>> explore in-transit: " + memo.stateIs("inTransit")) ;
                         // System.out.println(">>> exploration target: " + exploration_target) ;
                         // We should not need to re-calculate the path. If we are "inTransit" the path is
                         // already in the agent's memory
                         // return new Tuple(g, belief.findPathTo(g));
                         return new Pair(exploration_target,null);
    				 }
                     // in all other cases, the guard is not enabled:
    				 return null ;
                 })
               . lift();


        return FIRSTof(
        		 forceReplanPath(),
				 tryToUnstuck(),
				 explore_) ;
    }

}
