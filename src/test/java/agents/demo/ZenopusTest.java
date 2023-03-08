package agents.demo;



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
import static org.junit.jupiter.api.Assertions.* ;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;
import world.LabEntity;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;


public class ZenopusTest {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	 TestSettings.USE_SERVER_FOR_TEST = false ;
    	// Uncomment this to make the game's graphic visible:
    	//TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }
    
    private String zenopusDir() {
    	String dir = System.getProperty("user.dir")  + File.separator 
    			+ ".." + File.separator 
    			+ "LRlevels" + File.separator + "ddolevels" + File.separator
    			+ "zenopus" ;
    	return dir ;
    }

    @AfterAll
    static void close() { if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); }

    //@Test
    public void debugMeshxxx() {
    	//var config = new LabRecruitsConfig("ruinzenopus_LR_2",zenopusDir()) ;
    	var config = new LabRecruitsConfig("ruinzenopus_LR_2",zenopusDir()) ;
    	config.light_intensity = 0.3f ;
    	var environment = new LabRecruitsEnvironment(config);

        try {
	        var testAgent = new LabRecruitsTestAgent("recruit") 
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

	        var G = SEQ(SUCCESS(),SUCCESS(),SUCCESS(),SUCCESS()) ; 
	        testAgent . setGoal(G) ;
	        
	        var pathfinder = testAgent.getState().pathfinder() ;
	        pathfinder.perfect_memory_pathfinding = true ;
	        testAgent.update();
	        testAgent.update();
	        
	        Vec3 p = new Vec3(9,6,26) ;
	        Vec3 q = new Vec3(9,6,7) ;
	        var path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        q = new Vec3(11,6,7) ;
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        q = new Vec3(15,6,6) ;
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        q = new Vec3(15,9,16) ;
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ; 
	        
        }
        finally { environment.close(); }
    }

   
    //@Test
    public void test1() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("ruinzenopus_LR_2",zenopusDir()) ;
    	config.light_intensity = 0.1f ;
    	config.view_distance = 10 ;
    	var environment = new LabRecruitsEnvironment(config);

        try {
        	TestSettings.youCanRepositionWindow();

	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("recruit") 
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

	        // define the testing-task:
	        var G = SEQ(
	        		//GoalLib.entityInteracted("b0")ss
	        		GoalLib.entityInteracted("bshrine1"),
	        		GoalLib.atBGF("shrine1", 01f,true),
	        		GoalLib.entityInteracted("bshrine1")
	        );

	        
	        testAgent 
	        	. setTestDataCollector(new TestDataCollector()) 
	        	. setGoal(G) ;
	        
	        System.out.println(">>>>") ;
	        
	        //environment.startSimulation(); 
	        //boolean mkScreenShot = true ;
	        //boolean toviewShotTaken = false ;
	        //String imgBaseFileName = System.getProperty("user.dir") + File.separator + "tmp"  + File.separator + "tower_" ;
	        var time = System.currentTimeMillis() ;
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            //var b3k0 = (LabEntity) testAgent.state().get("b3k0") ;
	            //if (mkScreenShot && i % 100 ==0) {
	            //	environment.mkScreenShot(imgBaseFileName + i + ".png") ;
	            //}
	            //if (mkScreenShot && b3k0 != null && !toviewShotTaken 
	            //		&& Vec3.distSq(testAgent.state().worldmodel().getFloorPosition(),
	            //				b3k0.getFloorPosition()) <= 1
	            //		&& i % 100 !=0) {
	            //	environment.mkScreenShot(imgBaseFileName + "top_" + i + ".png") ;
	            //	toviewShotTaken = true ;
	            //}
	            i++ ;
	        	testAgent.update();
	        	if (i>3500) {
	        		break ;
	        	}
	        }
	        //if (mkScreenShot) {
            //	environment.mkScreenShot(imgBaseFileName + i + ".png") ;
            //}
	        time = System.currentTimeMillis() - time;
	        
	        // check that we have passed both tests above:
	        //assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 4) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        System.out.println("** # turns=" + i + ", exectime="+ time+ "ms") ;
	        // close
	        //testAgent.printStatus();
        }
        finally { environment.close(); }
    }
    
 
}
