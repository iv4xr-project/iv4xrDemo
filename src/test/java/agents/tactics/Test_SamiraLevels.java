package agents.tactics;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;
import static nl.uu.cs.aplib.AplibEDSL.* ;

public class Test_SamiraLevels {

	private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	//TestSettings.USE_SERVER_FOR_TEST = false ;
    	// Uncomment this to make the game's graphic visible:
        // TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
       	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { if(labRecruitsTestServer != null) labRecruitsTestServer.close(); }

	
	/**
	 * Using entityStateRefreshed to simply refresh the state of door-1
	 * @throws InterruptedException
	 */
	@Test
	public void test1() throws InterruptedException {
		GoalStructure g = SEQ(
				GoalLib.entityInteracted("button2"),
				GoalLib.entityStateRefreshed("door1"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInteracted("button4"),
				GoalLib.entityStateRefreshed("door1"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInteracted("button5"),
				GoalLib.entityStateRefreshed("door1"), 
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInteracted("button3"),
				GoalLib.entityStateRefreshed("door1"),
				GoalLib.entityInteracted("button1"),
				GoalLib.entityStateRefreshed("door1"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInspected("door1", e -> e.getBooleanProperty("isOpen")),
				GoalLib.entityInteracted("button7")
				)
									
				;
		var desc = "blabla" ;
		var agent = SomeCommonTestUtils.create_and_deploy_testagent("samiratest_2","agent1",desc) ;
		SomeCommonTestUtils.setgoal_and_run_agent(agent,g,30,200) ;
	}
	
	/**
	 * Using entityInCloseRange to approach door1 (and refresh its state as we do so)
	 */
	@Test
	public void test2() throws InterruptedException {
		GoalStructure g = SEQ(
				GoalLib.entityInteracted("button2"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInteracted("button4"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInteracted("button5"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInteracted("button3"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInteracted("button1"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInspected("door1", e -> e.getBooleanProperty("isOpen")),
				GoalLib.entityInteracted("button7")
				) ;
		var desc = "blabla" ;
		var agent = SomeCommonTestUtils.create_and_deploy_testagent("samiratest_2","agent1",desc) ;
		SomeCommonTestUtils.setgoal_and_run_agent(agent,g,20,200) ;
	}
	
	@Test
	public void test3() throws InterruptedException {
		GoalStructure g = SEQ(
				GoalLib.entityInteracted("button2"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInteracted("button4"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInteracted("button5"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInteracted("button3"),
				GoalLib.entityInCloseRange("door1"),
				GoalLib.entityInCloseRange("button5"),
				GoalLib.entityInCloseRange("door2"),
				GoalLib.entityInspected("door2", e -> !e.getBooleanProperty("isOpen"))
				) ;
		var desc = "blabla" ;
		var agent = SomeCommonTestUtils.create_and_deploy_testagent("samiratest_3","agent1",desc) ;
		SomeCommonTestUtils.setgoal_and_run_agent(agent,g,30,200) ;
	}
}
