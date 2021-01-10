package eu.agents.level.iv4xr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opentest4j.AssertionFailedError;

import game.Platform;

public class RunTest {
	
	public static void main(String[] args) throws InterruptedException, IOException{
		
		LevelTest.start();
		String levelGroup = "GameLevel1" ;
		String mutationGroup = "logic_loc" ;
		String testType = "smart" ;
		int logfileId = 9 ;
		runeExperiment(testType, levelGroup, mutationGroup,logfileId) ;
		
		LevelTest.close();
	}

	/**
	 * 
	 * @param testType       "simple" or "smart"
	 * @param levelGroup     e.g. "GameLevel1"
	 * @param mutationGroup  e.g. "loc" or "logic_loc"
	 */
	public static void runeExperiment(String testType, String levelGroup, String mutationGroup, int logId) throws InterruptedException, IOException{
		
		//String levelName = "GameLevel1"+File.separator+"result_logic_loc";
		String levelName = levelGroup + File.separator + "result_" + mutationGroup ;
		File directory = new File(Platform.LEVEL_PATH +File.separator+ levelName );
		File fileCount[] = directory.listFiles();
		
		//String folderPath = Platform.LEVEL_PATH +File.separator+ "GameLevel1";
		String folderPath = Platform.LEVEL_PATH + File.separator + levelGroup;
		File theDir = new File(folderPath);
		if(!theDir.exists())
			theDir.mkdirs();
		//String resultFile = folderPath+File.separator+"GameLevel1_result_loc_log6.csv";  
		String resultFile = folderPath + File.separator + levelGroup + "_result_" + testType + "_" + mutationGroup + "_log" + logId + ".csv";  
		BufferedWriter br = new BufferedWriter(new FileWriter(resultFile));
		StringBuilder sb = new StringBuilder();
		
		int numberOfSuccesses = 0 ;
		int numberOfFails = 0 ;
		
		//read file's name
    	for(int s = 0; s < fileCount.length; s++) {
	        String fileName = fileCount[s].getName();
	        fileName = fileName.replaceFirst("[.][^.]+$", "");
	    	sb.append(fileName);
			sb.append(","); 
			
			List<Object> myList = new ArrayList<Object>();
    		
	    	try {
	    		
	    		
	    		switch(testType) {
	    		  case "simple" :
	  	    		  /*normal level test*/
	  	    		  LevelTest objLevelTest = new LevelTest();
	  				  //LevelTest.start();
	  				  myList = objLevelTest.closetReachableTest(levelName, fileName );
	  				  //LevelTest.close();
	  				  break ;
	    		  case "smart" :
	    			  /*Level test smarter agent*/
	  	    		  LevelTestSmarterAgent objLevelTestSmarter = new LevelTestSmarterAgent();
	  	    		  //LevelTestSmarterAgent.start();
	  				  myList = objLevelTestSmarter.closetReachableTest(levelName, fileName );
	  				  //LevelTestSmarterAgent.close();
	  				  break ;
	  				default : throw new IllegalArgumentException() ;
	    		}

				
	    		// Thread.sleep(4000); // some delay to let you read intermediate result 

				for (Object element : myList) {
					sb.append(element);
					sb.append(","); 
					 
				}
				sb.append("\n");
				
						
	    	}catch(AssertionFailedError afe){
	    		sb.append("\n");
	    		//continue;
	    	}
	    	if(myList.size()==3 && myList.get(2).equals("success")) {
				numberOfSuccesses++ ;
			}
			else numberOfFails++ ;
			System.out.printf("##== success/fail/number: %d/%d/%d%n", numberOfSuccesses, numberOfFails, s+1 ) ;
	    	
    	}
    	br.write(sb.toString());
		br.close();
	}
}
