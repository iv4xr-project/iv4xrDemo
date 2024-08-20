Feature: Find and Interact with a button

  Background:
    Given the LabRecruits game path ''
    And the LabRecruits game level 'simple_enemy_bdd'
    And the LabRecruits graphics is 'true'
    And the agent-id is 'agent0'

  Scenario: LabRecruits button can be interacted without losing health
    Given the LabRecruits game starts
    When the agent interacts with the button '<button>'
    Then the agent health is minimum '<health>'
    And the LabRecruits game stops
    
    Examples:
      | button  | health |
      | button0 | 90     |
      | button1 | 100    |
      | button2 | 100    |