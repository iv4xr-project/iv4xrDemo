/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents.tactics;

import helperclasses.datastructures.Tuple;
import helperclasses.datastructures.Vec3;
import nl.uu.cs.aplib.agents.MiniMemory;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.GoalsCombinator;
import world.BeliefState;
import world.LabEntity;
import world.LegacyEntity;

import java.util.function.Predicate;

import static nl.uu.cs.aplib.AplibEDSL.*;

import eu.iv4xr.framework.mainConcepts.ObservationEvent.VerdictEvent;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.world.WorldEntity;

import static eu.iv4xr.framework.Iv4xrEDSL.* ;

/**
 * This class provide a set of standard useful sub-goals/sub-goal-structures
 * to be used by test agents to test Lab Recruits.
 */
public class GoalLib {
	
	
	/*
	public static Goal justObserve() {
		Goal g = new Goal("Just making an observation") 
				. toSolve(b -> true) 
				.withTactic(TacticLib.observe()) ;
		return g ;
	}
	*/
	
    /**
     * This method will construct a goal in which the agent will move to a known position.
     * The goal is solved if the agent's floor-position is within 0.4 distance from the
     * specified position.
     * 
     * The agent will fail the goal if it no longer believes the position to be reachable.
     */
    public static Goal positionInCloseRange(Vec3 goalPosition) {
        //define the goal
        Goal goal = new Goal("This position is in-range: " + goalPosition.toString())
        		    . toSolve((BeliefState belief) -> {
                        //check if the agent is close to the goal position
                        return goalPosition.distance(belief.worldmodel.getFloorPosition()) < 0.4;
                    });
        //define the goal structure
        Goal g = goal.withTactic(
        		 FIRSTof(//the tactic used to solve the goal
                   TacticLib.navigateTo(goalPosition),//move to the goal position
                   TacticLib.explore(), //explore if the goal position is unknown
                   ABORT())) ;
        return g;
    }
    

    /**
     * This method will return a goal structure in which the agent will sequentially move along 
     * specified positions. The agent does not literally have to be at each of these position;
     * being in the close-range (0.2 distance) is enough.
     * 
     * The goal-structure will fail if one of its position fails, which happen if the agent no
     * longer believes that the position is reachable.
     */
    public static GoalStructure positionsVisited(Vec3... positions) {
        GoalStructure[] subGoals = new GoalStructure[positions.length];

        for (int i = 0; i < positions.length; i++) {
            subGoals[i] = positionInCloseRange(positions[i]).lift();
        }
        return SEQ(subGoals);
    }
    
    
    /**
     * This method will construct a goal in which the agent will a reachable position nearby
     * the given entity. Positions east/west/south/north in the distance of 1.0 from
     * the entity will be tried. Actually, the used tactic will try to reach a position
     * in the distance of 0.7 from the entity, but this goal will be achieved when the
     * agent is within 1.0 radius.
     * 
     * Becareful when you tweak these distances. There is some inaccuracy in the underlying
     * navmesh reasoning that may come up with a nearby location "inside" a door, which is
     * obviously unreachable. If the threshold above is too small, we might end up with such
     * a case.
     * 
     * The agent will fail the goal if it no longer believes the position to be reachable.
     */
    public static GoalStructure entityInCloseRange(String entityId) {
    	//define the goal
        Goal goal = new Goal("This entity is closeby: " + entityId)
        		    . toSolve((BeliefState belief) -> {
                        //check if the agent is close to the goal position
        		    	var e = belief.worldmodel.getElement(entityId) ;
        		    	System.out.println("entityInCloseRange e" + e.getFloorPosition());
        		    	System.out.println("entityInCloseRange return value: " + (belief.worldmodel.getFloorPosition().distance(e.getFloorPosition()) <= 1));
        		    	if (e == null) return false ;
                        return belief.worldmodel.getFloorPosition().distance(e.getFloorPosition()) <= 1 ;
                    });
        //define the goal structure
        return goal.withTactic(
        		 FIRSTof( //the tactic used to solve the goal
                   TacticLib.navigateToCloseByPosition(entityId),//move to the goal position
                   TacticLib.explore(), //explore if the goal position is unknown
                   ABORT())) 
        	  . lift();
    }


