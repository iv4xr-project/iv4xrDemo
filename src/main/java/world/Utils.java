package world;

import java.util.LinkedList;
import java.util.List;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Obstacle;

public class Utils {
	
	/**
	 * This will add the following elements in the belief of B to the belief of A (notice the 
	 * direction: add B into A):
	 * 
	 * <ol>
	 * <li> seen navigation vertices
	 * <li> doors, switches, goal-flags, color-screens.
	 * <ol>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void mergeBelief(TestAgent A, TestAgent B) {
		var SA = (BeliefState) A.state() ;
		var SB = (BeliefState) B.state() ;
		// adding B's seen nav-vertices to A:
		int N = SA.pathfinder().vertices.size() ;
		for (int k=0; k<N; k++) {
			if (SB.pathfinder().seenVertices.get(k)) {
				SA.pathfinder().seenVertices.set(k, true) ;
			}
		}
		// add B's relevant entities to A:
        for (WorldEntity e : SB.worldmodel().elements.values()) {
        	String ety = e.type ;
        	if (ety.equals(LabEntity.DOOR) || ety.equals(LabEntity.SWITCH)
        			|| ety.equals(LabEntity.GOAL)
        			|| ety.equals(LabEntity.COLORSCREEN)) {
        		// add e to A:
        		var f = SA.worldmodel().updateEntity(e);
                if (e == f) {
                    // if they are equal, then e induces some state change in the WorldModel,
                	// and if e is a door or a color-screen it may affect its obstacle-state:
                	if (ety.equals(LabEntity.DOOR) || ety.equals(LabEntity.COLORSCREEN)){
                		Obstacle o = SA.findThisObstacleInNavGraph(e.id) ;
        	       		if (o==null) {
        	       			 // e has not been added to the navgraph, so we add it, and retrieve its
        	       			 // Obstacle-wrapper:
        	       			 SA.pathfinder().addObstacle((LabEntity) e);
        	       			 int Z = SA.pathfinder().obstacles.size();
        	       			 o = SA.pathfinder().obstacles.get(Z-1) ;
        	       			 if (! e.type.equals(LabEntity.DOOR)) {
        	       				 // unless it is a door, the obstacle is always blocking:
        	       				 o.isBlocking = true ;
        	       			 }
        	       		}
        	       		else {
        	       			o.obstacle = e ;
        	       		}
        	       		// if it is a door, toggle the blocking state of o to reflect the blocking state of e:
        	       		if (e.type.equals(LabEntity.DOOR)) {
        	       		   o.isBlocking = SA.worldmodel.isBlocking(e) ;
        	       		}
                	}
                }
        	} 
        }
	}

}
