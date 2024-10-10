package agents.demo.cleaned;

import static nl.uu.cs.aplib.AplibEDSL.Abort;
import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static nl.uu.cs.aplib.AplibEDSL.SUCCESS;
import static nl.uu.cs.aplib.AplibEDSL.action;
import static nl.uu.cs.aplib.AplibEDSL.goal;
import static nl.uu.cs.aplib.AplibEDSL.lift;

import agents.LabRecruitsTestAgent;
import agents.tactics.TacticLib;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.ProgressStatus;
import world.BeliefState;
import world.LabEntity;

public class Utils {
	
	public static void printShortStatus(LabRecruitsTestAgent agent, int turnNr) {
		BeliefState state = agent.state() ;
		System.out.println("*** " 
			+ turnNr + ", " + agent.getId() 
			+ " @" + state.worldmodel().position
			+ ", hp:" + state.worldmodel().health
			+ ", score:" + state.worldmodel().score) ;
	}
	
	/**
	 * Execute the given goal.
	 */
	public static ProgressStatus runGoal(LabRecruitsTestAgent agent, 
			GoalStructure G, 
			int numberOfTurns,
			long delayBetweenUpdates) throws InterruptedException {
		
		// give a first update turn:
		int i=0 ;
		agent.update();
		BeliefState state = agent.state() ;
		printShortStatus(agent,i) ;
		i++ ;
        
		while (G.getStatus().inProgress() 
				&& state.worldmodel().health > 0
				&& i < numberOfTurns
				) {
        	
			if (delayBetweenUpdates > 0)
				Thread.sleep(delayBetweenUpdates) ;
            
        	agent.update();
        	printShortStatus(agent,i) ;
        	i++ ;
        }
		
		return G.getStatus() ;
	}
	
	/**
	 * Check if the entity e is reachable from the current agent location.
	 */
	public static boolean isReachable(LabRecruitsTestAgent agent, String e) {
    	var e_ = (LabEntity) agent.state().get(e) ;
    	var wom = agent.state().worldmodel() ;
    	if (e_ != null) {
    		var path = agent.state().pathfinder().findPath(wom.getFloorPosition(), e_.getFloorPosition(), 0.2f) ;
    		return path != null ;
    	}
    	return false ;
    }
	
	/**
	 * This will re-explore the level. The currently known map is first wiped
	 * out from the agent's memory, and then explore is invoked.
	 */
	public static GoalStructure exploredOut() {
    	
    	Action wipeOutMem = action("wipe-out seen map")
    			.do1(S -> {
    				var B = (BeliefState) S ;
    				B.pathfinder().wipeOutMemory();
    				return true ;
    			}) ;
		
		return 
			SEQ(
				lift("wipe-out seen map", wipeOutMem),
				FIRSTof(goal("explored-out")
					.toSolve(S -> false)
					.withTactic(
							FIRSTof(TacticLib.explore(), Abort().lift()))
					.lift(),
					SUCCESS())) 
		;
    }
	
	/**
	 * Cause the agent to do nothing other than observing for k turns. 
	 */
	public static GoalStructure waitNturns(int k) {
		GoalStructure[] skips = new GoalStructure[k] ;
		for (int j=0; j<k; j++) {
			skips[j] = SUCCESS() ;
		}
		return SEQ(skips) ;
	}

}
