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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;


public class LR_mkScreenShot_Test {
	
	private LabRecruitsTestServer labRecruitsTestServer ;

    @BeforeEach
    void start() {
    	// set this to true to make the game's graphic visible:
    	var useGraphics = true ;
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
    public void test_env_observation() throws InterruptedException {
    	assertTrue(labRecruitsTestServer != null) ;
    	var env = new LabRecruitsEnvironment(new LabRecruitsConfig("buttons_doors_1"));
    	//new Scanner(System.in) . nextLine() ;
    	//LabWorldModel obs = env.observe("agent0") ;
    	Path p = Paths.get(System.getProperty("user.dir"),"tmp","myscreenshot.png") ;	
    	File f = new File(p.toString()) ;
    	f.delete() ;
    	assertFalse(f.exists()) ;
    	env.mkScreenShot(p.toString()) ;
    	Thread.sleep(1000);
    	assertTrue(f.exists()) ;
    	//new Scanner(System.in) . nextLine() ;
    }
    
}