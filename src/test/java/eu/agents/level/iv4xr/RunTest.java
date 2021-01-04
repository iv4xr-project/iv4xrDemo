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
		
		String levelName = "GameLevel1\\result_logic_loc";
		File directory = new File(Platform.LEVEL_PATH +"\\" + levelName );
		File fileCount[] = directory.listFiles();
		
		String folderPath = "C:\\Samira\\Ph.D\\new version of everythings\\iv4xrDemo-v2.0\\src\\test\\resources\\levels\\GameLevel1";
		File theDir = new File(folderPath);
		if(!theDir.exists())
			theDir.mkdirs();
		String resultFile = folderPath+"\\GameLevel1_result_smart_loc_log3.csv";  
		BufferedWriter br = new BufferedWriter(new FileWriter(resultFile));
		StringBuilder sb = new StringBuilder();
		
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
			
				for (Object element : myList) {
					sb.append(element);
					sb.append(","); 
					 
				}
				sb.append("\n");
	    	}catch(AssertionFailedError afe){
	    		sb.append("\n");
	    		continue;
	    	}
	    	
    	}
    	br.write(sb.toString());
		br.close();
	}
}