    /**
     * Construct a goal structure that will make an agent to move towards the given entity,
     * until it is in the interaction-distance with the entity; and then interacts with it.
     * Currently the used tactic is not smart enough to handle a moving entity, in particular 
     * if it moves while the agent is entering the interaction distance.
     * 
     * The goal fails if the agent no longer believes that the entity is reachable, or when it fails to 
     * interact with it.
     *
     * @param entityId The entity to walk to and interact with
     * @return A goal structure
     * @Incomplete: this goal should check if the object has a given desired state, and perhaps include a position check in the goal predicate itself
     */
    public static GoalStructure entityInteracted(String entityId) {
        //the first goal is to navigate to the entity:
        var goal1 = 
        	  goal(String.format("This entity is in interaction distance: [%s]", entityId))
        	  . toSolve((BeliefState belief) ->  belief.canInteract(entityId))
        	  . withTactic(
                    FIRSTof( //the tactic used to solve the goal
                    TacticLib.navigateTo(entityId), //try to move to the entity
                    TacticLib.explore(), //find the entity
                    ABORT())) 
              . lift();

        // then, the 2nd goal is to interact with the object:
        var goal2 = 
        	  goal(String.format("This entity is interacted: [%s]", entityId))
        	  . toSolve((BeliefState belief) -> {System.out.print("interacted" + entityId); return true;}) 
              . withTactic(
        		   FIRSTof( //the tactic used to solve the goal
                   TacticLib.interact(entityId),// interact with the entity
                   ABORT())) // observe the objects
              . lift();

        return SEQ(goal1, goal2);
    }
    

	/**
	 * This goal will make agent to navigate towards the given entity, and make sure that
	 * the agent has the latest observation of the entity. Getting the entity within
	 * sight is enough to complete this goal.
	 * 
	 * This goal fails if the agent no longer believes that the entity is reachable.
	 */
    public static GoalStructure entityStateRefreshed(String id){
        return goal("The belief on this entity is refreshed: " + id)
                .toSolve((BeliefState b) -> {
                	System.out.println("entityStateRefreshed id" + id); 
                              System.out.println(">> entity timest:" + b.worldmodel.getElement(id).timestamp) ;
                              var compare = b.evaluateEntity(id, e -> b.age(e) == 0L);
                              System.out.println(">> entity age:" + compare ) ;
                              System.out.println(">> world timest" + b.worldmodel.timestamp);
                              return compare ;
                              
                              
                             // return b.evaluateEntity(id, e -> b.age(e) == 0L);
                              })
                .withTactic(FIRSTof(
                        TacticLib.navigateToClosestReachableNode(id),
                        TacticLib.explore(),
                        ABORT()))
                .lift() ;
    }

    /**
     * This goal will make agent to navigate to the given entity, and make sure that the state of the
     * entity satisfies the given predicate.
     * 
     * The goal fail if the agent no longer believes that the entity is reachable, or when the
     * predicate is not satisfied when it is observed.
   
     * @param id: entityId
     * @return Goal
     */
    public static GoalStructure entityInspected(String id, Predicate<WorldEntity> predicate){
        return SEQ(
            entityStateRefreshed(id),
            goal("This entity is inspected: " + id)
            .toSolve((BeliefState b) -> b.evaluateEntity(id, predicate))
            .withTactic(
                SEQ(
                   TacticLib.observe(),
                   ABORT()))
            .lift()
        );
    }
    
    /**
     * Create a test-goal to check the state of an in-game entity, whether it satisfies the given predicate.
     * Internally, this goal will first spend one tick to get a fresh observation, then at the next tick it
     * will do the checking.
     * 
     * @param agent  The test agent to do the checking.
     * @param id     The id of the in-game entity to check.
     * @param info   Some string describing the check.
     * @param predicate  The predicate that is expected to hold on the entity.
     * @return
     */
    public static GoalStructure entityInvariantChecked(TestAgent agent, String id, String info, Predicate<WorldEntity> predicate){
        return SEQ(
            entityStateRefreshed(id),
            testgoal("Invariant check " + id, agent)
            . toSolve((BeliefState b) -> true) // nothing to solve
            . invariant(agent,                 // something to check :)
            		(BeliefState b) -> {
            			if (b.evaluateEntity(id, predicate))
            			   return new VerdictEvent("Object-check " + id, info, true) ;
            			else 
            			   return new VerdictEvent("Object-check " + id, info, false) ;
            			
            		}
            		)
            .withTactic(TacticLib.observe())
            .lift()
        );
    }
    
    /**
     * Create a test-goal to check the state of the game, whether it satisfies the given predicate.
     * Internally, this goal will first spend one tick to get a fresh observation, then at the next tick it
     * will do the checking.
     */ 
    public static GoalStructure invariantChecked(TestAgent agent, String info, Predicate<BeliefState> predicate){
        return SEQ(
            testgoal("Evaluate " + info, agent)
            .toSolve((BeliefState b) -> true) // nothing to solve
            .invariant(agent,                 // something to check :)
            		(BeliefState b) -> {
            			if (predicate.test(b))
            			   return new VerdictEvent("Inv-check", info, true) ;
            			else 
            			   return new VerdictEvent("Inv-check" , info, false) ;
            		    }
            		)
            .withTactic(SEQ(
                    TacticLib.observe(),
                    ABORT())).lift()
        );
    }

