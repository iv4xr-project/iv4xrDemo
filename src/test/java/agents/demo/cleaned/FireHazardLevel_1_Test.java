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

/**
 * A play-test of the level HZRDDirect, that it can be completed.
 */
public class FireHazardLevel_1_Test {

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

		var config = new LabRecruitsConfig("HZRDDirect");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("0")
				.attachState(new BeliefState())
				.attachEnvironment(environment);

		var testingTask = SEQ(
				positionInCloseRange(new Vec3(6,0,5)).lift(),
				positionInCloseRange(new Vec3(7,0,8)).lift(),
				positionInCloseRange(new Vec3(7,0,11)).lift(),
				positionInCloseRange(new Vec3(5,0,11)).lift(),
				positionInCloseRange(new Vec3(5,0,15)).lift(),
				positionInCloseRange(new Vec3(2,0,16)).lift(),
				positionInCloseRange(new Vec3(1,0,18)).lift(),
				positionInCloseRange(new Vec3(3,0,20)).lift(),
				positionInCloseRange(new Vec3(6,0,20)).lift(),
				entityInteracted("b1.1"),
				positionInCloseRange(new Vec3(5,0,25)).lift()
				) ;
				
		testAgent.setGoal(testingTask);
		
		Utils.runGoal(testAgent, testingTask, 250, 50);
		assertTrue(testingTask.getStatus().success()) ;

		BeliefState state = testAgent.state() ;		
		assertTrue(state.worldmodel().health >= 50) ;
		
		environment.close();
    }
        
}
