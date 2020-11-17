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
import eu.iv4xr.framework.mainConcepts.TestAgent;
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
import java.util.stream.Collectors;

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

import eu.fbk.iv4xr.mbt.MBTProperties;
import eu.fbk.iv4xr.mbt.efsm4j.EFSM;
import eu.fbk.iv4xr.mbt.efsm4j.Transition;
//import eu.fbk.iv4xr.mbt.efsm4j.labrecruits.LabRecruitsEFSMFactory;
import eu.fbk.iv4xr.mbt.efsm4j.labrecruits.LabRecruitsParameter;
import eu.fbk.iv4xr.mbt.strategy.GenerationStrategy;
import eu.fbk.iv4xr.mbt.strategy.SearchBasedStrategy;
import eu.fbk.iv4xr.mbt.testcase.AbstractTestSequence;
import eu.fbk.iv4xr.mbt.testcase.RandomLengthTestFactory;
import eu.fbk.iv4xr.mbt.testcase.Testcase;
import eu.fbk.iv4xr.mbt.testsuite.SuiteChromosome;

import org.evosuite.ga.Chromosome;

/**
 * An example demonstrating how to convert (abtsract) testcase from FBK's MBT to concrete tests in LR.
 * @author Samira, Wish
 */
public class mbtTest {
	
	
	// some LR-testing setup stuffs:
	
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
    
    
    public static class Pair<T,U> {
    	public T fst ;
    	public U snd ;
    	public Pair(T x, U y) { fst = x ; snd = y ; }
    }
	
    private String[][] convertToIntermediateStructure(Testcase testcase) {
    	// cast the test case to a TestSeq:		
        AbstractTestSequence tc = (AbstractTestSequence) testcase ;
    	// get the transitions and the corresponding params:
    	List transitions = tc.getPath().getTransitions() ;
    	List parameters  = tc.getPath().getParameterValues() ;
    			
    	// some intermediate structure to represent the test-case as a list of strings:
    	String[][] transitions_ = new String[transitions.size()][] ;
    			
    	for (int k=0; k< transitions.size(); k++) {
    		Transition t = (Transition) transitions.get(k);
    		// System.out.println(">>> " + t) ;
    		LabRecruitsParameter p  = (LabRecruitsParameter) parameters.get(k);
    		String[] step_  = { t.getSrc().getId(), t.getTgt().getId(), p.toString() };
    		transitions_[k] = step_ ;
    		// debug:
    		System.out.println("## Step " + k + ": " + transitions_[k][0] + " -- " + transitions_[k][2] + " --> " + transitions_[k][1]);
 	    }
    	return transitions_ ;
    }
    
    
    private Pair<GoalStructure,Integer> convertTestcase_to_Testingtask(TestAgent agent, String[][] transitions_) {
    	List<GoalStructure> subGoals = new LinkedList<GoalStructure>() ;
		int numberOfPassVerdicts = 0 ;
		for(int k=0; k < transitions_.length; k++) {
    		String state1 = transitions_[k][0];        	
    		String state2 = transitions_[k][1];   
    		String actionToDo = transitions_[k][2];
    		
    		boolean state1_isDoor = state1.endsWith("_p") || state1.endsWith("_m") ;
    		boolean state2_isDoor = state2.endsWith("_p") || state2.endsWith("_m") ;
    		String state1ID = state1_isDoor ? state1.substring(0, state1.length()-2) : state1 ;
    		String state2ID = state2_isDoor ? state2.substring(0, state2.length()-2) : state2 ;
    		
    		// Special case for the first transition. The agent-starting position may not be literally
    		// next to state-1, so it may need to travel to see it first.
        	if(k == 0) {
        		// every test sequence should start by refreshing the state of state-1 and approaching it:
	        	subGoals.add(GoalLib.entityStateRefreshed(state1ID)) ;
	        	//subGoals.add(GoalLib.entityInCloseRange(state1ID)) ;
        	}
        	
        	// Special case for treasure room.
        	// If state-1 or state-2 is the treasure room TR, we are going to ignore it for now.
    		// We will only check if the door leading to it is open, rather than checking if you can
    		// actually stand inside the TR
    		if (state1.equals("TR") || state2.equals("TR")) continue ;
    		
    		// Normal cases:
        	switch(actionToDo) {
        	  case "EXPLORE" : // the action is to travel
        		  
        		// Traveling from one side of a door to its other side is a bit tricky. Since the door is not
        		// large, we won't actually make the agent travels to the other side. There are two cases,
        		// see below.
          		if(state1_isDoor && state2_isDoor && state1ID.equals(state2ID)) {
          			// Case-1:
          			// if the previous step was side1-->side2, then this current step just wants to
          			// go back to side1, the we will skip this step. This is optimization.
          			if(k>0
          				&& transitions_[k-1][1].equals(state1) 
          				&& transitions_[k-1][0].equals(state2)) continue;
          			// Else we have case-2. So this must be where the agent came from elsewhere, and then arrives at
          			// one side of the door. Since the agent wants to travel to the other side, this implies that
          			// the agent expects the door to be open.
          			// This is interpreted as a test-oracle, so we insert a test-invariant goal.
          			// After checking this invariant we will just leave the agent to remain at where it is, and not
          			// literally asking it to go to the other side of the door.
          			numberOfPassVerdicts++;
          			subGoals.add(GoalLib.checkDoorState(state1ID));
          			subGoals.add(GoalLib.entityInvariantChecked(agent, state1ID, state1ID+" should be open", (WorldEntity e) -> e.getBooleanProperty("isOpen"))) ;
          		}
          		else { // other cases of traveling
          		   subGoals.add(GoalLib.entityStateRefreshed(state2ID)) ;
          		   // problematical:
                   // subGoals.add(GoalLib.entityInCloseRange(state2ID)) ;
          		}  
        		break ;
        	  
        	  case "TOGGLE"  : 
        		  subGoals.add(GoalLib.entityInteracted(state1ID)) ;
        		  break ;
        	}
        	if(actionToDo.equals("EXPLORE")) {
        		
        	}

        }
		
		GoalStructure testingTask = SEQ(subGoals.toArray(new GoalStructure[0])) ;
		
		
		return new Pair(testingTask,numberOfPassVerdicts) ;
    }
    
