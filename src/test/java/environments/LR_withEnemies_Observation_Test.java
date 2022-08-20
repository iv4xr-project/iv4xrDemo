/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package environments;

import org.junit.jupiter.api.Test;

import agents.TestSettings;
import eu.iv4xr.framework.spatial.Vec3;
import game.LabRecruitsTestServer;
import game.Platform;
import world.LabEntity;
import world.LabWorldModel;

import static org.junit.jupiter.api.Assertions.* ;

import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;


public class LR_withEnemies_Observation_Test {
	
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

    void hit_RETURN() {
		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
    	new Scanner(System.in) . nextLine() ;
	}
    
      
    
    @Test
    public void test_env_observation() {
    	assertTrue(labRecruitsTestServer != null) ;
    	var env = new LabRecruitsEnvironment(new LabRecruitsConfig("square_withEnemies"));
    	LabWorldModel obs = env.observe("agent0") ;
    	assertFalse(obs.gameover) ;
    	var orc1 = obs.elements.get("orc1") ;
    	var orc2 = obs.elements.get("orc2") ;
    	var smith = obs.elements.get("smith1") ;
    	assertTrue(orc1.type.equals(LabEntity.ENEMY)) ;
    	assertTrue(orc2.type.equals(LabEntity.ENEMY)) ;
    	assertTrue(smith.type.equals(LabEntity.NPC)) ;
    	//hit_RETURN() ;
    }
    
}