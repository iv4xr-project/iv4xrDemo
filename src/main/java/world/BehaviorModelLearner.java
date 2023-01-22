package world;

import java.util.*;
import java.util.stream.Collectors;

import agents.tactics.TacticLib;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.*;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.LineIntersectable;
import eu.iv4xr.framework.spatial.Obstacle;
import eu.iv4xr.framework.spatial.Vec3;

public class BehaviorModelLearner {
	
	boolean includeInModel(WorldEntity e) {
		switch (e.type) {
		case LabEntity.DOOR  :
		case LabEntity.SWITCH :
		case LabEntity.GOAL :
		case LabEntity.COLORSCREEN : return true ;
		default : 
		}
		return false ;
	}
	
	final static String START = "START" ;
	final static String ANCHOR = "ANCHOR" ;
	
	/**
	 * The zones that are considered to be already correct. They will not be
	 * destroyed during zone recalculation. However, more elements can still
	 * be added to stable zones.
	 */
	public List<String> stableZones = new LinkedList<>() ;
	
	/**
	 * Track the number of learning updates (calls to {@link #learn(BeliefState, GameWorldModel)})
	 * we have so far.
	 */
	int updateRound = 0 ;
	
	public int recalculateZonesInterval = 20 ;
	private int recalculateZonesCooldown = 0 ;
	
	private String lastInteractedButton = null ;
	
	/**
	 * Obviously to create an instance of the Learner :).
	 */
	public BehaviorModelLearner() { }

	boolean isDoor(GWObject o) {
		return o.type.equals(LabEntity.DOOR) ;
	}
	
	public boolean isButton(GWObject o) {
		return o.type.equals(LabEntity.SWITCH) ;
	}
	
	boolean isDoor(WorldEntity e) {
		return e.type.equals(LabEntity.DOOR) ;
	}
	
	boolean isButton(WorldEntity e) {
		return e.type.equals(LabEntity.SWITCH) ;
	}
	
	boolean isColorScreen(WorldEntity e) {
		return e.type.equals(LabEntity.COLORSCREEN) ;
	}
	
	boolean isObstacle(GWObject o) {
		return o.type.equals(LabEntity.DOOR) 
				|| o.type.equals(LabEntity.COLORSCREEN) ;
	}
	
	boolean isObstacle(LabEntity e) {
		return e.type.equals(LabEntity.DOOR) 
				|| e.type.equals(LabEntity.COLORSCREEN) ;
	}
	
	Vec3 getDoorCenterPoint(LabEntity door) {
		Vec3 center = door.getFloorPosition().copy() ;
		center.x = Math.round(center.x) ;
		center.z = Math.round(center.z) ;
		return center ;
		/*
		if (!(Boolean) door.properties.get("isOpen")) {
			// the door is closed
			return floorPosition ;
		}
		// else the door is open
		if (door.extent.x < door.extent.z) {
			// the door is open vertically
			float newX = (float) (Math.floor((double) door.position.x - 0.5) + 1) ;
			return new Vec3(newX ,door.position.y, door.position.z) ;
		}
		// else
		float newZ = (float) (Math.floor((double) door.position.z - 0.5) + 1) ;
		return new Vec3(door.position.x, door.position.y, newZ) ;
		*/
	}
	
	
	public GWObject getObj(GameWorldModel model, String id) {
		return model.defaultInitialState.objects.get(id) ;
	}
	
	private void fakelyBlockDoor(Obstacle o) {
		o.isBlocking = true ;
		LabEntity door = (LabEntity) o.obstacle ;
		Vec3 oldExtend = door.extent.copy() ;
		if ((boolean) door.properties.get("isOpen")) {
			// if the door is actually in an open state
			// make the door larger so that it would physically block
			// the door-frame it is in.
			// DONT forget to restore this!
			if (door.extent.x < door.extent.z) {
				door.extent.x += 0.75f ;
			}
			else {
				door.extent.z += 0.75f ;
			}
			//System.out.println(">>>> fakelyBlockDoor adjust " + door.id + " extent, org:" 
			//		+ oldExtend + ", new:" + door.extent) ;
		}
	}
	
	
	private void restoreFakelyBlockedDoor_ToUnblockingState(Obstacle o) {
		o.isBlocking = false ;
		LabEntity door = (LabEntity) o.obstacle ;
		Vec3 oldExtend = door.extent.copy() ;
		if ((boolean) door.properties.get("isOpen")) {
			// if the door is actually in an open state, the previos
			// fakely-block step adjust the door extent, so now we need
			// to undo this adjustment:
			if (door.extent.x > door.extent.z) {
				door.extent.x -= 0.75f ;
			}
			else {
				door.extent.z -= 0.75f ;
			}
			//System.out.println(">>>> restorefakelyBlockDoor adjust " + door.id + " extent, org:" 
			//		+ oldExtend + ", new:" + door.extent) ;

		}
	}
	
