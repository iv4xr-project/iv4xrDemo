/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package mbtTranslation;



import agents.LabRecruitsTestAgent;
import agents.TestSettings;
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


import eu.fbk.iv4xr.mbt.mbtTranslator;
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
public class mbtTest {

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
        var environment = new LabRecruitsEnvironment(new EnvironmentConfig("buttons_doors_1 - Copy",Platform.LEVEL_PATH));
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
	        
	        //get sequence of transitions
	        mbtTranslator mbt = new mbtTranslator();
	        var dighraph = mbt.singleTestCase();
	        
	        // define the testing-task:
	   
	        /*Automatically creating testing task based on the CSV files*/ 
	       LinkedList<GoalStructure> list=new LinkedList<GoalStructure>();
	       int j = 0;
	       
	       
	       
	       ArrayList<String[]> dighraphsArr = new ArrayList<>();	       
	       dighraphsArr.add(new String[]{"b_2","d_1_m","EXPLORE"});
	       dighraphsArr.add(new String[]{"d_1_m","b_1","EXPLORE"});
	       dighraphsArr.add(new String[]{"b_1","d_T_m","EXPLORE"});
	       dighraphsArr.add(new String[]{"d_T_m","d_T_p","EXPLORE"});
	       dighraphsArr.add(new String[]{"d_T_p","d_T_m","EXPLORE"});
	       dighraphsArr.add(new String[]{"d_T_m","d_1_m","TOGGLE"});
	       dighraphsArr.add(new String[]{"d_1_m","b_1","EXPLORE"});
	       dighraphsArr.add(new String[]{"b_1","b_2","EXPLORE"});
	       
	       dighraphsArr.add(new String[]{"d_T_m","b_1","EXPLORE"});
	       dighraphsArr.add(new String[]{"d_T_m","b_1","EXPLORE"});
	       dighraphsArr.add(new String[]{"d_T_m","b_1","EXPLORE"});
	       dighraphsArr.add(new String[]{"d_T_m","b_1","EXPLORE"});
	       dighraphsArr.add(new String[]{"d_T_m","b_1","EXPLORE"});
	       dighraphsArr.add(new String[]{"d_T_m","b_1","EXPLORE"});
	       dighraphsArr.add(new String[]{"d_T_m","b_1","EXPLORE"});
	       dighraphsArr.add(new String[]{"d_T_m","b_1","EXPLORE"});
	       
	        GoalStructure[] subGoals = new GoalStructure[dighraph.get(0).length*4];
	       //ArrayList<GoalStructure[]> subGoals = new ArrayList<>(); 
	       int NumberOfPassVerdicts = 0;
	        for(int i=0; i<dighraph.size(); i++) {
	        	for(int k=0; k< dighraph.get(i).length; k++) {
        		var state1 = dighraph.get(i)[k][0];        	
	        	var state2 = dighraph.get(i)[k][1];   
	        	var actionToDo = dighraph.get(i)[k][2];
	        	var state1ID = (state1.contains("p") || state1.contains("m")) ? state1.substring(0, state1.length()-2) :state1;
	        	var state2ID = (state2.contains("p") || state2.contains("m")) ? state2.substring(0, state2.length()-2) :state2;
	        	if(j == 0) {
		        	subGoals[j] = GoalLib.entityStateRefreshed(state1ID);
		        	j++;
		        	subGoals[j] = GoalLib.entityInCloseRange(state1ID);
		        	j++;
	        	}
	        	if(actionToDo == "EXPLORE") {
	        		if((state1.contains("p") || state1.contains("m")) && (state2.contains("p") || state2.contains("m"))) {
	        			if(dighraph.get(i)[k-1][1].equals(dighraph.get(i)[k][0]) && dighraph.get(i)[k-1][0].equals(dighraph.get(i)[k][1]))
	        			continue;
	        			NumberOfPassVerdicts++;
	        			subGoals[j] = GoalLib.checkDoorState(state1ID);
	        			j++;
		        		subGoals[j] = GoalLib.entityInvariantChecked(testAgent, state1ID, state1ID+" should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"));
	        		}else {
	        		subGoals[j] = GoalLib.entityStateRefreshed(state2ID);
	        		j++;
		        	subGoals[j] = GoalLib.entityInCloseRange(state2ID);
	        		}
	        	}
	        	if(actionToDo == "TOGGLE") {
	        		subGoals[j] = GoalLib.entityInteracted(state1ID);
	        	}
	        	j++;       
	        	} 	
	        }
	        
	        GoalStructure[] finalSubGoals = new GoalStructure[j];
	        for(int i=0;i< subGoals.length;i++) {
	        	if(subGoals[i] != null)
	        	finalSubGoals[i] = subGoals[i];
	        }
	        
	        var testingTask = SEQ(finalSubGoals);
	        


	        // attaching the goal and test data-collector
	        var dataCollector = new TestDataCollector();
	        testAgent . setTestDataCollector(dataCollector) . setGoal(testingTask) ;
	
	        
	        environment.startSimulation(); // this will press the "Play" button in the game for you
	        //goal not achieved yet
	        assertFalse(testAgent.success());
	
	      

	        int i = 0 ;
	        // keep updating the agent
	       
	        while (testingTask.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.getState().id + " @" + testAgent.getState().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ; 
	        	testAgent.update();               
	        	if (i>400) {
	        		break ;
	        	}
	        } 
	        
	        
	        testingTask.printGoalStructureStatus();
	        //Print result
	        System.out.println("******FINAL RESULT******"); 
	        if(testAgent.getTestDataCollector().getNumberOfPassVerdictsSeen() == NumberOfPassVerdicts) {
	    	   System.out.println("Goal successfully acheived");
	        }else {
	    	  System.out.println("Goal failed, " + " the total number of door has checked is: " + NumberOfPassVerdicts);
	        }
	        // check that we have passed both tests above:
	        assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == NumberOfPassVerdicts) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        // close
	       // testAgent.printStatus();
        }
        finally { environment.close(); }
    }




}

