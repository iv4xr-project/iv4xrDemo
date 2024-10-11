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


public class Zenopus_Test {

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

		var config = new LabRecruitsConfig("ruinzenopus_LR_2");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("recruit")
				.attachState(new BeliefState())
				.attachEnvironment(environment);
		
		var testingTask_1 = SEQ(
				entityInteracted("b0"),
				entityStateRefreshed2("d0"),
				entityInvariantChecked(testAgent,
	            		"d0",
	            		"d0 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				entityInteracted("b1"),
				entityStateRefreshed2("d1"),
				entityInvariantChecked(testAgent,
	            		"d1",
	            		"d1 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				entityInteracted("b3"),
				entityStateRefreshed2("d3"),
				entityInvariantChecked(testAgent,
	            		"d3",
	            		"d3 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;			
		
		var testingTask_2 = SEQ(
				entityInteracted("bJS"),
				entityStateRefreshed2("dJS"),
				entityInvariantChecked(testAgent,
	            		"dJS",
	            		"dJS should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				invariantChecked(testAgent,
						"health and score",
						(BeliefState S) -> 
							S.worldmodel().health <= 90
							&& S.worldmodel().score > 40)) ;
		
		var testingTask_3 = SEQ(
				entityInteracted("bFN0"),
				entityInteracted("bJE"),
				entityStateRefreshed2("dJE"),
				entityInvariantChecked(testAgent,
	            		"dJE",
	            		"dJE should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_4 = SEQ(
				entityInteracted("BridgeW"),
				entityInteracted("bAW"),
				entityStateRefreshed2("dAW"),
				entityInvariantChecked(testAgent,
	            		"dAW",
	            		"dAW should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_5 = SEQ(
				entityInteracted("bFN1"),
				entityInteracted("bAE"),
				entityStateRefreshed2("dAE"),
				entityInvariantChecked(testAgent,
	            		"dAE",
	            		"dAE should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_6 = SEQ(
				entityInteracted("bFN2"),
				entityInteracted("bshrine1"),
				entityStateRefreshed2("dshrine1"),
				entityInvariantChecked(testAgent,
	            		"dshrine1",
	            		"dshrine1 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				invariantChecked(testAgent,
						"health and score",
						(BeliefState S) -> S.worldmodel().health <= 90),
				atBGF( "shrine1",0.7f, true, false),
				Utils.waitNturns(2),
				invariantChecked(testAgent,
						"health and score",
						(BeliefState S) -> 
							S.worldmodel().health == 100
							&& S.worldmodel().score > 230)
				) ;
		
		var testingTask_7 = SEQ(
				entityInteracted("bPW"),
				entityStateRefreshed2("dPW"),
				entityInvariantChecked(testAgent,
	            		"dPW",
	            		"dPW should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_8 = SEQ(
				entityInteracted("bPS"),
				entityStateRefreshed2("dPS"),
				entityInvariantChecked(testAgent,
	            		"dPS",
	            		"dPS should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				) ;
		
		var testingTask_9 = SEQ(
				entityInteracted("bFC"),
				entityStateRefreshed2("dFC"),
				entityInvariantChecked(testAgent,
	            		"dFC",
	            		"dFC should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				invariantChecked(testAgent,
						"health and score",
						(BeliefState S) -> S.worldmodel().health <= 60),
				atBGF( "shrine2",0.7f, true, false),
				Utils.waitNturns(2),
				invariantChecked(testAgent,
						"health and score",
						(BeliefState S) -> 
							S.worldmodel().health == 100
							&& S.worldmodel().score > 330)
				) ;
		
		var testingTask = SEQ(
				testingTask_1, 
				testingTask_2,
				testingTask_3,
				testingTask_4,
				testingTask_5,
				testingTask_6,
				testingTask_7,
				testingTask_8,
				testingTask_9,
				entityInteracted("RFinal"),
				atBGF( "Finish",0.7f, true, false),
				Utils.waitNturns(2)
				) ;
				
		
		testAgent
			. setGoal(testingTask)
			. setTestDataCollector(new TestDataCollector()) ;
		
		Utils.runGoal(testAgent, testingTask, 2400, 20);
		assertTrue(testingTask.getStatus().success()) ;
		
		assertTrue(testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
		
		BeliefState state = testAgent.state() ;
		assertTrue(state.worldmodel().health == 100) ;
		
		environment.close();
    }
        
}
