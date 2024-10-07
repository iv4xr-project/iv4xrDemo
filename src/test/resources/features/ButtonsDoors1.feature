Feature: Testing the level button_doors_1

  Background:
    Given the LabRecruits game path ''
    And the game level 'buttons_doors_1'
    And the graphics is 'true'
    And the agent-id is 'agent1'

  Scenario: the initial state should be non-trivial
    Given the game starts
    And   the agent has explored the level
    
    Then entity 'door1' is observed
    And entity 'door3' is observed
    And door 'door1' is closed
    And door 'door3' is closed
    And entity 'button3' is unreachable
  
  Scenario: toggling button3 does not make door3 reachable
    Given the game starts
    And   the agent interacts with the button 'button1'
    And   the agent has explored the level
    And   the agent interacts with the button 'button3'
    And   the agent has explored the level
    
    Then entity 'door1' is observed
    And door 'door1' is closed
    And entity 'door3' is unreachable
    
  Scenario: the level can be finished
    Given the game starts
    And   the agent interacts with the button 'button1'
    And   the agent has explored the level
    And   the agent interacts with the button 'button3'
    And   the agent has explored the level
    And   the agent interacts with the button 'button4'
    And   the agent has explored the level
    
    Then door 'door3' is open
    And entity 'door3' is reachable
    And the agent score is 34
    