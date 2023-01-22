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


public class ApproachingEntity_Test {
	
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
    	if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); 
    }
    

    public void approach(String level, String entity) throws InterruptedException {
        
    	var config = new LabRecruitsConfig(level) ;
    	config.view_distance = 20 ;
    	
        var environment = new LabRecruitsEnvironment(config);

        LabRecruitsTestAgent agent = new LabRecruitsTestAgent("agent0")
        		                     . attachState(new BeliefState())
        		                     . attachEnvironment(environment) ;
        
        agent.state().pathfinder().setPerfectMemoryPathfinding(true);

        
        TestSettings.youCanRepositionWindow() ;

        var g = GoalLib.entityInCloseRange2(entity) ;
        agent.setGoal(g) ;
        
        Thread.sleep(2000);

        int i = 0 ;
        
        while (g.getStatus().inProgress()) {
        	agent.update();
            i++ ;
            System.out.println("*** " + i + ", " + agent.state().id + " @" + agent.state().worldmodel.position) ;
            System.out.println(">>>> " + ((LabWorldModel) agent.state().worldmodel).gameover)  ;
            Thread.sleep(30);
            if (i>=80) break ;
        }
        assertTrue(g.getStatus().success()) ;
        
        if (!environment.close())
            throw new InterruptedException("Unity refuses to close the Simulation!");

    }
    
    @Test
    public void testDE() throws InterruptedException { 
    	approach("square3","DE") ;

    }
    
    @Test
    public void testDW() throws InterruptedException { 
    	approach("square3","DW") ;

    }
    
    @Test
    public void testDN() throws InterruptedException { 
    	approach("square3","DN") ;

    }
    
    @Test
    public void testDS() throws InterruptedException { 
    	approach("square3","DS") ;

    }
    
    @Test
    public void testODE() throws InterruptedException { 
    	approach("square3","ODE") ;

    }
    
    @Test
    public void testODW() throws InterruptedException { 
    	approach("square3","ODW") ;

    }
    
    @Test
    public void testODN() throws InterruptedException { 
    	approach("square3","ODN") ;

    }
    
    @Test
    public void testODS() throws InterruptedException { 
    	approach("square3","ODS") ;

    }
}