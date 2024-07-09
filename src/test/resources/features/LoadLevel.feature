Feature: Load a LabRecruits level

  Scenario: LabRecruits level can be loaded and monster observed
    Given the path 'C:/Users/username/labrecruits/path'
    Given the level 'simple_enemy_bdd'
    Given the graphics 'true'
    When the game starts
    Then the agent 'agent0' observes the entity 'orc1'