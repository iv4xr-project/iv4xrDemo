package eu.gameLevel.iv4xr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameLevel2 extends GameLevelAbstract {

	@Override
	public void setConnections() {
		for(int i=0; i< numberOfRooms; i++) {
			List subArrDoorsName = new ArrayList<>();
			List subArrButtonsName = new ArrayList<>();
			for(int j=0; j< numberOfActiveDoors; j++) {
				/* select last room only one door with button */
				if (i == numberOfRooms - 1) {
					if (j>0) {
						continue;
					}
				}

				//for the first room select one between i+1 and number of button and for the rest of the rooms, select one button on previous room 
				if(i == 0) {
					subArrDoorsName = (subArrDoorsName.isEmpty()) ? new ArrayList<String>(doorsName.subList(i*numberOfDoors, (i+1)*numberOfDoors)) :subArrDoorsName;
					subArrButtonsName = (subArrButtonsName.isEmpty()) ? new ArrayList<String>(buttonsName.subList(i*numberOfButtons, (i+1)*numberOfButtons)) :subArrButtonsName;
				}else {
					subArrDoorsName = (subArrDoorsName.isEmpty()) ? new ArrayList<String>(doorsName.subList(i*numberOfDoors, (i+1)*numberOfDoors)) :subArrDoorsName;
					subArrButtonsName = (subArrButtonsName.isEmpty()) ? new ArrayList<String>(buttonsName.subList((i-1)*numberOfButtons, (i)*numberOfButtons)) :subArrButtonsName;
					
				}
				
				List copySubArraybuttonsName = new ArrayList<>();
				
				for( Object element : subArrButtonsName) {
					copySubArraybuttonsName.add(element);
					
				}
				List<String> randomPicksDoors = GameLevelUtility.pickNRandom(subArrDoorsName, 1);	
				
				for (String[] element : connections) {
					String a =  element[0];
					if(copySubArraybuttonsName.contains(a)) {
						copySubArraybuttonsName.remove(a);
					}	
				}
				List<String> randomPicksbuttons = GameLevelUtility.pickNRandom(copySubArraybuttonsName, 1);

				connections.add(new String[] {randomPicksbuttons.get(0), randomPicksDoors.get(0)});
				subArrDoorsName.remove(randomPicksDoors.get(0));

				subArrButtonsName.remove(randomPicksbuttons.get(0));
			}
		}
	}

	@Override
	public void setFinalDoor() {
		connections.get(connections.size()-1)[1] = doorsName.get(doorsName.size()-1);
		
	}



}
