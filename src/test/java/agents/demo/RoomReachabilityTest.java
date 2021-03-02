/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

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

import java.io.IOException;
import java.util.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;
import static helperclasses.GraphPlotter.* ;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;

/**
 * A simple test to demonstrate using iv4xr agents to test the Lab Recruits game.
 * The testing task is to verify that the closet in the east is reachable from
 * the player initial position, which it is if the door guarding it can be opened.
 * This in turn requires a series of switches and other doors to be opened.
 */
public class RoomReachabilityTest {

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
     * A test to verify that the east closet is reachable.
     * @throws IOException 
     */
    @Test
    public void closetReachableTest() throws InterruptedException, IOException {

    	var buttonToTest = "button1" ;
    	var doorToTest = "door1" ;

        // Create an environment
    	var config = new LabRecruitsConfig("buttons_doors_1") ;
    	config.light_intensity = 0.3f ;
    	var environment = new LabRecruitsEnvironment(config);
        if(USE_INSTRUMENT) instrument(environment) ;

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
	            GoalLib.entityInteracted("button1"),
                GoalLib.entityStateRefreshed("door1"),
	        	GoalLib.entityInvariantChecked(testAgent,
	            		"door1",
	            		"door1 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),

	        	GoalLib.entityInteracted("button3"),
	        	GoalLib.entityStateRefreshed("door2"),
	        	GoalLib.entityInvariantChecked(testAgent,
	            		"door2",
	            		"door2 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
	        	GoalLib.entityInteracted("button4"),
	        	//GoalLib.entityIsInRange("button3").lift(),
	        	//GoalLib.entityIsInRange("door1").lift(),
	        	GoalLib.entityStateRefreshed("door1"),
	        	GoalLib.entityInvariantChecked(testAgent,
	            		"door1",
	            		"door1 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
	        	//GoalLib.entityIsInRange("button1").lift(),
	        	GoalLib.entityStateRefreshed("door3"),
	        	GoalLib.entityInvariantChecked(testAgent,
	            		"door3",
	            		"door3 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
	        	GoalLib.entityInCloseRange("door3")
	        );
	        // attaching the goal and testdata-collector
	        var dataCollector = new TestDataCollector();
	        testAgent . setTestDataCollector(dataCollector) . setGoal(testingTask) ;


	        environment.startSimulation(); // this will press the "Play" button in the game for you
	        //goal not achieved yet
	        assertFalse(testAgent.success());

	        Map<Vec3,Float> traceData = new HashMap<>() ;
	        
	        int i = 0 ;
	        // keep updating the agent
	        while (testingTask.getStatus().inProgress()) {
	        	Vec3 position = testAgent.getState().worldmodel.position ;
	        	System.out.println("*** " + i + ", " + testAgent.getState().id + " @" + position) ;
	        	if (position != null) {
	        		Vec3 p_ = position.copy() ;
	        	    p_.z = 8- p_.z ;
	        		float score = (float) testAgent.getState().worldmodel.score / 40 ;
		        	traceData.put(p_,score) ;
	        	}
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>200) {
	        		break ;
	        	}
	        }
	        testingTask.printGoalStructureStatus();

	        // check that we have passed both tests above:
	        assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 4) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        // close
	        testAgent.printStatus();
	        
	        mkScatterGraph(traceData,"roomReachabilityTest.png",120,80,10f,4) ;
        }
        finally { environment.close(); }
    }
}
