package bdd.steps;

import org.junit.jupiter.api.Assertions;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.GoalLib;
import bdd.state.ButtonInteractionState;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
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

	/*
	 * Scenario steps
	 */

	@Given("the LabRecruits game starts")
	public void the_labrecruits_game_starts_with_graphics() {
		System.out.println("@Given: the LabRecruits game starts");
		TestSettings.USE_GRAPHICS = button_state.isLabRecruitsGraphics();

		LabRecruitsTestServer labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(button_state.getLabRecruitsPath());
		button_state.setLabRecruitsTestServer(labRecruitsTestServer);

		var config = new LabRecruitsConfig(button_state.getLabRecruitsLevel());
		button_state.setLabRecruitsEnvironment(new LabRecruitsEnvironment(config));
	}

	@When("the agent {string} interacts with the button {string}")
	public void the_agent_interacts_with_the_button(String agent, String button) throws InterruptedException {
		System.out.println("@When: the agent {string} interacts with the button: " + button);

		LabRecruitsTestAgent test_agent = new LabRecruitsTestAgent(agent)
				.attachState(new BeliefState())
				.attachEnvironment(button_state.getLabRecruitsEnvironment());

		GoalStructure goal = GoalLib.entityInteracted(button);

		test_agent.setGoal(goal);

		button_state.setLabRecruitsTestAgent(test_agent);

		int i = 0;
		test_agent.update();
		i = 1;
		while (goal.getStatus().inProgress()) {
			test_agent.update();
			i++;
			Thread.sleep(30);
			if (i>=100) break;
		}

		Assertions.assertTrue(goal.getStatus().success());
		goal.printGoalStructureStatus();
	}

	@Then("the agent health is minimum {string}")
	public void the_agent_health_is_minimum(String health) {
		System.out.println("@Then: the agent health is minimum: " + health);
		int test_agent_health = button_state.getLabRecruitsTestAgent().state().env().obs.agent.health;
		Assertions.assertTrue(test_agent_health >= Integer.parseInt(health));
	}

	//TODO: If previous Then step fails, this stop game function is not invoked
	@Then("the LabRecruits game stops")
	public void the_labrecruits_game_stops() {
		System.out.println("@Then: the LabRecruits game stops");
		button_state.getLabRecruitsEnvironment().close();
		button_state.getLabRecruitsTestServer().close();
	}

}
