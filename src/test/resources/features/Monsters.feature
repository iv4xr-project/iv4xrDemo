Feature: Test monsters
#
# Testing monsters, e.g. that they are drawn to the player, they hurt
# the player, and they block navigation.
#

  Background:
    Given the LabRecruits game path ''
    
  Scenario: Monsters come to the player and hurt the player
    Given the game level 'square_withEnemies'
    And   the graphics is 'true'
    And   the agent-id is 'agent0'
    And   the game starts
    When  the agent is close to the flag "Finish"
    And   the agent has waited 40 turns
    Then  entity 'orc1' is observed
    And   the agent health is at most 90
   
  Scenario: Monster blocks the player's path
    Given the game level 'simple2_withEnemies'
    And   the graphics is 'true'
    And   the agent-id is 'agent0'
    And   the game starts
    And   the agent has waited 2 turns
    Then  entity 'orc1' is observed
    And   entity 'button0' is unreachable

  Scenario: Monster comes to the player and hurts the player
    Given the game level 'simple_withEnemies'
    And   the graphics is 'true'
    And   the agent-id is 'agent0'
    And   the game starts
    When  the agent interacts with the button 'button0'
    And   the agent has waited 40 turns
    Then  entity 'orc1' is observed
    And   the agent health is at most 90