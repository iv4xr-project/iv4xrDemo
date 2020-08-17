package eu.gameLevel.iv4xr;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class GameLevelUtility {
	
	/*pick randomly a list of integer from a list */
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
	    	if(prompt != "level")
	        System.out.print("enter the number of " + prompt + "(less than "+ maxNumber +") :");
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
	    		System.out.print("enter the name of "+prompt+" you want to connect(use this structure: "+prompt+"1) ");
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
}
