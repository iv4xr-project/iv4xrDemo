package agents.demo.cleaned;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;

import static org.junit.jupiter.api.Assertions.* ;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;
import static agents.tactics.GoalLib.* ;


public class Tower_Test {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	TestSettings.USE_GRAPHICS = true ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(System.getProperty("user.dir")) ;
    }

    @AfterAll
    static void close() { 
    	if(labRecruitsTestServer!=null) 
    		labRecruitsTestServer.close(); 
    }

       
    @Test
    public void playtest() throws InterruptedException {

		var config = new LabRecruitsConfig("tower");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("agent0")
				.attachState(new BeliefState())
				.attachEnvironment(environment);
		
		var testingTask_1 = SEQ(
				entityInteracted("b1k0"),
				entityStateRefreshed2("dNorth1"),
				entityInvariantChecked(testAgent,
	            		"dNorth1",
	            		"dNorth1 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;			
		
		var testingTask_2 = SEQ(
				entityInteracted("b1k1"),
				entityStateRefreshed2("dEast0"),
				entityInvariantChecked(testAgent,
	            		"dEast0",
	            		"dEast0 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_3 = SEQ(
				entityInteracted("b0k1"),
				entityStateRefreshed2("d0k0"),
				entityInvariantChecked(testAgent,
	            		"d0k0",
	            		"d0k0 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_4 = SEQ(
				entityInteracted("b0k2"),
				entityStateRefreshed2("dEast0"),
				entityInvariantChecked(testAgent,
	            		"dEast0",
	            		"dEast0 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_5 = SEQ(
				entityStateRefreshed2("d2k0"),
				entityInteracted("b3k0"),
				entityStateRefreshed2("dWest0"),
				entityInvariantChecked(testAgent,
	            		"dWest0",
	            		"dWest0 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask = SEQ(
				testingTask_1, 
				testingTask_2,
				testingTask_3,
				testingTask_4,
				testingTask_5,
				atBGF( "Finish",0.7f, true, false),
				Utils.waitNturns(2)
				) ;
				
		
		testAgent
			. setGoal(testingTask)
			. setTestDataCollector(new TestDataCollector()) ;
		
		Utils.runGoal(testAgent, testingTask, 1800, 50);
		assertTrue(testingTask.getStatus().success()) ;
		
		assertTrue(testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
		
		BeliefState state = testAgent.state() ;
		assertTrue(state.worldmodel().score > 530 && state.worldmodel().health == 100) ;
		
		environment.close();
    }
        
}
