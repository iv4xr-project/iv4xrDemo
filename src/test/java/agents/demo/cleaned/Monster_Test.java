package agents.demo.cleaned;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;

import static org.junit.jupiter.api.Assertions.* ;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
import static agents.tactics.GoalLib.* ;


/**
 * Testing monsters, e.g. that they are drawn to the player, they hurt
 * the player, and they block navigation.
 */
public class Monster_Test {

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
    public void test0() 
    		throws InterruptedException 
    {

		var config = new LabRecruitsConfig("square_withEnemies");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("agent0")
				.attachState(new BeliefState())
				.attachEnvironment(environment);

		var testingTask = SEQ(
				atBGF("Finish",1.5f,true,false),
				Utils.waitNturns(40));
				
		testAgent.setGoal(testingTask);
		
		Utils.runGoal(testAgent, testingTask, 100, 30);
		assertTrue(testingTask.getStatus().success()) ;
		
		BeliefState state = testAgent.state() ;
		
		assertTrue(state.get("orc1") != null && state.worldmodel().health <= 90) ;
		
		environment.close();
    }
    
    @Test
    public void test1() 
    		throws InterruptedException 
    {

		var config = new LabRecruitsConfig("simple2_withEnemies");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("agent0")
				.attachState(new BeliefState())
				.attachEnvironment(environment);

		var testingTask = Utils.waitNturns(2) ;
				
		testAgent.setGoal(testingTask);
		
		Utils.runGoal(testAgent, testingTask, 10, 0);
		assertTrue(testingTask.getStatus().success()) ;
		
		BeliefState state = testAgent.state() ;
		
		assertTrue(state.get("orc1") != null) ;
		assertTrue(! Utils.isReachable(testAgent,"button0")) ;
		
		environment.close();
    }
    
    @Test
    public void test2() 
    		throws InterruptedException 
    {

		var config = new LabRecruitsConfig("simple_withEnemies");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("agent0")
				.attachState(new BeliefState())
				.attachEnvironment(environment);

		var testingTask = SEQ(
				entityInteracted("button0"),
				Utils.waitNturns(20)) ;
				
		testAgent.setGoal(testingTask);
		
		Utils.runGoal(testAgent, testingTask, 100, 30);
		assertTrue(testingTask.getStatus().success()) ;
		
		BeliefState state = testAgent.state() ;
		
		assertTrue(state.get("orc1") != null && state.worldmodel().health <= 90) ;
		
		environment.close();
    }
        
}
