package agents;



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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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


class Fire_andMonsters_sighted_Event_Test {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	// TestSettings.USE_SERVER_FOR_TEST = false ;
    	// Uncomment this to make the game's graphic visible:
    	// TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); }
  
    @Test
    void fireEventTest() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("simple_withFireAnd Flag") ;
    	var environment = new LabRecruitsEnvironment(config);
        
        try {
        	TestSettings.youCanRepositionWindow() ;

	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("agent0") // matches the ID in the CSV file
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment)
        		    . attachSyntheticEventsProducer(new EventsProducer()) ;
	        

	        var G = GoalLib.atBGF("b0", 0.5f, false) ;
	        
	        testAgent.setGoal(G) ;

	        environment.startSimulation(); // this will press the "Play" button in the game for you
	 
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** update nr " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            testAgent.update();
	            i++ ;	        	
	        	if (i>1000) {
	        		break ;
	        	}
	        }
	        
	        assertTrue(testAgent.success());
	        
	        System.out.println("** Event-trace:" + testAgent.getSyntheticEventsProducer().showTrace()) ;
	        
	        var eventstrace = testAgent.getSyntheticEventsProducer().trace ;
	        
	        assertTrue(eventstrace.stream().filter(m -> EventsProducer.isFireInSightEvent(m)).count() > 5) ;
	        
	        assertTrue(eventstrace.stream().filter(m -> 
	        	EventsProducer.isFireInSightEvent(m) && m.getArgs().length > 0 && m.getArgs()[0].equals(10)
	        	)
	        	.count() > 0) ;

        }
        finally { environment.close(); }
    }
    
    @Test
    void monsterEventTest() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("square_withEnemies") ;
    	config.view_distance = 5 ;
    	var environment = new LabRecruitsEnvironment(config);
        
        try {
        	TestSettings.youCanRepositionWindow() ;

	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("agent0") // matches the ID in the CSV file
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment)
        		    . attachSyntheticEventsProducer(new EventsProducer()) ;
	        

	        var G = GoalLib.atBGF("Finish", 0.5f, false) ;
	        
	        testAgent.setGoal(G) ;

	        environment.startSimulation(); // this will press the "Play" button in the game for you
	 
	        int i = 0 ;
	        // keep updating the agent
	        while (G.getStatus().inProgress()) {
	        	System.out.println("*** update nr " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            testAgent.update();
	            i++ ;	        	
	        	if (i>1000) {
	        		break ;
	        	}
	        }
	        
	        assertTrue(testAgent.success());
	        
	        System.out.println("** Event-trace:" + testAgent.getSyntheticEventsProducer().showTrace()) ;
	        
	        var eventstrace = testAgent.getSyntheticEventsProducer().trace ;
	        
	        assertTrue(eventstrace.stream().filter(m -> EventsProducer.isMonstersInSightEvent(m)).count() >= 2) ;
	        
	        assertTrue(eventstrace.stream().filter(m -> 
	        	EventsProducer.isMonstersInSightEvent(m) && m.getArgs().length > 0 && m.getArgs()[0].equals(2)
	        	)
	        	.count() > 0) ;

        }
        finally { environment.close(); }
    }
}
