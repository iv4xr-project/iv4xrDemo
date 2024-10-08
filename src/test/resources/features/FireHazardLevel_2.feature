Feature: Testing the level FireHazard Level 2

  Background:
    Given the LabRecruits game path ''
    And the game level 'HZRDIndirect'
    And the graphics is 'true'
    And the agent-id is '0'
               
  Scenario: play out to the end
    Given the game starts
    When  the agent is at 6,0,5
    When  the agent is at 8,0,1
    When  the agent is at 13,4,1
    When  the agent interacts with the button 'b4.1'
    When  the agent is at 13,4,3
    When  the agent interacts with the button 'b7.1'
    When  the agent is at 9,4,9
    When  the agent is at 8,4,6
    When  the agent is at 5,4,7
    When  the agent interacts with the button 'b8.2'
    When  the agent is at 1,4,13
    When  the agent interacts with the button 'b5.1'  
    When  the agent is at 1,4,22
    When  the agent is at 6,0,22
    When  the agent interacts with the button 'b1.1'
    When  the agent is at 5,0,25
    
    Then   the agent health is at least 50
    