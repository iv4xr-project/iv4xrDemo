package bdd.steps;

import static nl.uu.cs.aplib.AplibEDSL.goal;

import org.junit.jupiter.api.Assertions;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import bdd.state.LR_ScenarioState;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.spatial.Vec3;
import game.LabRecruitsTestServer;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nl.uu.cs.aplib.AplibEDSL;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.ProgressStatus;
import world.BeliefState;

public class LR_ScenarioStepsDefinition {

	private final LR_ScenarioState scenarioState;
	
	public LR_ScenarioStepsDefinition() {
		scenarioState = new LR_ScenarioState();
	}

	/*
	 * Background Given steps
	 */

	@Given("the LabRecruits game path {string}")
	public void the_labrecruits_game_path(String path) {
		scenarioState.setLabRecruitsPath(path);
		System.out.println("@Given: the LabRecruits game path: " + path);
	}

	@Given("the game level {string}")
	public void the_labrecruits_game_level(String level) {
		scenarioState.setLabRecruitsLevel(level);
		System.out.println("@Given: the game level: " + level);
	}

	@Given("the graphics is {string}")
	public void the_labrecuits_game_graphics_is(String graphics) {
		scenarioState.setLabRecruitsGraphics(Boolean.parseBoolean(graphics));
		System.out.println("@Given: the graphics is: " + graphics);
	}
	
	@Given("the agent-id is {string}")
	public void the_agentId_is(String agentId) {
		System.out.println("@Given: the agent-id is: " + agentId);
		scenarioState.setAgentId(agentId);
	}
	
	@Given("the delay between update is {int} ms")
	public void the_delay_between_update(int delay) {
		scenarioState.setDelayBetweenUpdates((long) delay);
		System.out.println("@Given: delay between update: " + delay + " ms");
	}

	/*
	 * Scenario steps
	 */

	@Given("the game starts")
	public void the_labrecruits_game_starts() {	
		System.out.println("@Given: the game starts");
		TestSettings.USE_GRAPHICS = scenarioState.isLabRecruitsGraphics();
		
		if (scenarioState.getLabRecruitsTestServer() == null) {
			LabRecruitsTestServer labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(scenarioState.getLabRecruitsPath());
			scenarioState.setLabRecruitsTestServer(labRecruitsTestServer);
		}

		var config = new LabRecruitsConfig(scenarioState.getLabRecruitsLevel());
		scenarioState.setLabRecruitsEnvironment(new LabRecruitsEnvironment(config));

		LabRecruitsTestAgent test_agent = new LabRecruitsTestAgent(scenarioState.getAgentId())
				.attachState(new BeliefState())
				.attachEnvironment(scenarioState.getLabRecruitsEnvironment());
		
		scenarioState.setLabRecruitsTestAgent(test_agent);
	}
	
	
	ProgressStatus executeGoal(LabRecruitsTestAgent test_agent, GoalStructure goal) throws InterruptedException {
		return executeGoal(test_agent,goal,scenarioState.getGoalExecutionBudget()) ;
	}
	
	ProgressStatus executeGoal(LabRecruitsTestAgent test_agent, GoalStructure goal, int budget) throws InterruptedException {
		test_agent.setGoal(goal);
		int i = 0;
		test_agent.update();
		var belief = test_agent.getState() ; 
		i = 1;
		while (goal.getStatus().inProgress() 
				&& i<=budget
				&& belief.worldmodel().health > 0
				) {
			test_agent.update();
			i++;
			if (scenarioState.getDelayBetweenUpdates() > 0)
				Thread.sleep(scenarioState.getDelayBetweenUpdates());
		}
		return goal.getStatus() ;
	}

	@When("the agent interacts with the button {string}")
	public void the_agent_interacts_with_the_button(String button) throws InterruptedException {
		System.out.println("@When: the agent interacts with the button: " + button);
		GoalStructure goal = GoalLib.entityInteracted(button);
		var status = executeGoal(scenarioState.getLabRecruitsTestAgent(),goal) ;
		Assertions.assertTrue(status.success());
	}
	
	@When("the agent is close to the entity {string}")
	public void the_agent_is_close_to_the_entity(String e) throws InterruptedException {
		System.out.println("@When: the agent is close to the entity: " + e);
		
		//GoalStructure goal = GoalLib.atBGF(e, 0.7f, true, false) ; // add some extra update rounds, don't do healing
		GoalStructure goal = GoalLib.entityStateRefreshed2(e) ;
		var status = executeGoal(scenarioState.getLabRecruitsTestAgent(),goal) ;
		Assertions.assertTrue(status.success());
	}
	
	@When("the agent is close to the flag {string}")
	public void the_agent_is_close_to_the_flag(String e) throws InterruptedException {
		System.out.println("@When: the agent is close to the flag: " + e);
		
		GoalStructure goal = GoalLib.atBGF(e, 0.7f, true, false) ; // add some extra update rounds, don't do healing
		var status = executeGoal(scenarioState.getLabRecruitsTestAgent(),goal) ;
		Assertions.assertTrue(status.success());
	}
	
	@When("the agent is at {float},{float},{float}")
	public void the_agent_is_at(float x, float y, float z) throws InterruptedException {
		Vec3 q = new Vec3(x,y,z); 	
		System.out.println("@When: the agent is at: " + q);		
		GoalStructure goal = GoalLib.positionInCloseRange(q).lift() ;
		var status = executeGoal(scenarioState.getLabRecruitsTestAgent(),goal) ;
		Assertions.assertTrue(status.success());
	}
	
