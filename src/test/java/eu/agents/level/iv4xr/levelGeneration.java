package eu.agents.level.iv4xr;



import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.*;


/**
 * Automatically creating CSV files and levels based on them.
 */

public class levelGeneration {

//	this function get the parameters of number of rooms, number of buttons, number of doors
//	, shape, relation between doors and buttons
//	

	public static List<Integer> pickNRandom(List<Integer> lst, int n) {
	    List<Integer> copy = new LinkedList<Integer>(lst);
	    Collections.shuffle(copy);
	    return copy.subList(0, n);
	}
	
	
	/*Get integer from console*/
	public static int getInt(Scanner sc, String prompt, int maxNumber)
	{
	    int temp = 0;
	    boolean isValid = false;
	    while (isValid == false)
	    {
	    	
	        System.out.print("Inter the number of " + prompt + "(less than "+ maxNumber +") :");
	        if (sc.hasNextInt())
	        {
	        	temp = sc.nextInt();
	            isValid = true;
	        }
	        else
	        {
	            System.out.println(
	                "Error! Invalid number. Try again.");
	        }
	        sc.nextLine();    
	        
	        if (isValid == true && temp <= 0)
	        {
	        	System.out.println(
	                    "Error! Number must be greater than 0.");
	                isValid = false;
	        }
	        else if (isValid == true && temp > maxNumber)
	        {
	            System.out.println(
	                "Error! Number must be less than " + maxNumber + ".");
	            isValid = false;
	        }
	    }
	    return temp;
	}
	
	/*Get string array from console*/
	public static String getString(Scanner sc, String prompt, int numberOfDoors, int numberOfButtons, List<String[]> connections)
	{
	    
	    boolean isValid = false;
	    	String temp = "";
	    	
		    while (isValid == false)
		    {
		    	int tempNumbers = 0;
		    	String tempString = "";
	    		System.out.print("Inter the name of "+prompt+" you want to connect(use this structure: "+prompt+"1) ");
		        if (sc.hasNext())
		        {
		        	temp = sc.next();
		        	/*Get number of the input*/	        	
		        	tempNumbers = (temp.matches(".*\\d.*") == false) ? 0 : Integer.parseInt(temp.replaceAll("[^0-9]", "")) ;      
		        	/*Get string of the input*/
		        	tempString= temp.replaceAll("[0-9]", "");
		        	
		            isValid = true;
		            
		        }   	
		        else
		        {
		            System.out.println(
		                "Error! Invalid string. Try again.");
		        }
		        sc.nextLine();     
		        if(isValid == true && !tempString.equals(prompt)
		        		|| (isValid == true && tempString.equals("button") &&  (tempNumbers <= 0  || tempNumbers > numberOfButtons))
		        		|| (isValid == true && prompt == "door" && (tempNumbers <= 0 || tempNumbers > numberOfDoors))
		        		) {
		        	System.out.println(
		                    "Error! Invalid string. Try again.");
		                isValid = false;
		        } 
		        else if(isValid == true ) {
		        	for (int i= 0 ; i < connections.size(); i++) {
		        		if(Arrays.stream(connections.get(i)).anyMatch(temp::equals)) {
		        			System.out.println(
				                    "Error! Invalid string. You used this name befor. Try again.");
				                isValid = false;
		        		}
		        	}
		        }
	    	}
		    
	    return temp;
	}
	
