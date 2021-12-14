/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents;

import environments.AgentCommand;
import environments.LabRecruitsEnvironment;
import environments.Request;
import eu.iv4xr.framework.mainConcepts.EmotiveTestAgent;
import eu.iv4xr.framework.mainConcepts.SyntheticEventsProducer;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.multiAgentSupport.ComNode;
import world.BeliefState;

/**
 * A subclass of {@link eu.iv4xr.framework.mainConcepts.EmotiveTestAgent}, which
 * in turn is a subclass of {@link eu.iv4xr.framework.mainConcepts.TestAgent}.
 * So it is a test-agent, which can be used to test the Lab Recruits game. It
 * provides some overriding of inherited methods, but facilitating some
 * convenience type casting.
 * 
 * <p>
 * Being an emotive-agent means that we can also attach an emotion-state to it.
 */
public class LabRecruitsTestAgent extends EmotiveTestAgent {

	
    /**
     * The constructor for the test agent.
     */
	public LabRecruitsTestAgent(String id) {
		super(id,null) ;
    }
	
    /**
     * The constructor for the test agent with an id or role attached to itself (this is required for agent communication).
     */
    public LabRecruitsTestAgent(String id, String role) {
        super(id, role);
    }
    
    public LabRecruitsTestAgent attachState(BeliefState state) {
    	state.id = this.id ;
    	state.worldmodel.agentId = this.id ;
    	super.attachState(state);
    	return this ;
    }
    
    public LabRecruitsTestAgent attachEnvironment(LabRecruitsEnvironment env) {
    	super.attachEnvironment(env) ;
    	return this ;
    }
    
    @Override
    public LabRecruitsTestAgent attachSyntheticEventsProducer(SyntheticEventsProducer syntheticEventsProducer) {
    	super.attachSyntheticEventsProducer(syntheticEventsProducer) ;
    	return this ;
    }
    
    @Override
    public LabRecruitsTestAgent setGoal(GoalStructure g) {
    	super.setGoal(g) ;
    	return this ;
    }
    
    @Override
    public LabRecruitsTestAgent registerTo(ComNode comNode) {
    	super.registerTo(comNode) ;
    	return this ;
    }
    
    @Override
    public LabRecruitsTestAgent setTestDataCollector(TestDataCollector dc) {
    	super.setTestDataCollector(dc) ;
    	return this ;
    }

    public boolean success(){
        if(currentGoal != null){
            return currentGoal.getStatus().success();
        }
        if(lastHandledGoal != null){
            return lastHandledGoal.getStatus().success();
        }
        return false;
    }

    public void printStatus(){
        if(currentGoal != null){
            currentGoal.printGoalStructureStatus();
            return;
        }
        if(lastHandledGoal != null){
            lastHandledGoal.printGoalStructureStatus();
            return;
        }
        System.out.println("NO GOAL COMPLETED");
    }

    //public void refresh() {
        //getState().updateBelief(env().observe(getState().id));
    //}

    /**
     * Just another name of {@link #state()}. 
     */
    public BeliefState getState(){
        return state() ;
    }
    
    @Override
    public BeliefState state(){
        return (BeliefState) this.state;
    }
    
    @Override
    public EventsProducer getSyntheticEventsProducer() {
    	return (EventsProducer) this.syntheticEventsProducer ;
    }
    
    //@Override
    //public BeliefState state(){
    //    return (BeliefState) this.state;
    //}

    public LabRecruitsEnvironment env(){
        return state().env();
    }
}
