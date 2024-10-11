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

/**
 * A play-test of the level Lab-1, that it can be completed.
 */
public class Lab1_Test {

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

		var config = new LabRecruitsConfig("lab1");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("agent0")
				.attachState(new BeliefState())
				.attachEnvironment(environment);
		
		var testingTask_1 = SEQ(
				entityInteracted("b_hall_1"),
				entityStateRefreshed2("d_store_e"),
				entityInvariantChecked(testAgent,
	            		"d_store_e",
	            		"d_store_e should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;			
		
		var testingTask_2 = SEQ(
				entityInteracted("b_store"),
				entityStateRefreshed2("d_store_n"),
				entityInvariantChecked(testAgent,
	            		"d_store_n",
	            		"d_store_n should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				entityStateRefreshed2("d_sidehall"),
				entityInvariantChecked(testAgent,
	            		"d_sidehall",
	            		"d_sidehall should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_3 = SEQ(
				entityInteracted("b_secret_1"),
				entityInteracted("b_side"),
				entityStateRefreshed2("d_sidehall"),
				entityInvariantChecked(testAgent,
	            		"d_sidehall",
	            		"d_sidehall should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				entityStateRefreshed2("d_lab_w"),
				entityInvariantChecked(testAgent,
	            		"d_lab_w",
	            		"d_lab_w should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				entityStateRefreshed2("d_bcroom"),
				entityInvariantChecked(testAgent,
	            		"d_bcroom",
	            		"d_bcroom should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_4 = SEQ(
				entityInteracted("b_secret_2"),
				entityStateRefreshed2("d_closet"),
				entityInvariantChecked(testAgent,
	            		"d_closet",
	            		"d_closet should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;	
		
		var testingTask_5 = SEQ(
				entityInteracted("b_closet"),
				entityStateRefreshed2("d_theater_s"),
				entityInvariantChecked(testAgent,
	            		"d_theater_s",
	            		"d_theater_s should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				entityStateRefreshed2("d_theater_e"),
				entityInvariantChecked(testAgent,
	            		"d_theater_e",
	            		"d_theater_e should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_6 = SEQ(
				entityInteracted("b_lab_e"),
				entityStateRefreshed2("d_tofinish"),
				entityInvariantChecked(testAgent,
	            		"d_tofinish",
	            		"d_tofinish should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_7 = SEQ(
				entityInteracted("b_finish"),
				entityStateRefreshed2("d_finish"),
				entityInvariantChecked(testAgent,
	            		"d_finish",
	            		"d_finish should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				invariantChecked(testAgent,
						"score is less than 100",
						(BeliefState S) -> S.worldmodel().score <= 100
						)
				) ;
		
		var testingTask = SEQ(
				testingTask_1, 
				testingTask_2,
				testingTask_3,
				testingTask_4,
				testingTask_5,
				testingTask_6,
				testingTask_7,
				atBGF( "finish",0.7f, true, false)
				) ;
				
		
		testAgent
			. setGoal(testingTask)
			. setTestDataCollector(new TestDataCollector()) ;
		
		Utils.runGoal(testAgent, testingTask, 800, 50);
		assertTrue(testingTask.getStatus().success()) ;
		
		assertTrue(testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
		
		BeliefState state = testAgent.state() ;
		assertTrue(state.worldmodel().score > 100) ;
		
		environment.close();
    }
        
}
