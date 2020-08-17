/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package eu.agents.level.iv4xr;



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
public class LevelTest {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	// Uncomment this to make the game's graphic visible:
    	TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

	/*Get connection between buttons and doors*/
    public List<List<String>> getData() {	
		//String csvFile = "E:\\Ph.D\\iv4xrDemo2-mutation\\src\\test\\resources\\levels\\GameLevel3\\GameLevel3_2020_07_30_09.41.26.csv";
    	//String csvFile = "E:\\Ph.D\\iv4xrDemo2-mutation\\src\\test\\resources\\levels\\GameLevel2_2020_08_03_16.59.20.csv";
    	String csvFile = Platform.LEVEL_PATH +"\\GameLevel3\\GameLevel3_2020_08_06_09.56.16.csv";
    	List<List<String>> records = new ArrayList<>();
		String delimiter = ",";
		try {
	         File file = new File(csvFile);
	         Scanner sc = new Scanner(file);
	         String line = "";
	         boolean isValid = true;
	         String[] tempArr;
	         while(sc.hasNext() && isValid) {
	            tempArr = sc.next().split(delimiter);
	            List<String> values = new ArrayList<String>();
	            for(String tempStr : tempArr) {
	            	if(!tempStr.endsWith("|w")) {
	            		values.add(tempStr);
	            	}else {
	            		isValid = false;
	            		break;
	            	}     	
	              // System.out.print(tempStr + " ");
	            }
	            if(!values.isEmpty())
	        	 records.add(values);
	         }
	        
	         } catch(IOException ioe) {
	            ioe.printStackTrace();
	         }
		return records;
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
        var environment = new LabRecruitsEnvironment(new EnvironmentConfig("GameLevel3_2020_08_06_09.56.16",Platform.LEVEL_PATH+"\\GameLevel3"));
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

	        /*Automatically creating testing task based on the CSV files*/ 
	       LinkedList<GoalStructure> list=new LinkedList<GoalStructure>();
	       int j = 0;
	       
	        GoalStructure[] subGoals = new GoalStructure[getData().size()*3];
	        int NumberOfPassVerdicts = 0;
	        for(int i=0; i<getData().size(); i++) {
	        	NumberOfPassVerdicts++;
	    		var buttonToTest = getData().get(i).get(0)  ;
	        	var doorToTest = getData().get(i).get(1) ;        	
	        	subGoals[j] = GoalLib.entityIsInteracted(buttonToTest);
	        	subGoals[j+1] = GoalLib.entityIsInRange(doorToTest).lift();
	        	subGoals[j+2] = GoalLib.entityInvariantChecked(testAgent,
	        			doorToTest, 
	            		doorToTest+"should be open", 
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen"));
	        	j= j+3;        	

	        }
	        var testingTask = SEQ(subGoals);
	        
	  

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
                System.out.println(beliefState);
	        	if (i>400) {
	        		break ;
	        	}
	        }
	        testingTask.printGoalStructureStatus();
	
	        // check that we have passed both tests above:
	        assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == NumberOfPassVerdicts) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        // close
	        testAgent.printStatus();
        }
        finally { environment.close(); }
    }




}

