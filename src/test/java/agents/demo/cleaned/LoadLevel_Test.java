package agents.demo.cleaned;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import static org.junit.jupiter.api.Assertions.* ;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;


public class LoadLevel_Test {

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
    public void test_loadlevel() throws InterruptedException {

		var config = new LabRecruitsConfig("simple_enemy_bdd");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("agent0")
				.attachState(new BeliefState())
				.attachEnvironment(environment);

		var testingTask = Utils.waitNturns(10) ;
				
		testAgent.setGoal(testingTask);
		
		Utils.runGoal(testAgent, testingTask, 100, 50);
		
		BeliefState state = testAgent.state() ;
		
		assertTrue(state.get("orc1") != null) ;
		
		environment.close();
    }
        
}