	/**
	 * Close all obstacles in the nav-graph. Return those that were originally open,
	 * so that we can restore them later.
	 */
	List<Obstacle<LineIntersectable>> closeAllObstacles(BeliefState S) {
		List<Obstacle<LineIntersectable>>  openObstacles = new LinkedList<>() ;

		for (var obstacle : S.pathfinder().obstacles) {
			if (!obstacle.isBlocking) {
				//System.out.println(">>> fakely closing door " 
				//			+ ((LabEntity) obstacle.obstacle).id) ;
				fakelyBlockDoor(obstacle) ;
				openObstacles.add(obstacle) ;
			}
		}
		return openObstacles ;
	}
	
	/**
	 * Get the obstacle-object that wraps the given door.
	 */
	private Obstacle<LineIntersectable> getObstacle(String doorId, BeliefState S) {
		for (var obstacle : S.pathfinder().obstacles) {
			LabEntity e = (LabEntity) obstacle.obstacle ;
			if (e.id.equals(doorId)) {
				return obstacle ;
			}
		}
		return null ;
	}
	
	private void restoreObstaclesState(List<Obstacle<LineIntersectable>> originallyOpen) {
		for (var obstacle : originallyOpen) {
			restoreFakelyBlockedDoor_ToUnblockingState(obstacle) ;
		}
	}
	
	/*
	private void restoreObstaclesState(BeliefState S, List<LabEntity> originallyOpen) {
		for (var door : originallyOpen) {
			S.pathfinder().setBlockingState(door,false);
		}
	}
	*/
	
	private void debugPrintObstaclesStates(BeliefState S) {
		System.out.println("### OBSTACLES:") ;
		for (var obstacle : S.pathfinder().obstacles) {
			LabEntity e = (LabEntity) obstacle.obstacle ;
			System.out.print("    " + e.id + ", blocking: " + obstacle.isBlocking) ;
			if (isDoor(e)) {
				System.out.print(", open:" + e.properties.get("isOpen")) ;
			}
			
			System.out.println("") ;
		}
	}
	
	/**
	 * Check if a newly learned entity e1 is in the same zone with an anchor
	 * object. The entity e1 can be a door. The anchor, being an anchor,
	 * CANNOT be a blocker.
	 * 
	 * <p>This method assumed that all doors have been (fakely) closed.
	 */
	private boolean checkIfNewEntity_IsInSameZone(GWObject o1, GWObject anchor, BeliefState S) {
		Obstacle<LineIntersectable> obstacleWrapper1 = null ;
		if (isObstacle(o1)) {
			obstacleWrapper1 = getObstacle(o1.id,S) ;
			// unblock e1, if it is a door; this should be sufficient, no need to
			// mess with the door extend here
			//System.out.println(">>>> fakely unblocking " + o1.id + ", anchor:" + anchor.id) ;
			//System.out.println("     e@" +o1.position + ", anchor@" + anchor.position) ;
			obstacleWrapper1.isBlocking = false ; 
		}
		var path = S.pathfinder().findPath(
				o1.position, 
				anchor.position,
				BeliefState.DIST_TO_FACE_THRESHOLD) ;	
		/*
		if (path != null) {
			System.out.println(">>>> " + o1.id 
					+ "@" + o1.position
					+ " ---> " 
					+ anchor.id + "@" + anchor.position + " #" + path.size()) ;
			debugPrintObstaclesStates(S) ;
		}
		*/
		if (obstacleWrapper1 != null) {
			// close it again...  this should be sufficient, no need to
			// mess with the door extend here
			obstacleWrapper1.isBlocking = true ; 
		}
		return path != null ;
	}
	
	/**
	 * Check if a newly learned entity e1 is in the given zone.
	 * This method assumed that all doors have been (fakely) closed.
	 */
	private boolean checkIfNewEntity_IsInZone(GWObject o1, 
			GWZone zone, 
			Map<String,GWObject> zoneAnchors,
			BeliefState S,
			GameWorldModel model) {
		GWObject anchor = zoneAnchors.get(zone.id) ;
		if (anchor == null) {
			// the zone has no anchor!! Should not happen
			// we will just return false:
			return false ;
		}
		return checkIfNewEntity_IsInSameZone(o1,anchor,S) ;
	}
	
