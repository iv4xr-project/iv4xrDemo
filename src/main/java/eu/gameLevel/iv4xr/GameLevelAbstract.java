package eu.gameLevel.iv4xr;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* define abstract methods such as set goal, set doors, set rooms */
public abstract class GameLevelAbstract {
	int numberOfDoors;
	int numberOfButtons;
	int numberOfRooms;
	int lenghtOfRoom;
	int numberOfActiveDoors;
	int numberofBlocks;
	int maximumNumberOfButtons;
	int maximumnumberOfDoors;
	int maximumNumberOfActiveDoors;
	int gameLevel;
	static int maximumNumberOfRooms = 10;
	static int maximumNumberOfLenght = 10;
	String goalRoomStructure[][] = new String[2][lenghtOfRoom];
	List<String> doorsName = new ArrayList<>();
	List<String> buttonsName = new ArrayList<>();
	ArrayList<String[][]> finalStructure = new ArrayList<String[][]>();
	ArrayList<String[]> connections = new ArrayList<>();
	List<Integer> rndDoor = new ArrayList<>();
	public void initial(int inputNumberOfActiveDoors) {
		numberOfActiveDoors = inputNumberOfActiveDoors;
		ArrayList<String[][]> finalStructure = new ArrayList<String[][]>();
		ArrayList<String[]> connections = new ArrayList<>();
		List<Integer> rndDoor = new ArrayList<>();
		
	}
	/*Create connections between doors and buttons based on the number of active doors automatically*/
	public abstract void setConnections();
	
	/* Create a list of doors name*/
	public void setDoorsName(int numberOfRooms, int numberOfDoors) {
		for(int i = 0; i< numberOfRooms*numberOfDoors; i++) {
			doorsName.add("door"+(i+1)+"");
		}
	};
	public void setButtonsName(int numberOfRooms, int numberOfButtons ) {
		for(int i = 0; i< numberOfRooms*numberOfButtons; i++) {
			buttonsName.add("button"+(i+1)+"");
		}

	};
	
	/*Replace the final door in the connection list*/
	public abstract void setFinalDoor();
	
	/*Create more than one room if there is more*/
	public void setRooms() {
		for(int k=0; k<numberOfRooms;k++) {
			String[][] temp = new String[lenghtOfRoom][lenghtOfRoom];
			for (int i = 0; i < lenghtOfRoom; i++) {
				for (int j = 0; j < lenghtOfRoom; j++) {
					if (i == 0 || i == lenghtOfRoom - 1 || j == 0 || j == lenghtOfRoom - 1) {
					/*We should add | at the first line of the row to recognize it is the beginning of the first place*/
						if(i == 0 && j == 0 && k ==0){
							temp[i][j] = "|w";
							}else {temp[i][j] = "w";}
						}
					else {
						temp[i][j] = "f";
					}
				}
				
				if(i == 0 && !rndDoor.isEmpty()){		
					for(int rnd=0; rnd< rndDoor.size(); rnd++)
					temp[i][rndDoor.get(rnd)] = "f";
				} 
			}
			
			/*Defining the positions that doors could be take(at least one wall in between of two doors)*/
			List<Integer> possiblePositionForDoors = new ArrayList<>();
			/*This variable is between the two walls from left and right*/
			int tmpRndDoor = new Random().nextInt(lenghtOfRoom - 2) + 1;
			possiblePositionForDoors.add(tmpRndDoor);
			/*based on the random number, we can create a list of possible doors while there is at list one wall in between*/
			while(tmpRndDoor+2 <lenghtOfRoom - 1) {
				if(!possiblePositionForDoors.contains(tmpRndDoor+2))
				possiblePositionForDoors.add(tmpRndDoor+2);
				tmpRndDoor = tmpRndDoor+2;
			}
			while(tmpRndDoor-2 > 0) {
				if(!possiblePositionForDoors.contains(tmpRndDoor-2))
					possiblePositionForDoors.add(tmpRndDoor-2);
				tmpRndDoor = tmpRndDoor-2;
			}
			/* Add room doors */
			//Pick up a number of element based on the number of active doors	
			List<Integer> randomPicksDoors = GameLevelUtility.pickNRandom(possiblePositionForDoors, numberOfDoors);		
			for(int i=0; i< randomPicksDoors.size(); i++) {
				int currentDoor = randomPicksDoors.get(i);
				rndDoor.add(currentDoor);	
				temp[lenghtOfRoom - 1][currentDoor] = "f:d>n^"+doorsName.get(0)+"";
				doorsName.remove(0);
			}
			finalStructure.add(temp);
		}
	}
	
	/* Adding buttons randomly in each room */
	public void setButtons() {
		for (int i = 0; i < numberOfRooms; i++) {
			/* firstly, we get the structure of each room */
			String[][] button = finalStructure.get(i);
			boolean isValid = false;
			for(int j=0; j<numberOfButtons; j++) {
				while(isValid == false) {
					int rnd1 = new Random().nextInt(lenghtOfRoom - 2) + 1;
					int rnd2 = new Random().nextInt(lenghtOfRoom - 2) + 1;
					if (button[rnd1][rnd2] == "f") {
						button[rnd1][rnd2] = "f:b>n^"+buttonsName.get(0)+"";
						buttonsName.remove(0);
						isValid = true;
				    }
				}
				isValid = false;
			}	
	   }
	}
	
	/* Adding blocks randomly in each room */
	public void setBlocks() {
		for (int i = 0; i < numberOfRooms; i++) {
			/* firstly, we get the structure of each room */
			String[][] button = finalStructure.get(i);
			boolean isValid = false;
			for(int j=0; j<numberofBlocks; j++) {
				while(isValid == false) {
					/*Blocks should not be at front of or back of doors therefore we should set specific range for them*/
					int rnd1 = new Random().nextInt(lenghtOfRoom - 4) + 2;
					int rnd2 = new Random().nextInt(lenghtOfRoom - 4) + 2;

					if (button[rnd1][rnd2] == "f" && button[rnd1][rnd2+1]== "f") {
						button[rnd1][rnd2] = "w";
						button[rnd1][rnd2+1] = "w";
						isValid = true;
				    }

				}
				isValid = false;
			}	
	   }
	};
	public void setGoalRoom() {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < lenghtOfRoom; j++) {
				//Suppose we only have one door that can access to the goal room
				//We choose the last door all the time
				if (i == 0 && j == rndDoor.get(rndDoor.size()-1)) {
					goalRoomStructure[i][j] = "f:ng>n^ng1";  
				} else {
					goalRoomStructure[i][j] = "w";
				}
			}

		}
	}
	
	public void setSecondWall() {
		for(int k=0; k< numberOfRooms+1 ;k++) {
			String[][] d = finalStructure.get(k); 
			String[][] wall = new String[d.length][lenghtOfRoom];
			for(int i=0; i<d.length; i++) {
				for (int j = 0; j < lenghtOfRoom; j++) {
					wall[i][j] = d[i][j];
					if(wall[i][j] != "w") {
						if(wall[i][j] == "|w") {
							wall[i][j] = "|w";
						}else
						wall[i][j] = "";
					}
				}	
			}
			
			/* Add second wall */
			finalStructure.add(wall);
		}
	}
	
	
	
}
