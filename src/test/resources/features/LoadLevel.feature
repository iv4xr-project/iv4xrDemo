Feature: Load a LabRecruits level
#
# Test that level loading works.
#

  Scenario: LabRecruits level can be loaded and monster observed
    #Given the path 'C:/Users/username/labrecruits/path'
    Given the LabRecruits game path ''
    And the game level 'simple_enemy_bdd'
    And the graphics is 'true'
    And the agent-id is 'agent0'
    And the game starts
    And the agent has waited 10 turns
    Then entity 'orc1' is observed