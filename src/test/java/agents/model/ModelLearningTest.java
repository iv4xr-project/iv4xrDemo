/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents.model;



import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.CoverterDot;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GWState;
import eu.iv4xr.framework.extensions.ltl.gameworldmodel.GameWorldModel;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import helperclasses.datastructures.linq.QArrayList;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.MyAgentState;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import static org.junit.jupiter.api.Assertions.* ;

import java.io.IOException;
import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonIOException;

import game.Platform;
import game.LabRecruitsTestServer;
import world.BehaviorModelLearner;
import world.BeliefState;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;

/**
 * A simple test to demonstrate using iv4xr agents to test the Lab Recruits game.
 * The testing task is to verify that the closet in the east is reachable from
 * the player initial position, which it is if the door guarding it can be opened.
 * This in turn requires a series of switches and other doors to be opened.
 */
public class ModelLearningTest {

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

    /**
     * A test to verify that the east closet is reachable.
     * @throws IOException 
     * @throws JsonIOException 
     */
    //@Test
    public void test_withbuttondoors1() throws InterruptedException, JsonIOException, IOException {

    	var buttonToTest = "button1" ;
    	var doorToTest = "door1" ;

        // Create an environment
    	var config = new LabRecruitsConfig("buttons_doors_1") ;
    	config.light_intensity = 0.3f ;
    	var environment = new LabRecruitsEnvironment(config);
    

        try {
        	TestSettings.youCanRepositionWindow() ;

	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("agent1") // matches the ID in the CSV file
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

	        // define the testing-task:
	        var testingTask = SEQ(
	            GoalLib.entityInteracted("button1"),
                GoalLib.entityStateRefreshed("door1"),
	        	GoalLib.entityInteracted("button3"),
	        	GoalLib.entityStateRefreshed("door2"),
	        	GoalLib.entityInteracted("button4"),
	        	//GoalLib.entityIsInRange("button3").lift(),
	        	//GoalLib.entityIsInRange("door1").lift(),
	        	GoalLib.entityStateRefreshed("door1"),
	        	GoalLib.entityStateRefreshed("door3"),
	        	GoalLib.entityInCloseRange("door3")
	        );
	        
	        testAgent.setGoal(testingTask) ;
	        
	        GameWorldModel model = new GameWorldModel(new GWState()) ;
	        BehaviorModelLearner learner = new BehaviorModelLearner() ;
	        testAgent.attachBehaviorModel(model, (S,m) -> {
	        	learner.learn((BeliefState) S, (GameWorldModel) m) ;
				return null ;
				}) ;
	        
	        //environment.startSimulation(); // this will press the "Play" button in the game for you
	        //goal not achieved yet
	        

	        int i = 0 ;
	        // keep updating the agent
	        while (testingTask.getStatus().inProgress()) {
	        	System.out.println("*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position) ;
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>200) {
	        		break ;
	        	}
	        }

	        assertTrue(testAgent.success());
	        
	        
	        learner.recalculateZones(testAgent.getState(), model);
	        model.defaultInitialState.currentAgentLocation = "START" + testAgent.getId() ;
			model.copyDefaultInitialStateToInitialState();
			model.name = "ButtonDoors1_learned" ;
			model.save("./tmp/ButtonDoors1_learned.json");
			CoverterDot.saveAs("./tmp/ButtonDoors1_learned.dot",model,true,true) ;

        }
        finally { environment.close(); }
    }
    
    GoalStructure open_and_check_doors(LabRecruitsTestAgent testAgent, String activatingButton, String... doors) {

        GoalStructure[] subgoals = new GoalStructure[1 + doors.length];
        subgoals[0] = GoalLib.entityInteracted(activatingButton);
        for (int k = 0; k < doors.length; k++) {
            String doorId = doors[k];
            subgoals[k+1] = GoalLib.entityStateRefreshed(doorId);
        }
        return SEQ(subgoals);
    }
    
    @Test
    public void test_withLab1() throws InterruptedException, JsonIOException, IOException {
    	
    	// Create an environment
        var config = new LabRecruitsConfig("lab1");
        config.light_intensity = 0.3f;
        var environment = new LabRecruitsEnvironment(config);

        try {
        	TestSettings.youCanRepositionWindow() ;

            // create a test agent
            var testAgent = new LabRecruitsTestAgent("agent0") // matches the ID in the CSV file
                    .attachState(new BeliefState()).attachEnvironment(environment);

            // define the testing-task:
            var testingTask = SEQ(open_and_check_doors(testAgent, "b_hall_1", "d_store_e"),
                    open_and_check_doors(testAgent, "b_store", "d_store_n", "d_sidehall"),
                    GoalLib.entityInteracted("b_secret_1"),
                    open_and_check_doors(testAgent, "b_side", "d_sidehall", "d_lab_w", "d_bcroom"),
                    open_and_check_doors(testAgent, "b_secret_2", "d_closet"),
                    open_and_check_doors(testAgent, "b_closet", "d_theater_s", "d_theater_e"),
                    open_and_check_doors(testAgent, "b_lab_e", "d_tofinish"),
                    open_and_check_doors(testAgent, "b_finish", "d_finish"), GoalLib.entityInCloseRange("finish")

            );
            
            testAgent.setGoal(testingTask) ;
	        
	        GameWorldModel model = new GameWorldModel(new GWState()) ;
	        BehaviorModelLearner learner = new BehaviorModelLearner() ;
	        testAgent.attachBehaviorModel(model, (S,m) -> {
	        	learner.learn((BeliefState) S, (GameWorldModel) m) ;
				return null ;
				}) ;
	        
            int i = 0;
            testAgent.update();
            System.out.println("==== " + testAgent.getState().worldmodel().getElement("doorX")) ;
            System.out.println("==== check here") ;
            // keep updating the agent
            while (testingTask.getStatus().inProgress()) {
                System.out.println(
                        "*** " + i + ", " + testAgent.state().id + " @" + testAgent.state().worldmodel.position);
                Thread.sleep(50);
                i++;
                testAgent.update();
                if (i > 800) {
                    break;
                }
            }
            assertTrue(testAgent.success());
            
            learner.recalculateZones(testAgent.getState(), model);
	        model.defaultInitialState.currentAgentLocation = "START" + testAgent.getId() ;
			model.copyDefaultInitialStateToInitialState();
			model.name = "Lab1_learned" ;
			model.save("./tmp/Lab1_learned.json");
			CoverterDot.saveAs("./tmp/Lab1_learned.dot",model,true,true) ;

        } finally {
            environment.close();
        }
    }
}