	@When("the agent has waited {int} turns")
	public void the_agent_has_waited(int k) throws InterruptedException {
		System.out.println("@When: the agent has waited: " + k + " turns");
		GoalStructure goal = AplibEDSL.FAIL() ; 
		var status = executeGoal(scenarioState.getLabRecruitsTestAgent(), AplibEDSL.FAIL(), k) ;
	}
	
	GoalStructure exploredOut() {
		return AplibEDSL.goal("explored-out")
		    .toSolve(S -> false)
		    .withTactic(AplibEDSL.FIRSTof(TacticLib.explore(), AplibEDSL.Abort().lift()))
		    .lift() 
		;
	}
	
	@When("the agent has explored the level") 
	public void the_agent_has_explored() throws InterruptedException {
		System.out.println("@When: the agent has re-explored the level");
		scenarioState.getLabRecruitsTestAgent().getState().pathfinder().wipeOutMemory();
		var status = executeGoal(scenarioState.getLabRecruitsTestAgent(),exploredOut()) ;
		Assertions.assertTrue(status.failed());
	}

	@Then("the agent health is {int}")
	public void the_agent_health_is(int health) {
		System.out.println("@Then: the agent health is: " + health);
		int test_agent_health = scenarioState.getLabRecruitsTestAgent().state().env().obs.agent.health;
		Assertions.assertTrue(test_agent_health == health);
	}
	
	@Then("the agent health is at least {int}")
	public void the_agent_health_is_atleast(int health) {
		System.out.println("@Then: the agent health is at least: " + health);
		int test_agent_health = scenarioState.getLabRecruitsTestAgent().state().env().obs.agent.health;
		Assertions.assertTrue(test_agent_health >= health);
	}
	
	@Then("the agent health is at most {int}")
	public void the_agent_health_is_atmost(int health) {
		System.out.println("@Then: the agent health is at most: " + health);
		int test_agent_health = scenarioState.getLabRecruitsTestAgent().state().env().obs.agent.health;
		Assertions.assertTrue(test_agent_health <= health);
	}
	
	@Then("the agent score is {int}")
	public void the_agent_score_is(int score) {
		System.out.println("@Then: the agent score is: " + score);
		int agent_score = scenarioState.getLabRecruitsTestAgent().state().env().obs.agent.score;
		Assertions.assertTrue(agent_score == score);
	}
	
	@Then("the agent score is less than {int}")
	public void the_agent_score_less_than(int score) {
		System.out.println("@Then: the agent score is less than: " + score);
		int agent_score = scenarioState.getLabRecruitsTestAgent().state().env().obs.agent.score;
		Assertions.assertTrue(agent_score < score);
	}
	
	@Then("the agent score is more than {int}")
	public void the_agent_score_more_than(int score) {
		System.out.println("@Then: the agent score is more than: " + score);
		int agent_score = scenarioState.getLabRecruitsTestAgent().state().env().obs.agent.score;
		Assertions.assertTrue(agent_score > score);
	}
	
	@Then("entity {string} is observed")
	public void this_entity_is_observed(String id) {
		System.out.println("@Then: entity " + id + " exists");
		var e = scenarioState.getLabRecruitsTestAgent().state().worldmodel().elements.get(id) ;
		Assertions.assertTrue(e != null);
	}
	
	@Then("door {string} is open")
	public void this_door_is_open(String door) {
		System.out.println("@Then: door " + door + " is open");
		var open = scenarioState.getLabRecruitsTestAgent().state().get(door) != null				
				   && scenarioState.getLabRecruitsTestAgent().state().isOpen(door) ;
		Assertions.assertTrue(open);
	}
	
	@Then("door {string} is closed")
	public void this_door_is_closed(String door) {
		System.out.println("@Then: door " + door + " is closed");
		var closed = scenarioState.getLabRecruitsTestAgent().state() != null		
				     && ! scenarioState.getLabRecruitsTestAgent().state().isOpen(door) ;
		Assertions.assertTrue(closed);
	}
	
	@Then("entity {string} is unreachable")
	public void this_entity_is_unreachable(String e) {
		System.out.println("@Then: entity " + e + " is unreachable");
		var wom = scenarioState.getLabRecruitsTestAgent().state().worldmodel() ;
		var e_ = wom.elements.get(e) ;
		if (e_ != null) {
			var path = scenarioState.getLabRecruitsTestAgent().state().pathfinder().findPath(wom.position, e_.position, 0.2f) ;
			Assertions.assertTrue(path == null);
		}	
	}
	
	@Then("entity {string} is reachable")
	public void this_entity_is_reachable(String e) {
		System.out.println("@Then: entity " + e + " is reachable");
		var wom = scenarioState.getLabRecruitsTestAgent().state().worldmodel() ;
		var e_ = wom.elements.get(e) ;
		Assertions.assertTrue(e_ != null);
		var path = scenarioState.getLabRecruitsTestAgent().state().pathfinder().findPath(wom.position, e_.position, 0.2f) ;
		Assertions.assertTrue(path == null);	
	}
	
	@After
	public void close_the_labrecruits_game(Scenario scenario){
		System.out.println("Closing the Lab Recruits game.");
		scenarioState.getLabRecruitsEnvironment().close();
		if (scenarioState.getLabRecruitsTestServer() != null) {
			scenarioState.getLabRecruitsTestServer().close();
			scenarioState.setLabRecruitsTestServer(null);
		}
	}
	
}
