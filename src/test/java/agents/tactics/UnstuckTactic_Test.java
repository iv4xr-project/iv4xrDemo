/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents.tactics;



import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import helperclasses.datastructures.linq.QArrayList;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.Tactic;

import static org.junit.jupiter.api.Assertions.* ;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;

/**
 * A simple test to demonstrate using iv4xr agents to test the Lab Recruits game.
 * The testing task is to verify that the closet in the east is reachable from
 * the player initial position, which it is if the door guarding it can be opened.
 * This in turn requires a series of switches and other doors to be opened.
 */
public class UnstuckTactic_Test {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	// TestSettings.USE_SERVER_FOR_TEST = false ;
    	// Uncomment this to make the game's graphic visible:
    	// TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); }
    
    void unstuckTest(String level, Vec3 targetPosition) throws InterruptedException {

    	// Create an environment
    	var config = new LabRecruitsConfig(level) ;
    	var environment = new LabRecruitsEnvironment(config);

        try {
        	if(TestSettings.USE_GRAPHICS) {
        		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
        		new Scanner(System.in) . nextLine() ;
        	}

	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("agent1") // matches the ID in the CSV file
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);
	        
	        environment.startSimulation(); // this will press the "Play" button in the game for you
	        // we need all nav-nodes to be marked as seen:
	        testAgent.state().pathfinder().perfect_memory_pathfinding = true ;
	        
	        
	        Tactic tactic = FIRSTof(
	        		TacticLib.tryToUnstuck(),
	        		action("move").do1((BeliefState S) -> {
	        			S.env().moveToward(S.id, S.worldmodel().getFloorPosition(), targetPosition) ;
	        			return S ;
	        		}).lift()) ;

	        var g = goal("to be at " + targetPosition)
	        		.toSolve((BeliefState B) -> 
	                      Vec3.dist(targetPosition,B.worldmodel().getFloorPosition()) <= 0.3)
	        		.withTactic(tactic)
	        		.lift() ;

	        testAgent . setGoal(g) ;
	        List<Vec3> path = new LinkedList<>() ;
	        path.add(targetPosition) ;
	        testAgent.state().applyPath(0, targetPosition, path); 
	        
	        int i = 0 ;
	        // keep updating the agent
	        while (g.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>50) {
	        		break ;
	        	}
	        }
	        g.printGoalStructureStatus();
	        assertTrue(g.getStatus().success()) ;

        }
        finally { environment.close(); }
    }
    
    /**
     * A test to verify that the east closet is reachable.
     * @throws InterruptedException 
     */
    @Test
    public void unstuckTest_1() throws InterruptedException {
    	unstuckTest("stucktest1", new Vec3(3,0,4.5f)) ;
    }
    
    @Test
    public void unstuckTest_2() throws InterruptedException {
    	unstuckTest("stucktest2", new Vec3(3.4f,0,5f)) ;
    }
}
