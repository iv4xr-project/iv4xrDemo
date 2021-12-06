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


public class DetectionOfChangedEntities_and_OpenDoorEvent_Test {

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

    void instrument(Environment env) {
    	env.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();
    }
    
    
    Map<String,Boolean> getButtonsDoorsState(BeliefState state) {
    	Map<String,Boolean> mystate = new HashMap<>() ;
    	for(var e : state.worldmodel.elements.values()) {
    		if (e.type.equals(LabEntity.DOOR)) {
    			mystate.put(e.id,state.isOpen(e.id)) ;
    		}
    		if (e.type.equals(LabEntity.SWITCH)) {
    			mystate.put(e.id,state.isOn(e.id)) ;
    		}
    	}
    	return mystate ;
    }
    
    
    

    /**
     * This test that the detection of changed-entities (entities that change state), in particular
     * doors and buttons, is correct. Doors and buttons are considered to change state when their
     * state of open/close or toggle-state changes. The field beliefstate.changedEntities can be 
     * queried to get a list of changed entities. 
     * 
     * In the same test we additionally also check if the eventProducer correctly produces
     * door-open event.
     */
    @Test
    public void changedEntitiesDetection_and_OpenDoorEvent_Test() throws InterruptedException {

        // Create an environment
    	var config = new LabRecruitsConfig("samira_8room") ;
    	var environment = new LabRecruitsEnvironment(config);
        
        try {
        	if(TestSettings.USE_GRAPHICS) {
        		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
        		new Scanner(System.in) . nextLine() ;
        	}

	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("agent1") // matches the ID in the CSV file
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);
	        
	        var eventProducer = new EventsProducer() ;
	        eventProducer.attachTestAgent(testAgent) ;
	        

	        // define the testing-task:
	        var testingTask = SEQ(
	            GoalLib.entityInteracted("button3"), GoalLib.entityStateRefreshed("door1"),
	            GoalLib.entityInteracted("button7"), GoalLib.entityStateRefreshed("door4"),
	            GoalLib.entityInteracted("button9"), GoalLib.entityStateRefreshed("door5"),
	            GoalLib.entityInteracted("button16"), GoalLib.entityStateRefreshed("door7"),
	            GoalLib.entityInteracted("button17"), GoalLib.entityStateRefreshed("door9"),
	            GoalLib.entityInteracted("button24"), GoalLib.entityStateRefreshed("door11"),
	            GoalLib.entityInteracted("button27"), GoalLib.entityStateRefreshed("door14"),
	            GoalLib.entityInteracted("button32"), GoalLib.entityStateRefreshed("door16"),
	            GoalLib.entityInCloseRange("treasure") 
	        );
	        
	        testAgent.setGoal(testingTask) ;

	        environment.startSimulation(); // this will press the "Play" button in the game for you
	        //goal not achieved yet
	        assertFalse(testAgent.success());
	        
	        // do one update and explicitly check reachability between these two points (which was
	        // unreachable before fixed):
	        testAgent.update() ;
	        var buttonsAndDoorsState0 = getButtonsDoorsState(testAgent.getState()) ;
	        System.out.println(">>> #buttons-and-doors seen intially: " + buttonsAndDoorsState0.size()) ;
	        System.out.println(">>> #changed entities after first update: " + testAgent.getState().changedEntities.size()) ;
	        
	        // check:
	        for(var eid : buttonsAndDoorsState0.keySet()) {
	        	assertTrue(testAgent.getState().changedEntities.stream().anyMatch(d -> d.id.equals(eid))) ;
	        }
	        for(var e : testAgent.getState().changedEntities) {
	        	assertTrue(buttonsAndDoorsState0.keySet().contains(e.id)) ;
	        }
	        

	        int i = 1 ;
	        // keep updating the agent
	        while (testingTask.getStatus().inProgress()) {
	        	buttonsAndDoorsState0 = getButtonsDoorsState(testAgent.getState()) ;
	        	System.out.println("*** update nr " + i + ", " + testAgent.getState().id + " @" + testAgent.getState().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	eventProducer.generateCurrentEvents() ;
	        	
	        	// figure out which buttons/doors are new or changed state. Also signal if
	        	// one door becomes open:
	        	
	        	var buttonsAndDoorsState1 = getButtonsDoorsState(testAgent.getState()) ;
	        	List<String> changed = new LinkedList<>() ;
	        	boolean thereIs_one_door_that_becomes_open = false ;
	        	for(var e : buttonsAndDoorsState1.entrySet()) {
	        		String e_id = e.getKey() ;
	        		if(!buttonsAndDoorsState0.keySet().contains(e_id)) {
	        			changed.add(e_id) ;
	        			var f = testAgent.getState().worldmodel.getElement(e_id) ;
	        			if(f.type.equals(LabEntity.DOOR) && e.getValue().equals(true)) {
	        				thereIs_one_door_that_becomes_open = true ;
	        			}
	        		}
	        		else {
	        			Boolean v1 = e.getValue() ;
	        			Boolean v0 = buttonsAndDoorsState0.get(e_id) ;
	        			if (! v0.equals(v1)) {
	        				changed.add(e_id) ;
	        				var f = testAgent.getState().worldmodel.getElement(e_id) ;
		        			if(f.type.equals(LabEntity.DOOR) && e.getValue().equals(true)) {
		        				thereIs_one_door_that_becomes_open = true ;
		        			}
	        			}
	        		}
	        	}
	        	if (!changed.isEmpty()) {
	        		System.out.println(">>> new entities seen, or change state ...") ;
	        	}
	        	// now check:
	        	for(var eid : changed) {
		        	assertTrue(testAgent.getState().changedEntities.stream().anyMatch(d -> d.id.equals(eid))) ;
		        }
		        for(var e : testAgent.getState().changedEntities) {
		        	assertTrue(changed.contains(e.id)) ;
		        }
		        // check that open-door-event is produced
		        if(thereIs_one_door_that_becomes_open) {
		        	System.out.println(">>> a door becomes open ...") ;
		        }
		        assertTrue(thereIs_one_door_that_becomes_open 
		        		== 
		        		eventProducer.currentEvents.stream().anyMatch(event -> EventsProducer.isOpeningADoorEvent(event))) ;
		        
	        	
	        	
	        	if (i>1000) {
	        		break ;
	        	}
	        }
	        testingTask.printGoalStructureStatus();

	        assertTrue(testAgent.success());
	        BeliefState belief = testAgent.getState() ;
	        LabEntity treasure = (LabEntity) belief.worldmodel.getElement("treasure") ;
	        assertTrue(Vec3.dist(belief.worldmodel().getFloorPosition(), treasure.getFloorPosition()) <= 1.2)  ;
	        // close
	        testAgent.printStatus();
        }
        finally { environment.close(); }
    }
}
