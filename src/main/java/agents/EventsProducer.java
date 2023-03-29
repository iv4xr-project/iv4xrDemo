package agents;

import java.nio.MappedByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

import eu.iv4xr.framework.mainConcepts.SyntheticEventsProducer;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.multiAgentSupport.ComNode;
import nl.uu.cs.aplib.multiAgentSupport.Message;
import nl.uu.cs.aplib.multiAgentSupport.Message.MsgCastType;
import world.LabEntity;

/**
 * This class interprets a Lab Recruit states and can produces events signaling
 * certain interesting things it observes in the state. Events will be
 * represented as instances of the class
 * nl.uu.cs.aplib.multiAgentSupport.Message. Generated instances will be placed
 * in a list which can be inspected. They will also be sent to a ComNode, if one
 * is provided.
 */
public class EventsProducer extends SyntheticEventsProducer {

	public static final String LabRecruits = "LabRecruits";

	static Message mkLabEvent(int priority, String eventName) {
		return new Message(LabRecruits, priority, MsgCastType.BROADCAST, "", eventName);
	}
	
	/**
	 * If the Lab-event carries an integer as data.
	 */
	static Message mkLabEvent(int priority, String eventName, Integer data) {
		return new Message(LabRecruits, priority, MsgCastType.BROADCAST, "", eventName, data);
	}

	public static final String OuchEventName = "Ouch";
	public static final String OpeningADoorEventName = "Opening a door";
	public static final String GetPointEventName = "Geting some point";
	public static final String LevelCompletedEventName = "Level is completed";
	public static final String LevelCompletionInSightEventName = "Level completion in sight";
	public static final String FireInSightEventName = "Fire in sight";
	public static final String MonstersInSightEventName = "Monsters in sight";
	

	/**
	 * Crossing fire ... it hurts.
	 */
	public static Message ouchEvent() {
		return mkLabEvent(2, OuchEventName);
	}

	public static boolean isOuchEvent(Message m) {
		return m.getMsgName().equals(OuchEventName);
	}

	/**
	 * This event is generated when a door that was observed as closed, is now
	 * observed as open.
	 */
	public static Message openingADoorEvent() {
		return mkLabEvent(1, OpeningADoorEventName);
	}

	public static boolean isOpeningADoorEvent(Message m) {
		return m.getMsgName().equals(OpeningADoorEventName);
	}

	/**
	 * This event is generated whenever the agent receives new points.
	 */
	public static Message getPointEvent() {
		return mkLabEvent(0, GetPointEventName);
	}

	public static boolean isGetPointEvent(Message m) {
		return m.getMsgName().equals(GetPointEventName);
	}

	/**
	 * This event is generated whenever the agent is close enough to an entity (e.g.
	 * a goal-flag) that marks the end-goal of an LR-level. This is identified by
	 * checking if the entity id contains the string "levelEnd".
	 */
	public static Message levelCompletedEvent() {
		return mkLabEvent(2, LevelCompletedEventName);
	}

	public static boolean isLevelCompletedEvent(Message m) {
		return m.getMsgName().equals(LevelCompletedEventName);
	}

	public static Message levelCompletionInSightEvent() {
		return mkLabEvent(1, LevelCompletionInSightEventName);
	}

	public static boolean isLevelCompletionInSightEvent(Message m) {
		return m.getMsgName().equals(LevelCompletionInSightEventName);
	}
	
	public static Message fireInSightEvent(int numberOfFire) {
		return mkLabEvent(1, FireInSightEventName, numberOfFire);
	}
	
	public static boolean isFireInSightEvent(Message m) {
		return m.getMsgName().equals(FireInSightEventName);
	}
	
	public static Message monstersInSightEvent(int numberOfMonsters) {
		return mkLabEvent(1, MonstersInSightEventName, numberOfMonsters);
	}
	
	public static boolean isMonstersInSightEvent(Message m) {
		return m.getMsgName().equals(MonstersInSightEventName);
	}

	public List<Message> trace = new LinkedList<>();

	/**
	 * The id of the entity that is used as the marker of level-end/completion. We
	 * will check containment rather than equality. So an entity e is considered to
	 * mark the level-completion if e.id contains the string indicated by
	 * idOfLevelEnd.
	 */
	public String idOfLevelEnd = "levelEnd";

