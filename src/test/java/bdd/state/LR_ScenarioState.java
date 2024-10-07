package bdd.state;

import agents.LabRecruitsTestAgent;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;

public class LR_ScenarioState {

	private String labRecruitsPath;
	private String labRecruitsLevel;
	private String agentId ;
	private boolean labRecruitsGraphics;
	//private LabRecruitsTestServer labRecruitsTestServer;
	private LabRecruitsEnvironment labRecruitsEnvironment;
	private LabRecruitsTestAgent labRecruitsTestAgent;
	
	/**
	 * Budget for executing a top-level goal-structure, when it is given to the agent.
	 * <p>Default: 1000 turns.
	 */
	private int goalExecutionBudget = 1000 ;
	
	/**
	 * Delay in ms between agent-updates. Default is 30.
	 */
	private long delayBetweenUpdates = 30 ;

	public String getLabRecruitsPath() {
		return labRecruitsPath;
	}

	public void setLabRecruitsPath(final String labRecruitsPath) {
		this.labRecruitsPath = labRecruitsPath;
	}

	public String getLabRecruitsLevel() {
		return labRecruitsLevel;
	}

	public void setLabRecruitsLevel(final String labRecruitsLevel) {
		this.labRecruitsLevel = labRecruitsLevel;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public boolean isLabRecruitsGraphics() {
		return labRecruitsGraphics;
	}

	public void setLabRecruitsGraphics(boolean labRecruitsGraphics) {
		this.labRecruitsGraphics = labRecruitsGraphics;
	}

	public LabRecruitsTestServer getLabRecruitsTestServer() {
		return LRserverInstance.LRserver ;
	}

	public void setLabRecruitsTestServer(LabRecruitsTestServer labRecruitsTestServer) {
		LRserverInstance.LRserver = labRecruitsTestServer;
	}

	public LabRecruitsEnvironment getLabRecruitsEnvironment() {
		return labRecruitsEnvironment;
	}

	public void setLabRecruitsEnvironment(final LabRecruitsEnvironment labRecruitsEnvironment) {
		this.labRecruitsEnvironment = labRecruitsEnvironment;
	}

	public LabRecruitsTestAgent getLabRecruitsTestAgent() {
		return labRecruitsTestAgent;
	}

	public void setLabRecruitsTestAgent(LabRecruitsTestAgent labRecruitsTestAgent) {
		this.labRecruitsTestAgent = labRecruitsTestAgent;
	}

	public int getGoalExecutionBudget() {
		return goalExecutionBudget;
	}

	public void setGoalExecutionBudget(int goalExecutionBudget) {
		this.goalExecutionBudget = goalExecutionBudget;
	}

	public long getDelayBetweenUpdates() {
		return delayBetweenUpdates;
	}

	public void setDelayBetweenUpdates(long delayBetweenUpdates) {
		this.delayBetweenUpdates = delayBetweenUpdates;
	}

}
