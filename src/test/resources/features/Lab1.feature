Feature: Testing the level Lab1

  Background:
    Given the LabRecruits game path ''
    And the LabRecruits game level 'lab1'
    And the LabRecruits graphics is 'true'
    And the agent-id is 'agent0'

  Scenario: play out to the end
    Given the LabRecruits game starts
    When  the agent interacts with the button 'b_hall_1'
    And   the agent is close to the entity "d_store_e"
    Then  door "d_store_e" is open
    
    When  the agent interacts with the button 'b_store'
    And   the agent is close to the entity "d_store_n"
    Then  door "d_store_n" is open
    
    When  the agent is close to the entity "d_sidehall"
    Then  door "d_sidehall" is open
    
    When  the agent interacts with the button 'b_secret_1'
    And   the agent interacts with the button 'b_side'
    And   the agent is close to the entity "d_sidehall"
    Then  door "d_sidehall" is open
    When  the agent is close to the entity "d_lab_w"
    Then  door "d_lab_w" is open
    When  the agent is close to the entity "d_bcroom"
    Then  door "d_bcroom" is open
    
    When  the agent interacts with the button 'b_secret_2'
    And   the agent is close to the entity "d_closet"
    Then  door "d_closet" is open
    
    When  the agent interacts with the button 'b_closet'
    And   the agent is close to the entity "d_theater_s"
    Then  door "d_theater_s" is open
    And   the agent is close to the entity "d_theater_e"
    Then  door "d_theater_e" is open   
    
    When  the agent interacts with the button 'b_lab_e'
    And   the agent is close to the entity "d_tofinish"
    Then  door "d_tofinish" is open
    
    When  the agent interacts with the button 'b_finish'
    And   the agent is close to the entity "d_finish"
    Then  door "d_finish" is open
    And   the agent score is less than 100
    
    When  the agent is close to the flag "finish"
    Then  the agent score is more than 100