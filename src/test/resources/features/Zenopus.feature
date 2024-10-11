Feature: Testing the level Zenopus
#
# This is a play test for the level Zenopus, that it can be completed.
# The level has fire hazards and monsters.
#

  Background:
    Given the LabRecruits game path ''
    And the game level 'ruinzenopus_LR_2'
    And the graphics is 'true'
    And the agent-id is 'recruit'
    And the delay between update is 50 ms


  Scenario: play out to reach the finish
    Given the game starts
    When  the agent interacts with the button 'b0'
    And   the agent is close to the entity 'd0'
    Then  door 'd0' is open
    
    When  the agent interacts with the button 'b1'
    And   the agent is close to the entity 'd1'
    Then  door 'd1' is open
    
    When  the agent interacts with the button 'b3'
    And   the agent is close to the entity 'd3'
    Then  door 'd3' is open
    
    When  the agent interacts with the button 'bJS'
    And   the agent is close to the entity 'dJS'
    Then  door 'dJS' is open
    And   the agent health is at most 90
    And   the agent score is more than 40
    
    When  the agent interacts with the button 'bFN0'
    
    When  the agent interacts with the button 'bJE'
    And   the agent is close to the entity 'dJE'
    Then  door 'dJE' is open
    
    When  the agent interacts with the button 'BridgeW'
    
    When  the agent interacts with the button 'bAW'
    And   the agent is close to the entity 'dAW'
    Then  door 'dAW' is open
    
    When  the agent interacts with the button 'bFN1'
   
    When  the agent interacts with the button 'bAE'
    And   the agent is close to the entity 'dAE'
    Then  door 'dAE' is open
     
    When  the agent interacts with the button 'bFN2'
    
    When  the agent interacts with the button 'bshrine1'
    And   the agent is close to the entity 'dshrine1'
    Then  door 'dshrine1' is open
    And   the agent health is at most 90
    
    
    When  the agent is close to the flag 'shrine1'
    When  the agent has waited 2 turns
    Then  the agent health is 100
    And   the agent score is more than 230
    
    When  the agent interacts with the button 'bPW'
    And   the agent is close to the entity 'dPW'
    Then  door 'dPW' is open
    
    When  the agent interacts with the button 'bPS'
    And   the agent is close to the entity 'dPS'
    Then  door 'dPS' is open    
    
    When  the agent interacts with the button 'bFC'
    And   the agent is close to the entity 'dFC'
    Then  door 'dFC' is open 
    And   the agent health is at most 60
    
    When  the agent is close to the flag 'shrine2'
    When  the agent has waited 2 turns
    Then  the agent health is 100
    And   the agent score is more than 330
    
    When  the agent interacts with the button 'RFinal'
    And   the agent is close to the flag 'Finish'
    Then  the agent health is 100
    
    