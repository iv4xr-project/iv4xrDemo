package agents;

import java.util.*;

import eu.iv4xr.framework.extensions.occ.Event;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import world.LabEntity;

/**
 * 
 * 
 * @author iswbprasetya
 *
 */
public class EventsProducer {
	
	/**
	 * Crossing fire ... it hurts.
	 */
	public static class OuchEvent extends Event {
		
		public OuchEvent() { super("Ouch") ; }
	}
	
	/**
	 * This event is generated when a door that was observed as closed, is now observed as open.
	 */
	public static class OpeningADoorEvent extends Event {
		
		public OpeningADoorEvent() { super("Opening a door") ; }
	}
	
	/**
	 * This event is generated whenever the agent receives new points.
	 */
	public static class GetPointEvent extends Event {
		
		public GetPointEvent() { super("Geting some point") ; }
	}
	
	public static class LevelCompletedEvent extends Event {
		public LevelCompletedEvent() { super("Level is completed") ; }
	}
	
	public static String OuchEventName = new OuchEvent().name ;
	public static String GetPointEventName = new GetPointEvent().name ;
	public static String OpeningADoorEventName = new OpeningADoorEvent().name ;
	public static String LevelCompletedEventName = new LevelCompletedEvent().name ;
	
	public List<Event> currentEvents = new LinkedList<>() ;
	
	LabRecruitsTestAgent agent ;
	
	public EventsProducer() { }
	
	public EventsProducer attachTestAgent(LabRecruitsTestAgent agent) {
		this.agent = agent ; return this ;
	}
	
	public void generateCurrentEvents() {
		currentEvents.clear() ;
		if(agent.getState().worldmodel.healthLost>0) currentEvents.add(new OuchEvent()) ;
		if(agent.getState().worldmodel.scoreGained>0) currentEvents.add(new GetPointEvent()) ;
		List<WorldEntity> z = agent.getState().changedEntities ;
		Vec3 p = agent.getState().worldmodel.position ;
		
		if(z!=null && z.stream().anyMatch(entity -> entity.type == LabEntity.DOOR && entity.getBooleanProperty("isOpen"))) {
			currentEvents.add(new OpeningADoorEvent()) ;
		}	
		
		if(agent.getState().worldmodel.elements.values().stream().anyMatch(entity -> 
		           entity.id.equals("levelEnd") 
		           && Vec3.dist(entity.position,p) <= 1.5)) {
			//System.out.println("@@@@@@ LEVEL END") ;
			currentEvents.add(new LevelCompletedEvent()) ;
		}
	}
	

}