    /**
     * This goal structure will cause the agent to share its memory once with all connected agents in the broadcast
     *
     * @param id: The id of the sending agent
     * @return A goal structure which will be concluded when the agent shared its memory once
     */
    public static GoalStructure memorySent(String id){
        return goal("Map is shared").toSolve((BeliefState belief) -> true).withTactic(
                TacticLib.shareMemory(id)
        ).lift();
    }

    /**
     * This goal structure will cause the agent to send a ping to the target agent
     * @param idFrom: The id of the sending agent
     * @param idTo: The id of the receiving agent
     * @return A goal structure which will be concluded when the agent send a ping to the target agent
     */
    public static Goal pingSent(String idFrom, String idTo){
        return new Goal("Send ping").toSolve((BeliefState belief) -> true).withTactic(
                TacticLib.sendPing(idFrom, idTo)
        );
    }
    
    /**
     * Use this goal to approach a door which according to the current belief is closed,
     * but the agent has a reason to suspect that it might be open now (e.g. because
     * it just pushed on a button that it thinks might open the door).
     * The agent cannot literally navigate to the door, because this would be prohibited
     * by its current navigation graph (which will say that the door is unreachable, and
     * therefore cannot provide a path to it).
     * Instead, this goal will try to find a neighboring navigation node N that is reachable.
     * If such N can be found, the agent will first navigate to N, hoping that this will
     * update its observation on the door (which it suspects to be open); and then it will
     * proceed to navigate to the door.
     * 
     * This goal fails if either no such N can be found, or if the agent cannot find witness
     * that the door is open (e.g. if it is indeed actuallu still closed).
     */
    
    
    public static GoalStructure navigate_toNearestNode_toDoor(String doorId) {
    	var memo = new MiniMemory("") ;
    	memo.memorize(new Vec3(1,0,1)) ; // dummy location 
    	Goal neighboringNodeIsFound = goal("A reachable node close to the door " + doorId + " is found.")
    	     . toSolve((BeliefState belief) -> {System.out.print("navigate_toNearestNode_toDoor: true always \n"); return true;}) 
    		 . withTactic(
    			 action("Finding a reachable neighbor to door " + doorId)
    			   . do1( (BeliefState belief) -> {
    				   var door = (LabEntity) belief.worldmodel.getElement(doorId) ;
    				   if (door == null) ABORT() ;
    				   var p = door.getFloorPosition() ;
    				   var containingNode = belief.mentalMap.pathFinder.graph.vecToNode(p) ;
    				   var knownNeighbors = belief.mentalMap.pathFinder.graph.getKnownNeighbours(
    						     containingNode, 
    						     belief.mentalMap.getKnownVertices(), 
    						     belief.blockedNodes) ;
    				   Double minDist = Double.MAX_VALUE ;
    				   Vec3 nearestNeighbor = null ;
    				   Vec3[] pathToNearestNeighbor = null ;
    				   for (var k : knownNeighbors) {
    					   Vec3 q = belief.mentalMap.pathFinder.graph.toVec3(k) ;
    					   var path = belief.canReach(q) ;
    					   if (path != null && path.length>0) {
    						   var dist = p.distance(q) ;
    						   if (dist < minDist)  {
    							   minDist = dist ;
    							   nearestNeighbor = q ;
    							   pathToNearestNeighbor = path ;
    						   } 
    					   } 
    				   }	
    				   if (nearestNeighbor == null) ABORT() ;
    				   Vec3 r = (Vec3) memo.memorized.get(0) ;
    				   r.x = nearestNeighbor.x ;
    				   r.y = nearestNeighbor.y ;
    				   r.z = nearestNeighbor.z ;
    				   //memo.memorize(pathToNearestNeighbor);
    				   
    				   return belief ;
    			   })
    			   .lift() 	
    					
    		   ) ;
    	
    	Goal neighboringNodeIsReached = goal("A reachable node close to the door " + doorId + " is reached.")
       	     . toSolve((BeliefState belief) -> {
       	    	   var q =(Vec3)  memo.memorized.get(0) ; 
       	    	   return belief.worldmodel.getFloorPosition().distance(q) <= 0.25 ;
       	       }) 
       		 . withTactic(TacticLib.dynamicNavigateTo(
       			   "Navigating to a reachable node close to the door " + doorId,
       			   (Vec3) memo.memorized.get(0))) ;
    			
    	var goal = SEQ(neighboringNodeIsFound.lift(),
    			       neighboringNodeIsReached.lift()) ;
    	
    	return goal ;
    }
    

    
    public static GoalStructure findingNewButtonAndInteracte(TestAgent agent){
    	return goal("find new inactive button and interact with it")
    			.toSolve( 				
    					(Tuple<String,BeliefState> s) -> { 				
//    						System.out.print("there is #####" + s.object1);
    						if(s.object1 == null) {
    							System.out.print("there is no button to find");
    							return true;
    							}
    						System.out.print("find new inactive button and interact with it");
    						return false;
    						}
    					)
    				.withTactic(SEQ(
                        TacticLib.interactToNextButton(agent),
                        ABORT()
                        )
    				)
    			.lift();
    }
 
