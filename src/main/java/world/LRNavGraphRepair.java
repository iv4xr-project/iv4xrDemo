package world;

import java.util.*;
import java.util.stream.Collectors;

import eu.iv4xr.framework.spatial.Vec3;
import eu.iv4xr.framework.spatial.meshes.Edge;


/**
 * Contain some functions to check if Lab Recruit's nav-graph is broken, 
 * and to fix it.
 */
public class LRNavGraphRepair {
	
	
	/**
	 * Count the number of navigation nodes, in the same floor as the agent that owns the state,
	 * which is unreachable from the agent location. 
	 */
	static public int checkUnreachableNodes(BeliefState state) {
		Vec3 pos0 = state.worldmodel().getFloorPosition() ;
		var pf = state.pathfinder() ;
		
		// ===
		// force some change on the pathfinder state; we will restore it later:
		var originalValueOfPerfectMemoryPathFindingFlag = pf.perfect_memory_pathfinding ;
		pf.perfect_memory_pathfinding = true ;
		List<Boolean> originalObstaclesState = pf.obstacles.stream()
				.map(o -> o.isBlocking)
				.collect(Collectors.toList());
		// open all obstacles:
		pf.obstacles.stream().forEach(o -> { o.isBlocking = false ; } ); 
		// ====
		
		int i0 = pf.getNearestUnblockedVertex(pos0, 0.2f) ;
		System.out.println("# pos0:" + pos0) ;
		int N = pf.vertices.size() ;
		int unreachableCount = 0 ;
		int onFloor = 0 ;
		for (int k=0; k<N; k++) {
			//if (k>100) break ;
			Vec3 v = pf.vertices.get(k) ;
			//System.out.println("# " + v) ;
			if (Math.abs(v.y - pos0.y) > 0.1) continue ;
			onFloor++ ;
			//var path = pf.findPath(pos0, v, 0.1f)  ;
			var path = pf.findPath(i0,k)  ;
			if (path == null) {
				//System.out.println("xxx " + v) ;
				unreachableCount++ ;
			}
		}
		System.out.println(">>> #nodes=" + N) ;
		System.out.println(">>> #obstacles=" + pf.obstacles.size()) ;
		System.out.println(">>> #nodes at elevation " + pos0.y + ":" + onFloor) ;
		System.out.println(">>> #unreachable=" + unreachableCount) ;
	
		// == restoring the path-finder state:
		pf.perfect_memory_pathfinding = originalValueOfPerfectMemoryPathFindingFlag ;
		for (int k=0; k < pf.obstacles.size(); k++) {
			pf.obstacles.get(k).isBlocking = originalObstaclesState.get(k) ;
		}
		// ==
		return unreachableCount ;
	}
	
	
	static public int repairMissingEdges(BeliefState state) {
		System.out.println(">>> trying to repair missing edges ...") ;
		Vec3 pos0 = state.worldmodel().getFloorPosition() ;
		var pf = state.pathfinder() ;
		
		// ===
		// force some change on the pathfinder state; we will restore it later:
		var originalValueOfPerfectMemoryPathFindingFlag = pf.perfect_memory_pathfinding ;
		pf.perfect_memory_pathfinding = true ;

		List<Boolean> originalObstaclesState = pf.obstacles.stream()
						.map(o -> o.isBlocking)
						.collect(Collectors.toList());
		// open all obstacles:
		pf.obstacles.stream().forEach(o -> { o.isBlocking = false ; } ); 
		// ====
		
		int N = pf.vertices.size() ;
		int numberOfRepairs = 0 ;
		for (int k=0; k<N; k++) {
			Vec3 v1 = pf.vertices.get(k) ;
			if (Math.abs(v1.y - pos0.y) > 0.1) continue ;
			for (int j=0; j<N; j++) {
				if (j==k) continue ;
				Vec3 v2 = pf.vertices.get(j) ;
				if (Math.abs(v2.y - pos0.y) > 0.1) continue ;
				if (Vec3.distSq(v1, v2) > 0.01) continue ;
				// so we have v1 and v2 that are close to each other
				var path = pf.findPath(k,j)  ;
				if (path == null) {
					// possibly broken, fix it:
					numberOfRepairs++ ;
					pf.edges.put(new Edge(k,j)); 
				}	
			}
		}
		System.out.println(">>> #repaired pairs:" + numberOfRepairs) ;
		
		// == restoring the path-finder state:
		pf.perfect_memory_pathfinding = originalValueOfPerfectMemoryPathFindingFlag ;
		for (int k=0; k < pf.obstacles.size(); k++) {
			pf.obstacles.get(k).isBlocking = originalObstaclesState.get(k) ;
		}
		// ==
		return numberOfRepairs ;
	}
	
	
	/**
	 * Get all navigation nodes that are members of exactly two triangles. These are
	 * "corners".
	 */
	static public List<Integer> getCorners(BeliefState state) {
		Vec3 pos0 = state.worldmodel().getFloorPosition() ;
		var pf = state.pathfinder() ;
		int i0 = pf.getNearestUnblockedVertex(pos0, 0.2f) ;
		System.out.println("# pos0:" + pos0) ;
		int N = pf.vertices.size() ;
		List<Integer> corners = new LinkedList<>() ;
		int onFloor = 0 ;
		for (int k=0; k<N; k++) {
			//if (k>100) break ;
			Vec3 v = pf.vertices.get(k) ;
			//System.out.println("# " + v) ;
			if (Math.abs(v.y - pos0.y) > 0.1) continue ;
			onFloor++ ;	
			int C = 0 ;
			for (var face : pf.faces) {
				for (int j=0; j < face.vertices.length  ; j++) {
					if (k == face.vertices[j]) {
						C++ ;
					}
				}
			}
			if (C == 2) {
				// then k is a corner!
				corners.add(k) ;
			}
		}
		System.out.println(">>> #nodes at y=" + pos0.y + " :" + onFloor) ;
		System.out.println(">>> #corners =" + corners.size()) ;
		return corners ;
	}
	
}