	/**
	 * Convert FBK testcase tc to iv4xr testing-task, which is a GoalStructure. Such a structure can latter
	 * be given to the test-agent for execution.
	 */
    Pair<GoalStructure,Integer> convertTestcase_to_Testingtask(TestAgent agent, Testcase testcase) {
		// some intermediate structure to represent the test-case as a list of strings:
		String[][] transitions_ = convertToIntermediateStructure(testcase) ;
		return convertTestcase_to_Testingtask(agent,transitions_) ;
	}
	
	/**
	 * Convert a bunch of FBK-test cases to an intermediate representation for translation.
	 */
	List<Pair<GoalStructure,Integer>> convertTestSuite_to_Testingtasks(TestAgent agent, List<Testcase> testsuite) {
		return testsuite.stream().map(tc -> convertTestcase_to_Testingtask(agent,tc)).collect(Collectors.toList()) ;
	}

    
    
	
	
	private String[] split3(String tr) {
		int k = tr.indexOf(" -- ") ;
		String src = tr.substring(0,k) ;
		int m = tr.indexOf(" --> ") ;
		String action = tr.substring(k + " -- ".length(), m) ;
		String destination = tr.substring(m + " --> ".length(), tr.length()) ;
		String [] result = { src, destination, action } ;
		return result ;
	}
	
	// A sample test-case generated by MBT, frozen just to test the translation.
	// The translator is invoked to translate it to a testing task for the agent.
	Pair<GoalStructure,Integer>  sample1(TestAgent agent) {
	       
	       String[] transitions_ ={
	    		   "b_0 -- EXPLORE --> d_1_m",
	    		   "d_1_m -- EXPLORE --> b_1",
	    		   "b_1 -- EXPLORE --> b_0",
	    		   "b_0 -- EXPLORE --> d_T_m",
	    		   "d_T_m -- EXPLORE --> d_1_m",
	    		   "d_1_m -- EXPLORE --> b_0"
	    		   };
	       
	       String[][] result = new String[transitions_.length][] ;
	       for (int k=0; k<transitions_.length; k++) {
	    	   result[k] = split3(transitions_[k]) ;
	       }
	       return convertTestcase_to_Testingtask(agent,result) ;
    }
	
