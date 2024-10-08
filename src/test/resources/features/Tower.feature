Feature: Testing the level R8_fire3

  Background:
    Given the LabRecruits game path ''
    And the game level 'tower'
    And the graphics is 'true'
    And the agent-id is 'agent0'
    And the delay between update is 50 ms

  Scenario: play out to reach the finish
    Given the game starts
    When  the agent interacts with the button 'b1k0'
    And   the agent is close to the entity "dNorth1"
    Then  door "dNorth1" is open
      
    When  the agent interacts with the button 'b1k1'
    And   the agent is close to the entity "dEast0"
    Then  door "dEast0" is open
    
    When  the agent interacts with the button 'b0k1'
    And   the agent is close to the entity "d0k0"
    Then  door "d0k0" is open
    
    When  the agent interacts with the button 'b0k2'
    And   the agent is close to the entity "dEast0"
    Then  door "dEast0" is open
    
    When  the agent is close to the entity "d2k0"
    
    When  the agent interacts with the button 'b3k0'
    And   the agent is close to the entity "dWest0"
    Then  door "dWest0" is open
      
    When  the agent is close to the flag "Finish"
    And   the agent has waited 2 turns
    Then  the agent score is more than 530
    And   the agent health is 100
    