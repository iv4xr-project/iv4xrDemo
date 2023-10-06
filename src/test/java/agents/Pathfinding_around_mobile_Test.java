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
import world.LabEntity;
import world.LabWorldModel;

// Creates an agent that walks a preset route.


public class Pathfinding_around_mobile_Test {
	
	private static LabRecruitsTestServer labRecruitsTestServer = null ;

    @BeforeAll
    static void start() {
    	// Uncomment this to make the game's graphic visible:
    	TestSettings.USE_GRAPHICS = true ;
    	//SocketReaderWriter.debug = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { 
    	SocketReaderWriter.debug = false ;
    	if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); }
    

    //@Test
    public void test_avoid_otheragent_and_npc() throws InterruptedException {
        
    	var config = new LabRecruitsConfig("pathplanning_around_mobile") ;
    	config.view_distance = 20 ;
    	
        var environment = new LabRecruitsEnvironment(config);

        LabRecruitsTestAgent agent = new LabRecruitsTestAgent("agent0")
        		                     . attachState(new BeliefState())
        		                     . attachEnvironment(environment) ;

        // press play in Unity
        if (! environment.startSimulation())
            throw new InterruptedException("Unity refuses to start the Simulation!");

        TestSettings.youCanRepositionWindow() ;

        var target = "button0" ;
        var g = GoalLib.atBGF(target,0.5f,false) ;
        agent.setGoal(g) ;
        

        int i = 0 ;
        while (g.getStatus().inProgress()) {
        	agent.update();
            i++ ;
            System.out.println("*** " + i + ", " + agent.state().id + " @" + agent.state().worldmodel.position) ;
            System.out.println(">>>> " + ((LabWorldModel) agent.state().worldmodel).gameover)  ;
            Thread.sleep(30);
            if (i>=100) break ;
        }
        assertTrue(g.getStatus().success()) ;
        
        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

    }
    
    @Test
    public void test_replan() throws InterruptedException {
        
    	var config = new LabRecruitsConfig("pathplanning_around_mobile2") ;
    	config.view_distance = 20 ;
    	
        var environment = new LabRecruitsEnvironment(config);

        LabRecruitsTestAgent agent = new LabRecruitsTestAgent("agent0")
        		                     . attachState(new BeliefState())
        		                     . attachEnvironment(environment) ;
        
        LabRecruitsTestAgent agentBob = new LabRecruitsTestAgent("bob")
                . attachState(new BeliefState())
                . attachEnvironment(environment) ;

        // press play in Unity
        if (! environment.startSimulation())
            throw new InterruptedException("Unity refuses to start the Simulation!");

        if(TestSettings.USE_GRAPHICS) {
    		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
    		new Scanner(System.in) . nextLine() ;
    	}

        var g1 = GoalLib.atBGF("button0",0.5f,false) ;
        agent.setGoal(g1) ;
        
        var g2 = GoalLib.atBGF("button1",0.3f,false) ;
        agentBob.setGoal(g2) ;

        // first run the main agent few-rounds:
        int i = 0 ;
       	agent.update();
       	var p0 = agent.getState().worldmodel.position ;
        while (i < 3) {
        	agent.update();
            System.out.println("*** " + i + ", " + agent.state().id + " @" + agent.state().worldmodel.position) ;
            System.out.println(">>>> " + ((LabWorldModel) agent.state().worldmodel).gameover)  ;
            Thread.sleep(30);        
            i++ ;
        }
       	var p1 = agent.getState().worldmodel.position ;
        var button0 = agent.getState().worldmodel.elements.get("button0") ;
        assertTrue(Vec3.dist(button0.position,p1) < Vec3.dist(button0.position,p0)) ;
        assertTrue(Math.abs(p0.z = p1.z) <= 1) ; 
       	
       	
        // now agent bob:
        while (g2.getStatus().inProgress()) {
        	agentBob.update();
            i++ ;
            System.out.println("*** " + i + ", " + agentBob.state().id + " @" + agentBob.state().worldmodel.position) ;
            System.out.println(">>>> " + ((LabWorldModel) agentBob.state().worldmodel).gameover)  ;
            Thread.sleep(30);
            if (i>=30) break ;
        }
        assertTrue(g2.getStatus().success()) ;
        
        while (g1.getStatus().inProgress()) {
        	agent.update();
            System.out.println("*** " + i + ", " + agent.state().id + " @" + agent.state().worldmodel.position) ;
            System.out.println(">>>> " + ((LabWorldModel) agent.state().worldmodel).gameover)  ;
            Thread.sleep(30);        
            i++ ;
            if (i>=100) break ;
        }
        assertTrue(g1.getStatus().success()) ;

        //assertTrue(g.getStatus().success()) ;
        
        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

    }
}