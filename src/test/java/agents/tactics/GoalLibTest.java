package agents.tactics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Scanner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import game.LabRecruitsTestServer;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import static nl.uu.cs.aplib.AplibEDSL.* ;
import world.BeliefState;

public class GoalLibTest {
	
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

	void hit_RETURN() {
		if(TestSettings.USE_GRAPHICS) {
    		System.out.println("You can drag then game window elsewhere for beter viewing. Then hit RETURN to continue.") ;
    		new Scanner(System.in) . nextLine() ;
    	}
	}
	
	/**
	 * A generic method to create a test-agent, and connect it to the LR game, and 
	 * load the specified level.
	 */
	LabRecruitsTestAgent create_and_deploy_testagent(String levelName, String agentId, String testDescription) {
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
	void setgoal_and_run_agent(
			LabRecruitsTestAgent agent, 
			GoalStructure g, 
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
            Thread.sleep(30);
            i++ ;
            if (i>terminationThreshold) {
            	break ;
            }
        }
        g.printGoalStructureStatus();
        // hit_RETURN() ;
        // check that the given goal is solved:
        assertTrue(g.getStatus().success()) ;
	}
	
    
    @Test
    public void test_positionIsInCloseRange() throws InterruptedException {
    	var targetLocation1 = new Vec3(9,0,1) ;
    	GoalStructure g = GoalLib.positionInCloseRange(targetLocation1).lift() ;
    	var desc = ", target location: " + targetLocation1 ;
    	var agent = create_and_deploy_testagent("smallmaze","agent1",desc) ;
    	setgoal_and_run_agent(agent,g,120) ;

    	targetLocation1 = new Vec3(9,0,1) ;
    	var targetLocation2 = new Vec3(2.5f,0,3) ;
    	g = SEQ(GoalLib.positionInCloseRange(targetLocation1).lift(),
    			GoalLib.positionInCloseRange(targetLocation2).lift()) ;
    	desc = ", targets: " + targetLocation1 + " then " + targetLocation2 ;
    	agent = create_and_deploy_testagent("buttons_doors_1","agent1",desc) ;
    	setgoal_and_run_agent(agent,g,120) ;    
    }
    
    @Test
    public void test_atBGF() throws InterruptedException {
    	String target = "button1" ;
    	float delta = 0.6f ;
    	GoalStructure g = GoalLib.atBGF(target,delta,false) ;
    	var desc = ", target entity: " + target ;
    	var agent = create_and_deploy_testagent("buttons_doors_1","agent1",desc) ;
    	setgoal_and_run_agent(agent,g,120) ;
    	
    	var targetPos = agent.getState().worldmodel().getElement(target).getFloorPosition() ;
    	var agentPos = agent.getState().worldmodel().getFloorPosition() ;
    	assertTrue(Vec3.dist(agentPos,targetPos) <= delta) ;
    	
    	target = "FLAG" ;
    	g = GoalLib.atBGF(target,delta,true) ;
    	desc = ", target entity: " + target ;
    	agent = create_and_deploy_testagent("visibilitytest","BOSS",desc) ;
    	setgoal_and_run_agent(agent,g,120) ;
    	
    	targetPos = agent.getState().worldmodel().getElement(target).getFloorPosition() ;
    	agentPos = agent.getState().worldmodel().getFloorPosition() ;
    	assertTrue(Vec3.dist(agentPos,targetPos) <= delta) ;
    	assertTrue(agent.getState().worldmodel().score >= 100) ;
    	
    	target = "FLAG" ;
    	g = SEQ(GoalLib.entityInteracted("button0"),
    			GoalLib.atBGF(target,delta,true)) ;
    	desc = ", target entity: " + target ;
    	agent = create_and_deploy_testagent("visibilitytest","agent0",desc) ;
    	setgoal_and_run_agent(agent,g,120) ;
    	
    	//var obs = agent.env().observe("agent0") ;
    	//agent.update();
    	//agent.update();
    	targetPos = agent.getState().worldmodel().getElement(target).getFloorPosition() ;
    	agentPos = agent.getState().worldmodel().getFloorPosition() ;
    	assertTrue(Vec3.dist(agentPos,targetPos) <= delta) ;
    	assertTrue(agent.getState().worldmodel().score >= 100) ;
    }
    
