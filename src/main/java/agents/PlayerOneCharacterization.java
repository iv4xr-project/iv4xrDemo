package agents;


import static agents.EventsProducer.*;

import eu.iv4xr.framework.extensions.occ.BeliefBase;
import eu.iv4xr.framework.extensions.occ.Emotion;
import eu.iv4xr.framework.extensions.occ.Event;
import eu.iv4xr.framework.extensions.occ.Goal;
import eu.iv4xr.framework.extensions.occ.GoalStatus;
import eu.iv4xr.framework.extensions.occ.UserCharacterization;
import eu.iv4xr.framework.extensions.occ.BeliefBase.Goals_Status;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import world.BeliefState;
import world.LabEntity;

public class PlayerOneCharacterization extends UserCharacterization {
	
	public static Goal questIsCompleted = new Goal("quest is completed").withSignificance(10) ;
	public static Goal gotAsMuchPointsAsPossible = new Goal("get as much points as possible").withSignificance(5) ;
	
	public static class EmotionBeliefBase implements BeliefBase {
		
		Goals_Status goals_status = new Goals_Status() ;
		
		BeliefState functionalstate ;
		
		public EmotionBeliefBase() { }
		
		public EmotionBeliefBase attachFunctionalState(BeliefState functionalstate) {
			this.functionalstate = functionalstate ;
			return this ;
		}

		@Override
		public Goals_Status getGoalsStatus() { return goals_status ;  }
		
	}
	
	public PlayerOneCharacterization() {
		
	}
	
	static String KeyDoor = "door3" ;
	static String GoalIten = "levelEnd" ;
	static int maxScore = 620 ; // 20 buttons, 2 goal-flags
	
	/**
	 * Modeling the effect of various events in terms of how they affect the goals.
	 */
	@Override
	public void eventEffect(Event e, BeliefBase beliefbase) {
		
		EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;
		int health = bbs.functionalstate.worldmodel.health ;
		int point = bbs.functionalstate.worldmodel.score ;
		
		GoalStatus gQIC_status = bbs.getGoalsStatus().goalStatus(questIsCompleted.name) ;
		GoalStatus gGAMP_status = bbs.getGoalsStatus().goalStatus(gotAsMuchPointsAsPossible.name) ;
		
		// logic for Ouch-event:
		if (e instanceof OuchEvent) effectOfOuchEvent(beliefbase) ;
		else if (e instanceof OpeningADoorEvent) effectOfOpeningADoorEvent(beliefbase) ;
		else if (e instanceof GetPointEvent) effectOfGetPointEvent(beliefbase) ;
		else if (e instanceof LevelCompletedEvent) effectOfLevelCompletedEvent(beliefbase) ;
	}
	
	private void effectOfOuchEvent(BeliefBase beliefbase) {
		EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;
		int health = bbs.functionalstate.worldmodel.health ;
		int point = bbs.functionalstate.worldmodel.score ;

		// updating belief on the quest-completed goal; if the health drops below 50,
		// decrease this goal likelihood by 3.
		// If the health drops to 0, game over. The goal is marked as failed.
		GoalStatus status = bbs.getGoalsStatus().goalStatus(questIsCompleted.name) ;
		if(status != null && health<50) {
			status.likelihood = Math.max(0,status.likelihood - 3) ;
			if(health <=0) {
				status.setAsFailed();
			}
		}
		// updating the belief on the get-max-point goal
		status = bbs.getGoalsStatus().goalStatus(gotAsMuchPointsAsPossible.name) ;
		if(status != null && health<50) {
			status.likelihood = Math.max(0,status.likelihood - 3) ;
			if(health <=0) {
				status.setAsFailed();
			}
		}	
	}
	
	private void effectOfLevelCompletedEvent(BeliefBase beliefbase) {
		EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;
		int health = bbs.functionalstate.worldmodel.health ;
		GoalStatus status = bbs.getGoalsStatus().goalStatus(questIsCompleted.name) ;
		if(status != null && health>0) {
			status.setAsAchieved();
		}
	}
		
	private void effectOfOpeningADoorEvent(BeliefBase beliefbase) {
		EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;
		int health = bbs.functionalstate.worldmodel.health ;
		int point = bbs.functionalstate.worldmodel.score ;

		boolean finalDoorIsOpen = false ;
		int numberOfDoorsMadeOpen =  0 ;
		int numberOfDoorsMadeClosed = 0 ;

		if (bbs.functionalstate.changedEntities != null) {
			for(WorldEntity e : bbs.functionalstate.changedEntities) {
				if(e.type == LabEntity.DOOR) {
					if(e.getBooleanProperty("isOpen")) {
						numberOfDoorsMadeOpen++ ;
						if(e.id.equals(KeyDoor)) {
							finalDoorIsOpen = true ;
						}
					}
					else numberOfDoorsMadeClosed++ ;
					
				}	
			}
		}	

		// updating belief on the quest-completed goal
		GoalStatus status = bbs.getGoalsStatus().goalStatus(questIsCompleted.name) ;
		if(status != null) {
			status.likelihood = Math.min(80,status.likelihood + 10*(numberOfDoorsMadeOpen - numberOfDoorsMadeClosed)) ;
			if(finalDoorIsOpen) {
				status.likelihood = 100 ;
			}
		}		
	}
	
	private void effectOfGetPointEvent(BeliefBase beliefbase) {
		EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;
		int scoreGained = (100*bbs.functionalstate.worldmodel.scoreGained / maxScore) ;

		// updating the belief on the get-max-point goal
		GoalStatus status = bbs.getGoalsStatus().goalStatus(gotAsMuchPointsAsPossible.name) ;
		if(status != null) {
			status.likelihood = Math.min(100,status.likelihood + scoreGained) ;
		}
	}
		

	// various rules:
	
	public int desirabilityAppraisalRule(Goals_Status goals_status, String eventName, String goalName) {
		if(eventName.equals(OuchEventName)) return -10 ;
		if(eventName.equals(OpeningADoorEventName) && goalName.equals(questIsCompleted.name)) {
			   return 800 ;
		}
		if(eventName.equals(OpeningADoorEventName) && goalName.equals(gotAsMuchPointsAsPossible.name)) {
			   return 400 ;
		}
		   if(eventName.equals(GetPointEventName) && goalName.equals(gotAsMuchPointsAsPossible.name)) {
			   return 10 ;
		   }
		   return 0 ;
	}
	
	public int emotionIntensityDecayRule(Emotion.EmotionType etype) { return 2 ; }

	public int intensityThresholdRule(Emotion.EmotionType etyp) { return 0 ; }
		


}
