Feature: Testing the level R8_fire3

  Background:
    Given the LabRecruits game path ''
    And the LabRecruits game level 'R8_fire3'
    And the LabRecruits graphics is 'true'
    And the agent-id is 'Elono'

  Scenario: play out to reach the finish
    Given the LabRecruits game starts
    When  the agent interacts with the button 'b0'
    And   the agent is close to the entity "door3"
    Then  door "door3" is open
    
    When  the agent is close to the entity "door1"
    Then  door "door1" is open
    
    When  the agent interacts with the button 'b9'
    And   the agent is close to the entity "door0"
    Then  door "door0" is open
    
    When  the agent interacts with the button 'b3'
    And   the agent is close to the entity "door4"
    Then  door "door4" is open
    And   the agent health is at least 20
    And   the agent health is at most 50
    And   the agent score is more than 32
    
    When  the agent is close to the flag "Finish"
    Then  the agent score is more than 532
    And   the agent health is 100
