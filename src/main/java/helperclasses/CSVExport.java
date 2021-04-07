package helperclasses;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExport {
	
	public static void exportToCSV(List<String[]> data, String filename) throws IOException {
		
		StringBuffer buf = new StringBuffer() ;
		int k=0 ;
		for(String[] row : data) {
			if(k>0) buf.append("\n") ;
			for(int y=0; y<row.length; y++) {
				if(y>0) buf.append(",") ;
				buf.append(row[y]) ;
			}
			k++ ;
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(buf.toString()) ;
        writer.close();
		
	}

}
