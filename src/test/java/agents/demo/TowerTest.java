package agents.demo;



import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import helperclasses.datastructures.linq.QArrayList;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import static org.junit.jupiter.api.Assertions.* ;

import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;

/**
 * Multi-store example. Explore has an issue on this Tower-level. TODO.
 */
public class TowerTest {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	// TestSettings.USE_SERVER_FOR_TEST = false ;
    	// Uncomment this to make the game's graphic visible:
    	TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); }

    //@Test
    public void debugMesh() {
    	var config = new LabRecruitsConfig("tower") ;
    	config.light_intensity = 0.3f ;
    	var environment = new LabRecruitsEnvironment(config);

        try {
	        var testAgent = new LabRecruitsTestAgent("agent0") 
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

	        var G = SEQ(SUCCESS(),SUCCESS(),SUCCESS(),SUCCESS()) ; 
	        testAgent . setGoal(G) ;
	        
	        var pathfinder = testAgent.getState().pathfinder() ;
	        pathfinder.perfect_memory_pathfinding = true ;
	        testAgent.update();
	        testAgent.update();
	        
	        Vec3 p = new Vec3(9,6,26) ;
	        Vec3 q = new Vec3(9,6,7) ;
	        var path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        q = new Vec3(11,6,7) ;
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        q = new Vec3(15,6,6) ;
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        q = new Vec3(15,9,16) ;
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ; 
	        
        }
        finally { environment.close(); }
    }

    /**
     * Debug that the agent can reach the tower-top. To debug, use a version of the tower
     * level where all blocking doors to the top are open/removed.
     */
    //@Test
    public void debugReachTop() throws InterruptedException {

    	var config = new LabRecruitsConfig("tower") ;
    	config.light_intensity = 0.3f ;
    	config.view_distance = 6 ;
    	var environment = new LabRecruitsEnvironment(config);

        try {
        	TestSettings.youCanRepositionWindow() ;

	        var testAgent = new LabRecruitsTestAgent("agent0") 
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

	        var G = SEQ(
	        		//GoalLib.entityInteracted("b3k0"),
	        		GoalLib.entityInteracted("b4k0")
	        );

	        
	        testAgent 
	        	. setTestDataCollector(new TestDataCollector()) 
	        	. setGoal(G) ;
	        
	        //environment.startSimulation(); 
	        int i = 0 ;
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>1500) {
	        		break ;
	        	}
	        }
	        assertTrue(testAgent.success());
        }
        finally { environment.close(); }
    }
    /**
     * Debug that the agent can reach the tower=top
     */
    //@Test
    public void test1() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("tower") ;
    	config.light_intensity = 0.3f ;
    	config.view_distance = 6 ;
    	var environment = new LabRecruitsEnvironment(config);

        try {
        	if(TestSettings.USE_GRAPHICS) {
        		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
        		new Scanner(System.in) . nextLine() ;
        	}

	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("agent0") 
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

	        // define the testing-task:
	        var G = SEQ(
	        		GoalLib.entityInteracted("b1k0"),
	        		GoalLib.entityStateRefreshed("dNorth1"),
	        		GoalLib.entityInteracted("b1k1"),
	        		//GoalLib.entityStateRefreshed("dEast0"),
	        		GoalLib.entityInCloseRange("dEast0")
	        		//GoalLib.entityInteracted("b0k1")
	        		//GoalLib.entityStateRefreshed("d0k0"),
	        		//GoalLib.entityInteracted("b0k2"),
	        		//GoalLib.entityStateRefreshed("d2k0")
	        );

	        
	        testAgent 
	        	. setTestDataCollector(new TestDataCollector()) 
	        	. setGoal(G) ;
	        


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
	        //assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 4) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        // close
	        //testAgent.printStatus();
        }
        finally { environment.close(); }
    }
    
 
}
