Feature: Load a LabRecruits level

  Scenario: LabRecruits level can be loaded and monster observed
    Given the path ''
    And   the level 'simple_enemy_bdd'
    And   the graphics 'true'
    When the game starts
    Then the agent 'agent0' observes the entity 'orc1'