/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package game;

import agents.LabRecruitsTestAgent;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.spatial.Vec3;
import helperclasses.datastructures.linq.QArrayList;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

import org.junit.jupiter.api.Assertions ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import world.BeliefState;

import java.util.function.Predicate;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;

public class SimpleUnityTest {

    private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
        if(USE_SERVER_FOR_TEST){
        	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
            labRecruitsTestServer = new LabRecruitsTestServer(
                    USE_GRAPHICS,
                    Platform.PathToLabRecruitsExecutable(labRecruitesExeRootDir));
            labRecruitsTestServer.waitForGameToLoad();
        }
    }

    @AfterAll
    static void close() {
        if(USE_SERVER_FOR_TEST)
            labRecruitsTestServer.close();
    }

    @Test
    public void observePositionTest() {

        var config = new LabRecruitsConfig("observePositionTest")
                .replaceSeed(500)
                .replaceFireSpreadSpeed(2f);

        LabRecruitsEnvironment environment = new LabRecruitsEnvironment(config);
        if(USE_INSTRUMENT)
            environment.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();

        // create the agent
        var agent = new LabRecruitsTestAgent("agent0")
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

        // The agent wants to know its position
        GoalStructure goal = goal(config.level_name)
                .toSolve((BeliefState belief) -> belief.worldmodel.position != null)
                // the agent should find its position just by observing
                .withTactic(TacticLib.observe())
                .lift();

        agent.setGoal(goal);

        //goal not achieved yet
        Assertions.assertTrue(goal.getStatus().inProgress());

        // Toggle play in Unity
        Assertions.assertTrue(environment.startSimulation());

        //update one round
        agent.update();

        //agent should now know where it is
        Assertions.assertFalse(goal.getStatus().inProgress());
        goal.printGoalStructureStatus();

        environment.close();
    }

    @Test
    public void observeSwitchTest() {

        var config = new LabRecruitsConfig("observeSwitchTest");

        LabRecruitsEnvironment environment = new LabRecruitsEnvironment(config);
        if(USE_INSTRUMENT)
            environment.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();

        // create the agent
        var agent = new LabRecruitsTestAgent("agent0")
        		    . attachState(new BeliefState())
        		    . attachEnvironment(environment);

        GoalStructure goal = goal(config.level_name)
                .toSolve((BeliefState belief) -> 
                    {
                        if (belief.knownEntities().size() != 1) return false;
                        var button = belief.worldmodel.getElement("button0");
                        return 
                            button != null && 
                            button.position.x == 1 && 
                            button.position.z == 1;
                    })
                .withTactic(TacticLib.observe()) // the agent should be able to see the button by observing
                .lift();

        agent.setGoal(goal);

        //goal not achieved yet
        Assertions.assertTrue(goal.getStatus().inProgress());

        // Toggle play in Unity
        Assertions.assertTrue(environment.startSimulation());

        //update one round
        agent.update();

        //agent should now know where it is
        Assertions.assertFalse(goal.getStatus().inProgress());

        goal.printGoalStructureStatus();

        environment.close();
    }

    /**
     * The agent should only be able to see 1 of the 5 switches. The other 4 are hidden behind walls..
     */
    @Test
    public void observeVisibleSwitchesTest() {

        var config = new LabRecruitsConfig("observeVisibleSwitches");

        LabRecruitsEnvironment environment = new LabRecruitsEnvironment(config);
        if(USE_INSTRUMENT)
            environment.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();

        // create the agent
        var agent = new LabRecruitsTestAgent("agent0")
    		        . attachState(new BeliefState())
    		        . attachEnvironment(environment);

        GoalStructure goal = goal(config.level_name)
                .toSolve((BeliefState belief) -> {
                    var button = belief.worldmodel.getElement("button1");
                    return 
                        button != null && 
                        button.position.x == 3 && 
                        button.position.z == 1;
                })
                .withTactic(TacticLib.observe()) // the agent should be able to see the button by observing
                .lift();

        agent.setGoal(goal);

        //goal not achieved yet
        Assertions.assertTrue(goal.getStatus().inProgress());

        // Toggle play in Unity
        Assertions.assertTrue(environment.startSimulation());

        //update one round
        agent.update();
        
        //agent should now know where it is
        Assertions.assertFalse(goal.getStatus().inProgress());

        goal.printGoalStructureStatus();

        environment.close();
    }

    @Test
    public void moveToButtonTest() throws InterruptedException {
        var config = new LabRecruitsConfig("moveToButton");

        LabRecruitsEnvironment environment = new LabRecruitsEnvironment(config);
        if(USE_INSTRUMENT)
            environment.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();

        // create the agent
        var agent = new LabRecruitsTestAgent("agent0")
    		        . attachState(new BeliefState())
    		        . attachEnvironment(environment);

        GoalStructure goal = SEQ(
                // 'entityInspected' breaks this test --Naraenda
        		GoalLib.positionInCloseRange(new Vec3(1,0,1)).lift(),
                GoalLib.entityInspected("button0", e -> ! e.getBooleanProperty("isOn")),
                GoalLib.entityInteracted("button0"),
                GoalLib.entityInspected("button0", e -> e.getBooleanProperty("isOn")),
                GoalLib.positionInCloseRange(new Vec3(1,0,1)).lift()
        );

        agent.setGoal(goal);

        //goal not achieved yet
        Assertions.assertTrue(goal.getStatus().inProgress());

        // Toggle play in Unity
        Assertions.assertTrue(environment.startSimulation());

        int i = 0 ;
        while (goal.getStatus().inProgress()) {
            agent.update();
            System.out.println("*** " + i + ": " + agent.state().id + " @" + agent.state().worldmodel.position) ;
            System.out.println("Can interact: " + agent.state().canInteract("button0"));
            if (i>90) {
            	   break ;
            }
            Thread.sleep(30);
            i++ ;
        }
        
        goal.printGoalStructureStatus();


        //agent should now know where it is
        Assertions.assertFalse(goal.getStatus().inProgress());
        
        environment.close();
    }
}
