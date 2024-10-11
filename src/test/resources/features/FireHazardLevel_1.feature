Feature: Testing the level FireHazard Level 1
#
# A play-test of the level HZRDDirect, that it can be completed.
#

  Background:
    Given the LabRecruits game path ''
    And the game level 'HZRDDirect'
    And the graphics is 'true'
    And the agent-id is '0'

  Scenario: play out to the end
    Given the game starts
    When  the agent is at 6,0,5
    When  the agent is at 7,0,8
    When  the agent is at 7,0,11
    When  the agent is at 5,0,11
    When  the agent is at 5,0,16
    When  the agent is at 2,0,16
    When  the agent is at 1,0,18
    When  the agent is at 3,0,20
    When  the agent is at 6,0,20
    When  the agent interacts with the button 'b1.1'
    When  the agent is at 5,0,25
    
    Then   the agent health is at least 50
    