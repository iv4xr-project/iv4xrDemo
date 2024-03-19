package leveldefUtil;

import static nl.uu.cs.aplib.utils.CSVUtility.readTxtFile;

import java.io.IOException;
import java.util.*;
import nl.uu.cs.aplib.utils.Pair;

/**
 * To obtain the connection button-doors from an LR definition file.
 */
public class LRconnectionLogic {
	
	/**
	 * Parse an LR-definition file and return its button-door connections. This is
	 * represented as a set of pairs (button,door) that are connected.
	 */
	public static Set<Pair<String,String>> parseConnections(String file) throws IOException {
		var content = readTxtFile(file) ;
		// can't use lines() .. that is java 11:
		// var rows = content.lines().toList() ;
		var rows = content.split("\\n") ;
		
		Set<Pair<String,String>> connections = new HashSet<>() ;
		for (var i=0; i<rows.length ; i++) {
			String r = rows[i] ;
			if (r.startsWith("|")) break ;
			var components = r.split(",") ;
			if (components.length >= 2) {
				String button = components[0] ;
				for (int d=1; d<components.length; d++) {
					String door = components[d] ;
					connections.add(new Pair<String,String>(button,door)) ;
				}
			}
		}
		return connections ;
	}
 	
	/**
	 * Compare two connections-logic. One is reference logic, the other can be thought as
	 * some inferred or guess logic. The result is a map:
	 * 
	 * <p> "#connections" : number of connections button-door in the refernce logic.
	 * <br> "#inferred" : number of connections in the inferred logic.
	 * <br> "#correct" : number of connections C, present in both logics.
	 * <br> "#wrong" : the number of connections C that are in the inferred logic, but 
	 *                 are not present in the reference logic.
     *
	 */
	public static Map<String,Integer> compareConnection(
			            Set<Pair<String,String>> referenceLogic,
			            Set<Pair<String,String>> logic	
			) {
		int numberOfConnections = referenceLogic.size() ;
		int numberOfInferredConnections = logic.size() ;
		
		int numberOfCorrectlyInferredConnections = 0 ;
		for (var C : referenceLogic) {
			if (logic.contains(C))
				numberOfCorrectlyInferredConnections++ ;
		}
		int numberOfWrongConnections = 0 ;
		for (var C : logic) {
			if (! referenceLogic.contains(C))
				numberOfWrongConnections++ ;
		}
		
		Map<String,Integer> R = new HashMap<>() ;
		R.put("#connections", numberOfConnections) ;
		R.put("#inferred", numberOfInferredConnections) ;
		R.put("#correct", numberOfCorrectlyInferredConnections) ;
		R.put("#wrong", numberOfWrongConnections) ;
		return R ;
	}
	
	public static void main(String[] args) throws IOException {
		String file = "./src/test/resources/levels/buttons_doors_1.csv" ;
		var L0 = parseConnections(file) ;
		System.out.println(compareConnection(L0,L0)) ;
	}
	

}
