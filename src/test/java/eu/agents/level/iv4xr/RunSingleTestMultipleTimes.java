package eu.agents.level.iv4xr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.opentest4j.AssertionFailedError;

import game.Platform;

public class RunSingleTestMultipleTimes {

	public static void main(String[] args) throws InterruptedException, IOException{
		
		String levelName = "GameLevel3\\result_loc";
		File directory = new File(Platform.LEVEL_PATH +"\\" + levelName );
		File fileCount[] = directory.listFiles();
		
		String folderPath = "C:\\Samira\\Ph.D\\iv4xrDemo2\\src\\test\\resources\\levels\\GameLevel3\\result_loc";
		File theDir = new File(folderPath);
		if(!theDir.exists())
			theDir.mkdirs();
		String resultFile = folderPath+"\\GameLevel3_result_smarttttt_loc.csv";  
		BufferedWriter br = new BufferedWriter(new FileWriter(resultFile));
		StringBuilder sb = new StringBuilder();
		
		/*Read the result file of whole samples*/
		String csvFile = Platform.LEVEL_PATH +"\\GameLevel3\\result of unity without connector\\GameLevel3_result_smart_loc.csv";
    	List<List<String>> records = new ArrayList<>();
		String delimiter = ",";
		File file = new File(csvFile);
        Scanner sc = new Scanner(file);
        String line = "";
        String[] tempArr;
        /*get failed samples and 10 random success samples*/
        List<String> failed = new ArrayList<String>();
        List<String> success = new ArrayList<String>();
        while(sc.hasNext()) {
           tempArr = sc.next().split(delimiter);
		           	if(tempArr.length > 1) {	
		           		success.add(tempArr[0]);
		           	}else {
		           		failed.add(tempArr[0]);
		           	}     	
        }
        
        /*Select random files of success samples*/
        Collections.shuffle(success);
        List<String> randomSuccess = success.subList(0, 10);
        randomSuccess.add("failed");
        randomSuccess.addAll(failed);
        
        for(int i = 0; i < randomSuccess.size(); i++) {
        	System.out.println(randomSuccess.get(i));
 
        	if(randomSuccess.get(i) != "failed") {
        	//read file's name  
	    	for(int s = 0; s < 10; s++) {
		        String fileName = randomSuccess.get(i);
		    	sb.append(fileName);
				sb.append(","); 
				
				
					try {
			    		List<Object> myList = new ArrayList<Object>();
			    		
			    		/*normal level test*/
	//		    		LevelTest objLevelTest = new LevelTest();
	//					LevelTest.start();
	//					myList = objLevelTest.closetReachableTest(levelName, fileName );
	//					LevelTest.close();
			    		
			    		/*Level test smarter agent*/
			    		LevelTestSmarterAgent objLevelTestSmarter = new LevelTestSmarterAgent();
			    		LevelTestSmarterAgent.start();
						myList = objLevelTestSmarter.closetReachableTest(levelName, fileName );
						LevelTestSmarterAgent.close();					
						for (Object element : myList) {
							sb.append(element);
							sb.append(","); 	 
						}
						sb.append("\n");
			    	}catch(AssertionFailedError afe){
			    		sb.append("failed");
			    		sb.append("\n");
			    		continue;
			    	}
		    	
	    	}
	    	
        }
        else{continue;} 	
        }
        br.write(sb.toString());
		br.close();
	}
}
