package eu.gameLevel.iv4xr;

import java.util.ArrayList;
import java.util.List;

public class GameLevel1 extends GameLevelAbstract {

	
	/*Simple way: select buttons to open the doors in each rooms*/
	@Override
	public void setConnections() {
		
//		for(int i=0; i< numberOfRooms; i++) {
//			List<String[]> temp = new ArrayList<>();
//			List subArrDoorsName = new ArrayList<>();
//			List subArrButtonsName = new ArrayList<>();
//			for(int j=0; j< numberOfActiveDoors; j++) {
//				/* select last room only one door with button */
//				if (i == numberOfRooms - 1) {
//					if (j>0) {
//						continue;
//					}
//				}
//
//				//select one between i+1 and number of button 
//		
//				subArrDoorsName = (subArrDoorsName.isEmpty()) ? new ArrayList<String>(doorsName.subList(i*numberOfDoors, (i+1)*numberOfDoors)) :subArrDoorsName;
//				subArrButtonsName = (subArrButtonsName.isEmpty()) ? new ArrayList<String>(buttonsName.subList(i*numberOfButtons, (i+1)*numberOfButtons)) :subArrButtonsName;
//				
//				List<String> randomPicksDoors = GameLevelUtility.pickNRandom(subArrDoorsName, 1);	
//				List<String> randomPicksbuttons = GameLevelUtility.pickNRandom(subArrButtonsName, 1);
//				//select doors sorted by id
//				connections.add(new String[] {randomPicksbuttons.get(0), subArrDoorsName.get(0).toString()});
//				subArrDoorsName.remove(subArrDoorsName.get(0));
//				//select doors randomly
//				//connections.add(new String[] {randomPicksbuttons.get(0), randomPicksDoors.get(0)});
//				//subArrDoorsName.remove(randomPicksDoors.get(0));
//
//				subArrButtonsName.remove(randomPicksbuttons.get(0));
//			}
//		}
		
		connections.add(new String[] {"button4", "door1"});
		connections.add(new String[] {"button3", "door2"});
		connections.add(new String[] {"button5", "door4"});
		
	}

	@Override
	public void setFinalDoor() {
		connections.get(connections.size()-1)[1] = doorsName.get(doorsName.size()-1);
	}



}
