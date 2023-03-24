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
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import game.LabRecruitsTestServer;
import eu.iv4xr.framework.spatial.Obstacle;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.BasicAgent;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

import static nl.uu.cs.aplib.AplibEDSL.* ;
import world.BeliefState;
import world.LabEntity;
import world.LabWorldModel;

// Creates an agent that walks a preset route.


class Autohealing_Test {
	
	private static LabRecruitsTestServer labRecruitsTestServer = null ;

    @BeforeAll
    static void start() {
    	// Uncomment this to make the game's graphic visible:
    	//TestSettings.USE_GRAPHICS = true ;
    	//SocketReaderWriter.debug = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { 
    	SocketReaderWriter.debug = false ;
    	if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); }
    

    void runAgent(TestAgent agent, GoalStructure G) throws InterruptedException {
    	agent.setGoal(G) ;
        int i = 0 ;
        agent.update();
        i = 1 ;        
        while (G.getStatus().inProgress()) {
        	agent.update();
            i++ ;
            var state = (BeliefState) agent.state() ;
            System.out.println("*** " + i + ", " + state.id + " @" + state.worldmodel.position) ;
            Thread.sleep(30);
            if (i>=200) break ;
        }
    }
    
    @Test
    void test_entityInCloseRange2_heal() throws InterruptedException {
        
    	var config = new LabRecruitsConfig("simple_withFireAnd Flag") ;
    	
    	config.view_distance = 20f ;
    	
        var environment = new LabRecruitsEnvironment(config);

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
        
        var g = GoalLib.entityInCloseRange2("b0") ;
        
        runAgent(agent,g) ;
        
        var wom = (LabWorldModel) agent.state().worldmodel ;
        assertTrue(g.getStatus().success()) ;
        assertTrue(wom.score >= 100) ;
        
        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

    }
    
    @Test
    void test_BGF_heal() throws InterruptedException {
        
    	var config = new LabRecruitsConfig("simple_withFireAnd Flag") ;
    	
    	config.view_distance = 6f ;
    	
        var environment = new LabRecruitsEnvironment(config);

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
        
        var g = GoalLib.atBGF("b0", 0.6f, false) ;
        
        runAgent(agent,g) ;
        
        var wom = (LabWorldModel) agent.state().worldmodel ;
        assertTrue(g.getStatus().success()) ;
        assertTrue(wom.score >= 100) ;
        
        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

    }
    
    @Test
    void test_entityStateRefreshed_heal() throws InterruptedException {
        
    	var config = new LabRecruitsConfig("simple_withFireAnd Flag") ;
    	
    	config.view_distance = 6f ;
    	
        var environment = new LabRecruitsEnvironment(config);

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
        
        var g = GoalLib.entityStateRefreshed2("b0") ;
        
        runAgent(agent,g) ;
        
        var wom = (LabWorldModel) agent.state().worldmodel ;
        assertTrue(g.getStatus().success()) ;
        assertTrue(wom.score >= 100) ;
        
        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

    }
    
}