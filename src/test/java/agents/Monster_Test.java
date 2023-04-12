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
import eu.iv4xr.framework.spatial.Obstacle;
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
        
        var g = SEQ(GoalLib.atBGF("Finish",1.5f,true),
        		SUCCESS(),
        		SUCCESS(),
        		SUCCESS())
        		;
        
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
        //assertTrue(Vec3.dist(wom.position, orc1.position) <= 1.5f) ;
        
        System.out.println(">>> orc1 = " + orc1) ;
        System.out.println(">>> orc1 prev state: " + orc1.getPreviousState()) ;
        
        //add few updates:
        Thread.sleep(1000);
        wom = agent.state().env().observe("agent0") ;
        assertTrue(wom.health <= 90) ;  
        
        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

    }
    
    @Test
    public void testMonsterBlockingLogic() {

    	var config = new LabRecruitsConfig("simple2_withEnemies") ;   	
        var environment = new LabRecruitsEnvironment(config);

        try {
	        var testAgent = new LabRecruitsTestAgent("agent0") 
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

	        var G = SEQ(SUCCESS(),SUCCESS(),SUCCESS(),SUCCESS()) ; 
	        testAgent . setGoal(G) ;
	        
	        var pathfinder = testAgent.getState().pathfinder() ;
	        pathfinder.perfect_memory_pathfinding = true ;
	        testAgent.update();
	        testAgent.update();
	        
	        var wom = (LabWorldModel) testAgent.state().worldmodel ;
	        var orc1 = wom.getElement("orc1") ;
	        System.out.println(">>> orc1 = " + orc1) ;
	        
	        Vec3 p = new Vec3(1,0,8) ;
	        Vec3 q = new Vec3(10,0,8) ;
	        var path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        
	        assertTrue(path == null) ;
	                }
        finally { environment.close(); }
    }
    
    @Test
    public void testNavigatingAroundMonster() throws InterruptedException {
    	
    	var config = new LabRecruitsConfig("simple_withEnemies") ;   	
        var environment = new LabRecruitsEnvironment(config);

        LabRecruitsTestAgent agent = new LabRecruitsTestAgent("agent0")
        		                     . attachState(new BeliefState())
        		                     . attachEnvironment(environment) ;

        if(TestSettings.USE_GRAPHICS) {
    		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
    		new Scanner(System.in) . nextLine() ;
    	}
        
        var g = GoalLib.entityInteracted("button0") ;
        
        agent.setGoal(g) ;
        

        int i = 0 ;
        agent.update();
        i = 1 ;
        var wom = (LabWorldModel) agent.state().worldmodel ;
        var orc1 = wom.getElement("orc1") ;
        System.out.println(">>> orc1 = " + orc1) ;
        
        while (g.getStatus().inProgress()) {
        	agent.update();
            i++ ;
            System.out.println("*** " + i + ", " + agent.state().id + " @" + agent.state().worldmodel.position) ;
            System.out.println(">>>> " + ((LabWorldModel) agent.state().worldmodel).gameover)  ;
            Thread.sleep(30);
            if (i>=70) break ;
        }
        
        //assertTrue(((LabWorldModel) agent.state().worldmodel).gameover == true)  ;
        
        wom = (LabWorldModel) agent.state().worldmodel ;
        orc1 = wom.getElement("orc1") ;
        
        System.out.println(">>> orc1 = " + orc1) ;
        // add few updates:
        //Thread.sleep(1000);
        //agent.update();
        assertTrue(g.getStatus().success()) ;  
        
        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

    }
}