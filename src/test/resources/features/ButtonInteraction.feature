Feature: Find and Interact with a button

  Background:
    #Given the LabRecruits game path 'C:/Users/username/labrecruits/path'
    Given the LabRecruits game path ''
    And the game level 'simple_enemy_bdd'
    And the graphics is 'true'
    And the agent-id is 'agent0'

  Scenario: LabRecruits button can be interacted without losing health
    Given the game starts
    When the agent interacts with the button '<button>'
    Then the agent health is at least <health>
    
    Examples:
      | button  | health |
      | button0 | 90     |
      | button1 | 100    |
      | button2 | 100    |