    @Test
    public void test_atBGF_on_FBKmediumlevel() throws InterruptedException {
    	String goalflag = "gf0" ;
    	float delta = 0.5f ;
    	var desc = ", target entity: " + goalflag ;
    	var agent = create_and_deploy_testagent("fbk_mediumlevel","Agent1",desc) ;
    	agent.setTestDataCollector(new TestDataCollector()) ;
    	
    	GoalStructure g = SEQ(
    			GoalLib.entityStateRefreshed("door0"),
    			GoalLib.entityStateRefreshed("b0"),
    			GoalLib.entityInteracted("b0"),
    			GoalLib.entityStateRefreshed("door2"),
    			GoalLib.entityStateRefreshed("door0"),
    			GoalLib.entityStateRefreshed("door2"),
    			GoalLib.entityStateRefreshed("door0"),
    			GoalLib.entityStateRefreshed("b0"),
    			GoalLib.entityStateRefreshed("door2"),
    			GoalLib.atBGF(goalflag,delta,false),
    			GoalLib.invariantChecked(agent, 
    					"agent is close to " + goalflag, 
    					S -> {
    						var e = S.worldmodel().getElement(goalflag) ;
    						return Vec3.dist(S.worldmodel().getFloorPosition(), e.getFloorPosition()) <= delta ;
    					})
        )
    	;
    	/*
    	 b0-{explore[EXPLORE];}->d0m
d0m-{explore[EXPLORE];}->b0
b0-{toggle[TOGGLE];}->b0
b0-{explore[EXPLORE];}->d2m
d2m-{explore[EXPLORE];}->d0m
d0m-{explore[EXPLORE];}->d2m
d2m-{explore[EXPLORE];}->d0m
d0m-{explore[EXPLORE];}->b0
b0-{explore[EXPLORE];}->d2m
d2m-{explore[EXPLORE];}->d2p
d2p-{explore[EXPLORE];}->gf0
gf0-{explore[EXPLORE];}->d1p
d1p-{explore[EXPLORE];}->d2p
d2p-{explore[EXPLORE];}->d2m
d2m-{explore[EXPLORE];}->b0
    	 */
    	setgoal_and_run_agent(agent,g,600) ;
    	
    	var targetPos = agent.getState().worldmodel().getElement(goalflag).getFloorPosition() ;
    	var agentPos = agent.getState().worldmodel().getFloorPosition() ;
    	assertTrue(Vec3.dist(agentPos,targetPos) <= delta) ;
    	assertEquals(0,agent.getTestDataCollector().getNumberOfFailVerdictsSeen()) ;
    }
    
	
	@Test
	public void test_entityStateRefreshed() throws InterruptedException {
		var button1 = "button1" ;
		GoalStructure g = GoalLib.entityStateRefreshed(button1) ;
		var desc = ", target entity: " + button1 ;
		var agent = create_and_deploy_testagent("smallmaze","agent1",desc) ;
		setgoal_and_run_agent(agent,g,120) ;
		BeliefState state = (BeliefState) agent.state() ;
		assertTrue(state.worldmodel.getElement(button1) != null) ;
		assertFalse(state.isOn(button1)) ;
		
		button1 = "button1" ;
		var button2 = "button2" ;
		g = SEQ(GoalLib.entityStateRefreshed(button1),
				GoalLib.entityStateRefreshed(button2))  ;
		desc = ", target entity: " + button1 + " then" + button2 ;
		agent = create_and_deploy_testagent("buttons_doors_1","agent1",desc) ;
		setgoal_and_run_agent(agent,g,150) ;
		state = (BeliefState) agent.state() ;
		assertTrue(state.worldmodel.getElement(button1) != null) ;
		assertFalse(state.isOn(button1)) ;
		assertTrue(state.worldmodel.getElement(button2) != null) ;
		assertFalse(state.isOn(button2)) ;
	}
	
	@Test
	public void test_entityIsInteracted() throws InterruptedException {
		var button1 = "button1" ;
		var desc = ", target entity: " + button1 ;
		var agent = create_and_deploy_testagent("smallmaze","agent1",desc) ;
		agent.setTestDataCollector(new TestDataCollector()) ;
		GoalStructure g = 
			SEQ(GoalLib.entityInteracted(button1),
				GoalLib.entityInvariantChecked(agent,
					button1, 
	            	"" + button1 + " should be on", 
	            (WorldEntity e) -> e.getBooleanProperty("isOn")));
		setgoal_and_run_agent(agent,g,120) ;
		BeliefState state = (BeliefState) agent.state() ;
		assertTrue(state.worldmodel.getElement(button1) != null) ;
		assertTrue(state.isOn(button1)) ;
	    assertTrue(agent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
	    assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 1) ;
		
		
		button1 = "button1" ;
		var button2 = "button2" ;
		desc = ", target entity: " + button1 + " then" + button2 ;
		agent = create_and_deploy_testagent("buttons_doors_1","agent1",desc) ;
		agent.setTestDataCollector(new TestDataCollector()) ;
		
		g = SEQ(GoalLib.entityInteracted(button1),
				GoalLib.entityInvariantChecked(agent,
						button1, 
		            	"" + button1 + " should be on", 
		            	(WorldEntity e) -> e.getBooleanProperty("isOn")),
				GoalLib.entityInteracted(button2),
				GoalLib.entityInvariantChecked(agent,
						button2, 
		            	"" + button2 + " should be on", 
		            	(WorldEntity e) -> e.getBooleanProperty("isOn")))  ;
		
		setgoal_and_run_agent(agent,g,120) ;
		state = (BeliefState) agent.state() ;
		assertTrue(state.worldmodel.getElement(button1) != null) ;
		assertTrue(state.isOn(button1)) ;
		assertTrue(state.worldmodel.getElement(button2) != null) ;
		assertTrue(state.isOn(button2)) ;
	    assertTrue(agent.getTestDataCollector().getNumberOfFailVerdictsSeen() == 0) ;
	    assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
	}
	
	/**
	 * Using a level from Samira to test.
	 * @throws InterruptedException
	 */
	@Test
	public void samiratest() throws InterruptedException {
		var button2 = "button2" ;
		var button3 = "button3" ;
		var button5 = "button5" ;
		var door1 = "door1" ;
		var door4 = "door4" ;
		var door6 = "door6" ;
		
	
		var desc = ", the test is to check door6, which is not directly reachable. "  ;
		var agent = create_and_deploy_testagent("samiratest","agent1",desc) ;
		GoalStructure g = 
			SEQ(GoalLib.entityInteracted(button2),
				GoalLib.entityStateRefreshed(door1),
				GoalLib.entityInteracted(button3),
				GoalLib.entityStateRefreshed(door4),
				GoalLib.entityInteracted(button5),
				//GoalLib.entityStateRefreshed(door4), not needed
				GoalLib.entityStateRefreshed(door6));
		setgoal_and_run_agent(agent,g,300) ;
		BeliefState state = (BeliefState) agent.state() ;
		assertTrue(state.worldmodel.getElement(door6) != null) ;
		assertTrue(state.isOpen(door6)) ;
	}

}
