package agents.demo.cleaned;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
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


public class FireHazardLevel_2_Test {

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

		var config = new LabRecruitsConfig("HZRDIndirect");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("0")
				.attachState(new BeliefState())
				.attachEnvironment(environment);
		
		var testingTask = SEQ(
				positionInCloseRange(new Vec3(6,0,5)).lift(),
				positionInCloseRange(new Vec3(8,0,1)).lift(),
				positionInCloseRange(new Vec3(13,4,1)).lift(),
				entityInteracted("b4.1"),
				positionInCloseRange(new Vec3(13,4,3)).lift(),
				entityInteracted("b7.1"),
				positionInCloseRange(new Vec3(9,4,9)).lift(),
				positionInCloseRange(new Vec3(8,4,6)).lift(),
				positionInCloseRange(new Vec3(5,4,7)).lift(),
				entityInteracted("b8.2"),
				positionInCloseRange(new Vec3(1,4,13)).lift(),
				entityInteracted("b5.1"),
				positionInCloseRange(new Vec3(1,4,22)).lift(),
				positionInCloseRange(new Vec3(6,0,22)).lift(),
				entityInteracted("b1.1"),
				positionInCloseRange(new Vec3(5,0,25)).lift()
				) ;
				
		testAgent.setGoal(testingTask);
		
		Utils.runGoal(testAgent, testingTask, 600, 50);
		assertTrue(testingTask.getStatus().success()) ;
		
		BeliefState state = testAgent.state() ;
		assertTrue(state.worldmodel().health >= 50) ;
		
		environment.close();
    }
        
}
