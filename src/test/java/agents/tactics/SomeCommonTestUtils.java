package agents.tactics;

import java.util.Scanner;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;

public class SomeCommonTestUtils {
	
	public static void hit_RETURN() {
		if(TestSettings.USE_GRAPHICS) {
    		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
    		new Scanner(System.in) . nextLine() ;
    	}
	}
	
	/**
	 * A generic method to create a test-agent, and connect it to the LR game, and 
	 * load the specified level.
	 */
	public static LabRecruitsTestAgent create_and_deploy_testagent(String levelName, String agentId, String testDescription) {
        System.out.println("======= Level: " + levelName + ", " + testDescription) ;
		
        var environment = new LabRecruitsEnvironment(new LabRecruitsConfig(levelName));
        
        LabRecruitsTestAgent agent = new LabRecruitsTestAgent(agentId)
        		                     . attachState(new BeliefState())
        		                     . attachEnvironment(environment) ;
        return agent ;
	}
    
	/**
	 * A convenience method to assign a goal to a test agent, and run it on the LR instance
	 * it is connected to.
	 * At the end, the method checks if the goal is indeed achieved.
	 */
	public static void setgoal_and_run_agent(
			LabRecruitsTestAgent agent, 
			GoalStructure g, 
			int sleepTime,
			int terminationThreshold) throws InterruptedException {
    	
        // give the goal to the agent:
        agent.setGoal(g) ;
		hit_RETURN() ;

        // press play in Unity
        if (! agent.env().startSimulation()) throw new InterruptedException("Unity refuses to start the Simulation!");

        // now run the agent:
        int i = 0 ;
        while (g.getStatus().inProgress()) {
            agent.update();
            System.out.println("*** " + i + "/" 
               + agent.state().worldmodel.timestamp + ", "
               + agent.state().id + " @" + agent.state().worldmodel.position) ;
            Thread.sleep(sleepTime);
            i++ ;
            if (i>terminationThreshold) {
            	break ;
            }
        }
        g.printGoalStructureStatus();
        // check that the given goal is solved:
        //assertTrue(g.getStatus().success()) ;
        hit_RETURN() ;

	}

}
