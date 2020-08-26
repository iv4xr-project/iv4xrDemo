/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package eu.agents.level.iv4xr;



import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.demo.FireHazardAgentDirect;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.world.WorldEntity;
import helperclasses.datastructures.linq.QArrayList;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.GoalsCombinator;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;

import static org.junit.jupiter.api.Assertions.* ;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;
import world.LegacyEntity;
import world.LegacyInteractiveEntity;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;

/**
 * A simple test to demonstrate using iv4xr agents to test the Lab Recruits game.
 * The testing task is to verify that the closet in the east is reachable from
 * the player initial position, which it is if the door guarding it can be opened.
 * This in turn requires a series of switches and other doors to be opened.
 * 
 * ISSUE to be solved!!
 *    * When the agent believes that a door is closed, it will refuse to navigate
 *      to it (because it is not reachable according to its nav-map).
 *      We need instead navigate to a position close to it to observe it.
 *    * Agent can get stuck in a bending corner!
 */
public class LevelTestSmarterAgent {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	// Uncomment this to make the game's graphic visible:
    	TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); }
    
    void instrument(Environment env) {
    	env.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();
    }

    /**
     * A test to verify that the east closet is reachable.
     */
    @Test
    public void closetReachableTest() throws InterruptedException {
    	

        // Create an environment
        var environment = new LabRecruitsEnvironment(new EnvironmentConfig("GameLevel2_2020_08_21_17.54.52",Platform.LEVEL_PATH+"\\GameLevel2"));
        if(USE_INSTRUMENT) instrument(environment) ;

        try {
        	if(TestSettings.USE_GRAPHICS) {
        		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
        		new Scanner(System.in). nextLine() ;
        	}
        	var beliefState = new BeliefState();
	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("agent1") // matches the ID in the CSV file
        		    . attachState(beliefState)
        		    . attachEnvironment(environment);
	
	         
	     // define the testing-task:
	       
	    	
	    	//Function<Integer,GoalStructure> fgoal1 = i  -> GoalLib.doorIsInRange_smarter("door1");
	    	
	    	//var gf = fgoal1.apply(0) ;
//it works for the first evel	    	
//	    	var testingTask = SEQ(
//	    			//Checking the door of room one, if it can not be open by the set button try to find another button
//	    			SEQ(
//		    			GoalLib.entityIsInteracted("button2"),
//		    			GoalLib.doorIsInRange_smarter("door1"),
//		    			// try to find new button which can open the door of the room one 
//		    			FIRSTof(GoalLib.checkDoorState("door1", (WorldEntity e) -> e.getBooleanProperty("isOpen")),REPEAT(SEQ(REPEAT(FIRSTof(GoalLib.interactWithNextButton(testAgent))),SEQ(GoalLib.navigate_toNearestNode_toDoor("door1"),
//	    					GoalLib.entityIsInRange("door1").lift()),GoalLib.checkDoorState("door1", (WorldEntity e) -> e.getBooleanProperty("isOpen")))))
//	    			),
//	    			SEQ(
//		    			GoalLib.entityIsInteracted("button5"),
//		    			GoalLib.doorIsInRange_smarter("door2"),
//		    			FIRSTof(
//		    					GoalLib.checkDoorState("door2", (WorldEntity e) -> e.getBooleanProperty("isOpen")),
//		    					REPEAT(SEQ(REPEAT(FIRSTof(GoalLib.interactWithNextButton(testAgent))),SEQ(GoalLib.navigate_toNearestNode_toDoor("door2"),
//		    					GoalLib.entityIsInRange("door2").lift()),GoalLib.checkDoorState("door2", (WorldEntity e) -> e.getBooleanProperty("isOpen"))))
//		    					)
//	    			)
//	    					);
	    	
	    	
	    	var testingTask = SEQ(
	    			//Checking the door of room one, if it can not be open by the set button try to find another button
	    			SEQ(
		    			GoalLib.entityInteracted("button3"),
		    			GoalLib.doorIsInRange_smarter("door1"),
		    			// try to find new button which can open the door of the room one 
		    			FIRSTof(GoalLib.checkDoorState("door1", (WorldEntity e) -> e.getBooleanProperty("isOpen")),REPEAT(SEQ(REPEAT(FIRSTof(GoalLib.interactWithNextButton(testAgent))),GoalLib.doorIsInRange_smarter("door1"),GoalLib.checkDoorState("door1", (WorldEntity e) -> e.getBooleanProperty("isOpen")))))
	    			),
	    			
	    			
	    			SEQ(
		    			GoalLib.entityInteracted("button7"),
		    			GoalLib.entityIsInRange("door2").lift(),
		    			FIRSTof(
		    					GoalLib.checkDoorState("door2", (WorldEntity e) -> e.getBooleanProperty("isOpen")),
		    					REPEAT(SEQ(REPEAT(FIRSTof(GoalLib.interactWithNextButton(testAgent))),GoalLib.doorIsInRange_smarter("door1"),GoalLib.checkDoorState("door2", (WorldEntity e) -> e.getBooleanProperty("isOpen"))))
		    					)
	    			)
	    					);
	    	//var testingTask = REPEAT(SEQ(REPEAT(FIRSTof(goal3)),goal1,goal2));

 						
	     
	        		
	       //var testingTask = SEQ(GoalLib.testAddAfter(),goal3);
	       //var testingTask = REPEAT(SEQ(REPEAT(FIRSTof(goal3)),goal1,goal2));
	    	//var testingTask = FIRSTof(goal2);
	    	// attaching the goal and test data-collector
	        var dataCollector = new TestDataCollector();
	        testAgent . setTestDataCollector(dataCollector) . setGoal(testingTask) ;
	        //testAgent.setGoal(testingTask);
	        
	        environment.startSimulation(); // this will press the "Play" button in the game for you
	        //goal not achieved yet
	        assertFalse(testAgent.success());
	
	      

	        int i = 0 ;
	        // keep updating the agent
	        
	        while (testingTask.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.getState().id + " @" + testAgent.getState().worldmodel.position) ;
	            Thread.sleep(50);
	            
//				for(int j=0; j<testingTask.getSubgoals().size();j++) {
//					System.out.print(testingTask.getSubgoals().get(j)) ;
//				}

	            i++ ; 
	        	testAgent.update();
	        	
	        	//System.out.print(beliefState.isStuck());
//	        	System.out.print(beliefState.hashCode());
	        	//System.out.print("knowndoors");
	        	//System.out.print(beliefState.knownButtons_sortedByAgeAndDistance().toString());
	        	//System.out.print(beliefState.isOn("button2"));
	        	
	        	//System.out.print(beliefState.worldmodel.);
	        	//System.out.print(beliefState);

	        	
	        	
	        	if (i>200) {
	        		break ;
	        	}
	        }
	        testingTask.printGoalStructureStatus();
	
	        // check that we have passed both tests above:
	        assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 1) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        // close
	        testAgent.printStatus();
        }
        finally { environment.close(); }
    }




}

