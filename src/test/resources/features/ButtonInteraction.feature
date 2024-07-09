Feature: Find and Interact with a button

  Background:
    Given the LabRecruits game path 'C:/Users/username/labrecruits/path'
    Given the LabRecruits game level 'simple_enemy_bdd'
    Given the LabRecruits graphics is 'true'

  Scenario: LabRecruits button can be interacted without losing health
    Given the LabRecruits game starts
    When the agent '<agent>' interacts with the button '<button>'
    Then the agent health is minimum '<health>'
    And the LabRecruits game stops
    
    Examples:
      | agent  | button  | health |
      | agent0 | button0 | 90     |
      | agent0 | button1 | 100    |
      | agent0 | button2 | 100    |