package bdd.steps;

import static nl.uu.cs.aplib.AplibEDSL.goal;

import org.junit.jupiter.api.Assertions;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import agents.tactics.TacticLib;
import bdd.state.LoadLevelState;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import game.LabRecruitsTestServer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;

public class LoadLevelSteps {

	private final LoadLevelState load_state;

	public LoadLevelSteps() {
		load_state = new LoadLevelState();
	}

	@Given("the path {string}")
	public void the_path(String path) {
		load_state.setLabRecruitsPath(path);
		System.out.println("@Given: the path: " + path);
	}

	@Given("the level {string}")
	public void the_level(String level) {
		load_state.setLabRecruitsLevel(level);
		System.out.println("@Given: the level: " + level);
	}

	@Given("the graphics {string}")
	public void the_graphics(String graphics) {
		load_state.setLabRecruitsGraphics(Boolean.parseBoolean(graphics));
		System.out.println("@Given: the graphics: " + graphics);
	}

	@When("the game starts")
	public void the_game_starts() {
		System.out.println("@When: the game starts");

		TestSettings.USE_GRAPHICS = load_state.isLabRecruitsGraphics();

		LabRecruitsTestServer labRecruitsTestServer = TestSettings.start_LabRecruitsTestServer(load_state.getLabRecruitsPath());
		load_state.setLabRecruitsTestServer(labRecruitsTestServer);

		var config = new LabRecruitsConfig(load_state.getLabRecruitsLevel());
		load_state.setLabRecruitsEnvironment(new LabRecruitsEnvironment(config));
	}

	@Then("the agent {string} observes the entity {string}")
	public void the_agent_observes_the_entity(String agent, String entity) throws InterruptedException {
		System.out.println("@Then: the agent {string} observes the entity {string}");
		LabRecruitsTestAgent test_agent = new LabRecruitsTestAgent(agent)
				.attachState(new BeliefState())
				.attachEnvironment(load_state.getLabRecruitsEnvironment());

		// The agent wants to observe the entity
		GoalStructure goal = goal("Observe the desired entity: " + entity)
				.toSolve((BeliefState belief) -> belief.worldmodel.getElement(entity) != null)
				.withTactic(TacticLib.observe())
				.lift();
		test_agent.setGoal(goal);

		int i = 0;
		test_agent.update();
		i = 1;
		while (goal.getStatus().inProgress()) {
			test_agent.update();
			i++;
			Thread.sleep(30);
			if (i>=10) break;
		}

		Assertions.assertTrue(goal.getStatus().success());
		goal.printGoalStructureStatus();

		load_state.getLabRecruitsEnvironment().close();
		load_state.getLabRecruitsTestServer().close();
	}

}
