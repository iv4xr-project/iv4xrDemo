package agents.tactics;

import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.* ;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;
import static nl.uu.cs.aplib.AplibEDSL.* ;

public class Test_FBKLevels {
	
	private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
        //TestSettings.USE_SERVER_FOR_TEST = false ;
    	// Uncomment this to make the game's graphic visible:
        //TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
       	labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(labRecruitesExeRootDir) ;
    }

    @AfterAll
    static void close() { if(labRecruitsTestServer != null) labRecruitsTestServer.close(); }

    /**
     * Use this to test a broken level fbkrandom2. The room just north of doo16 contains
     * broken mesh. This is now fixed in the level, so the test below will succeed. But
     * the problem of broken mesh remaims. TO DO
     * 
     * We need to implement a mesh fixing algorithm. Essentially when a triangle corner is
     * not aligned to the corners of adjacent triangles, but still lies on one of their sides,
     * we can add connection. But this is not done. TO DO.
     */
    @Test
	public void test1() throws InterruptedException {
    	
    	Goal g = goal("explore").toSolve((BeliefState S) -> false) 
    			.withTactic(
    					FIRSTof(TacticLib.explore(),
    							ABORT()))
    			
    			;
    
    	var desc = "blabla" ;
		var agent = SomeCommonTestUtils.create_and_deploy_testagent("fbkrandom2","Agent1",desc) ;
		SomeCommonTestUtils.setgoal_and_run_agent(agent,g.lift(),30,200) ;
		
		assertTrue(agent.state().worldmodel().getElement("b16") != null) ;
		assertTrue(agent.state().worldmodel().getElement("b25") != null) ;
    }
}