    public static GoalStructure checkDoorState(String id, Predicate<WorldEntity> predicate) {
    	Goal goal =  goal("Ckecking door state")
        		.toSolve(
        				(BeliefState belief) -> { 
        					System.out.print("Ckecking door state : " + belief.isOpen(id) + "id of the door : " +id);
        	       	    	   if(belief.isOpen(id)) {
        	       	    		System.out.print(" it is open \n");
        	       	    		  return true; 
        	       	    	   }
        	       	    	   return false;
        	       	       }
        				)
        		.withTactic(
        				SEQ(
                        TacticLib.observe(),
                        ABORT()))
        	
        		;  
    	
    	return goal.lift();
    }
    
 public static GoalStructure checkButtonState(String id) {
    	
    	//move to the object
    	Goal goal1 = goal(String.format("This entity is in interaction distance: [%s]", id))
        		. toSolve((BeliefState belief) -> {return belief.canInteract(id);});
    	
    	Goal goal2 =  goal("check button state")
    			.toSolve((BeliefState b)-> {
    					if(b.isOn(id)) {
    						return true;
    						}
    					return false;}
    					)
    			;
    			
    			 //Set the tactics with which the goals will be solved
    	        GoalStructure g1 = goal1.withTactic(
    	        		FIRSTof( //the tactic used to solve the goal
    	                   TacticLib.navigateTo(id), //try to move to the entity
    	                   TacticLib.explore(), //find the entity
    	                   ABORT()
    	                   )) 
    	                .lift();
    	        GoalStructure g2 = goal2.withTactic(SEQ(
                        TacticLib.observe(),
                        ABORT())).lift();
    return SEQ(g1,g2);
    }
 
 public static GoalStructure success() {
	 return goal(String.format("success"))
     		. toSolve((BeliefState belief) -> { return true;})
     		.withTactic(TacticLib.observe())
     		.lift();
	
 }
 
 public static <State> GoalStructure success__() {
	 return goal(String.format("success"))
     		. toSolve((State belief) -> { return true;})
     		. withTactic(action("").do1((State state) -> state).lift()) 
     		.lift();
	
 }
 
 public static GoalStructure activeButtonPredicateed() {
	 return goal(String.format("active Button Predicateed"))
			 .toSolve( 				
 					(Tuple<String,BeliefState> s) -> { 		
 						if(s.object1.contains("equal")) {
 							return true;
 						}
     			return false;
     			})
     		.withTactic(SEQ(
     				action("check number of inactive button: solved means there is something wrong").do1((BeliefState belief)-> {
     						var knownsButtons = belief.knownButtons(); 
     						int countActiveButton = 0;
     						for(int j=0; j<knownsButtons.size(); j++) {
                   			 if(knownsButtons.get(j).getBooleanProperty("isOn")) {
                   				 countActiveButton = countActiveButton +1;
                   			 }
                   			if(countActiveButton == knownsButtons.size()) {return new Tuple("equal",belief) ;} 
                   		 }
							return new Tuple("notEqual",belief) ;
     						}
     						).lift(),
     				ABORT()
     				))
     		
     		.lift();
	
 }
 
 
 public static <State>GoalStructure lift____(Predicate<State> p) {
	 return testgoal("Evaluate goal predicate ")
	            .toSolve((Boolean b) ->  b ) 
	           
	            .withTactic(
	            		SEQ(
	            		action("check number of inactive button: solved means there is something wrong").do1((State belief)-> {
     						return p.test(belief) ;
     						}
     						).lift()
	            		,
	                    ABORT()))
	            .lift() ;
	
 }
 
 //try to define new repeat
 public static <State>GoalStructure NEWREPEAT(Predicate<State> p, GoalStructure subgoal) {
	 GoalStructure[] subgoals = new GoalStructure[2];
	 subgoals[0] =  lift____(p);
	 subgoals[1] = success__();
		return new GoalStructure(GoalsCombinator.REPEAT, 
						new GoalStructure (GoalsCombinator.FIRSTOF,
						new GoalStructure (GoalsCombinator.SEQ , subgoals)
						,subgoal
						)
						
				) ;
	}

}
