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
	        	GoalLib.positionsVisited(new Vec3(10.5f,0,4f))
	        );
	        // attaching the goal and testdata-collector
	        var dataCollector = new TestDataCollector();
	        testAgent . setTestDataCollector(dataCollector) . setGoal(testingTask) ;


	        environment.startSimulation(); // this will press the "Play" button in the game for you
	        //goal not achieved yet
	        assertFalse(testAgent.success());

	        List traceData = new LinkedList() ;
	        List fearData = new LinkedList() ;
	        List<String[]> csvData = new LinkedList<>() ;
	        String[] csvRow = { "t", "x", "y", "hope", "joy", "satisfaction", "fear" } ;
	        csvData.add(csvRow) ;
	        EmotionAppraisalSystem eas = new EmotionAppraisalSystem(testAgent.getId()).withUserModel(new PlayerOneCharacterization()) ;
	        eas.beliefbase = new EmotionBeliefBase() .attachFunctionalState(testAgent.getState()) ;
	        eas.addGoal(questIsCompleted,50);
	        eas.addGoal(gotAsMuchPointsAsPossible,50) ;
	        EventsProducer eventsProducer = new EventsProducer() .attachTestAgent(testAgent) ;
	        
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
	        		Float score = (float) testAgent.getState().worldmodel.score / 40 ;
	        		List row = new LinkedList() ;
	        		List fearRow = new LinkedList() ;
	        		row.add(p_) ;
	        		fearRow.add(p_) ;	        		
	        		// row.add(score) ;
	        		Emotion hope_completingQuest = eas.getEmotion(questIsCompleted.name,EmotionType.Hope) ;
	        		Emotion joy_completingQuest = eas.getEmotion(questIsCompleted.name,EmotionType.Joy) ;
	        		Emotion satisfaction_completingQuest = eas.getEmotion(questIsCompleted.name,EmotionType.Satisfaction) ;
	        		Emotion fear_completingQuest = eas.getEmotion(questIsCompleted.name,EmotionType.Fear) ;
	        		
	        		float hope_completingQuest_intensity = hope_completingQuest!=null ? (float) hope_completingQuest.intensity / 800f : 0f ;
	        		float joy_completingQuest_intensity  = joy_completingQuest!=null ? (float) joy_completingQuest.intensity / 800f : 0f ;
	        		float satisfaction_completingQuest_intensity = satisfaction_completingQuest!=null ? (float) satisfaction_completingQuest.intensity/800f : 0f ;
	        		float fear_completingQuest_intensity = fear_completingQuest!=null ? (float) fear_completingQuest.intensity/800f : 0f ;
	        		
	        		row.add(hope_completingQuest_intensity) ;
	        		row.add(joy_completingQuest_intensity) ;
	        		row.add(satisfaction_completingQuest_intensity) ;
	        		fearRow.add(fear_completingQuest_intensity) ;
	        		fearRow.add(0f) ; fearRow.add(0f) ;
	        		
	        		String[] csvRow_ = { "" + i,
	        				"" + p_.x , "" + p_.z , 
	        				"" + hope_completingQuest_intensity, 
	        				"" + joy_completingQuest_intensity, 
	        				"" + satisfaction_completingQuest_intensity,
	        				"" + fear_completingQuest_intensity} ;

	        		
		        	traceData.add(row) ;
		        	fearData.add(fearRow) ;
		        	csvData.add(csvRow_) ;
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
	        
	        mkScatterGraph(traceData,"roomReachabilityTest.png",5*120,5*80,5*10f,5*4) ;
	        mkScatterGraph(fearData,"fear.png",5*120,5*80,5*10f,5*4) ;
	        exportToCSV(csvData,"data.csv") ;
        }
        finally { environment.close(); }
    }
}