	// A sample test-case generated by MBT, frozen just to test the translation.
	// The translator is invoked to translate it to a testing task for the agent.
	Pair<GoalStructure,Integer>  sample2(TestAgent agent) {
	       
		   String[] transitions_ = {
			   "b_0 -- EXPLORE --> b_1",
		       "b_1 -- TOGGLE --> b_1",	       
		       "b_1 -- TOGGLE --> b_1",
		       "b_1 -- TOGGLE --> b_1",
		       "b_1 -- EXPLORE --> b_0",
		       "b_0 -- EXPLORE --> d_T_m",
		       "d_T_m -- EXPLORE --> b_0",
		       "b_0 -- EXPLORE --> d_1_m",
		       "d_1_m -- EXPLORE --> d_1_p",
		       "d_1_p -- EXPLORE --> b_2",
		       "b_2 -- TOGGLE --> b_2",
		       "b_2 -- EXPLORE --> d_1_p" 
		   } ;

	       String[][] result = new String[transitions_.length][] ;
	       for (int k=0; k<transitions_.length; k++) {
	    	   result[k] = split3(transitions_[k]) ;
	       }
	       return convertTestcase_to_Testingtask(agent,result) ;
    }
	
	Pair<GoalStructure,Integer>  sample3(TestAgent agent) {
		String[] transitions_ = {
				"b_0 -- TOGGLE --> b_m",
				"b_0 -- EXPLORE --> b_1",
				"b_1 -- EXPLORE --> d_T_m",
				"d_T_m -- EXPLORE --> d_1_m",
				"d_1_m -- EXPLORE --> b_0",
				"b_0 -- TOGGLE --> b_0",
				"b_0 -- EXPLORE --> b_1",
				"b_1 -- TOGGLE --> b_1",
				"b_1 -- TOGGLE --> b_1",
				"b_1 -- TOGGLE --> b_1",
				"b_1 -- EXPLORE --> d_T_m",
				"d_T_m -- EXPLORE --> b_0",
				"b_0 -- EXPLORE --> d_T_m",
				"d_T_m -- EXPLORE --> b_0",
				"b_0 -- EXPLORE --> b_1",
				"b_1 -- EXPLORE --> b_0",
				"b_0 -- EXPLORE --> d_T_m",
				"d_T_m -- EXPLORE --> b_0",
				"b_0 -- EXPLORE --> d_T_m",
				"d_T_m -- EXPLORE --> b_1",
				"b_1 -- EXPLORE --> d_T_m",
				"d_T_m -- EXPLORE --> d_1_m",
				"d_1_m -- EXPLORE --> d_1_p",
				"d_1_p -- EXPLORE --> b_2",
				"b_2 -- TOGGLE --> b_2",
				"b_2 -- EXPLORE --> d_1_p",
				"d_1_p -- EXPLORE --> b_2",
				"b_2 -- EXPLORE --> d_1_p",
				"d_1_p -- EXPLORE --> d_2_m",
				"d_2_m -- EXPLORE --> d_2_p",
				"d_2_p -- EXPLORE --> b_3",
				"b_3 -- EXPLORE --> d_2_p",
				"d_2_p -- EXPLORE --> b_3",
				"b_3 -- TOGGLE --> b_3",
				"b_3 -- EXPLORE --> d_2_p",
				"d_2_p -- EXPLORE --> b_3",
				"b_3 -- EXPLORE --> d_2_p",
				"d_2_p -- EXPLORE --> d_2_m",
				"d_2_m -- EXPLORE --> d_2_p"
		} ;
		String[][] result = new String[transitions_.length][] ;
	       for (int k=0; k<transitions_.length; k++) {
	    	   result[k] = split3(transitions_[k]) ;
	       }
	    return convertTestcase_to_Testingtask(agent,result) ;
	}
	
	
	/**
	 * Generating a test-suite using GA-MBT, then turn each to a testing-task for the agent.
	 * This returns a list of pairs (tt,k) where tt is a testing-task and k is the number
	 * of invariant-checks injected into tt (so, we expect them all to be passed).
	 */
	List<Pair<GoalStructure,Integer>>  generateWithGA(TestAgent agent) {
		// Setup a simple example of generating a single test-case using MBT:
        
		List<Pair<GoalStructure, Integer>> pairs = new ArrayList<>();
		
    	MBTProperties.SUT_EFSM = "labrecruits.buttons_doors_1" ;
    	GenerationStrategy generationStrategy = new SearchBasedStrategy<Chromosome>();
		SuiteChromosome solution = generationStrategy.generateTests();
    	
		// get a single (abstract) testcase ... let's pick the longest one then:
	    for(int k=0; k < solution.getTestChromosomes().size(); k++) {
	    	Testcase testcase = solution.getTestChromosome(k).getTestcase();
	    	Pair<GoalStructure, Integer> pair = convertTestcase_to_Testingtask(agent,testcase);
	    	pairs.add(pair);
	    }
	    // invoke the translator here:
	    return  pairs;
	}
    

