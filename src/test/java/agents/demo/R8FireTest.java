package agents.demo;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.goalsAndTactics.Sa1Solver;
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

import java.util.Scanner;
import java.util.function.Function;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;
import world.LabEntity;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;
import static eu.iv4xr.framework.Iv4xrEDSL.* ;

/**
 * Another demo of testing with iv4xr. This one uses the R8_fire3 level. The level is of
 * medium-size. It was based on a generated level by FBK, and then pimped by hand. The leven
 * has fire hazards and monsters. Monsters in Lab Recruits cannot be killed, but they are
 * slow and will pause after hitting. Players can heal by touching a goal flag (once).
 */
public class R8FireTest {

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
    static void close() { if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); }

    
    /**
     * In this testing task/scenario we run the scenario:
     * 
     * <p>  door3 ; door1 ; door0 ; door4 ; Finish 
     * 
     * <p> We want to check that Finish is reachable in this scenario. After passing door4
     * the agent health should be between 20..50 and it should have at least 34 points. 
     * After touching Finish the health should be 100 and the point should be at least 524.
     * 
     * <p>You will notice that in order to 'script' the above testing task/scenario,
     * the approach below requires that we also specify how to open each of the doors
     * above, e.g. by specifying that a certain button needs to be interacted first
     * to get a certain door open.
     */
    //@Test
    public void scripted_testscenario1() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("R8_fire3") ;
    	config.light_intensity = 0.3f ;
    	//config.agent_speed = 0.2f ;
    	//config.view_distance = 6 ;
    	var environment = new LabRecruitsEnvironment(config);
    	TestSettings.youCanRepositionWindow() ;
    	
    	// create a test agent
        var testAgent = new LabRecruitsTestAgent("Elono") ;
        
        // define the testing-task:
        var G = SEQ(
        		GoalLib.entityInteracted("b0"),
        		GoalLib.entityStateRefreshed("door3"),
        		assertTrue_(testAgent,"","door3 is open",(BeliefState S) -> S.isOpen("door3")),
        		GoalLib.entityStateRefreshed("door1"),
        		assertTrue_(testAgent,"","door1 is open",(BeliefState S) -> S.isOpen("door1")),
        		GoalLib.entityInteracted("b9"),
        		GoalLib.entityStateRefreshed("door0"),
        		assertTrue_(testAgent,"","door0 is open",(BeliefState S) -> S.isOpen("door0")),
        		GoalLib.entityInteracted("b3"),
        		GoalLib.entityStateRefreshed("door4"),
        		assertTrue_(testAgent,"","door4 is open",(BeliefState S) -> S.isOpen("door4")),
        		assertTrue_(testAgent,"","health and score ok",(BeliefState S) -> 
        			S.worldmodel().health >= 20 && S.worldmodel().health <= 50
        			&& S.worldmodel().score >= 33),
        		GoalLib.atBGF ("Finish",0.2f,true),
        		assertTrue_(testAgent,"","health and score max",(BeliefState S) -> 
    				S.worldmodel().health == 100 && S.worldmodel().score >= 533)
        );

        
        testAgent 
        	. attachState(new BeliefState())
        	. attachEnvironment(environment)
        	. setTestDataCollector(new TestDataCollector())
        	. setTestDataCollector(new TestDataCollector()) 
        	. setGoal(G) ;
        
        try {
	        //environment.startSimulation(); 
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>3500) {
	        		break ;
	        	}
	        }
	        
	        // check that we have passed both tests above:
	        assertTrue(testAgent.success());
	        assertTrue(testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
	        //testAgent.printStatus();
	        //new Scanner(System.in) . nextLine() ;
        }
        finally { environment.close(); }
    }
    
    /**
     * A goal that will just explore the world for some time-budget. The goal itself
     * will never be achieved; we just use it to provide a bogus goal to trigger 
     * exploration work.
]    */
    public static GoalStructure exploreG(int budget) {
    	GoalStructure G = goal("exploring")
    			.toSolve((BeliefState S) -> false)
    			.withTactic(
    				FIRSTof(
    				  TacticLib.explore(),
    				  ABORT()))
    			.lift()
    			.maxbudget(budget)
    			 ;
    	return G  ;
    }
    
    /**
     * In this testing task/scenario we run the scenario:
     * 
     * <p>  door3 ; door10 ; door9 ; [door6] ; Finish 
     * 
     * <p> We want to check that Finish is reachable in this scenario. After passing door4
     * the agent health should be between 20..50 and it should have at least 34 points. 
     * After touching Finish the health should be 100 and the point should be at least 524.
     * 
     * <p>Unlike the previous way of 'scripting' a test scenario, this time we
     * use a simple search algorithm called SA1 to look for a button that can open
     * a given target door. The algorithm works if the setup is not too complicated (in
     * particular multi-connections make things complicated for SA1). Using this we
     * can avoid explicitly scripting how to open some doors.
     */
    @Test
    public void scenario2() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("R8_fire3") ;
    	config.light_intensity = 0.3f ;
    	//config.agent_speed = 0.2f ;
    	//config.view_distance = 6 ;
    	var environment = new LabRecruitsEnvironment(config);
    	TestSettings.youCanRepositionWindow() ;
    	var testAgent = new LabRecruitsTestAgent("Elono")  ;
    	
    	// configuring SA1-solver:		
    	var sa1Solver = new Sa1Solver<Integer>(
    			(S, e) -> {
    				var B = (BeliefState) S ;
    				var f = (LabEntity) e ;
    				var path = B.pathfinder().findPath(B.worldmodel().getFloorPosition(), f.getFloorPosition(), BeliefState.DIST_TO_FACE_THRESHOLD) ;
    				return path != null ;
    			},
    			(S, e) -> Vec3.distSq(S.worldmodel().position, e.position),
    			S -> (e1, e2) -> Vec3.distSq(e1.position,e2.position),
    			eId -> GoalLib.entityInteracted(eId),
    			eId -> GoalLib.entityStateRefreshed(eId),
    			S -> {
    				var B = (BeliefState) S ;
    				var path = B.pathfinder().explore(B.worldmodel().getFloorPosition(), BeliefState.DIST_TO_FACE_THRESHOLD) ;
    			   return path == null;
    			},
    			budget -> exploreG(budget)
    			);

    	
    	// Configure the SA1-solver for tasks to open a door:
    	int explorationBudget = 20;
    	Function <String,GoalStructure> openWithSA1 = 
    		doorToOpen -> 
    			sa1Solver.solver(
    					testAgent, 
    					doorToOpen, 
    					e -> e.type.equals(LabEntity.SWITCH), 
    					S -> ((BeliefState) S) . isOpen(doorToOpen) , 
    					Policy.NEAREST_TO_TARGET, 
    					explorationBudget);
    	
    	// define the testing-task; notice we use the SA1-solver
        var G = SEQ(
        		openWithSA1.apply("door3"),
        		assertTrue_(testAgent,"","door3 is open",(BeliefState S) -> S.isOpen("door3")),
        		GoalLib.entityInteracted("b9"),
        		openWithSA1.apply("door10"),
        		assertTrue_(testAgent,"","door10 is open",(BeliefState S) -> S.isOpen("door10")),
        		openWithSA1.apply("door9"),
        		assertTrue_(testAgent,"","door9 is open",(BeliefState S) -> S.isOpen("door9")),
        		GoalLib.atBGF("gf1",0.2f,true),
        		GoalLib.atBGF("Finish",0.2f,true),
        		assertTrue_(testAgent,"","health and score max",(BeliefState S) -> 
					S.worldmodel().health == 100 && S.worldmodel().score >= 633)
        		
        );

        
        testAgent 
        	. attachState(new BeliefState())
        	. attachEnvironment(environment)
        	. setTestDataCollector(new TestDataCollector())
        	. setTestDataCollector(new TestDataCollector()) 
        	. setGoal(G) ;
        
        try {
        	
	        //environment.startSimulation(); 
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>3500) {
	        		break ;
	        	}
	        }
	        
	        // check that we have passed both tests above:
	        assertTrue(testAgent.success());
	        assertTrue(testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
	        //testAgent.printStatus();
	        //new Scanner(System.in) . nextLine() ;
        }
        finally { environment.close(); }
    }
    
 
}
