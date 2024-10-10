package agents.demo.cleaned;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import static nl.uu.cs.aplib.AplibEDSL.* ;

import static org.junit.jupiter.api.Assertions.* ;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;


public class ButtonsDoors1_Test {

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

       
    //@Test
    public void test0() throws InterruptedException {

		var config = new LabRecruitsConfig("buttons_doors_1");
		var environment = new LabRecruitsEnvironment(config);

		// create a test agent
		var testAgent = new LabRecruitsTestAgent("agent1")
				.attachState(new BeliefState())
				.attachEnvironment(environment);

		var testingTask = Utils.exploredOut() ;
				
		testAgent.setGoal(testingTask);
		
		Utils.runGoal(testAgent, testingTask, 100, 50);
		
		BeliefState state = testAgent.state() ;
		
		assertTrue(state.get("door1") != null && state.get("door3") != null) ;
		assertTrue(! (Boolean) state.val("door1","isOpen") 
				&& ! (Boolean) state.val("door3","isOpen")
				&& ! Utils.isReachable(testAgent,"button3")
				) ;
		
		environment.close();
    }
    
    //@Test
    public void test1() throws InterruptedException {

		var config = new LabRecruitsConfig("buttons_doors_1");
		var environment = new LabRecruitsEnvironment(config);

		var testAgent = new LabRecruitsTestAgent("agent1")
				.attachState(new BeliefState())
				.attachEnvironment(environment);

		var testingTask = SEQ(
				GoalLib.entityInteracted("button1"),
				Utils.exploredOut(),
				GoalLib.entityInteracted("button3"),
				Utils.exploredOut()) ;
		
		testAgent.setGoal(testingTask);
			
		Utils.runGoal(testAgent, testingTask, 200, 50);
		
		BeliefState state = testAgent.state() ;
		
		assertTrue(state.get("door1") != null
			&& ! (Boolean) state.val("door1","isOpen") 
			&& ! Utils.isReachable(testAgent,"door3")
			) ;
		
		environment.close();
    }
    
    @Test
    public void test2() throws InterruptedException {

		var config = new LabRecruitsConfig("buttons_doors_1");
		var environment = new LabRecruitsEnvironment(config);

		var testAgent = new LabRecruitsTestAgent("agent1")
				.attachState(new BeliefState())
				.attachEnvironment(environment);

		var testingTask = SEQ(
				GoalLib.entityInteracted("button1"),
				Utils.exploredOut(),
				GoalLib.entityInteracted("button3"),
				Utils.exploredOut(),
				GoalLib.entityInteracted("button4"),
				Utils.exploredOut()) ;
		
		testAgent.setGoal(testingTask);
			
		Utils.runGoal(testAgent, testingTask, 250, 50);
		
		BeliefState state = testAgent.state() ;
		
		assertTrue((Boolean) state.val("door3","isOpen") 
			&& Utils.isReachable(testAgent,"door3")
			&& (Integer) state.worldmodel().score == 34
			) ;
		
		environment.close();
    }
    
}