    /**
     * A test to verify that the east closet is reachable.
     */
    @Test
    public void treasureDoorWorkingTest() throws InterruptedException {
    	

        
//        try {
        	if(TestSettings.USE_GRAPHICS) {
        		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
        		new Scanner(System.in). nextLine() ;
        	}
        	TestSettings.USE_SERVER_FOR_TEST = false;  
    		var testAgent = new LabRecruitsTestAgent("agent1");
            
	        // convert the MBT-testsuite to testing-tasks:
            //Pair<GoalStructure,Integer> tt = sample3(testAgent);
            List<Pair<GoalStructure,Integer>> tts = generateWithGA(testAgent);
            // loop over all tts
            for (Pair<GoalStructure, Integer> tt : tts) {
            	// Create an environment
            	var environment = new LabRecruitsEnvironment(new EnvironmentConfig("buttons_doors_1_FBK",Platform.LEVEL_PATH));
            	if(USE_INSTRUMENT) instrument(environment) ;

            	// create a test agent:
            	var beliefState = new BeliefState();
            	testAgent. attachState(beliefState)
            			. attachEnvironment(environment);

            	GoalStructure testingtask = tt.fst ;
	            int numberOfInvariants = tt.snd ;
	            
		        // attaching the goal/testing-task and test data-collector
		        var dataCollector = new TestDataCollector();
		        testAgent . setTestDataCollector(dataCollector) . setGoal(testingtask) ;

		        // Ok.. let's now run the test. This will auto-steer the test-agent. At each invariant-checking,
		        // the result of the checking will be added into the above data-collector.
		        
		        environment.startSimulation(); // this will press the "Play" button in the game for you
		        //goal not achieved yet
		        assertFalse(testAgent.success());
	
		        int i = 0 ;
		        // keep updating the agent
		        while (testingtask.getStatus().inProgress()) {
		        	System.out.println("*** " + i + ", " + testAgent.getState().id + " @" + testAgent.getState().worldmodel.position) ;
		            Thread.sleep(30);
		            i++ ; 
		        	testAgent.update();               
		        	if (i>800) {
		        		break ;
		        	}
		        } 
		        
		        // just printing the goals' status for information:
		        testingtask.printGoalStructureStatus();
		        
		        // Inspecting the test findings:
		        System.out.println("** #invariants to pass: " + numberOfInvariants) ; 
		        // check that we have passed all invariants:
		        assertEquals(numberOfInvariants,dataCollector.getNumberOfPassVerdictsSeen()) ;
		        // and also check that goal status is success
		        assertTrue(testAgent.success());
		        
		        environment.close();
            }
            System.out.println("** Nunber of test-cases: " + tts.size()) ;
       // }
//        finally { environment.close(); }
    }




}

