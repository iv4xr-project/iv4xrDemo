package eu.gameLevel.iv4xr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
public class GameLevelMain {

	
	public static void main(String[] args) throws IOException {
		
		Scanner sc = new Scanner(System.in);
		System.out.print("Which level would you like to create:"
				+"\n"+ "(1) First level is connectiong each door to the button at the same room. enter 1 for selecting this level"
				+"\n"+"(2) Second level is connecting each door to the button at the previous room(exept for the first room)(number of "
						+ "button in each room shoulb be two times of number of active doors) "
						+"\n"+"(3) Third level is the same as the second level by adding blocks in each room"
				+"\n"+ "Enter the game level: ");
		
		int gameLevel = GameLevelUtility.getInt(sc, "level", 3);
		//create it automatically
		String name = "GameLevel"+gameLevel;
		GameLevelAbstract game = new GameLevel1();
		if(gameLevel == 1)
			 game = new GameLevel1();
		else if(gameLevel == 2) {
			game = new GameLevel2();
		}
		else if(gameLevel == 3) {
			game = new GameLevel3();
		}
		game.lenghtOfRoom = GameLevelUtility.getInt(sc, "lenght of rooms", GameLevelAbstract.maximumNumberOfLenght);
		/*
		 * maximumNumberOfButtons is based on the space that we have in our room depends
		 * on length of room, I decided to decrease 10 number of it too. there is no
		 * reason:)
		 */
		game.maximumNumberOfButtons = (game.lenghtOfRoom -1)*(game.lenghtOfRoom -1) - 10;
		game.numberOfRooms = GameLevelUtility.getInt(sc, "rooms", GameLevelAbstract.maximumNumberOfRooms);
		game.maximumnumberOfDoors = (game.lenghtOfRoom - 2)/2;
		game.numberOfDoors = GameLevelUtility.getInt(sc, "doors", game.maximumnumberOfDoors);
		game.maximumNumberOfActiveDoors = game.numberOfDoors;
		game.numberOfActiveDoors = GameLevelUtility.getInt(sc, "active doors", game.maximumNumberOfActiveDoors);
		game.numberOfButtons = GameLevelUtility.getInt(sc, "buttons",game.maximumNumberOfButtons);
		
		if(gameLevel == 3) {
			/*I decided to limit the number of blocks based on the number of active doors, there is no reason*/
			game.numberofBlocks =  GameLevelUtility.getInt(sc, "blocks",game.numberOfActiveDoors);
		}
		//game.initial(numberOfActiveDoors);
		// does it work or not
		game.goalRoomStructure = new String[2][game.lenghtOfRoom];
		
		/* Create a list of doors name*/
		game.setDoorsName(game.numberOfRooms, game.numberOfDoors);
		
		/* Create a list of buttons name*/
		game.setButtonsName(game.numberOfRooms, game.numberOfButtons);
		
		/*Get connections between doors and buttons based on the number of active doors from user*/	
//		for(int i=0; i< numberOfActiveDoors; i++) {
//	    	String temp[] = new String[2];
//	    		temp[0] = getString(new Scanner(System.in), "button",numberOfDoors,numberOfButtons,connections );
//	    		temp[1] = getString(new Scanner(System.in), "door",numberOfDoors,numberOfButtons,connections );
//	    		connections.add(temp);
//	    }
		
		/*Create connections between doors and buttons based on the number of active doors automatically*/	
		/*Simple way: select buttons in each rooms*/
		
		game.setConnections();

		/*Replace the final door in the connection list*/
		game.setFinalDoor();
		
		/*Create more than one room if there is more*/
		if(game.numberOfRooms >= 1) { 
			game.setRooms();
		}
		
		
		/* Adding agent in a specific place */
		game.finalStructure.get(0)[1][1] = "f:a^agent1";
		
		/* Adding buttons randomly in one place */
			game.setButtons();

		/*Adding blocks randomly in each room*/
			if(gameLevel == 3) {
				game.setBlocks();
			}
		/* this for create the main structure of the goal room */
		game.setGoalRoom();

		/* Add goal room to the entire rooms */
		game.finalStructure.add(game.goalRoomStructure);
		
		
		/*Create second wall on top of rooms*/
		game.setSecondWall();

		/* Print arrayList in console */
		
		  for (String[][] strArr : game.finalStructure) {
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
		String folderPath = "C:\\Samira\\Ph.D\\iv4xrDemo2\\src\\test\\resources\\levels\\"+ name+"\\result";
		File theDir = new File(folderPath);
		if(!theDir.exists())
			theDir.mkdirs();
		/*Create non repeated name for CSV files*/
		Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH.mm.ss");
        String currentDateTime = format.format(date);
        String fileName = folderPath+"\\"+name+"_"+currentDateTime+".csv";
       
		BufferedWriter br = new BufferedWriter(new FileWriter(fileName));
		StringBuilder sb = new StringBuilder();
		// Append connection of doors and buttons at top of the excels
			for (String[] element : game.connections) {
				for (String a : element) {
					sb.append(a);
					sb.append(","); 
				}
				sb.append("\n"); 
			}	
		//sb.append("\n");
		
		/* Apply the final structure to the CSV file */
		for(String[][] list : game.finalStructure) {
			  for (String[] a : list) {
				  for (String i : a) {
					  sb.append(i); sb.append(","); 
				  } 
				  sb.append("\n"); 
				  }
			}
	
		br.write(sb.toString());
		br.close();
	
		//public static final String delimiter = ",";
//		String csvFile = "C:\\Samira\\Ph.D\\iv4xrDemo2\\src\\test\\resources\\levels\\GameLevel3\\GameLevel3_2020_07_30_09.41.26.csv";
//		List<List<String>> records = new ArrayList<>();
//		try {
//	         File file = new File(csvFile);
//	         Scanner sc = new Scanner(file);
//	         String line = "";
//	         boolean isValid = true;
//	         String[] tempArr;
//	         while(sc.hasNext() && isValid) {
//	            tempArr = sc.next().split(delimiter);
//	            List<String> values = new ArrayList<String>();
//	            for(String tempStr : tempArr) {
//	            	if(!tempStr.endsWith("|w")) {
//	            		values.add(tempStr);
//	            	}else {
//	            		isValid = false;
//	            		break;
//	            	}
//	            	
//	               System.out.print(tempStr + " ");
//	            }
//	            if(!values.isEmpty())
//	        	 records.add(values);
//	        	 System.out.println();
//	         }
//	        
//	         } catch(IOException ioe) {
//	            ioe.printStackTrace();
//	         }
//		System.out.println(records);
	}

}