	List<GWZone> getZones(GWObject o, GameWorldModel model) {
		List<GWZone> zs = new LinkedList<>() ;
		for(var Z : model.zones) {
			if (Z.members.contains(o.id)) {
				zs.add(Z) ;
				if(! isDoor(o)) {
					return zs ;
				}
				if (zs.size() == 2) return zs ;
			}
		}
		return zs ;
	}
	
	GWObject getAnAnchor(GWZone zone, GameWorldModel model) {
		for(var oId : zone.members) {
			GWObject o = getObj(model,oId) ;
		    if (! isObstacle(o)) return o ;
		}
		return null ;
	}
	
	public GWZone getCurrentZone(BeliefState S, GameWorldModel model) {
		var openDoors = closeAllObstacles(S) ;
		GWZone currentZone = null ;
		for(var Z : model.zones) {
			for (var oId : Z.members) {
				GWObject o = getObj(model,oId) ;
				if (isObstacle(o)) continue ;
				var path = S.pathfinder().findPath(
						S.worldmodel().getFloorPosition(), 
						o.position,
						BeliefState.DIST_TO_FACE_THRESHOLD) ;	
				if (path != null && !path.isEmpty()) {
					currentZone = Z ;
					break ;
				}
					
			}
			if (currentZone != null) break ;
		}
		restoreObstaclesState(openDoors) ;
		return currentZone ;
	}
	
	public List<String> getCriticalDoors(BeliefState S, String targetEntity) {
		List<String> critDoors = new LinkedList<>() ;
		LabEntity target = S.worldmodel().getElement(targetEntity) ;
		if (target==null) 
			return null ;
  		for (WorldEntity e : S.worldmodel().elements.values()) {
  			var e_ = (LabEntity) e ;
			if (!isDoor(e) || e.id.equals(targetEntity)) continue ;
			if (! S.pathfinder().isBlocking(e_))
				continue ;
			
			S.pathfinder().setBlockingState(e_,false);			
		    var path = TacticLib.calculatePathToDoor(S, target, 0.9f) ;
			if (path != null && !path.snd.isEmpty()) {
				critDoors.add(e.id) ;
			}
			S.pathfinder().setBlockingState(e_,true);
			if (critDoors.size()>0) return critDoors ;
		}
		return null ;
	}
	
	public void recalculateZones(BeliefState S, GameWorldModel model) {
		
		// wipe all zones, except those marked as stable, or has no anchor:
		model.zones.removeIf(Z -> ! stableZones.contains(Z.id) || getAnAnchor(Z,model)==null) ;
		
		Map<String,GWObject> anchors = new HashMap<>() ;
		for (var Z : model.zones) {
			anchors.put(Z.id, getAnAnchor(Z,model)) ;
		}
		
		// get all non-obstacle objects that are not in any zone:
		List<GWObject> zonelessNonObstacles = 
				model.defaultInitialState.objects.values().stream()
				.filter(o -> !isObstacle(o) && getZones(o,model).isEmpty())
				.collect(Collectors.toList()) ;
		
		
		var originallyOpenObstacles = closeAllObstacles(S) ;
		
		// check if zoneless-nonObstacles (e.g. buttons) can be placed in
		// ones. If not, create new zones as needed:
		while (! zonelessNonObstacles.isEmpty()) {
			GWObject o = zonelessNonObstacles.remove(0) ;
			//LabEntity e = S.worldmodel().getElement(o.id) ;
			// check if o can be put in a zone we have so far:
			int zoneCount = 0 ;
			for (var Z : model.zones) {
				boolean inZ = checkIfNewEntity_IsInZone(o,Z,anchors,S,model) ;
				if (inZ) {
					Z.members.add(o.id) ;
					zoneCount++ ;
					if (! isDoor(o)) break ;
					if (zoneCount == 2) break ;
				}
			}
			if (zoneCount==0) {
				// the object cannot be placed in existing zones.
				// Create a new zone:
				String zoneId = "Z" + o.id ;
				GWZone newZone = new GWZone(zoneId) ;
				newZone.addMembers(o.id);
				model.zones.add(newZone) ;
				anchors.put(zoneId, o) ;
			}
		}
		
		// check if obstacles that are zoneless can now be put in zones,
		// also if old obstacles can be put in new zones
		List<GWObject> obstaclesThatCanStillBePlaced = 
				model.defaultInitialState.objects.values().stream()
				.filter(o -> { 
					if (!isObstacle(o)) return false ;
					int numberOfZones = getZones(o,model).size() ;
					return numberOfZones == 0
							|| (numberOfZones==1 && isDoor(o)) ; })
				.collect(Collectors.toList()) ;
		
		for(GWObject obstacle : obstaclesThatCanStillBePlaced) {
			assignObstacleToZone(obstacle,anchors,S,model) ;
		}
			
		// remove dummy anchor nodes if the zone can do without it:
		
		restoreObstaclesState(originallyOpenObstacles) ;
	}
	
