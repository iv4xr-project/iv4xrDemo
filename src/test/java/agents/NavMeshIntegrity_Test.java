/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents;



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
import world.LabEntity;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;


public class NavMeshIntegrity_Test {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	// TestSettings.USE_SERVER_FOR_TEST = false ;
    	// Uncomment this to make the game's graphic visible:
    	// TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); }

    void instrument(Environment env) {
    	env.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();
    }

    /**
     * The nav-mesh sent by Unity can be broken, where a single node (a corner of a triable in the mesh)
     * could be split into a pair of nodes, which are unreachable from each other. We have implemented
     * a fix for this by force-merging such twins. This test run on a level where it is know that Unity
     * produces such a broken nav-mesh. We test if our fix works.
     */
    @Test
    public void reachEndLevel_Test() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("samira_8room") ;
    	var environment = new LabRecruitsEnvironment(config);
        
        try {
        	if(TestSettings.USE_GRAPHICS) {
        		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
        		new Scanner(System.in) . nextLine() ;
        	}

	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("agent1") // matches the ID in the CSV file
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

	        // define the testing-task:
	        var testingTask = SEQ(
	            GoalLib.entityInteracted("button3"), GoalLib.entityStateRefreshed("door1"),
	            GoalLib.entityInteracted("button7"), GoalLib.entityStateRefreshed("door4"),
	            GoalLib.entityInteracted("button9"), GoalLib.entityStateRefreshed("door5"),
	            GoalLib.entityInteracted("button16"), GoalLib.entityStateRefreshed("door7"),
	            GoalLib.entityInteracted("button17"), GoalLib.entityStateRefreshed("door9"),
	            GoalLib.entityInteracted("button24"), GoalLib.entityStateRefreshed("door11"),
	            GoalLib.entityInteracted("button27"), GoalLib.entityStateRefreshed("door14"),
	            GoalLib.entityInteracted("button32"), GoalLib.entityStateRefreshed("door16"),
	            GoalLib.entityInCloseRange("treasure") 
	        );
	        
	        testAgent.setGoal(testingTask) ;

	        environment.startSimulation(); // this will press the "Play" button in the game for you
	        //goal not achieved yet
	        assertFalse(testAgent.success());
	        
	        // do one update and explicitly check reachability between these two points (which was
	        // unreachable before fixed):
	        testAgent.update() ;
	        testAgent.getState().pathfinder.perfect_memory_pathfinding = true ;
	        assertTrue(testAgent.getState().pathfinder.findPath(new Vec3(3,0,70), new Vec3(3,0,77.5f), 0.1f) != null) ;
            testAgent.getState().pathfinder.perfect_memory_pathfinding = false ;

	        int i = 0 ;
	        // keep updating the agent
	        while (testingTask.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.getState().id + " @" + testAgent.getState().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>1000) {
	        		break ;
	        	}
	        }
	        testingTask.printGoalStructureStatus();

	        assertTrue(testAgent.success());
	        BeliefState belief = testAgent.getState() ;
	        LabEntity treasure = (LabEntity) belief.worldmodel.getElement("treasure") ;
	        assertTrue(Vec3.dist(belief.worldmodel.getFloorPosition(), treasure.getFloorPosition()) <= 1.2)  ;
	        // close
	        testAgent.printStatus();
        }
        finally { environment.close(); }
    }
}
