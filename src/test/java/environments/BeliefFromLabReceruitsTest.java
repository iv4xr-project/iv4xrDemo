package environments;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static nl.uu.cs.aplib.AplibEDSL.* ;
import nl.uu.cs.aplib.mainConcepts.* ;
import agents.LabRecruitsTestAgent;
import agents.TestSettings;
import eu.iv4xr.framework.spatial.Vec3;
import game.LabRecruitsTestServer;
import game.Platform;
import world.BeliefState;

public class BeliefFromLabReceruitsTest {

	private LabRecruitsTestServer labRecruitsTestServer ;

    @BeforeEach
    void start() {
    	// set this to true to make the game's graphic visible:
    	var useGraphics = false ;
    	SocketReaderWriter.debug = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
    	labRecruitsTestServer =new LabRecruitsTestServer(
    			useGraphics,
                Platform.PathToLabRecruitsExecutable(labRecruitesExeRootDir));
    	labRecruitsTestServer.waitForGameToLoad();
    }

    @AfterEach
    void close() { 
    	SocketReaderWriter.debug = false ;
    	if(labRecruitsTestServer!=null) labRecruitsTestServer.close(); 
    }
    
    void hit_RETURN() {
		System.out.println("Hit RETURN to continue.") ;
    	new Scanner(System.in) . nextLine() ;
	}
    
    /**
     * Create and deploy an agent with a dummy goal.
     */
    LabRecruitsTestAgent deployAgent(String agentName, String levelName) {
    	GoalStructure goal =  goal("g0")
    			. toSolve(S -> false)
    			. withTactic(action("a").do1(S -> S).lift())
    			. lift();
    	
    	var env = new LabRecruitsEnvironment(new LabRecruitsConfig(levelName)) ;
    	
    	//hit_RETURN() ;
    	
    	var agent = new LabRecruitsTestAgent(agentName) 
    			    . attachState(new BeliefState())
    			    . attachEnvironment(env) 
    			    . setGoal(goal) ;
    	
    	return agent ;
    }
    
    /**
     * Some basic checks the BeliefState.
     */
    @Test
    public void test0() {
    	assertTrue(labRecruitsTestServer != null) ;
        // test using the "minimal" level:
    	var level = "minimal" ;
    	System.out.println(">>> level: " + level) ;
    	var agent = deployAgent("agent0",level) ;
    	
    	assertTrue(agent.state().worldmodel != null) ;
    	assertTrue(agent.state().worldmodel.agentId.equals("agent0")) ;
    	
    	agent.update();
    	assertTrue(agent.state().worldmodel.getElement("button0") != null) ;
    	assertTrue(agent.state().isOn("button0") == false) ;
    }
    
    @Test
    public void test1() {
    	assertTrue(labRecruitsTestServer != null) ;
        // test using the "minimal" level:
    	var level = "square2" ;
    	System.out.println(">>> level: " + level) ;
    	var agent = deployAgent("agent0",level) ;
    	
    	assertTrue(agent.state().worldmodel != null) ;
    	assertTrue(agent.state().worldmodel.agentId.equals("agent0")) ;
    	
    	agent.update();
    	
    	var belief = agent.state() ;
    	var wom = agent.state().worldmodel ;
    	
    	assertTrue(wom.getElement("button0") != null) ;
    	assertTrue(belief.isOn("button0") == false) ;
    	assertTrue(belief.isOpen("door0") == false) ;
    	
    	assertTrue(wom.getElement("button1") != null) ;
    	assertTrue(wom.getElement("button0") != null) ;
    	assertTrue(wom.getElement("escreen0") != null) ;
    	assertTrue(wom.getElement("escreen0").getStringProperty("color").toString().equals("0.0/0.0/0.0")) ;
    	
    	
    	//hit_RETURN()  ;
    }
    
