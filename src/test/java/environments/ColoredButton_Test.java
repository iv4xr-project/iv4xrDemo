/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package environments;

import org.junit.jupiter.api.Test;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import eu.iv4xr.framework.spatial.Vec3;
import game.LabRecruitsTestServer;
import game.Platform;
import world.BeliefState;
import world.LabEntity;
import world.LabWorldModel;
import world.Observation.GameObject.Color;

import static nl.uu.cs.aplib.AplibEDSL.*;

import static org.junit.jupiter.api.Assertions.* ;

import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;


class ColoredButton_Test {
	
	private LabRecruitsTestServer labRecruitsTestServer ;

    @BeforeEach
    void start() {
    	// set this to true to make the game's graphic visible:
    	var useGraphics = false ;
    	SocketReaderWriter.debug = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer =new LabRecruitsTestServer(
    			useGraphics,
                Platform.PathToLabRecruitsExecutable(labRecruitesExeRootDir));
    	labRecruitsTestServer.waitForGameToLoad();
    }

    @AfterEach
    void close() { 
    	SocketReaderWriter.debug = false ;
    	if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); 
    }

    
    @Test
    void colorObservation() {
    	assertTrue(labRecruitsTestServer != null) ;
    	var env = new LabRecruitsEnvironment(new LabRecruitsConfig("square_withColorButton"));
    	LabWorldModel obs = env.observe("agent0") ;
    	var cbutton1 = obs.elements.get("CB1") ;
    	var cbutton2 = obs.elements.get("CB2") ;
    	var cscreen = obs.elements.get("screen") ;
    	System.out.println("=== CB1: " + cbutton1) ;
    	System.out.println("=== CB2: " + cbutton2) ;
    	System.out.println("=== screen: " + cscreen) ;
    	Color c1 = (Color) cbutton1.properties.get("color") ;
    	Color c2 = (Color) cbutton2.properties.get("color") ;	
    	Color c3 = (Color) cscreen.properties.get("color") ;
    	assertTrue(c1.equals(new Color(0f,0f,1f))) ;
    	assertTrue(c3.equals(new Color(0f,0f,0f))) ;
    	
    	float val_D9 = ((float) (13*16 + 9))/255f ;
    	
    	assertTrue(c2.equals(new Color(val_D9,val_D9,val_D9))) ;
    	
    }
    
    @Test
    void interaction_with_colloredButton_test() throws InterruptedException {
    	
    	var env = new LabRecruitsEnvironment(new LabRecruitsConfig("square_withColorButton"));
    	
    	 LabRecruitsTestAgent agent = new LabRecruitsTestAgent("agent0")
                 . attachState(new BeliefState())
                 . attachEnvironment(env) ;
    	
    	var G = SEQ(GoalLib.entityInteracted("CB1"),
    			    GoalLib.entityInteracted("CB2")) ;
    	
        agent.setGoal(G) ;
        
        Thread.sleep(1000);

        int i = 0 ;
        
        while (G.getStatus().inProgress()) {
        	agent.update();
            i++ ;
            System.out.println("*** " + i + ", " + agent.state().id + " @" + agent.state().worldmodel.position) ;
            //System.out.println(">>>> " + ((LabWorldModel) agent.state().worldmodel).gameover)  ;
            Thread.sleep(30);
            if (i>=80) break ;
        }
        
        assertTrue(G.getStatus().success()) ;
    }
    
}