	public static void main(String[] args) throws IOException {
		int numberOfDoors = 1;
		int numberOfButtons = 1;
		int numberOfRooms = 2;
		int lenghtOfRoom = 8;
		int NumberOfActiveDoors =1;
		int maximumNumberOfRooms = 10;
		int maximumNumberOfLenght = 10;
		
		ArrayList<String[][]> finalStructure = new ArrayList<String[][]>();
		ArrayList<String[]> connections = new ArrayList<>();
		List<Integer> rndDoor = new ArrayList<>();
		
		lenghtOfRoom = getInt(new Scanner(System.in), "lenght of rooms", maximumNumberOfLenght);
		/*
		 * maximumNumberOfButtons is based on the space that we have in our room depends
		 * on length of room, I decided to decrease 10 number of it too. there is no
		 * reason:)
		 */
		int maximumNumberOfButtons = (lenghtOfRoom -1)*(lenghtOfRoom -1) - 10;
		numberOfRooms = getInt(new Scanner(System.in), "rooms", maximumNumberOfRooms);
		int maximumnumberOfDoors = (lenghtOfRoom - 2)/2;
		numberOfDoors = getInt(new Scanner(System.in), "doors", maximumnumberOfDoors);
		int maximumNumberOfActiveDoors = numberOfDoors;
		NumberOfActiveDoors = getInt(new Scanner(System.in), "active doors", maximumNumberOfActiveDoors);
		numberOfButtons = getInt(new Scanner(System.in), "buttons",maximumNumberOfButtons);
		String goalRoomStructure[][] = new String[2][lenghtOfRoom];
		List<String> doorsName = new ArrayList<>();
		List<String> buttonsName = new ArrayList<>();
		
		/* Create a list of doors name*/
		for(int i = 0; i< numberOfRooms*numberOfDoors; i++) {
			doorsName.add("door"+(i+1)+"");
		}
		/* Create a list of buttons name*/
		for(int i = 0; i< numberOfRooms*numberOfButtons; i++) {
			buttonsName.add("button"+(i+1)+"");
		}
		
		/*Get connections between doors and buttons based on the number of active doors from user*/	
//		for(int i=0; i< NumberOfActiveDoors; i++) {
//	    	String temp[] = new String[2];
//	    		temp[0] = getString(new Scanner(System.in), "button",numberOfDoors,numberOfButtons,connections );
//	    		temp[1] = getString(new Scanner(System.in), "door",numberOfDoors,numberOfButtons,connections );
//	    		connections.add(temp);
//	    }
		
		/*Create connections between doors and buttons based on the number of active doors automatically*/	
		/*Simple way: select buttons in each rooms*/
		
		for(int i=0; i< numberOfRooms; i++) {
			List<String[]> temp = new ArrayList<>();
			List subArrDoorsName = new ArrayList<>();
			List subArrButtonsName = new ArrayList<>();
			for(int j=0; j< NumberOfActiveDoors; j++) {
				/* select last room only one door as goal */
				if (i == numberOfRooms - 1) {
					if (j>0) {
						continue;
					}
				}

				//select one between i+1 and number of button 
		
				subArrDoorsName = (subArrDoorsName.isEmpty()) ? new ArrayList<String>(doorsName.subList(i*numberOfDoors, (i+1)*numberOfDoors)) :subArrDoorsName;
				subArrButtonsName = (subArrButtonsName.isEmpty()) ? new ArrayList<String>(buttonsName.subList(i*numberOfButtons, (i+1)*numberOfButtons)) :subArrButtonsName;
				
				List<String> randomPicksDoors = pickNRandom(subArrDoorsName, 1);	
				List<String> randomPicksbuttons = pickNRandom(subArrButtonsName, 1);
				
				connections.add(new String[] {randomPicksbuttons.get(0), randomPicksDoors.get(0)});
				subArrDoorsName.remove(randomPicksDoors.get(0));

				subArrButtonsName.remove(randomPicksbuttons.get(0));
			}
		}
		/*Replace the final door in the connection list*/
		connections.get(connections.size()-1)[1] = doorsName.get(doorsName.size()-1);
		/*Create more than one room if there is more*/
		if(numberOfRooms >= 1) { 
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
					List<Integer> randomPicksDoors = pickNRandom(possiblePositionForDoors, numberOfDoors);		
					for(int i=0; i< randomPicksDoors.size(); i++) {
						int currentDoor = randomPicksDoors.get(i);
						rndDoor.add(currentDoor);	
						temp[lenghtOfRoom - 1][currentDoor] = "f:d>n^"+doorsName.get(0)+"";
						doorsName.remove(0);
					}
					finalStructure.add(temp);
				}
		}
		
		
		
		/* Adding agent in a specific place */
		finalStructure.get(0)[1][1] = "f:a^agent1";
		
		/* Adding buttons randomly in one place */
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

		/* this for create the main structure of final(goal) room */
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < lenghtOfRoom; j++) {
				//Suppose we only have one door that can access to the goal room
				//We choose the last door all the time(because list will add the number at the beginning of the list we select the first one in list )
				if (i == 0 && j == rndDoor.get(rndDoor.size()-1)) {
					goalRoomStructure[i][j] = "f";  
				} else {
					goalRoomStructure[i][j] = "w";
				}
			}

		}

		/* Add goal room to the entire rooms */
		finalStructure.add(goalRoomStructure);
		
		
		/*Create second wall on top of rooms*/
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

		/* Print arrayList in console */
		
		  for (String[][] strArr : finalStructure) {
			  for(int i = 0 ; i <strArr.length; i++){
				  System.out.print( Arrays.toString(strArr[i]) + " "); 
				  }
		  
		  System.out.println("\n");
		  
		  }
		 
		/* Print array in console */
//		for (String[] a : oneRoomStructure) {
//			for (String i : a) {
//				System.out.print(i + "\t");
//			}
//			System.out.println("\n");
//		}

		/* Write in the CSV file */
		BufferedWriter br = new BufferedWriter(new FileWriter("myfile.csv"));
		StringBuilder sb = new StringBuilder();

		// Append connection of doors and buttons at top of the excels
		//for(int i=0; i < numberOfRooms; i++) {
			for (String[] element : connections) {
				System.out.print(element + "\t");
				for (String a : element) {
					sb.append(a);
					sb.append(","); 
				}
				sb.append("\n"); 
			}	
		//}
		sb.append("\n");
		
		/* Apply the final structure to the CSV file */
		for(String[][] list : finalStructure) {
			  for (String[] a : list) {
				  for (String i : a) {
					  sb.append(i); sb.append(","); 
				  } 
				  sb.append("\n"); 
				  }
			}
		
		
		
		
		br.write(sb.toString());
		br.close();
	}

}