    @Test
    public void testPathfinding1() {
    	assertTrue(labRecruitsTestServer != null) ;
        // test using the "minimal" level:
    	var level = "squareWithDoorBetween" ;
    	System.out.println(">>> level: " + level) ;
    	var agent = deployAgent("agent0",level) ;
    	var belief = agent.state() ;
    	var wom = agent.state().worldmodel() ;
    	
    	agent.update();
    	
    	assertTrue(wom.getElement("button0") != null) ;
    	assertTrue(belief.isOpen("door0") == false) ;
    	var button = wom.getElement("button0") ;
    	var door = wom.getElement("door0") ;
    	System.out.println("Door position " + door.position) ;
     	System.out.println("Door floor position: " + door.getFloorPosition()) ;
     	System.out.println("Door extent " + door.extent) ;
     	
    	// the door should be recognized as an obstacle
    	assertTrue(belief.pathfinder().obstacles.size() == 1) ;
     	assertTrue(belief.pathfinder().obstacles.get(0).isBlocking == true) ;
     	assertTrue(belief.pathfinder().obstacles.get(0).obstacle == door) ;
     	
    	// the button is reachable
    	var result = belief.findPathTo(button.position,true) ;
    	assertTrue(result.snd.size() > 0) ;
     	
     	// literally the door position is not reachable:
     	result = belief.findPathTo(door.getFloorPosition(),true) ;
     	assertTrue(result == null) ;
     	//System.out.println(path) ;
     
     	// cheat; pretend all nodes are explored. Location (7,0,5) should
     	// not be reachable (blocked by the door):
     	belief.pathfinder().perfect_memory_pathfinding = true ;
     	result = belief.findPathTo(new Vec3(7f,0,5),true) ;
     	assertTrue(result == null) ;
     	//System.out.println(path) ;
     	
     	
     	// simulate that the door is open:
     	belief.pathfinder().obstacles.get(0).isBlocking = false ;
        	
     	// the door location should now be reachable:
     	result = belief.findPathTo(door.getFloorPosition(),true) ;
     	assertTrue(result.snd.size() > 0 ) ;
     	//System.out.println(path) ;
     	
     	// the location (7,0,5) should now also be reachable:
     	result = belief.findPathTo(new Vec3(7f,0,5),true) ;
     	assertTrue(result.snd.size() > 0 ) ;
     	//System.out.println(path) ;
    	
    }
    
    @Test
    public void testPathfinding2() {
    	assertTrue(labRecruitsTestServer != null) ;
        
    	// this level has two parts separated by a column of chairs; the 2nd part
    	// should not be reachable
    	var level = "squareWithOneButtonAndChairsBetween" ;
    	System.out.println(">>> level: " + level) ;
    	var agent = deployAgent("agent0",level) ;
    	var belief = agent.state() ;
    	var wom = agent.state().worldmodel() ;
    	
    	agent.update();
    	
    	// override pathfinder to pretend all nodes are explored. 
    	belief.pathfinder().perfect_memory_pathfinding = true ;

    	// Location (5,0,5) should NOTE be reachable (blocked by the door):
    	var result = belief.findPathTo(new Vec3(5,0,5),true) ;
     	assertTrue(result == null) ;
     	
     	// this level has a gap, so (5,0,5) should be reachable:
     	level = "squareWithOneButtonAndChairsBetween_andHole" ;
    	System.out.println(">>> level: " + level) ;
    	agent = deployAgent("agent0",level) ;
    	belief = agent.state() ;
    	wom = agent.state().worldmodel() ;
    	
    	agent.update();
    	
    	belief.pathfinder().perfect_memory_pathfinding = true ;
    	result = belief.findPathTo(new Vec3(5,0,5),true) ;
     	assertTrue(result.snd.size()>0) ;
     	
     	// this level has a door, so (9,0,1) should not be reachable:
     	level = "longcorridorWithDoorBetween" ;
    	System.out.println(">>> level: " + level) ;
    	agent = deployAgent("agent0",level) ;
    	belief = agent.state() ;
    	wom = agent.state().worldmodel() ;
    	
    	agent.update();
    	
    	var door = wom.getElement("door0") ;
    	
    	System.out.println("Door position " + door.position) ;
     	System.out.println("Door floor position: " + door.getFloorPosition()) ;
     	System.out.println("Door extent " + door.extent) ;
    	
        // override pathfinder to pretend all nodes are explored. 
    	belief.pathfinder().perfect_memory_pathfinding = true ;

    	// location (9,0,1) and the door should NOT be reachable:
    	result = belief.findPathTo(new Vec3(9,0,1),true) ;
     	assertTrue(result==null) ;
     	result = belief.findPathTo(door.getFloorPosition(),true) ;
     	assertTrue(result==null) ;
     	
     	
        // simulate that the door is open:
     	belief.pathfinder().obstacles.get(0).isBlocking = false ;
        	
     	// location (9,0,1) and the door location should now be reachable:
     	result = belief.findPathTo(new Vec3(9,0,1),true) ;
     	assertTrue(result.snd.size() > 0 ) ;
     	result = belief.findPathTo(door.getFloorPosition(),true) ;
     	assertTrue(result.snd.size() > 0 ) ;
     	//System.out.println(path) ;
    }
}
