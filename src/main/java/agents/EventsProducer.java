package agents;

import java.util.*;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.multiAgentSupport.ComNode;
import nl.uu.cs.aplib.multiAgentSupport.Message;
import nl.uu.cs.aplib.multiAgentSupport.Message.MsgCastType;
import world.LabEntity;

/**
 * This class interprets a Lab Recruit states and can produces events signaling
 * certain interesting things it observes in the state. Events will be represented
 * as instances of the class nl.uu.cs.aplib.multiAgentSupport.Message. Generated
 * instances will be placed in a list which can be inspected. They will also be
 * sent to a ComNode, if one is provided.
 */
public class EventsProducer {
	
    public static final String LabRecruits = "LabRecruits" ;
    
    static Message mkLabEvent(int priority, String eventName) { 
        return new Message(LabRecruits,priority,MsgCastType.BROADCAST,"",eventName) ;
    }
    
    public static final String OuchEventName = "Ouch" ;
    public static final String OpeningADoorEventName = "Opening a door" ;
    public static final String GetPointEventName = "Geting some point" ;
    public static final String LevelCompletedEventName = "Level is completed" ; 
    
    /**
	 * Crossing fire ... it hurts.
	 */
	public static Message ouchEvent() { return mkLabEvent(2,OuchEventName) ; }
	public static boolean isOuchEvent(Message m) { return m.getMsgName().equals(OuchEventName) ; }
	
	/**
	 * This event is generated when a door that was observed as closed, is now observed as open.
	 */
	public static Message openingADoorEvent() { return mkLabEvent(1,OpeningADoorEventName) ; }
	public static boolean isOpeningADoorEvent(Message m) { return m.getMsgName().equals(OpeningADoorEventName) ; }
    
	/**
	 * This event is generated whenever the agent receives new points.
	 */
	public static Message getPointEvent(){ return mkLabEvent(0,GetPointEventName) ; }
    public static boolean isGetPointEvent(Message m) { return m.getMsgName().equals(GetPointEventName) ; }

    public static Message levelCompletedEvent(){ return mkLabEvent(2,LevelCompletedEventName) ; }
	public static boolean isLevelCompletedEvent(Message m) { return m.getMsgName().equals(LevelCompletedEventName) ; }


	/**
	 * Produced events will be placed here.
	 */
	public List<Message> currentEvents = new LinkedList<>() ;
	
	/**
	 * If not null, produced events will also be sent to this communication node.
	 * Agents registering to this node will then automatically receive the events.
	 */
	public ComNode communicationNode ;
	
	LabRecruitsTestAgent agent ;
	
	public EventsProducer() { }
	
	public EventsProducer attachComNode(ComNode comnode) { 
	    communicationNode = comnode ; 
	    return this ; 
	}
	
	public EventsProducer attachTestAgent(LabRecruitsTestAgent agent) {
		this.agent = agent ; return this ;
	}
	
	public void generateCurrentEvents() {
		currentEvents.clear() ;
		if(agent.getState().worldmodel.healthLost>0) currentEvents.add(ouchEvent()) ;
		if(agent.getState().worldmodel.scoreGained>0) currentEvents.add(getPointEvent()) ;
		List<WorldEntity> z = agent.getState().changedEntities ;
		Vec3 p = agent.getState().worldmodel.position ;
		
		if(z!=null && z.stream().anyMatch(entity -> entity.type == LabEntity.DOOR && entity.getBooleanProperty("isOpen"))) {
			currentEvents.add(openingADoorEvent()) ;
		}	
		
		if(agent.getState().worldmodel.elements.values().stream().anyMatch(entity -> 
		           entity.id.equals("levelEnd") 
		           && Vec3.dist(entity.position,p) <= 1.5)) {
			//System.out.println("@@@@@@ LEVEL END") ;
			currentEvents.add(levelCompletedEvent()) ;
		}
		// send the events to the comNode, if one is provided:
		if(communicationNode != null) {
		    for(Message m : currentEvents) communicationNode.send(m) ;
		}
	}
	

}
