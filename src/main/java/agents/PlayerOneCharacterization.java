package agents;

import eu.iv4xr.framework.extensions.occ.BeliefBase;
import eu.iv4xr.framework.extensions.occ.Event;
import eu.iv4xr.framework.extensions.occ.Goal;
import eu.iv4xr.framework.extensions.occ.GoalStatus;
import static eu.iv4xr.framework.extensions.occ.Rules.*;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import world.BeliefState;
import world.LabEntity;

public class PlayerOneCharacterization {
	
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
	
	/**
	 * Crossing fire ... it hurts.
	 */
	public static class OuchEvent extends Event {
		
		public OuchEvent() {
			super("Ouch") ;
		}

		public void applyEffectOnBeliefBase(BeliefBase beliefbase) {
			
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
	}
	
	static String FinalDoor = "DFinal" ;
	static int maxScore = 620 ; // 20 buttons, 2 goal-flags
	
	/**
	 * This event is generated when a door that was observed as closed, is now observed as open.
	 */
	public static class OpeningADoorEvent extends Event {
		
		public OpeningADoorEvent() {
			super("Opening a door") ;
		}

		@Override
		public void applyEffectOnBeliefBase(BeliefBase beliefbase) {
			EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;
			int health = bbs.functionalstate.worldmodel.health ;
			int point = bbs.functionalstate.worldmodel.score ;
			
			boolean finalDoorIsOpen = false ;
			int numberOfDoorsMadeOpen =  0 ;
			int numberOfDoorsMadeClosed = 0 ;
			
			if (bbs.functionalstate.changedEntities != null) {
				for(WorldEntity e : bbs.functionalstate.changedEntities) {
					if(e.type == LabEntity.SWITCH) {
						if(e.getBooleanProperty("isOpen")) numberOfDoorsMadeOpen++ ;
						else numberOfDoorsMadeClosed++ ;
						if(e.id.equals(FinalDoor)) {
							finalDoorIsOpen = true ;
						}
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
		
	}
	
	/**
	 * This event is generated whenever the agent receives new points.
	 */
	public static class GetPointEvent extends Event {
		
		public GetPointEvent() {
			super("Geting some point") ;
		}


		@Override
		public void applyEffectOnBeliefBase(BeliefBase beliefbase) {
			EmotionBeliefBase bbs = (EmotionBeliefBase) beliefbase ;
			int scoreGained = (100*bbs.functionalstate.worldmodel.scoreGained / maxScore) ;
			
			// updating the belief on the get-max-point goal
			GoalStatus status = bbs.getGoalsStatus().goalStatus(gotAsMuchPointsAsPossible.name) ;
			if(status != null) {
				status.likelihood = Math.min(100,status.likelihood + scoreGained) ;
			}
			
		}
		
	}
	
	public static String OuchEventName = new OuchEvent().name ;
	public static String GetPointEventName = new GetPointEvent().name ;
	public static String OpeningADoorEventName = new OpeningADoorEvent().name ;
	
	public static AppraisalRules playerOneAppraisalRules = new AppraisalRules()
		   .withDesirabilityRule(new AppraisalRule(
			   goals_status -> eventName -> goalName -> {
				   if(eventName.equals(OuchEventName)) return -10 ;
				   if(eventName.equals(OpeningADoorEventName) && goalName.equals(questIsCompleted.name)) {
					   return 10 ;
				   }
				   if(eventName.equals(OpeningADoorEventName) && goalName.equals(gotAsMuchPointsAsPossible.name)) {
					   return 5 ;
				   }
				   if(eventName.equals(GetPointEventName) && goalName.equals(gotAsMuchPointsAsPossible.name)) {
					   return 10 ;
				   }
				   return 0 ;
			   }) 
			) ;
	
	
	public static EmotionIntensityDecayRule playerOneEmotionDecayRule = new EmotionIntensityDecayRule(ety -> 1) ;
    public static EmotionIntensityThresholdRule emotionThreshold = new EmotionIntensityThresholdRule(ety -> 20) ;
    public static GoalTowardsGoalLikelihoodRule goalGoalRule = new GoalTowardsGoalLikelihoodRule(bbs -> cause -> consequent -> null) ;
			
	
	


}