	public EventsProducer() { }

	public EventsProducer attachComNode(ComNode comnode) {
		communicationNode = comnode;
		return this;
	}

	/**
	 * Clear the trace/history of events kept in this event-producer. This will also
	 * imply that one-off events can now fire again.
	 */
	public void reset() {
		trace.clear();
	}

	// public EventsProducer attachTestAgent(LabRecruitsTestAgent agent) {
	// this.agent = agent ; return this ;
	// }

	LabRecruitsTestAgent agent() {
		return (LabRecruitsTestAgent) agent;
	}

	void generateEvent(Message m, boolean oneOff) {
		boolean send = true;
		if (oneOff) {
			boolean hasbeenFired = trace.stream().anyMatch(m2 -> m2.getMsgName().equals(m.getMsgName()));
			if (hasbeenFired)
				send = false;

		}
		if (send) {
			currentEvents.add(m);
			trace.add(m);
			if (communicationNode != null) {
				communicationNode.send(m);
			}
		}
	}

	static final boolean ONEOFF = true;
	
	int numberOfFireSeen_previousUpdate = 0 ;
	int numberOfMonstersSeen_previousUpdate = 0 ;

	@Override
	public void generateCurrentEvents() {
		// don't forget to clear this first! :
		currentEvents.clear();
		
		var wom = agent().state().worldmodel() ;

		if (wom.scoreGained > 0)
			generateEvent(getPointEvent(), !ONEOFF);
		List<WorldEntity> z = agent().state().changedEntities;
		Vec3 p = wom.position;

		if (z != null && z.stream()
				.anyMatch(entity -> entity.type == LabEntity.DOOR && entity.getBooleanProperty("isOpen"))) {
			generateEvent(openingADoorEvent(), !ONEOFF);
		}

		// check level-end:
		WorldEntity levelEnd = null;
		for (var entity : wom.elements.values()) {
			if (entity.id.contains(idOfLevelEnd)) {
				levelEnd = entity;
				break;
			}
		}
		if (levelEnd != null) {
			if (levelEnd.timestamp == wom.timestamp) {
				generateEvent(levelCompletionInSightEvent(), ONEOFF);
			}
			if (Vec3.dist(levelEnd.position, p) <= 1.5) {
				// System.out.println("@@@@@@ LEVEL END") ;
				generateEvent(levelCompletedEvent(), ONEOFF);
			}
		}
		
		// ouch event:
		if (wom.healthLost > 0)
			generateEvent(ouchEvent(), !ONEOFF);
		
		// fire and monsters in sight events:
		int numOfFire_insight = (int) wom.elements.values()
				.stream()
				.filter(e -> e.type.equals(LabEntity.FIREHAZARD) && e.timestamp == wom.timestamp)
				.count() ;
		
		int numOfMonsters_insight = (int) wom.elements.values()
				.stream()
				.filter(e -> e.type.equals(LabEntity.ENEMY) && e.timestamp == wom.timestamp)
				.count() ;
		// only generate the fire/monster-event if there is fire in-sight, and it is more than
		// what was seen in the previous update:
		if (numOfFire_insight>0 && numOfFire_insight > numberOfFireSeen_previousUpdate) {
			generateEvent(fireInSightEvent(numOfFire_insight), !ONEOFF);
		}
		if (numOfMonsters_insight>0 && numOfMonsters_insight > numberOfMonstersSeen_previousUpdate) {
			generateEvent(monstersInSightEvent(numOfMonsters_insight), !ONEOFF);
		}
		numberOfFireSeen_previousUpdate = numOfFire_insight ;
		numberOfMonstersSeen_previousUpdate = numOfMonsters_insight ;
	}
	
	public String showTrace() {
		int k=0 ;
		StringBuffer buf = new StringBuffer() ;
		for (var m : trace) {
			if (k>0) buf.append("\n") ;
			buf.append("" + k + ": " + m.getMsgName()) ;
			if (m.getArgs().length > 0) {
				buf.append(", data: ") ;
				for (int i=0; i < m.getArgs().length; i++) {
					if (i>0) buf.append(", ") ;
					buf.append(m.getArgs()[i].toString()) ;
				}
			}
			k++ ;
		}
		return buf.toString() ;
	}

}
