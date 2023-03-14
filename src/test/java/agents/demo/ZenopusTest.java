package agents.demo;



import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.goalsAndTactics.Sa1Solver;
import eu.iv4xr.framework.goalsAndTactics.Sa1Solver.Policy;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import helperclasses.datastructures.linq.QArrayList;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import static org.junit.jupiter.api.Assertions.* ;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.Function;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;
import world.LabEntity;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;


class ZenopusTest {

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
    void debugMesh() {
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
	        
	        for (var o : pathfinder.obstacles) {
	        	o.isBlocking = false ;
	        }
	        
	        Vec3 p = new Vec3(127,3,117) ;
	        //Vec3 q = new Vec3(127,3,119) ;
	        Vec3 q = new Vec3(131,3,119) ;
	        var path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        
	        p = new Vec3(77,3,67) ;
	        q = new Vec3(74,3,62) ;
	        
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;

	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        
	        p = new Vec3(86,3,57) ;
	        q = new Vec3(90,3,57) ;
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        
	        p = new Vec3(159,3,55) ;
	        q = new Vec3(159,1,45) ;
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        
	        p = new Vec3(159,3,127) ;
	        q = new Vec3(159,2,131) ;
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        
	        p = new Vec3(159,3,128f) ;
	        q = new Vec3(159,0,137) ;
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        		
	        p = new Vec3(159f,2.7f,128.92f) ;
	        q = new Vec3(159,0,137) ;
	        path = pathfinder.findPath(p,q, BeliefState.DIST_TO_FACE_THRESHOLD) ;
	        System.out.println("### " + p + "-->" + q + ": " + path) ;
	        	        
        }
        finally { environment.close(); }
    }
    
    static String imgDir = System.getProperty("user.dir") 
			+ File.separator + "tmp"  
			+ File.separator + "zenopus" ;
    
    public static GoalStructure justMkScreenShot(String imgDir, String id) {
    	String prefix = imgDir + File.separator + id + "_" ;
    	Action smile = action("mkScreenShot")
    			.do1((BeliefState S) -> {
    			   String filename = prefix + S.worldmodel.timestamp + ".png" ;
    			   return S.env().mkScreenShot(filename) ;	
    			})
    			
    			;
    	return lift("making screenshot",smile) ;
    }
    
    static boolean ENABLE_SCREENSHOOTING = true ;
    
    public static GoalStructure mkScreenShot(String imgDir, String id) {
    	if (ENABLE_SCREENSHOOTING)
    		return SEQ(GoalLib.atBGF(id, 0.6f,false), justMkScreenShot(imgDir,id)) ;
    	else
    		return GoalLib.atBGF(id, 0.6f,false) ;
    }
    
    @Test
    void debugTest() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("ruinzenopus_LR_2",zenopusDir()) ;
    	config.light_intensity = 0.1f ;
    	config.view_distance = 20 ;
    	var environment = new LabRecruitsEnvironment(config);

        try {
        	TestSettings.youCanRepositionWindow();

	        // create a test agent
	        var testAgent = 
	        		new LabRecruitsTestAgent("tester") 
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

	        
	        var G = SEQ( 
	        		mkScreenShot(imgDir,"bN")
	        		) ;

	        
	        testAgent 
	        	. setTestDataCollector(new TestDataCollector()) 
	        	. setGoal(G) ;
	        
	        var time = System.currentTimeMillis() ;
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>3500) {
	        		break ;
	        	}
	        }
	        time = System.currentTimeMillis() - time;
	        System.out.println("** # turns=" + i + ", exectime="+ time+ "ms") ;
	        
	        // check that we have passed both tests above:
	        //assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 4) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        // close
	        //testAgent.printStatus();
        }
        finally { environment.close(); }
    }

   
    //@Test
    void test1() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("ruinzenopus_LR_2",zenopusDir()) ;
    	config.light_intensity = 0.1f ;
    	config.view_distance = 20 ;
    	var environment = new LabRecruitsEnvironment(config);

        try {
        	TestSettings.youCanRepositionWindow();

	        // create a test agent
	        var testAgent = 
	        		new LabRecruitsTestAgent("recruit") 
	        		//new LabRecruitsTestAgent("tester") 
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

	        // define the testing-task:
	        var G = SEQ(
	        		mkScreenShot(imgDir,"RStart"),
	        		GoalLib.entityInteracted("b0"), GoalLib.entityStateRefreshed2("d0"),
	        		mkScreenShot(imgDir,"CStart"),
	        		GoalLib.entityInteracted("b1"), GoalLib.entityStateRefreshed2("d1"),
	        		mkScreenShot(imgDir,"XStart"),
	        		GoalLib.entityInteracted("b3"), GoalLib.entityStateRefreshed2("d3"),
	        		mkScreenShot(imgDir,"b4"),
	        		mkScreenShot(imgDir,"CTrap"),	
	        		mkScreenShot(imgDir,"CAfterTrap1"), 
	        		mkScreenShot(imgDir,"CAfterTrap2"), 
	        		GoalLib.entityInteracted("bJS"), GoalLib.entityStateRefreshed2("dJS"),
	        		mkScreenShot(imgDir,"RJ"), 
	        		GoalLib.entityInteracted("bFN0"),
	        		GoalLib.entityInteracted("bJE"),  GoalLib.entityStateRefreshed2("dJE"),
	        		mkScreenShot(imgDir,"BridgeW"),
	        		GoalLib.entityInteracted("bAW"),  GoalLib.entityStateRefreshed2("dAW"),
	        		mkScreenShot(imgDir,"RA"),
	        		GoalLib.entityInteracted("bFN1"),
	        		GoalLib.entityInteracted("bAE"),  GoalLib.entityStateRefreshed2("dAE"),
	        		mkScreenShot(imgDir,"BridgeE"),
	        		GoalLib.entityInteracted("bFN2"),
	        		GoalLib.entityInteracted("bshrine1"), GoalLib.entityStateRefreshed2("dshrine1"),
	        		mkScreenShot(imgDir,"shrine1"),
	        		mkScreenShot(imgDir,"CPW"),
	        		GoalLib.entityInteracted("bPW"), GoalLib.entityStateRefreshed2("dPW"),
	        		mkScreenShot(imgDir,"RP"),
	        		GoalLib.entityInteracted("bPS"), GoalLib.entityStateRefreshed2("dPS"),
	        		mkScreenShot(imgDir,"CPS"),
	        		mkScreenShot(imgDir,"RG"),
	        		mkScreenShot(imgDir,"CFN"),
	        		mkScreenShot(imgDir,"bFC"),
	        		GoalLib.entityInteracted("bFC"), GoalLib.entityStateRefreshed2("dFC"),
	        		GoalLib.atBGF("shrine2", 0.2f, true),
	        		mkScreenShot(imgDir,"RFinal"),
	        		GoalLib.atBGF("Finish", 0.2f, true),
	           		mkScreenShot(imgDir,"Finish")
	        );
	        
	        testAgent 
	        	. setTestDataCollector(new TestDataCollector()) 
	        	. setGoal(G) ;
	        
	        var time = System.currentTimeMillis() ;
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>3500) {
	        		break ;
	        	}
	        }
	        time = System.currentTimeMillis() - time;
	        System.out.println("** # turns=" + i + ", exectime="+ time+ "ms") ;
	        
	        // check that we have passed both tests above:
	        //assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 4) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        // close
	        //testAgent.printStatus();
        }
        finally { environment.close(); }
    }
    
    //@Test
    void test2() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("ruinzenopus_LR_2",zenopusDir()) ;
    	config.light_intensity = 0.1f ;
    	config.view_distance = 20 ;
    	var environment = new LabRecruitsEnvironment(config);
    	
    	// configuring SA1-solver:		
    	var sa1Solver = new Sa1Solver<Integer>(
    			(S, e) -> {
    				var B = (BeliefState) S ;
    				var f = (LabEntity) e ;
    				var path = B.pathfinder().findPath(B.worldmodel().getFloorPosition(), f.getFloorPosition(), BeliefState.DIST_TO_FACE_THRESHOLD) ;
    				return path != null ;
    			},
    			(S, e) -> Vec3.distSq(S.worldmodel().position, e.position),
    			S -> (e1, e2) -> Vec3.distSq(e1.position,e2.position),
    			eId -> GoalLib.entityInteracted(eId),
    			eId -> GoalLib.entityStateRefreshed2(eId),
    			S -> {
    				var B = (BeliefState) S ;
    				var path = B.pathfinder().explore(B.worldmodel().getFloorPosition(), BeliefState.DIST_TO_FACE_THRESHOLD) ;
    			   return path == null;
    			},
    			budget -> R8FireTest.exploreG(budget)
    			);
    	
    	sa1Solver.useGlobalVisitedSet = true;
    	// exclude b2 from the search to prevent the agent to go all the way to the start to
    	// try to open the FN dooor/s
    	sa1Solver.globalVisited.add("b2") ;
    	

        try {
        	TestSettings.youCanRepositionWindow();

	        // create a test agent
	        var testAgent = 
	        		new LabRecruitsTestAgent("recruit") 
	        		//new LabRecruitsTestAgent("tester") 
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);
	        
	        // Configure the SA1-solver for tasks to open a door:
	    	int explorationBudget = 20;
	    	Function <String,GoalStructure> openWithSA1 = 
	    		doorToOpen -> 
	    			sa1Solver.solver(
	    					testAgent, 
	    					doorToOpen, 
	    					e -> e.type.equals(LabEntity.SWITCH) && e.id.startsWith("b"), 
	    					S -> ((BeliefState) S) . isOpen(doorToOpen) , 
	    					Policy.NEAREST_TO_TARGET, 
	    					explorationBudget);

	        // define the testing-task:
	        var G = SEQ(
	        		mkScreenShot(imgDir,"RStart"),
	        		openWithSA1.apply("d0"),
	        		mkScreenShot(imgDir,"CStart"),
	        		openWithSA1.apply("d1"),
	        		mkScreenShot(imgDir,"XStart"),
	        		openWithSA1.apply("d3"),
	        		mkScreenShot(imgDir,"b4"),
	        		mkScreenShot(imgDir,"CTrap"),	
	        		mkScreenShot(imgDir,"CAfterTrap1"), 
	        		mkScreenShot(imgDir,"CAfterTrap2"), 
	        		openWithSA1.apply("dJS"),
	        		mkScreenShot(imgDir,"RJ"),
	        		GoalLib.entityInteracted("bFN0"),
	        		openWithSA1.apply("dJE"),
	        		mkScreenShot(imgDir,"BridgeW"),
	        		openWithSA1.apply("dAW"),
	        		mkScreenShot(imgDir,"RA"),
	        		GoalLib.entityInteracted("bFN1"),
	        		openWithSA1.apply("dAE"),
	        		mkScreenShot(imgDir,"BridgeE"),
	        		openWithSA1.apply("dshrine1"),
	        		mkScreenShot(imgDir,"shrine1"),
	        		GoalLib.entityInteracted("bFN2"),
	        		mkScreenShot(imgDir,"CPW"),
	        		openWithSA1.apply("dPW"),
	        		mkScreenShot(imgDir,"RP"),
	        		openWithSA1.apply("dPS"),
	        		mkScreenShot(imgDir,"CPS"),
	        		mkScreenShot(imgDir,"RG"),
	        		mkScreenShot(imgDir,"CFN"),
	        		// we can try this, but the agent won't survive going through the fire 3x:
	        		// openWithSA1.apply("dFN2"),
	        		mkScreenShot(imgDir,"bFC"),
	        		openWithSA1.apply("dFC"),
	        		GoalLib.atBGF("shrine2", 0.2f, true),
	        		mkScreenShot(imgDir,"CFinalN"),
	        		mkScreenShot(imgDir,"RFinal"),
	        		GoalLib.atBGF("Finish", 0.2f, true),
	           		mkScreenShot(imgDir,"Finish") 
	        );
	        
	        testAgent 
	        	. setTestDataCollector(new TestDataCollector()) 
	        	. setGoal(G) ;
	        
	        var time = System.currentTimeMillis() ;
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(20);
	            i++ ;
	        	testAgent.update();
	        	if (i>3500) {
	        		break ;
	        	}
	        }
	        time = System.currentTimeMillis() - time;
	        System.out.println("** # turns=" + i + ", exectime="+ time+ "ms") ;
	        
	        assertTrue(testAgent.success());
        }
        finally { environment.close(); }
    }
 
}
