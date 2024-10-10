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


public class R8Fire_3_Test {

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

		var config = new LabRecruitsConfig("R8_fire3");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("Elono")
				.attachState(new BeliefState())
				.attachEnvironment(environment);
		
		var testingTask_1 = SEQ(
				entityInteracted("b0"),
				entityStateRefreshed2("door3"),
				entityInvariantChecked(testAgent,
	            		"door3",
	            		"door3 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				entityStateRefreshed2("door1"),
				entityInvariantChecked(testAgent,
	            		"door1",
	            		"door1 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;			
		
		var testingTask_2 = SEQ(
				entityInteracted("b9"),
				entityStateRefreshed2("door0"),
				entityInvariantChecked(testAgent,
	            		"door0",
	            		"door0 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_3 = SEQ(
				entityInteracted("b3"),
				entityStateRefreshed2("door4"),
				entityInvariantChecked(testAgent,
	            		"door4",
	            		"door4 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				invariantChecked(testAgent,
						"score is less than 100",
						(BeliefState S) ->
							20 <= S.worldmodel().health
							&& S.worldmodel().health < 50
							&& S.worldmodel().score > 32
						)
				) ;
		
		
		var testingTask = SEQ(
				testingTask_1, 
				testingTask_2,
				testingTask_3,
				atBGF( "Finish",0.7f, true, false)
				) ;
				
		
		testAgent
			. setGoal(testingTask)
			. setTestDataCollector(new TestDataCollector()) ;
		
		Utils.runGoal(testAgent, testingTask, 1800, 80);
		assertTrue(testingTask.getStatus().success()) ;
		
		assertTrue(testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
		
		BeliefState state = testAgent.state() ;
		assertTrue(state.worldmodel().score > 532 && state.worldmodel().health == 100) ;
		
		environment.close();
    }
        
}
