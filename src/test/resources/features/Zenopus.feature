Feature: Testing the level Zenopus

  Background:
    Given the LabRecruits game path ''
    And the LabRecruits game level 'ruinzenopus_LR_2'
    And the LabRecruits graphics is 'true'
    And the agent-id is 'recruit'

TODO, covert this:

mkScreenShot(dataDir,"RStart"),
              GoalLib.entityInteracted("b0"), GoalLib.entityStateRefreshed2("d0"),
              GoalLib.entityInteracted("b1"), GoalLib.entityStateRefreshed2("d1"),
              GoalLib.entityInteracted("b3"), GoalLib.entityStateRefreshed2("d3"),
              mkScreenShot(dataDir,"b4"),
              mkScreenShot(dataDir,"CTrap"),  
              mkScreenShot(dataDir,"CAfterTrap1"), 
              mkScreenShot(dataDir,"CAfterTrap2"), 
              GoalLib.entityInteracted("bJS"), GoalLib.entityStateRefreshed2("dJS"),
              GoalLib.entityInteracted("bFN0"),
              GoalLib.entityInteracted("bJE"),  GoalLib.entityStateRefreshed2("dJE"),
              mkScreenShot(dataDir,"BridgeW"),
              GoalLib.entityInteracted("bAW"),  GoalLib.entityStateRefreshed2("dAW"),
              mkScreenShot(dataDir,"RA"),
              GoalLib.entityInteracted("bFN1"),
              GoalLib.entityInteracted("bAE"),  GoalLib.entityStateRefreshed2("dAE"),
              mkScreenShot(dataDir,"BridgeE"),
              GoalLib.entityInteracted("bFN2"),
              GoalLib.entityInteracted("bshrine1"), GoalLib.entityStateRefreshed2("dshrine1"),
              mkScreenShot(dataDir,"shrine1"),
              mkScreenShot(dataDir,"CPW"),
              GoalLib.entityInteracted("bPW"), GoalLib.entityStateRefreshed2("dPW"),
              mkScreenShot(dataDir,"RP"),
              GoalLib.entityInteracted("bPS"), GoalLib.entityStateRefreshed2("dPS"),
              mkScreenShot(dataDir,"CPS"),
              mkScreenShot(dataDir,"RG"),
              mkScreenShot(dataDir,"CFN"),
              mkScreenShot(dataDir,"bFC"),
              GoalLib.entityInteracted("bFC"), GoalLib.entityStateRefreshed2("dFC"),
              GoalLib.atBGF("shrine2", 0.2f, true),
              mkScreenShot(dataDir,"RFinal"),
              GoalLib.atBGF("Finish", 0.2f, true),
                mkScreenShot(dataDir,"Finish")


  Scenario: play out to reach the finish
    Given the LabRecruits game starts
    When  the agent interacts with the button 'b0'
    And   the agent is close to the entity "door3"
    Then  door "door3" is open
    
    When  the agent is close to the entity "door1"
    Then  door "door1" is open
    
    When  the agent interacts with the button 'b9'
    And   the agent is close to the entity "door0"
    Then  door "door0" is open
    
    When  the agent interacts with the button 'b3'
    And   the agent is close to the entity "door4"
    Then  door "door4" is open
    And   the agent health is at least 20
    And   the agent health is at most 50
    And   the agent score is more than 32
    
    When  the agent is close to the flag "Finish"
    Then  the agent score is more than 532
    And   the agent health is 100
