package agents.demo.cleaned;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;

import static org.junit.jupiter.api.Assertions.* ;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

import static org.junit.jupiter.params.provider.Arguments.arguments ;

import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;


/**
 * Testing few button interactions on a simple level.
 */
public class ButtonInteraction_Test {

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

    static List<Arguments> testInputs = Arrays.asList(
    	    arguments("button0", 90),
    	    arguments("button1", 100),
    	    arguments("button2", 100)
    	);
    
    @ParameterizedTest
    @FieldSource("testInputs")
    public void test_buttoninteractions(String button, int expectedHealth) 
    		throws InterruptedException 
    {

		var config = new LabRecruitsConfig("simple_enemy_bdd");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("agent0")
				.attachState(new BeliefState())
				.attachEnvironment(environment);

		var testingTask = GoalLib.entityInteracted(button) ;
				
		testAgent.setGoal(testingTask);
		
		Utils.runGoal(testAgent, testingTask, 100, 50);
		assertTrue(testingTask.getStatus().success()) ;
		
		BeliefState state = testAgent.state() ;
		
		assertTrue(state.worldmodel().health >= expectedHealth) ;
		
		environment.close();
    }
        
}
