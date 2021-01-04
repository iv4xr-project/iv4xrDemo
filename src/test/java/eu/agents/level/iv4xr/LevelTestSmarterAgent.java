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
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
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
    	TestSettings.USE_SERVER_FOR_TEST = false ;
    	//TestSettings.USE_GRAPHICS = true ;
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
     * @return 
     * @return 
     * @return 
     * @return 
     * @return 
     * @return 
     * @return 
     * @return 
     * @return 
     * @return 
     * @return 
     * @return 
     * @return 
     * @return 
     * @return 
     */
    /*For running this method from the file RunTest, you need to change the method type to List<Object>
     *  and Add "String levelName,String fileName" ad a input. Also, you should comment these to variable
     *  from the beginning of the code and uncomment the return value at the end of it
     * */
    @Test
    public List<Object> closetReachableTest(String levelName,String fileName) throws InterruptedException {
    	
    	//GameLevel2_2020_08_24_14.24.15 - non-exist door  invalid
    	//GameLevel2_2020_08_24_17.24.47-original - non-exist door
    	//GameLevel3_2020_08_24_16.41.23
    	//String levelName = "level 1";
    	//String fileName = "GameLevel1_2020_08_28_13.32.04 - Copy";
        // Create an environment
        var environment = new LabRecruitsEnvironment(new LabRecruitsConfig(fileName,Platform.LEVEL_PATH+"\\" + levelName));
        if(USE_INSTRUMENT) instrument(environment) ;
        int cycleNumber = 0 ;
        long totalTime = 0;
        String finalResult = "null";
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
	
	    	
	        

	  	  var testingTask = SEQ( //Checking the door of room one, if it can not be open
	   	 
	   		SEQ(
	   		  GoalLib.entityInteracted("button3") , // button1 works because of the block can not see door
	   		  GoalLib.entityStateRefreshed("door1"),
	   		  GoalLib.entityInCloseRange("door1"),
	   		  
	   		  // try to find new button which can open the door of the room one
	   		  FIRSTof(GoalLib.checkDoorState("door1"),
	   				GoalLib.NEWREPEAT(
	   						(BeliefState b) -> GoalLib.activeButtonPredicate(b),
	   						  SEQ(
	   								  FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
	   								  GoalLib.entityStateRefreshed("door1"),
	   								  GoalLib.entityInCloseRange("door1"),
	   								  GoalLib.checkDoorState("door1")
	   								  )
	   						  )
	   				  ) 
	   		,GoalLib.entityInvariantChecked(testAgent, "door1", "door1 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
	   		  ),
	   	  
	   		SEQ( 
	   				FIRSTof(GoalLib.checkButtonState("button1"),GoalLib.entityInteracted("button1")), 
	   				GoalLib.entityStateRefreshed("door2"),
	   				GoalLib.entityInCloseRange("door2"),
	   				FIRSTof(
	   						GoalLib.checkDoorState("door2"),
	   						GoalLib.NEWREPEAT(
	   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
	   								SEQ(
	   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
	   										GoalLib.entityStateRefreshed("door2"),
	   										GoalLib.entityInCloseRange("door2"),
	   										GoalLib.checkDoorState("door2")
	   										)
	   								)
	   						) 
	   										
	   				,GoalLib.entityInvariantChecked(testAgent, "door2", "door2 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
	   			),
	   		SEQ( 
	   				FIRSTof(GoalLib.checkButtonState("button6"),GoalLib.entityInteracted("button6")), 
	   				GoalLib.entityStateRefreshed("door4"),
	   				GoalLib.entityInCloseRange("door4"),
	   				FIRSTof(
	   						GoalLib.checkDoorState("door4"),
	   						GoalLib.NEWREPEAT(
	   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
	   								SEQ(
	   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
	   										GoalLib.entityStateRefreshed("door4"),
	   										GoalLib.entityInCloseRange("door4"),
	   										GoalLib.checkDoorState("door4")
	   										)
	   								)
	   						) 
	   				,GoalLib.entityInvariantChecked(testAgent, "door4", "door4 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
	   				)
		  	);
	        
	        
	    	// test for third level : GameLevel3_2020_08_24_16.41.23 //2 doors active
//	    	var testingTask = SEQ(
//	    			//Checking the door of room one, if it can not be open by the set button try to find another button
//	    			SEQ(
//	    				GoalLib.entityInteracted("button2"), // button1 works because of the block can not see door
//	        			GoalLib.entityStateRefreshed("door1"),
//	        			GoalLib.entityInCloseRange("door1"),
//	        			
//	        			// try to find new button which can open the door of the room one 
//	        			FIRSTof(GoalLib.checkDoorState("door1"),
//	        					REPEAT(
//	        							SEQ(
//	        									FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
//	        									GoalLib.entityStateRefreshed("door1"),
//	        									GoalLib.entityInCloseRange("door1"),
//	        									GoalLib.checkDoorState("door1")
//	        									)
//	        							)
//	        					)
//	    			),
//	    			
//	    			SEQ(
//	    					FIRSTof(GoalLib.checkButtonState("button4"),GoalLib.entityInteracted("button4")),
//	    	    			GoalLib.entityStateRefreshed("door2"),
//	    	    			GoalLib.entityInCloseRange("door2"),
//	    	    		
//	    	    			// try to find new button which can open the door of the room one 
//	    	    			FIRSTof(GoalLib.checkDoorState("door2"),REPEAT(SEQ(REPEAT(FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent))),GoalLib.entityStateRefreshed("door2"),
//	    	    	    			GoalLib.entityInCloseRange("door2"),GoalLib.checkDoorState("door2"))))
//	        			),
//	    			SEQ(
//	    					FIRSTof(GoalLib.checkButtonState("button7"),GoalLib.entityInteracted("button7")),
//	        			GoalLib.entityStateRefreshed("door4"),
//	        			GoalLib.entityInCloseRange("door4"),
//	        			FIRSTof(
//	        					GoalLib.checkDoorState("door4"),
//	        					REPEAT(SEQ(REPEAT(FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent))),GoalLib.entityStateRefreshed("door4"),
//	        			    			GoalLib.entityInCloseRange("door4"),GoalLib.checkDoorState("door4")))
//	        					)
//	    			)
//	    		);
	    	
	  	  //------------------------
	        //this has some problem to find a door GameLevel2_2020_08_21_17.54.52
//	    	var testingTask = SEQ(
//	    			//Checking the door of room one, if it can not be open by the set button try to find another button
//	    			SEQ(
//		    			GoalLib.entityInteracted("button3"),
//		    			GoalLib.entityStateRefreshed("door1"),
//		    			GoalLib.entityInCloseRange("door1"),
//		    			// try to find new button which can open the door of the room one 
//		    			FIRSTof(GoalLib.checkDoorState("door1", (WorldEntity e) -> e.getBooleanProperty("isOpen")),
//		    					REPEAT(SEQ(FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),GoalLib.entityStateRefreshed("door1"),
//		    							GoalLib.entityInCloseRange("door1"),GoalLib.checkDoorState("door1", (WorldEntity e) -> e.getBooleanProperty("isOpen")))))
//	    			),
//
//	    			SEQ(
//		    			FIRSTof(GoalLib.checkButtonState("button7"), GoalLib.entityInteracted("button7")),
//		    			GoalLib.entityStateRefreshed("door2"),
//		    			FIRSTof(
//		    					GoalLib.checkDoorState("door2", (WorldEntity e) -> e.getBooleanProperty("isOpen")),
//		    					REPEAT(SEQ(REPEAT(FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent))),GoalLib.entityStateRefreshed("door2"),GoalLib.entityInCloseRange("door2"), GoalLib.checkDoorState("door2", (WorldEntity e) -> e.getBooleanProperty("isOpen"))))
//		    					)
//		    					)
//	    					);
//--------------------------------------------------
	  	  
	  	  
	        // new repeat
	        //GameLevel1_2020_08_07_10.42.26 - Copy  success
	        //GameLevel2_2020_08_24_17.24.47-original - non-exist button work for this one too, but it can solve the goal
	        // because it can't get stuck behind the door 
	        
//	    	var testingTask = SEQ(
//				SEQ(
//	    			GoalLib.entityInteracted("button2"),
//	    			GoalLib.entityStateRefreshed("door1"),
//	    			GoalLib.entityInCloseRange("door1"),
//	    			FIRSTof(
//	    					GoalLib.checkDoorState("door1"),
//	    					GoalLib.NEWREPEAT(
//	  	                            (BeliefState b) -> GoalLib.activeButtonPredicate(b),
//	    							SEQ(
//	    									FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
//	    									GoalLib.entityStateRefreshed("door1"),
//	    									GoalLib.entityInCloseRange("door1"),
//	    									GoalLib.checkDoorState("door1")
//	    									)
//	    								)				
//	    					)
	    			//,GoalLib.entityInvariantChecked(testAgent, "door1", "door1 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//				)
//				,
//				SEQ(
//		    			GoalLib.entityInteracted("button5"),
//		    			GoalLib.entityStateRefreshed("door2"),
//		    			GoalLib.entityInCloseRange("door2"),
//		    			FIRSTof(
//		    					GoalLib.checkDoorState("door2"),
//		    					GoalLib.NEWREPEAT(
//		    							(BeliefState b) -> GoalLib.activeButtonPredicate(b),
//		    							
//		    							SEQ(
//		    									FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
//		    									GoalLib.entityStateRefreshed("door2"),
//		    									GoalLib.entityInCloseRange("door2"),
//		    									GoalLib.checkDoorState("door2")
//		    									)
//		    							
//		    									)				
//		    							
//		    					)
//		    			,GoalLib.entityInvariantChecked(testAgent, "door2", "door2 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//					)
//				,
//				SEQ(
//		    			GoalLib.entityInteracted("button7"),
//		    			GoalLib.entityStateRefreshed("door4"),
//		    			GoalLib.entityInCloseRange("door4"),
//		    			FIRSTof(
//		    					GoalLib.checkDoorState("door4"),
//		    					GoalLib.NEWREPEAT(
//		    							(BeliefState b) -> GoalLib.activeButtonPredicate(b),
//		    							
//		    							SEQ(
//		    									FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
//		    									GoalLib.entityStateRefreshed("door4"),
//		    									GoalLib.entityInCloseRange("door4"),
//		    									GoalLib.checkDoorState("door4")
//		    									)
//		    							
//		    									)				
//		    							
//		    					)
//		    			,GoalLib.entityInvariantChecked(testAgent, "door4", "door4 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//					)
//				);
	        
	        //level one : GameLevel1_2020_08_28_13.32.04
//	    	var testingTask = SEQ(
//				SEQ(
//	    			GoalLib.entityInteracted("button2"),
//	    			GoalLib.entityStateRefreshed("door1"),
//	    			GoalLib.entityInCloseRange("door1"),
//	    			FIRSTof(
//	    					GoalLib.checkDoorState("door1", (WorldEntity e) -> e.getBooleanProperty("isOpen")),
//	    					REPEAT(
//	    							FIRSTof(
//	    								SEQ(GoalLib.success(), GoalLib.activeButtonPredicateed()),
//		    							SEQ(
//		    									FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
//		    									GoalLib.entityStateRefreshed("door1"),
//		    									GoalLib.entityInCloseRange("door1"),
//		    									GoalLib.checkDoorState("door1", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//		    									)
//		    							)
//	    							)
//	    					),
//	    			GoalLib.entityInvariantChecked(testAgent, "door1", "door1 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//				)

//				,
//				SEQ(
//		    			FIRSTof(GoalLib.checkButtonState("button4"), GoalLib.entityInteracted("button4")),
//		    			GoalLib.entityStateRefreshed("door4"),
//		    			GoalLib.entityInCloseRange("door4"),
//		    			FIRSTof(
//		    					GoalLib.checkDoorState("door4", (WorldEntity e) -> e.getBooleanProperty("isOpen")),
//		    					REPEAT(
//		    							SEQ(
//		    									FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
//		    									GoalLib.entityStateRefreshed("door4"),
//		    									GoalLib.entityInCloseRange("door4"),
//		    									GoalLib.checkDoorState("door4", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//		    									)
//		    							)
//		    					)
//					)
//	        );
     	
	        // level1 : 2 rooms one door: GameLevel1_2020_08_21_16.18.44
//	    	var testingTask = SEQ(
//	    			//Checking the door of room one, if it can not be open by the set button try to find another button
//	    			SEQ(
//		    			GoalLib.entityInteracted("button2"),
//		    			GoalLib.entityStateRefreshed("door1"),
//		    			GoalLib.entityInCloseRange("door1"),
//		    			// try to find new button which can open the door of the room one 
//		    			FIRSTof(
//		    					GoalLib.checkDoorState("door1", (WorldEntity e) -> e.getBooleanProperty("isOpen")),
//		    					REPEAT(SEQ(REPEAT(FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent))),GoalLib.entityStateRefreshed("door1"),GoalLib.entityInCloseRange("door1"),GoalLib.checkDoorState("door1", (WorldEntity e) -> e.getBooleanProperty("isOpen"))))
//		    					)
//	    			),
//	    			SEQ(
//		    			FIRSTof(GoalLib.checkButtonState("button5", GoalLib.entityInteracted("button5")),
//		    			GoalLib.entityStateRefreshed("door2"),
//		    			GoalLib.entityInCloseRange("door2"),
//		    			FIRSTof(
//		    					GoalLib.checkDoorState("door2", (WorldEntity e) -> e.getBooleanProperty("isOpen")),
//		    					REPEAT(SEQ(REPEAT(FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent))),GoalLib.entityStateRefreshed("door2"),GoalLib.entityInCloseRange("door2"),GoalLib.checkDoorState("door2", (WorldEntity e) -> e.getBooleanProperty("isOpen"))))
//		    					)
//	    			)
//	    					);
	    	
	        

	

	// test for second level, 2 doors active : GameLevel2_2020_08_24_17.24.47-original 
	
//	  var testingTask = SEQ( //Checking the door of room one, if it can not be open
//	 
//		SEQ(
//		  GoalLib.entityInteracted("button2") , // button1 works because of the block can not see door
//		  GoalLib.entityStateRefreshed("door1"),
//			GoalLib.entityInCloseRange("door1"),
//		  
//		  // try to find new button which can open the door of the room one
//		  FIRSTof(GoalLib.checkDoorState("door1", (WorldEntity e) -> e.getBooleanProperty("isOpen")),
//				  REPEAT(SEQ(FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),GoalLib.entityStateRefreshed("door1"),
//		    	GoalLib.entityInCloseRange("door1"),GoalLib.checkDoorState("door1",(WorldEntity e) -> e.getBooleanProperty("isOpen"))))) 
//		  ,GoalLib.entityInvariantChecked(testAgent, "door1", "door1 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//				),
//	  
//		SEQ( 
//			FIRSTof(GoalLib.checkButtonState("button5"),GoalLib.entityInteracted("button5")),//button4 get stuck 
//			GoalLib.entityStateRefreshed("door2"),
//			GoalLib.entityInCloseRange("door2"),
//	 
//		  // try to find new button which can open the door of the room one
//			FIRSTof(GoalLib.checkDoorState("door2", (WorldEntity e) -> e.getBooleanProperty("isOpen")),
//					REPEAT(SEQ(FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),GoalLib.entityStateRefreshed("door2"),GoalLib.entityInCloseRange("door2"),GoalLib.checkDoorState("door2",
//		  			(WorldEntity e) -> e.getBooleanProperty("isOpen"))))) 
//			,GoalLib.entityInvariantChecked(testAgent, "door2", "door2 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//				), 
//		
//		
//		SEQ(
//			FIRSTof(GoalLib.checkButtonState("button8"),GoalLib.entityInteracted("button8")),//get stuck if it is button7
//			GoalLib.entityStateRefreshed("door4"),
//			GoalLib.entityInCloseRange("door4"), 
//			FIRSTof(
//					GoalLib.checkDoorState("door4", (WorldEntity e) -> e.getBooleanProperty("isOpen")),
//					REPEAT(SEQ(FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),GoalLib.entityStateRefreshed("door4"),
//			    			GoalLib.entityInCloseRange("door4"),GoalLib.checkDoorState("door4",(WorldEntity e) -> e.getBooleanProperty("isOpen")))) 
//					)
//			,GoalLib.entityInvariantChecked(testAgent, "door4", "door4 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//			)
//		);
	 

//	// test for third level : GameLevel3_2020_08_24_16.19.35	    	
//	var testingTask = SEQ(
//			//Checking the door of room one, if it can not be open by the set button try to find another button
//			SEQ(
//    			GoalLib.entityInteracted("button2"),
//    			GoalLib.entityStateRefreshed("door1"),
//    			GoalLib.entityInCloseRange("door1"),
//    			
//    			// try to find new button which can open the door of the room one 
//    			FIRSTof(GoalLib.checkDoorState("door1"),
//    					REPEAT(SEQ(FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
//    					GoalLib.entityStateRefreshed("door1"),GoalLib.entityInCloseRange("door1"),GoalLib.checkDoorState("door1"))))
//			),
//			
//			
//			SEQ(
//    			FIRSTof(GoalLib.checkButtonState("button7"), GoalLib.entityInteracted("button7")),
//    			GoalLib.entityStateRefreshed("door2"),
//    			GoalLib.entityInCloseRange("door2"),
//    			FIRSTof(
//    					GoalLib.checkDoorState("door2"),
//    					REPEAT(SEQ(FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),GoalLib.entityStateRefreshed("door2"),
//   							GoalLib.entityInCloseRange("door2"),GoalLib.checkDoorState("door2")))
//    					)
//			)
//		);

   	
	        
	        

	        
	        

	    	

 						
	     
	        		
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
	
	      

	        
	        // keep updating the agent
	        long startTime = System.currentTimeMillis();
	        while (testingTask.getStatus().inProgress()) {
	        	System.out.println("*** " + cycleNumber + ", " + testAgent.getState().id + " @" + testAgent.getState().worldmodel.position) ;
	            Thread.sleep(50);
	            
	            cycleNumber++ ; 
	        	testAgent.update();
	        	
//	        	if (i>=206) {
//	        	   System.out.println(">>>>>") ;
//	        	}
	        	
	        	if (cycleNumber>800) {
	        		break ;
	        	}
	        }
	        long endTime = System.currentTimeMillis();
	        totalTime = endTime - startTime;
	       //testingTask.printGoalStructureStatus();
	       System.out.println("******run time******");
	       System.out.println(totalTime/1000);
	       System.out.println("******cycle number******");
	       System.out.println(cycleNumber);
	       int numberOfDoors = 3;
	       if(testAgent.getTestDataCollector().getNumberOfPassVerdictsSeen() == numberOfDoors) {
	    	   System.out.println("Goal successfully acheived");
	    	   finalResult = "success";
	       }else {
	    	  System.out.println("Goal failed, " + testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen()+ " number of doors has not opened, the total number of doors is:" + numberOfDoors);
	    	  finalResult = "failed";
	       }


	        
	        // check that we have passed both tests above:
	        assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 3) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        // close
	        testAgent.printStatus();
        }
        finally { environment.close(); }
        List<Object> myList = new ArrayList<Object>();
        myList.add(cycleNumber);
        myList.add(totalTime/1000);
        myList.add(finalResult);
        return myList;
    }




}

