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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
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
    	//TestSettings.USE_SERVER_FOR_TEST = false ;
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

    /*Get connection between buttons and doors*/
    public List<List<String>> getData(String levelName, String fileName) {	
    	String csvFile = Platform.LEVEL_PATH +File.separator+ levelName+File.separator+ fileName+".csv";
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
	            }
	            if(!values.isEmpty())
	        	 records.add(values);
	         }
	        
	         } catch(IOException ioe) {
	            ioe.printStackTrace();
	         }
		return records;
    }
    public ArrayList<List<String>> getButtons(String levelName, String fileName) {	
    	String csvFile = Platform.LEVEL_PATH +File.separator+ levelName+File.separator+ fileName+".csv";
    	String delimiter = ",";  	
    	ArrayList<List<String>> buttonInRooms = new ArrayList<List<String>>();
		try {
	         File file = new File(csvFile);
	         Scanner sc = new Scanner(file);
	         String[] tempArr;
	         List<String> buttons = new ArrayList<String>();
	         while(sc.hasNext()) {
	        	 //tempArr each row
	        	
	            tempArr = sc.next().split(delimiter);  
	            List<String> values = new ArrayList<String>();
	            for(String tempStr : tempArr) {
	            	if(tempStr.contains("button") && tempStr.contains("^")) {
	            		buttons.add(tempStr.substring(tempStr.lastIndexOf("^") + 1));
	            	}    	
	            	if(tempStr.contains("door") && tempStr.contains("^")) {
	            		List b = new ArrayList(buttons);
	            		buttonInRooms.add(b);
	            		buttons.removeAll(buttons);
	            		break;
	            	}
	            }
	         }
	        
	         } catch(IOException ioe) {
	            ioe.printStackTrace();
	         }
		Random rand = new Random(); 
       
		/*Print buttons*/
//		for(int i=0; i<buttonInRooms.size();i++) {
//			System.out.println("*****");
//			System.out.print(buttonInRooms.get(i).size());
//			System.out.print(buttonInRooms.get(i).get(rand.nextInt(buttonInRooms.get(i).size()))); 
//			for(int j=0;j<buttonInRooms.get(i).size(); j++) {
//				System.out.println("*****------");
//				System.out.print(buttonInRooms.get(i).get(j));
//			}
//		}
		return buttonInRooms;
    }
    
	/*pick randomly a button from a list */
	public static List<String> pickNRandom(List<String> lst, int n) {
	    List<String> copy = new LinkedList<String>(lst);
	    Collections.shuffle(copy);
	    return copy.subList(0, n);
	}
    /**
     * A test to verify that the east closet is reachable.
     * Choice a file to test
     */ 
    @Test
    public void closetReachableTest() throws InterruptedException {
    	String levelName = "GameLevel1";
    	String[] fileNames = {
    			"GameLevel1_2021_02_17_16.46.33-8rooms"
    			//,"GameLevel1_2020_09_30_20.45.01"

    			
    	} ;
    	String summary = "" ;
    	for(var file : fileNames) {
    		System.out.println("##== Testing " + file) ;
    	    var result = closetReachableTest(levelName,file) ;
    	    summary += file + ": ";
    	    if(result.size()==3) summary += result.get(2) + "\n" ;			
    	    else summary += "fail\n" ;
    	}
    	System.out.println("##==\n" + summary) ;
    }
   
     
    /**
     * A test to verify that the east closet is reachable.
     */
    
    public List<Object> closetReachableTest(String levelName,String fileName) throws InterruptedException {
    	
        // Create an environment
    	var LRconfig = new LabRecruitsConfig(fileName,Platform.LEVEL_PATH +File.separator+ levelName) ;
    	LRconfig.agent_speed = 0.1f ;
        var environment = new LabRecruitsEnvironment(LRconfig);
        if(USE_INSTRUMENT) instrument(environment) ;
        int cycleNumber = 0 ;
        long totalTime = 0;
        String finalResult = "null";
    	Random rand = new Random();
        ArrayList<List<String>> buttonInRooms = getButtons(levelName, fileName);
        int numberOfRooms = buttonInRooms.size();
        
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
	
	    	
	        /*Create the sequence automatically*/
//	        var getDataConection = getData(levelName, fileName);
//	        /*Export the number of doors in each room: To do*/
//	        int numberOfDoorInEachRoom = 2;
//	        int countingDoors = 0;
//	        int countingRooms = 0;
//	        int NumberOfPassVerdicts = 0;
//	        int f = 0;
//	        GoalStructure[] subGoal1 = new GoalStructure[4];
//	        GoalStructure[] subGoal2 = new GoalStructure[5];
//	        GoalStructure[] subGoalsFinal = new GoalStructure[getDataConection.size()];
//	        
//	        for(int i=0; i<getDataConection.size(); i++) {
//	        	NumberOfPassVerdicts++;
//	        	countingDoors++;
//
//	        	var doorToTest = getDataConection.get(i).get(1) ;  
//	        	subGoal1[0] = FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent));
//	        	subGoal1[1] = GoalLib.entityStateRefreshed(doorToTest);
//	        	subGoal1[2] = GoalLib.entityInCloseRange(doorToTest);
//	        	subGoal1[3] = GoalLib.checkDoorState(doorToTest);
//	        	var lastSeq = SEQ(subGoal1);
//	        	var newReapet = GoalLib.NEWREPEAT((BeliefState b) -> GoalLib.activeButtonPredicate(b),lastSeq); 
//	        	var firstOf = FIRSTof(GoalLib.checkDoorState(doorToTest),newReapet);
//	        	/*get randomly a button in each related room*/
//	        	var selecteButtonRandomly = buttonInRooms.get(countingRooms).get(rand.nextInt(buttonInRooms.get(countingRooms).size()));
//	        	if(i == getDataConection.size() - 1) selecteButtonRandomly = "button32";
//	        	subGoal2[0] = i == 0 ? GoalLib.entityInteracted(selecteButtonRandomly) : FIRSTof(GoalLib.checkButtonState(selecteButtonRandomly),GoalLib.entityInteracted(selecteButtonRandomly));
//	        	subGoal2[1] = GoalLib.entityStateRefreshed(doorToTest);
//	        	subGoal2[2] = GoalLib.entityInCloseRange(doorToTest);
//	        	subGoal2[3] = firstOf;
//	        	System.out.println("Door to check");
//	        	System.out.println(doorToTest);
//	        	System.out.println(selecteButtonRandomly);
//	    	    subGoal2[4] = GoalLib.entityInvariantChecked(testAgent, doorToTest, doorToTest+"should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"));   
//	    	    var finalSeq = SEQ(subGoal2);
//	    	    subGoalsFinal[f] = finalSeq;
//	    	    f++;  
//	        	if(countingDoors>=numberOfDoorInEachRoom) {
//	        		countingRooms++;
//	        		countingDoors = 0;
//	        		}
//	        }
//	        
//	        var testingTask = SEQ(subGoalsFinal);      

	        
	        
	        
	        
	        
	        
//	  	  var testingTask = SEQ( //Checking the door of room one, if it can not be open
//	   	 
//	   		SEQ(
//	   		  GoalLib.entityInteracted("button3") , 
//	   		  GoalLib.entityStateRefreshed("door1"),
//	   		  GoalLib.entityInCloseRange("door1"),
//	   		  
//	   		  // try to find new button which can open the door in the room one
//	   		  FIRSTof(
//	   				GoalLib.checkDoorState("door1"),
//	   				GoalLib.NEWREPEAT(
//	   						(BeliefState b) -> GoalLib.activeButtonPredicate(b),
//	   						  SEQ(
//	   								  FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
//	   								  GoalLib.entityStateRefreshed("door1"),
//	   								  GoalLib.entityInCloseRange("door1"),
//	   								  GoalLib.checkDoorState("door1")
//	   								  )
//	   						  )
//	   				  ) 
//	   		,GoalLib.entityInvariantChecked(testAgent, "door1", "door1 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//	   		  ),
//	   	  
//	   		SEQ( 
//	   				FIRSTof(GoalLib.checkButtonState("button3"),GoalLib.entityInteracted("button3")), 
//	   				GoalLib.entityStateRefreshed("door2"),
//	   				GoalLib.entityInCloseRange("door2"),
//	   				FIRSTof(
//	   						GoalLib.checkDoorState("door2"),
//	   						GoalLib.NEWREPEAT(
//	   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
//	   								SEQ(
//	   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
//	   										GoalLib.entityStateRefreshed("door2"),
//	   										GoalLib.entityInCloseRange("door2"),
//	   										GoalLib.checkDoorState("door2")
//	   										)
//	   								)
//	   						) 
//	   										
//	   				,GoalLib.entityInvariantChecked(testAgent, "door2", "door2 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//	   			),
//	   		SEQ( 
//	   				FIRSTof(GoalLib.checkButtonState("button5"),GoalLib.entityInteracted("button5")), 
//	   				GoalLib.entityStateRefreshed("door4"),
//	   				GoalLib.entityInCloseRange("door4"),
//	   				FIRSTof(
//	   						GoalLib.checkDoorState("door4"),
//	   						GoalLib.NEWREPEAT(
//	   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
//	   								SEQ(
//	   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
//	   										GoalLib.entityStateRefreshed("door4"),
//	   										GoalLib.entityInCloseRange("door4"),
//	   										GoalLib.checkDoorState("door4")
//	   										)
//	   								)
//	   						) 
//	   				,GoalLib.entityInvariantChecked(testAgent, "door4", "door4 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
//	   				)
//		  	);
	        
		  	  var testingTask = SEQ( //Checking the door of room one, if it can not be open
		   	 
		   		SEQ(
		   		  GoalLib.entityInteracted("button2") , 
		   		  GoalLib.entityStateRefreshed("door1"),
		   		  GoalLib.entityInCloseRange("door1"),
		   		  
		   		  // try to find new button which can open the door in the room one
		   		  FIRSTof(
		   				GoalLib.checkDoorState("door1"),
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
		   				FIRSTof(GoalLib.checkButtonState("button5"),GoalLib.entityInteracted("button5")), 
		   				GoalLib.entityStateRefreshed("door3"),
		   				GoalLib.entityInCloseRange("door3"),
		   				FIRSTof(
		   						GoalLib.checkDoorState("door3"),
		   						GoalLib.NEWREPEAT(
		   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
		   								SEQ(
		   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
		   										GoalLib.entityStateRefreshed("door3"),
		   										GoalLib.entityInCloseRange("door3"),
		   										GoalLib.checkDoorState("door3")
		   										)
		   								)
		   						) 
		   				,GoalLib.entityInvariantChecked(testAgent, "door3", "door3 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
		   				),
		   		
		   		SEQ( 
		   				FIRSTof(GoalLib.checkButtonState("button8"),GoalLib.entityInteracted("button8")), 
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
		   				),
		   		SEQ( 
		   				FIRSTof(GoalLib.checkButtonState("button12"),GoalLib.entityInteracted("button12")), 
		   				GoalLib.entityStateRefreshed("door5"),
		   				GoalLib.entityInCloseRange("door5"),
		   				FIRSTof(
		   						GoalLib.checkDoorState("door5"),
		   						GoalLib.NEWREPEAT(
		   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
		   								SEQ(
		   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
		   										GoalLib.entityStateRefreshed("door5"),
		   										GoalLib.entityInCloseRange("door5"),
		   										GoalLib.checkDoorState("door5")
		   										)
		   								)
		   						) 
		   				,GoalLib.entityInvariantChecked(testAgent, "door5", "door5 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
		   				),
		   		SEQ( 
		   				FIRSTof(GoalLib.checkButtonState("button11"),GoalLib.entityInteracted("button11")), 
		   				GoalLib.entityStateRefreshed("door6"),
		   				GoalLib.entityInCloseRange("door6"),
		   				FIRSTof(
		   						GoalLib.checkDoorState("door6"),
		   						GoalLib.NEWREPEAT(
		   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
		   								SEQ(
		   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
		   										GoalLib.entityStateRefreshed("door6"),
		   										GoalLib.entityInCloseRange("door6"),
		   										GoalLib.checkDoorState("door6")
		   										)
		   								)
		   						) 
		   				,GoalLib.entityInvariantChecked(testAgent, "door6", "door6 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
		   				),
		   		SEQ( 
		   				FIRSTof(GoalLib.checkButtonState("button15"),GoalLib.entityInteracted("button15")), 
		   				GoalLib.entityStateRefreshed("door7"),
		   				GoalLib.entityInCloseRange("door7"),
		   				FIRSTof(
		   						GoalLib.checkDoorState("door7"),
		   						GoalLib.NEWREPEAT(
		   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
		   								SEQ(
		   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
		   										GoalLib.entityStateRefreshed("door7"),
		   										GoalLib.entityInCloseRange("door7"),
		   										GoalLib.checkDoorState("door7")
		   										)
		   								)
		   						) 
		   				,GoalLib.entityInvariantChecked(testAgent, "door7", "door7 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
		   				),
		   		SEQ( 
		   				FIRSTof(GoalLib.checkButtonState("button13"),GoalLib.entityInteracted("button13")), 
		   				GoalLib.entityStateRefreshed("door8"),
		   				GoalLib.entityInCloseRange("door8"),
		   				FIRSTof(
		   						GoalLib.checkDoorState("door8"),
		   						GoalLib.NEWREPEAT(
		   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
		   								SEQ(
		   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
		   										GoalLib.entityStateRefreshed("door8"),
		   										GoalLib.entityInCloseRange("door8"),
		   										GoalLib.checkDoorState("door8")
		   										)
		   								)
		   						) 
		   				,GoalLib.entityInvariantChecked(testAgent, "door8", "door8 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
		   				),
		   		SEQ( 
		   				FIRSTof(GoalLib.checkButtonState("button18"),GoalLib.entityInteracted("button18")), 
		   				GoalLib.entityStateRefreshed("door9"),
		   				GoalLib.entityInCloseRange("door9"),
		   				FIRSTof(
		   						GoalLib.checkDoorState("door9"),
		   						GoalLib.NEWREPEAT(
		   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
		   								SEQ(
		   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
		   										GoalLib.entityStateRefreshed("door9"),
		   										GoalLib.entityInCloseRange("door9"),
		   										GoalLib.checkDoorState("door9")
		   										)
		   								)
		   						) 
		   				,GoalLib.entityInvariantChecked(testAgent, "door9", "door9 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
		   				),
		   		SEQ( 
		   				FIRSTof(GoalLib.checkButtonState("button19"),GoalLib.entityInteracted("button19")), 
		   				GoalLib.entityStateRefreshed("door10"),
		   				GoalLib.entityInCloseRange("door10"),
		   				FIRSTof(
		   						GoalLib.checkDoorState("door10"),
		   						GoalLib.NEWREPEAT(
		   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
		   								SEQ(
		   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
		   										GoalLib.entityStateRefreshed("door10"),
		   										GoalLib.entityInCloseRange("door10"),
		   										GoalLib.checkDoorState("door10")
		   										)
		   								)
		   						) 
		   				,GoalLib.entityInvariantChecked(testAgent, "door10", "door10 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
		   				),
		   		SEQ( 
		   				FIRSTof(GoalLib.checkButtonState("button20"),GoalLib.entityInteracted("button20")), 
		   				GoalLib.entityStateRefreshed("door11"),
		   				GoalLib.entityInCloseRange("door11"),
		   				FIRSTof(
		   						GoalLib.checkDoorState("door11"),
		   						GoalLib.NEWREPEAT(
		   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
		   								SEQ(
		   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
		   										GoalLib.entityStateRefreshed("door11"),
		   										GoalLib.entityInCloseRange("door11"),
		   										GoalLib.checkDoorState("door11")
		   										)
		   								)
		   						) 
		   				,GoalLib.entityInvariantChecked(testAgent, "door11", "door11 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
		   				),
		   		SEQ( 
		   				FIRSTof(GoalLib.checkButtonState("button23"),GoalLib.entityInteracted("button23")), 
		   				GoalLib.entityStateRefreshed("door12"),
		   				GoalLib.entityInCloseRange("door12"),
		   				FIRSTof(
		   						GoalLib.checkDoorState("door12"),
		   						GoalLib.NEWREPEAT(
		   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
		   								SEQ(
		   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
		   										GoalLib.entityStateRefreshed("door12"),
		   										GoalLib.entityInCloseRange("door12"),
		   										GoalLib.checkDoorState("door12")
		   										)
		   								)
		   						) 
		   				,GoalLib.entityInvariantChecked(testAgent, "door12", "door12 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
		   				),
		   		SEQ( 
		   				FIRSTof(GoalLib.checkButtonState("button25"),GoalLib.entityInteracted("button25")), 
		   				GoalLib.entityStateRefreshed("door13"),
		   				GoalLib.entityInCloseRange("door13"),
		   				FIRSTof(
		   						GoalLib.checkDoorState("door13"),
		   						GoalLib.NEWREPEAT(
		   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
		   								SEQ(
		   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
		   										GoalLib.entityStateRefreshed("door13"),
		   										GoalLib.entityInCloseRange("door13"),
		   										GoalLib.checkDoorState("door13")
		   										)
		   								)
		   						) 
		   				,GoalLib.entityInvariantChecked(testAgent, "door13", "door13 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
		   				),
		   		SEQ( 
		   				FIRSTof(GoalLib.checkButtonState("button25"),GoalLib.entityInteracted("button25")), 
		   				GoalLib.entityStateRefreshed("door14"),
		   				GoalLib.entityInCloseRange("door14"),
		   				FIRSTof(
		   						GoalLib.checkDoorState("door14"),
		   						GoalLib.NEWREPEAT(
		   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
		   								SEQ(
		   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
		   										GoalLib.entityStateRefreshed("door14"),
		   										GoalLib.entityInCloseRange("door14"),
		   										GoalLib.checkDoorState("door14")
		   										)
		   								)
		   						) 
		   				,GoalLib.entityInvariantChecked(testAgent, "door14", "door14 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
		   				),
		   		SEQ( 
   				FIRSTof(GoalLib.checkButtonState("button32"),GoalLib.entityInteracted("button32")), 
   				GoalLib.entityStateRefreshed("door16"),
   				GoalLib.entityInCloseRange("door16"),
   				FIRSTof(
   						GoalLib.checkDoorState("door16"),
   						GoalLib.NEWREPEAT(
   								(BeliefState b) -> GoalLib.activeButtonPredicate(b),
   								SEQ(
   										FIRSTof(GoalLib.findingNewButtonAndInteracte(testAgent)),
   										GoalLib.entityStateRefreshed("door16"),
   										GoalLib.entityInCloseRange("door16"),
   										GoalLib.checkDoorState("door16")
   										)
   								)
   						) 
   				,GoalLib.entityInvariantChecked(testAgent, "door16", "door16 should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))
   				)
			  	);
	           
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
	        	if (cycleNumber>2600) {
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
	       int numberOfDoors = 15;
	       if(testAgent.getTestDataCollector().getNumberOfPassVerdictsSeen() == numberOfDoors) {
	    	   System.out.println("Goal successfully acheived");
	    	   finalResult = "success";
	       }else {
	    	  System.out.println("Goal failed, " + testAgent.getTestDataCollector().getNumberOfFailVerdictsSeen()+ " number of doors has not opened, the total number of doors is:" + numberOfDoors);
	    	  finalResult = "failed";
	       }

	       testAgent.printStatus();
	        
	        // check that we have passed both tests above:
	        assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == numberOfDoors) ;
	        // goal status should be success
	        assertTrue(testAgent.success());
	        // close
	        
        }
        finally { environment.close(); }
        List<Object> myList = new ArrayList<Object>();
        myList.add(cycleNumber);
        myList.add(totalTime/1000);
        myList.add(finalResult);
        return myList;
    }




}

