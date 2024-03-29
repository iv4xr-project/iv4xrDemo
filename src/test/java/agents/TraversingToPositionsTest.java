/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents;

import static agents.TestSettings.USE_INSTRUMENT;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import agents.tactics.GoalLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import environments.SocketReaderWriter;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import game.LabRecruitsTestServer;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.BasicAgent;
import static nl.uu.cs.aplib.AplibEDSL.* ;
import world.BeliefState;

// Creates an agent that walks a preset route.


public class TraversingToPositionsTest {
	
	private static LabRecruitsTestServer labRecruitsTestServer = null ;

    @BeforeAll
    static void start() {
    	// Uncomment this to make the game's graphic visible:
    	// TestSettings.USE_GRAPHICS = true ;
    	//SocketReaderWriter.debug = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { 
    	SocketReaderWriter.debug = false ;
    	if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); }
    

    @Test
    public void reachPositions() throws InterruptedException {
        
        var environment = new LabRecruitsEnvironment(new LabRecruitsConfig("hollowsquare"));

        LabRecruitsTestAgent agent = new LabRecruitsTestAgent("agent0")
        		                     . attachState(new BeliefState())
        		                     . attachEnvironment(environment) ;

        // press play in Unity
        if (! environment.startSimulation())
            throw new InterruptedException("Unity refuses to start the Simulation!");

        if(TestSettings.USE_GRAPHICS) {
    		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
    		new Scanner(System.in) . nextLine() ;
    	}
        var p1 = new Vec3(1,0,8) ;
        var p2 = new Vec3(8,0,8) ;
        var p3 = new Vec3(8,0,1) ;
        var p4 = new Vec3(1,0,1) ;
        
        // Make the agent reach each positon sequentially.
        var g = SEQ(
        		    GoalLib.positionsVisited(p1),
        		    GoalLib.invariantChecked(agent,"p1 is reached", 
        		    		(BeliefState s) -> Vec3.dist(s.worldmodel().getFloorPosition(),p1) < 0.5), 
        		    GoalLib.positionsVisited(p2),
        		    GoalLib.invariantChecked(agent,"p2 is reached", 
        		    		(BeliefState s) -> Vec3.dist(s.worldmodel().getFloorPosition(),p2) < 0.5), 
        		    GoalLib.positionsVisited(p3),
        		    GoalLib.invariantChecked(agent,"p3 is reached", 
        		    		(BeliefState s) -> Vec3.dist(s.worldmodel().getFloorPosition(),p3) < 0.5), 
        		    GoalLib.positionsVisited(p4),
        		    GoalLib.invariantChecked(agent,"p4 is reached", 
        		    		(BeliefState s) -> Vec3.dist(s.worldmodel().getFloorPosition(),p4) < 0.5) 
        		);
        
        var dataCollector = new TestDataCollector() ;
        agent.setTestDataCollector(dataCollector).setGoal(g) ;

        int i = 0 ;
        while (g.getStatus().inProgress()) {
            agent.update();
            i++ ;
            System.out.println("*** " + i + ", " + agent.state().id + " @" + agent.state().worldmodel.position) ;
            Thread.sleep(30);
            if (i>=150) break ;
        }
        g.printGoalStructureStatus();
        System.out.println("#pass: " + dataCollector.getNumberOfPassVerdictsSeen()) ;
        assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 4) ;
        
        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

    }
}