	private void assignObstacleToZone(GWObject obstacle, 
			Map<String,GWObject> anchors,
			BeliefState S,
			GameWorldModel model) {
		int zoneCount = getZones(obstacle,model).size() ;
		if (zoneCount>=2) return ;
		//LabEntity e = S.worldmodel().getElement(obstacle.id) ;
		for (var Z : model.zones) {
			boolean inZ = checkIfNewEntity_IsInZone(obstacle,Z,anchors,S,model) ;
			if (inZ) {
				Z.members.add(obstacle.id) ;
				zoneCount++ ;
				if (! isDoor(obstacle)) break ;
				if (zoneCount == 2) break ;
			}
		}
	}
	
	
	private void firstUpdate(BeliefState S, GameWorldModel model) {
		String id = "START" + S.id ;
		GWObject agentStart = getObj(model,id) ;
		if (agentStart != null) return ;
		
		// else we create a node representing the agent start location:
		agentStart = new GWObject(id, "START") ;
		agentStart.position = S.worldmodel().getFloorPosition().copy() ;
		agentStart.destroyed = true ;
		model.defaultInitialState.objects.put(agentStart.id, agentStart) ;
	}
	
	public void learn(BeliefState S, GameWorldModel model) {
		//System.out.print(">>>> invoking learn");
		if (updateRound == 0) 
			// creating initial zone and agent-start node, if this is the first call to
			// learn, and if we start from an empty model:
			firstUpdate(S,model) ;
		learnWorker(S,model) ;
		updateRound++ ;
		if (recalculateZonesCooldown >= recalculateZonesInterval) {
			recalculateZones(S,model) ;
			recalculateZonesCooldown = 0 ;
		}
		recalculateZonesCooldown++ ;
	}
	
    private void learnWorker(BeliefState S, GameWorldModel model) {
    	
    	GWState modelstate = model.defaultInitialState ;
    	for(WorldEntity e : S.changedEntities) {
    		if (getObj(model,e.id) != null) // the entity is already in the model
    			continue ;
    		if (!includeInModel(e)) // check if we want this type of entity in the model
    			continue ;
    		
    		// else the entity has not been included in the model

    		LabEntity e_ = (LabEntity) e ;
    		boolean isDOOR = isDoor(e) ;
    		GWObject o = new GWObject(e.id,e.type) ;
    		o.position = e_.getFloorPosition() ;
    		if (isDOOR) o.position = getDoorCenterPoint(e_) ;
			o.extent = e.extent.copy() ;
			modelstate.objects.put(o.id,o) ;
			if (isObstacle(o)) {
				o.properties.put(GameWorldModel.IS_OPEN_NAME,false) ;
				model.blockers.add(o.id) ;
			}
			// if e has color, would be the case for button and color-screen:
			Object color_ = e.properties.get("color") ;
			if (color_ != null) {
				o.properties.put("color", color_.toString()) ;
			}	
    	}
    	
    	// update the tracking of last interacted button
    	for(WorldEntity e : S.changedEntities) {
    		if (isButton(e) && e.hasPreviousState()) {
				lastInteractedButton = e.id ;
				//System.out.println(">>>> detect interact button " + e.id) ;
				break ;
			}
    	}
    	// check which doors change state, to update the object-links:
    	if (lastInteractedButton != null) {
    		for(WorldEntity e : S.changedEntities) {
        		if ((isDoor(e) || isColorScreen(e)) && e.hasPreviousState()) {
        			// e change state!
        			Set<String> affected = model.objectlinks.get(lastInteractedButton) ;
        			if (affected == null) {
        				affected = new HashSet<String>() ;
        				model.objectlinks.put(lastInteractedButton, affected) ;
        			}
        			affected.add(e.id) ;
        		}
        	}
    	}
    	
    }
}
