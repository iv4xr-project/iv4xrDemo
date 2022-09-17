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


public class Monster_Test {
	
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
    

    @Test
    public void testMonster() throws InterruptedException {
        
    	var config = new LabRecruitsConfig("square_withEnemies") ;
    	
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
        
        var target = "Finish" ;
        var g = GoalLib.atBGF("orc1",1f,true) ;
        
        agent.setGoal(g) ;
        

        int i = 0 ;
        agent.update();
        assertTrue(((LabWorldModel) agent.state().worldmodel).gameover == false)  ;
        i = 1 ;
        System.out.println(">>>> " + ((LabWorldModel) agent.state().worldmodel).gameover)  ;
        
        while (g.getStatus().inProgress()) {
        	agent.update();
            i++ ;
            System.out.println("*** " + i + ", " + agent.state().id + " @" + agent.state().worldmodel.position) ;
            System.out.println(">>>> " + ((LabWorldModel) agent.state().worldmodel).gameover)  ;
            Thread.sleep(30);
            if (i>=150) break ;
        }
        
        //assertTrue(((LabWorldModel) agent.state().worldmodel).gameover == true)  ;
        
        var wom = (LabWorldModel) agent.state().worldmodel ;
        var orc1 = wom.getElement("orc1") ;
        
        assertTrue(wom.elements.values().stream().filter(e -> e.type == LabEntity.ENEMY).count() == 2) ;
        assertTrue(Vec3.dist(wom.position, orc1.position) <= 1.5f) ;
        
        // add few updates:
        //Thread.sleep(1000);
        //agent.update();
        assertTrue(wom.health <= 90) ;  
        
        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

    }
}