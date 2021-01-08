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
		
		String levelName = "GameLevel2"+File.separator+"result_logic_loc";
		//String levelName = "GameLevel1"+File.separator+"result_logic_loc";
		File directory = new File(Platform.LEVEL_PATH +File.separator+ levelName );
		File fileCount[] = directory.listFiles();
		
		String folderPath = Platform.LEVEL_PATH +File.separator+ "GameLevel2"+File.separator+ "final_results\\result_logic_loc";
		File theDir = new File(folderPath);
		if(!theDir.exists())
			theDir.mkdirs();
		String resultFile = folderPath+File.separator+"GameLevel2_result_logic_loc2.csv";  
		//String resultFile = folderPath+File.separator+"GameLevel1_result_smart_loc_log6.csv";  
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
			
			
	    	try {
	    		
	    		List<Object> myList = new ArrayList<Object>();
	    		
	    		/*normal level test*/
//	    		LevelTest objLevelTest = new LevelTest();
//				LevelTest.start();
//				myList = objLevelTest.closetReachableTest(levelName, fileName );
//				LevelTest.close();
	    		
	    		/*Level test smarter agent*/
	    		LevelTestSmarterAgent objLevelTestSmarter = new LevelTestSmarterAgent();
	    		LevelTestSmarterAgent.start();
				myList = objLevelTestSmarter.closetReachableTest(levelName, fileName );
				LevelTestSmarterAgent.close();
				
	    		Thread.sleep(2000); // add some delay to allow LR run to close?

				for (Object element : myList) {
					sb.append(element);
					sb.append(","); 
					 
				}
				sb.append("\n");
				
				if(myList.size()==3 && myList.get(2).equals("success")) {
					numberOfSuccesses++ ;
				}
				else numberOfFails++ ;
				System.out.printf("##== success/fail/number: %d/%d/%d%n", numberOfSuccesses, numberOfFails, s+1 ) ;
				
	    	}catch(AssertionFailedError afe){
	    		sb.append("\n");
	    		continue;
	    	}
	    	
    	}
    	br.write(sb.toString());
		br.close();
	}
}
