package bdd.steps;

import org.junit.jupiter.api.Assertions;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import bdd.state.ButtonInteractionState;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nl.uu.cs.aplib.AplibEDSL;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.ProgressStatus;
import world.BeliefState;

public class ButtonInteractionSteps {

	private final ButtonInteractionState button_state;

	public ButtonInteractionSteps() {
		button_state = new ButtonInteractionState();
	}

	/*
	 * Background Given steps
	 */

	@Given("the LabRecruits game path {string}")
	public void the_labrecruits_game_path(String path) {
		button_state.setLabRecruitsPath(path);
		System.out.println("@Given: the LabRecruits game path: " + path);
	}

	@Given("the LabRecruits game level {string}")
	public void the_labrecruits_game_level(String level) {
		button_state.setLabRecruitsLevel(level);
		System.out.println("@Given: the LabRecruits game level: " + level);
	}

	@Given("the LabRecruits graphics is {string}")
	public void the_labrecuits_game_graphics_is(String graphics) {
		button_state.setLabRecruitsGraphics(Boolean.parseBoolean(graphics));
		System.out.println("@Given: the LabRecruits graphics is: " + graphics);
	}
	
	@Given("the agent-id is {string}")
	public void the_agentId_is(String agentId) {
		System.out.println("@Given: the agent-id is: " + agentId);
		button_state.setAgentId(agentId);
	}

	/*
	 * Scenario steps
	 */

	@Given("the LabRecruits game starts")
	public void the_labrecruits_game_starts() {	
		System.out.println("@Given: the LabRecruits game starts");
		TestSettings.USE_GRAPHICS = button_state.isLabRecruitsGraphics();

		LabRecruitsTestServer labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(button_state.getLabRecruitsPath());
		button_state.setLabRecruitsTestServer(labRecruitsTestServer);

		var config = new LabRecruitsConfig(button_state.getLabRecruitsLevel());
		button_state.setLabRecruitsEnvironment(new LabRecruitsEnvironment(config));

		LabRecruitsTestAgent test_agent = new LabRecruitsTestAgent(button_state.getAgentId())
				.attachState(new BeliefState())
				.attachEnvironment(button_state.getLabRecruitsEnvironment());
		
		button_state.setLabRecruitsTestAgent(test_agent);
	}
	
	
	ProgressStatus executeGoal(LabRecruitsTestAgent test_agent, GoalStructure goal) throws InterruptedException {
		test_agent.setGoal(goal);
		int i = 0;
		test_agent.update();
		i = 1;
		while (goal.getStatus().inProgress() && i<=button_state.getGoalExecutionBudget()) {
			test_agent.update();
			i++;
			Thread.sleep(button_state.getDelayBetweenUpdates());
		}
		return goal.getStatus() ;
	}

	@When("the agent interacts with the button {string}")
	public void the_agent_interacts_with_the_button(String button) throws InterruptedException {
		System.out.println("@When: the agent interacts with the button: " + button);
		GoalStructure goal = GoalLib.entityInteracted(button);
		var status = executeGoal(button_state.getLabRecruitsTestAgent(),goal) ;
		Assertions.assertTrue(status.success());
	}
	
	@When("the agent is close to the entity {string}")
	public void the_agent_is_close_to_the_entity(String e) throws InterruptedException {
		System.out.println("@When: the agent is close to the entity: " + e);
		GoalStructure goal = GoalLib.atBGF(e, 0.7f, true, false) ; // add some extra update rounds, don't do healing
		var status = executeGoal(button_state.getLabRecruitsTestAgent(),goal) ;
		Assertions.assertTrue(status.success());
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
		button_state.getLabRecruitsTestAgent().getState().pathfinder().wipeOutMemory();
		var status = executeGoal(button_state.getLabRecruitsTestAgent(),exploredOut()) ;
		Assertions.assertTrue(status.failed());
	}

	@Then("the agent health is minimum {string}")
	public void the_agent_health_is_minimum(String health) {
		System.out.println("@Then: the agent health is minimum: " + health);
		int test_agent_health = button_state.getLabRecruitsTestAgent().state().env().obs.agent.health;
		Assertions.assertTrue(test_agent_health >= Integer.parseInt(health));
	}
	
	@Then("the agent health is at most {string}")
	public void the_agent_health_is_atmost(String health) {
		System.out.println("@Then: the agent health is at most: " + health);
		int test_agent_health = button_state.getLabRecruitsTestAgent().state().env().obs.agent.health;
		Assertions.assertTrue(test_agent_health <= Integer.parseInt(health));
	}
	
	@Then("the agent score is {string}")
	public void the_agent_score_is(String score) {
		System.out.println("@Then: the agent point is: " + score);
		int test_agent_health = button_state.getLabRecruitsTestAgent().state().env().obs.agent.score;
		Assertions.assertTrue(test_agent_health == Integer.parseInt(score));
	}
	
	@Then("entity {string} is observed")
	public void this_entity_is_observed(String id) {
		System.out.println("@Then: entity " + id + " exists");
		var e = button_state.getLabRecruitsTestAgent().state().worldmodel().elements.get(id) ;
		Assertions.assertTrue(e != null);
	}
	
	@Then("door {string} is open")
	public void this_door_is_open(String door) {
		System.out.println("@Then: door " + door + " is open");
		var open = button_state.getLabRecruitsTestAgent().state().isOpen(door) ;
		Assertions.assertTrue(open);
	}
	
	@Then("door {string} is closed")
	public void this_door_is_closed(String door) {
		System.out.println("@Then: door " + door + " is closed");
		var open = button_state.getLabRecruitsTestAgent().state().isOpen(door) ;
		Assertions.assertTrue(! open);
	}
	
	@Then("entity {string} is unreachable")
	public void this_entity_is_unreachable(String e) {
		System.out.println("@Then: entity " + e + " is unreachable");
		var wom = button_state.getLabRecruitsTestAgent().state().worldmodel() ;
		var e_ = wom.elements.get(e) ;
		if (e_ != null) {
			var path = button_state.getLabRecruitsTestAgent().state().pathfinder().findPath(wom.position, e_.position, 0.2f) ;
			Assertions.assertTrue(path == null);
		}	
	}
	
	@Then("entity {string} is reachable")
	public void this_entity_is_reachable(String e) {
		System.out.println("@Then: entity " + e + " is reachable");
		var wom = button_state.getLabRecruitsTestAgent().state().worldmodel() ;
		var e_ = wom.elements.get(e) ;
		Assertions.assertTrue(e_ != null);
		var path = button_state.getLabRecruitsTestAgent().state().pathfinder().findPath(wom.position, e_.position, 0.2f) ;
		Assertions.assertTrue(path == null);	
	}

	//TODO: If previous Then step fails, this stop game function is not invoked
	@Then("the LabRecruits game stops")
	public void the_labrecruits_game_stops() {
		System.out.println("@Then: the LabRecruits game stops");
		button_state.getLabRecruitsEnvironment().close();
		button_state.getLabRecruitsTestServer().close();
	}

}
