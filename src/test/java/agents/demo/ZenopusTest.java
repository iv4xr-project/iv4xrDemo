package agents.demo;



import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.goalsAndTactics.Sa1Solver;
import eu.iv4xr.framework.goalsAndTactics.Sa1Solver.Policy;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.ObservationEvent.ScalarTracingEvent;
import eu.iv4xr.framework.spatial.Vec3;
import helperclasses.datastructures.linq.QArrayList;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.utils.Pair;

import static org.junit.jupiter.api.Assertions.* ;

import java.io.File;
import java.io.IOException;
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
    
    static String dataDir = System.getProperty("user.dir") 
			+ File.separator + "tmp"  
			+ File.separator + "zenopus" ;

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
    
    
    
    static boolean ENABLE_SCREENSHOOTING = false ;
    
    public static GoalStructure mkScreenShot(String imgDir, String id) {
    	if (ENABLE_SCREENSHOOTING)
    		return SEQ(GoalLib.atBGF(id, 0.6f,false), GoalLib.makeScreenShot(imgDir,id)) ;
    	else
    		return GoalLib.atBGF(id, 0.6f,false) ;
    }
    
    long startrunTime ;
    
    Pair<String,Number>[] instrumenter(LabRecruitsTestAgent agent, boolean measureResponseTime, SimpleState S) {
    	var S_ = (BeliefState) S ;
    	Pair<String,Number>[] data = new Pair[7] ;
    	var time2 = System.currentTimeMillis() ;
    	if (measureResponseTime) {
    		S_.env().observe(agent.getId()) ;
    		data[2] = new Pair<String,Number>("resptime",System.currentTimeMillis() - time2) ;
    	}
    	else {
    		data[2] = new Pair<String,Number>("resptime",-1) ;
    	}
    	data[0] = new Pair<String,Number>("timestamp", S_.worldmodel().  timestamp) ;
    	data[1] = new Pair<String,Number>("time", time2 - startrunTime) ;
    	var p = S_.worldmodel().getFloorPosition() ;

    	data[3] = new Pair<String,Number>("x", p.x) ;
    	data[4] = new Pair<String,Number>("y", p.y) ;
    	data[5] = new Pair<String,Number>("z", p.z) ;
    	
    	data[6] = new Pair<String,Number>("hp", S_.worldmodel().health) ;
    	
    	return data ;
    }
    
    void runAgent(LabRecruitsTestAgent agent, GoalStructure G, 
    		int delayBetweenUpdates,
    		boolean useInstrumenter,
    		boolean measureResponseTime,
    		String traceFileToSave,
    		int budget) 
    		throws InterruptedException, IOException 
    {
    	agent 
    	. setTestDataCollector(new TestDataCollector()) 
    	. setGoal(G) ;
    	
    	// deliberately not hooking the scalar-instrumenter this way, as
        // we need to do the sampling outside agent.update() ... because else
    	// the response-time measurement mess up with the LR-side
    	//
    	//if (useInstrumenter) 
    	//	agent.withScalarInstrumenter(S -> instrumenter(agent,measureResponseTime,S)) ;

    	startrunTime = System.currentTimeMillis() ;
    	int i = 0 ;
    	// keep updating the agent
    	while (G.getStatus().inProgress()) {
    		System.out.println("*** " + i + ", " + agent.state().id + " @" + agent.state().worldmodel.position) ;
    		agent.update();
    		i++ ;
    		Thread.sleep(delayBetweenUpdates);
    		if (useInstrumenter)  {
    			Pair<String,Number>[] properties = instrumenter(agent,measureResponseTime,agent.state()) ;
                agent.registerEvent(new ScalarTracingEvent(properties)) ;
    		}
    		if (i>budget) {
    			break ;
    		}
    	}
    	var time = System.currentTimeMillis() - startrunTime;
    	System.out.println("** # turns=" + i + ", exectime="+ time+ "ms") ;
    	if (traceFileToSave != null) {
    		String fname = dataDir + File.separator + traceFileToSave + ".csv" ;
    		System.out.println("** saving trace data to " + fname) ;
    		agent.getTestDataCollector().saveTestAgentScalarsTraceAsCSV(agent.getId(),fname);
    	}
    	
    }
    
    //@Test
    void debugTest() throws InterruptedException, IOException {

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
	        		GoalLib.entityInteracted("bPN"),  GoalLib.entityStateRefreshed2("dPN"),
	        		mkScreenShot(dataDir,"mazeE"),
	        		mkScreenShot(dataDir,"bN"),
	        		mkScreenShot(dataDir,"mazeW")
	        		) ;
	        
	        G = SEQ(mkScreenShot(dataDir,"CFinalN"),
	        		mkScreenShot(dataDir,"RFinal")) ;
	        
	        var useInstrumenter = true ;
	        var measureResponseTime = true ;
	        var traceFileToSave = "debugRun" ;
	        runAgent(testAgent,G,20,useInstrumenter,measureResponseTime,traceFileToSave,1000) ;
	        assertTrue(G.getStatus().success()) ;
        }
        finally { environment.close(); }
    }

   
    //@Test
    void test1() throws InterruptedException, IOException {

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
	        		mkScreenShot(dataDir,"RStart"),
	        		GoalLib.entityInteracted("b0"), GoalLib.entityStateRefreshed2("d0"),
	        		mkScreenShot(dataDir,"CStart"),
	        		GoalLib.entityInteracted("b1"), GoalLib.entityStateRefreshed2("d1"),
	        		mkScreenShot(dataDir,"XStart"),
	        		GoalLib.entityInteracted("b3"), GoalLib.entityStateRefreshed2("d3"),
	        		mkScreenShot(dataDir,"b4"),
	        		mkScreenShot(dataDir,"CTrap"),	
	        		mkScreenShot(dataDir,"CAfterTrap1"), 
	        		mkScreenShot(dataDir,"CAfterTrap2"), 
	        		GoalLib.entityInteracted("bJS"), GoalLib.entityStateRefreshed2("dJS"),
	        		mkScreenShot(dataDir,"RJ"), 
	        		GoalLib.entityInteracted("bFN0"),
	        		GoalLib.entityInteracted("bJE"),  GoalLib.entityStateRefreshed2("dJE"),
	        		mkScreenShot(dataDir,"BridgeW"),
	        		GoalLib.entityInteracted("bAW"),  GoalLib.entityStateRefreshed2("dAW"),
	        		mkScreenShot(dataDir,"RA"),
	        		GoalLib.entityInteracted("bFN1"),
	        		GoalLib.entityInteracted("bAE"),  GoalLib.entityStateRefreshed2("dAE"),
	        		mkScreenShot(dataDir,"BridgeE"),
	        		GoalLib.entityInteracted("bFN2"),
	        		GoalLib.entityInteracted("bshrine1"), GoalLib.entityStateRefreshed2("dshrine1"),
	        		mkScreenShot(dataDir,"shrine1"),
	        		mkScreenShot(dataDir,"CPW"),
	        		GoalLib.entityInteracted("bPW"), GoalLib.entityStateRefreshed2("dPW"),
	        		mkScreenShot(dataDir,"RP"),
	        		GoalLib.entityInteracted("bPS"), GoalLib.entityStateRefreshed2("dPS"),
	        		mkScreenShot(dataDir,"CPS"),
	        		mkScreenShot(dataDir,"RG"),
	        		mkScreenShot(dataDir,"CFN"),
	        		mkScreenShot(dataDir,"bFC"),
	        		GoalLib.entityInteracted("bFC"), GoalLib.entityStateRefreshed2("dFC"),
	        		GoalLib.atBGF("shrine2", 0.2f, true),
	        		mkScreenShot(dataDir,"RFinal"),
	        		GoalLib.atBGF("Finish", 0.2f, true),
	           		mkScreenShot(dataDir,"Finish")
	        );
	        
	        var useInstrumenter = true ;
	        var measureResponseTime = false ;
	        var traceFileToSave = "test1Run" ;
	        runAgent(testAgent,G,20,useInstrumenter,measureResponseTime,traceFileToSave,3500) ;
	        assertTrue(G.getStatus().success()) ;
        }
        finally { environment.close(); }
    }
    
    //@Test
    void test_shortScenario() throws InterruptedException {

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
        	
        	String agentId = "recruit" ;

	        // create a test agent
	        var testAgent = 
	        		new LabRecruitsTestAgent(agentId) 
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
	        		mkScreenShot(dataDir,"RStart"),
	        		openWithSA1.apply("d0"),
	        		mkScreenShot(dataDir,"CStart"),
	        		openWithSA1.apply("d1"),
	        		mkScreenShot(dataDir,"XStart"),
	        		openWithSA1.apply("d3"),
	        		mkScreenShot(dataDir,"b4"),
	        		mkScreenShot(dataDir,"CTrap"),	
	        		mkScreenShot(dataDir,"CAfterTrap1"), 
	        		mkScreenShot(dataDir,"CAfterTrap2"), 
	        		openWithSA1.apply("dJS"),
	        		mkScreenShot(dataDir,"RJ"),
	        		GoalLib.entityInteracted("bFN0"),
	        		openWithSA1.apply("dJE"),
	        		mkScreenShot(dataDir,"BridgeW"),
	        		openWithSA1.apply("dAW"),
	        		mkScreenShot(dataDir,"RA"),
	        		GoalLib.entityInteracted("bFN1"),
	        		openWithSA1.apply("dAE"),
	        		mkScreenShot(dataDir,"BridgeE"),
	        		openWithSA1.apply("dshrine1"),
	        		mkScreenShot(dataDir,"shrine1"),
	        		//GoalLib.entityInteracted("bFN2"),
	        		mkScreenShot(dataDir,"CPW"),
	        		openWithSA1.apply("dPW"),
	        		mkScreenShot(dataDir,"RP"),
	        		openWithSA1.apply("dPS"),
	        		mkScreenShot(dataDir,"CPS"),
	        		mkScreenShot(dataDir,"RG"),
	        		mkScreenShot(dataDir,"CFN"),
	        		// we can try this, but the agent won't survive going through the fire 3x:
	        		openWithSA1.apply("dFN2"),
	        		mkScreenShot(dataDir,"bFC"),
	        		openWithSA1.apply("dFC"),
	        		GoalLib.atBGF("shrine2", 0.2f, true),
	        		mkScreenShot(dataDir,"CFinalN"),
	        		mkScreenShot(dataDir,"RFinal"),
	        		GoalLib.atBGF("Finish", 0.2f, true),
	           		mkScreenShot(dataDir,"Finish") 
	        );
	        
	        testAgent 
	        	. setTestDataCollector(new TestDataCollector()) 
	        	. setGoal(G) ;
	        
	        var time = System.currentTimeMillis() ;
	        long minRespTime = Long.MAX_VALUE ;
	        long maxRespTime = 0 ;
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(20);
	            i++ ;
	        	testAgent.update();
	        	//var time2 = System.currentTimeMillis() ;
	        	//testAgent.state().env().observe(agentId) ;
	        	//long delta = System.currentTimeMillis() - time2 ;
	        	//minRespTime = delta < minRespTime ? delta : minRespTime ;
	        	//maxRespTime = delta > maxRespTime ? delta : maxRespTime ;
	        	
	        	if (i>3500) {
	        		break ;
	        	}
	        }
	        time = System.currentTimeMillis() - time;
	        System.out.println("** # turns=" + i + ", exectime="+ time+ "ms") ;
	        //System.out.println("     minResptime = " + minRespTime) ;
	        //System.out.println("     maxRespTime = " + maxRespTime) ;
	        
	        
	        assertTrue(testAgent.success());
        }
        finally { environment.close(); }
    }
 
}
