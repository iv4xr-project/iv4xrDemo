/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents.demo;



import agents.EventsProducer;
import agents.LabRecruitsTestAgent;
import agents.PlayerOneCharacterization;
import static agents.PlayerOneCharacterization.*;

import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.extensions.occ.Emotion;
import eu.iv4xr.framework.extensions.occ.EmotionAppraisalSystem;
import eu.iv4xr.framework.extensions.occ.Event;
import eu.iv4xr.framework.extensions.occ.Emotion.EmotionType;
import eu.iv4xr.framework.extensions.occ.Event.Tick;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import helperclasses.datastructures.linq.QArrayList;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import static org.junit.jupiter.api.Assertions.* ;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import game.Platform;
import game.LabRecruitsTestServer;
import world.BeliefState;
import static helperclasses.GraphPlotter.* ;
import static helperclasses.CSVExport.* ;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;

/**
 * A simple test to demonstrate using iv4xr agents to test the Lab Recruits game.
 * The testing task is to verify that the closet in the east is reachable from
 * the player initial position, which it is if the door guarding it can be opened.
 * This in turn requires a series of switches and other doors to be opened.
 */
public class RoomReachabilityTest {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	// TestSettings.USE_SERVER_FOR_TEST = false ;
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
     * @throws IOException 
     */
    @Test
    public void closetReachableTest() throws InterruptedException, IOException {

    	var buttonToTest = "button1" ;
    	var doorToTest = "door1" ;

        // Create an environment
    	var config = new LabRecruitsConfig("buttons_doors_1_setup2") ;
    	config.light_intensity = 0.45f ;
    	var environment = new LabRecruitsEnvironment(config);
        if(USE_INSTRUMENT) instrument(environment) ;

        try {
        	if(TestSettings.USE_GRAPHICS) {
        		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
        		new Scanner(System.in) . nextLine() ;
        	}

	        // create a test agent
	        var testAgent = new LabRecruitsTestAgent("agent1") // matches the ID in the CSV file
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

	        // define the testing-task:
	        var testingTask = SEQ(
	            GoalLib.entityInteracted("button1"),
                GoalLib.entityStateRefreshed("door1"),
	        	GoalLib.entityInvariantChecked(testAgent,
	            		"door1",
	            		"door1 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),

	        	GoalLib.entityInteracted("button3"),
	        	GoalLib.entityStateRefreshed("door2"),
	        	GoalLib.entityInvariantChecked(testAgent,
	            		"door2",
	            		"door2 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
	        	GoalLib.entityInteracted("button4"),
	        	//GoalLib.entityIsInRange("button3").lift(),
	        	//GoalLib.entityIsInRange("door1").lift(),
	        	GoalLib.entityStateRefreshed("door1"),
	        	GoalLib.entityInvariantChecked(testAgent,
	            		"door1",
	            		"door1 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
	        	//GoalLib.entityIsInRange("button1").lift(),
	        	GoalLib.entityStateRefreshed("door3"),
	        	GoalLib.entityInvariantChecked(testAgent,
	            		"door3",
	            		"door3 should be open",
	            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
	        	GoalLib.entityInCloseRange("door3"),
	        	GoalLib.positionsVisited(new Vec3(11.3f,0,4f))
	        );
	        // attaching the goal and testdata-collector
	        var dataCollector = new TestDataCollector();
	        testAgent . setTestDataCollector(dataCollector) . setGoal(testingTask) ;

	        // add an event-producer to the test agent so that it produce events for
	        // emotion appraisals:
	        EventsProducer eventsProducer = new EventsProducer() .attachTestAgent(testAgent) ;
	        
	        // Create an emotion appraiser, and hook it to the agent:
	        EmotionAppraisalSystem eas = new EmotionAppraisalSystem(testAgent.getId()) ;
	        eas. attachEmotionBeliefBase(new EmotionBeliefBase() .attachFunctionalState(testAgent.getState())) 
	           . withUserModel(new PlayerOneCharacterization()) 
	           . addGoal(questIsCompleted,50)
	           . addGoal(gotAsMuchPointsAsPossible,50) 
	           . addInitialEmotions() ;
	        
	        // some lists for collecting experiment data:     
	        List<String[]> csvData_goalQuestIsCompleted = new LinkedList<>() ;
	        String[] csvRow = { "t", "x", "y", "hope", "joy", "satisfaction", "fear" } ;
	        csvData_goalQuestIsCompleted.add(csvRow) ;
	        List<String[]> csvData_goalGetMuchPoints = new LinkedList<>() ;
	        csvData_goalGetMuchPoints.add(csvRow) ;
	        Function<Emotion,Float> normalizeIntensity = e -> e!=null ? (float) e.intensity / 800f : 0f ;
	        
	        environment.startSimulation(); // this will press the "Play" button in the game for you
	        //goal not achieved yet
	        assertFalse(testAgent.success());

	        int i = 0 ;
	        // keep updating the agent
	        while (testingTask.getStatus().inProgress()) {
	        	Vec3 position = testAgent.getState().worldmodel.position ;
	        	System.out.println("*** " + i + ", " + testAgent.getState().id + " @" + position) ;
	        	eventsProducer.generateCurrentEvents();
	        	if(eventsProducer.currentEvents.isEmpty()) eventsProducer.currentEvents.add(new Tick()) ;
	        	
	        	for(Event e : eventsProducer.currentEvents) {
	        		eas.update(e, i);
	        	}
	        	if (position != null) {
	        		Vec3 p_ = position.copy() ;
	        	    p_.z = 8- p_.z ;
	        		Float score = (float) testAgent.getState().worldmodel.score ;
	        		System.out.println("*** score=" + score) ;

	        		float hope_completingQuest = normalizeIntensity.apply(eas.getEmotion(questIsCompleted.name,EmotionType.Hope)) ;
	        		float joy_completingQuest = normalizeIntensity.apply(eas.getEmotion(questIsCompleted.name,EmotionType.Joy)) ;
	        		float satisfaction_completingQuest = normalizeIntensity.apply(eas.getEmotion(questIsCompleted.name,EmotionType.Satisfaction)) ;
	        		float fear_completingQuest = normalizeIntensity.apply(eas.getEmotion(questIsCompleted.name,EmotionType.Fear)) ;
	        		
	        		float hope_getMuchPoints = normalizeIntensity.apply(eas.getEmotion(gotAsMuchPointsAsPossible.name,EmotionType.Hope)) ;
	        		float joy_getMuchPoints = normalizeIntensity.apply(eas.getEmotion(gotAsMuchPointsAsPossible.name,EmotionType.Joy)) ;
	        		float satisfaction_getMuchPoints = normalizeIntensity.apply(eas.getEmotion(gotAsMuchPointsAsPossible.name,EmotionType.Satisfaction)) ;
	        		float fear_getMuchPoints = normalizeIntensity.apply(eas.getEmotion(gotAsMuchPointsAsPossible.name,EmotionType.Fear)) ;
	        		
	 
	        		String[] csvRow1 = { "" + i,
	        				"" + p_.x , "" + p_.z , 
	        				"" + hope_completingQuest, 
	        				"" + joy_completingQuest, 
	        				"" + satisfaction_completingQuest,
	        				"" + fear_completingQuest} ;
	        		
	        		String[] csvRow2 = { "" + i,
	        				"" + p_.x , "" + p_.z , 
	        				"" + hope_getMuchPoints, 
	        				"" + joy_getMuchPoints, 
	        				"" + satisfaction_getMuchPoints,
	        				"" + fear_getMuchPoints} ;
	       		
		        	csvData_goalQuestIsCompleted.add(csvRow1) ;
		        	csvData_goalGetMuchPoints.add(csvRow2) ;
	        	}
	            Thread.sleep(50);
	            i++ ;
	        	testAgent.update();
	        	if (i>200) {
	        		break ;
	        	}
	        }
	        testingTask.printGoalStructureStatus();

	        // check that we have passed both tests above:
	        assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 4) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        // close
	        testAgent.printStatus();
	        
	        exportToCSV(csvData_goalQuestIsCompleted,"data_goalQuestCompleted.csv") ;
	        exportToCSV(csvData_goalGetMuchPoints,"data_goalGetMuchPoints.csv") ;
        }
        finally { environment.close(); }
    }
}
