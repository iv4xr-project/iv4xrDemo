package agents.demo;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.CoverterDot;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GWState;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GWZone;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GameWorldModel;
import eu.iv4xr.framework.goalsAndTactics.Sa1Solver;
import eu.iv4xr.framework.goalsAndTactics.Sa2Solver;
import eu.iv4xr.framework.goalsAndTactics.Sa1Solver.Policy;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.Entity.EntityType;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentState;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.Utils;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import static org.junit.jupiter.api.Assertions.* ;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BehaviorModelLearner;
import world.BeliefState;
import world.LabEntity;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;
import static eu.iv4xr.framework.Iv4xrEDSL.* ;

/**
 * Testing a simple ButtonDoors1 level using SA2 algorithm. The level is simple,
 * but it has a thief-catcher construct.
 */
public class SA2Test {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	// TestSettings.USE_SERVER_FOR_TEST = false ;
    	// Uncomment this to make the game's graphic visible:
    	//TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { 
    	if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); 
    }
    
   
    /**
     * A goal that will just explore the world for some time-budget. The goal itself
     * will never be achieved; we just use it to provide a bogus goal to trigger 
     * exploration work.
     * 
     * For now we ignore the given heuristicLocation.
]    */
    GoalStructure exploreG() {
    	GoalStructure G = goal("exploring")
    			.toSolve((BeliefState S) -> false)
    			.withTactic(
    				FIRSTof(
    				  TacticLib.explore(),
    				  ABORT()))
    			.lift()
    			 ;
    	return G  ;
    }
    
    List<String> getConnectedEnablersFromBelief(String shrine, BeliefState S) {
		List<String> openers = new LinkedList<>() ;
		if (S.gwmodel != null) {
			for (var entry : S.gwmodel.objectlinks.entrySet()) {
				if (entry.getValue().contains(shrine)) {
					openers.add(entry.getKey()) ;
					return openers ;
				}
			}
		}
		return openers ;
	}
    
    
    @Test
    public void testWithSA2() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("buttons_doors_1b") ;
    	//var config = new LabRecruitsConfig("samira_8room") ;
    	
    	config.light_intensity = 0.3f ;
    	//config.agent_speed = 0.2f ;
    	//config.view_distance = 6 ;
    	var environment = new LabRecruitsEnvironment(config);
    	TestSettings.youCanRepositionWindow() ;
    	var testAgent = new LabRecruitsTestAgent("agent1")  ;
    	
    	// configuring SA2-solver:	
    	Sa2Solver<Integer> sa2Solver = new Sa2Solver<>(
    			// reachabilityChecker:
    			(S, e) -> {
    				var B = (BeliefState) S ;
    				var f = (LabEntity) e ;
    				if (f.type.equals(LabEntity.DOOR)) {
    					return TacticLib.calculatePathToDoor(B,f,0.9f) != null ;
    				}
    				else {
    					return B.findPathTo(f.getFloorPosition(),true) != null ;
    				}
				},
				(S, e) -> Vec3.distSq(S.worldmodel().position, e.position),
				S -> (e1, e2) -> Vec3.distSq(e1.position, e2.position),
				eId -> GoalLib.entityInteracted(eId),
				eId -> GoalLib.entityStateRefreshed2(eId),
				// explorationExhausted:
				S ->  {
    				var B = (BeliefState) S ;
    				var path = B.pathfinder().explore(B.worldmodel().getFloorPosition(), BeliefState.DIST_TO_FACE_THRESHOLD) ;
    			    return path == null;
				}, 
				dummy -> exploreG()
			);

    	
    	BehaviorModelLearner learner = new BehaviorModelLearner() ;
    	GameWorldModel model = new GameWorldModel(new GWState()) ;
    	
    	
    	// Goal: find a shrine and cleanse it:
    	//String targetDoor = "door3" ;
    	Function<String,GoalStructure> solveWithSA2 = targetDoor ->
    	     sa2Solver.solver(testAgent, 
    			targetDoor, 
    			new Vec3(10,0,4),
    			//new Vec3(10,0,82),
    			e -> e.type.equals("" + LabEntity.DOOR),
    			e -> e.type.equals("" + LabEntity.SWITCH), 
    			e -> e.type.equals("" + LabEntity.DOOR) && (boolean) e.properties.get("isOpen"),
    			(door,S) -> getConnectedEnablersFromBelief(door, (BeliefState) S),
    			// identify critical door:
    			(door,S) -> {
    				learner.recalculateZones((BeliefState) S, model);
    				return learner.getCriticalDoors((BeliefState) S,door) ;
    			},
    			S -> { var S_ = (BeliefState) S;
    			   var e = S.worldmodel.elements.get(targetDoor);
    			   if (e == null)
    				   return false;
    			    return(boolean) e.properties.get("isOpen"); }, 
    			Policy.NEAREST_TO_TARGET);
    	
    	/*
    	GoalStructure G0 = SEQ(GoalLib.entityInteracted("button1"),
    			GoalLib.entityStateRefreshed2("door1"),
    			GoalLib.entityInteracted("button3"),
    			GoalLib.entityStateRefreshed2("door3")
    			) ;
    	 */
    	GoalStructure G0 = SEQ(GoalLib.entityInteracted("button3"),
    	    		 GoalLib.entityStateRefreshed2("door1"),
    	    		 GoalLib.entityInteracted("button7"),
    	    		 GoalLib.entityStateRefreshed2("door4"),
    	    		 GoalLib.entityStateRefreshed2("door6")
    	    		 ) ;
    	     
        var G = solveWithSA2.apply("door3") ;
        //var G = solveWithSA2.apply("door16") ;
        
        
        testAgent 
        	. attachState(new BeliefState())
        	. attachEnvironment(environment)
        	. setGoal(G) ;
                
        testAgent.attachBehaviorModel(model, (S,m) -> {
        	learner.learn((BeliefState) S, (GameWorldModel) m) ;
			return null ;
			}) ;
        
        try {
        	
	        //environment.startSimulation(); 
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	//System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	/*
	        	if (i>100) {
	        		var door1 = testAgent.state().worldmodel().getElement("door1") ;
	        		System.out.println("=== door 1: time=" + door1.timestamp + ", world: " + testAgent.state().worldmodel().timestamp) ;
	        		System.out.println("===         stuttertime=" + door1.lastStutterTimestamp 
	        				+ ", has prev-state: " + door1.hasPreviousState()
	        				+ ", prev time: " + door1.getPreviousState().timestamp) ;
	        		System.out.println("===         isOpen=" + door1.properties.get("isOpen")
	        		     + ", previous: " + door1.getPreviousState().properties.get("isOpen") ) ;
	        		
	        		
	        	}
	        	*/
	        	if (i>2000) {
	        		break ;
	        	}
	        }
	        
	        // check that we have passed both tests above:
	        assertTrue(testAgent.success());
	        //testAgent.printStatus();
	        //new Scanner(System.in) . nextLine() ;
	        
	        learner.recalculateZones(testAgent.getState(), model);
	        model.defaultInitialState.currentAgentLocation = "START" + testAgent.getId() ;
			model.copyDefaultInitialStateToInitialState();
			model.name = "ButtonDoors1_learned2" ;
			//model.save("./tmp/ButtonDoors1_learned2.json");
			//CoverterDot.saveAs("./tmp/ButtonDoors1_learned2.dot",model,true,true) ;

        }
        finally { environment.close(); }
    }
    
 
}
