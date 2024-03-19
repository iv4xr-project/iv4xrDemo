package leveldefUtil;

import java.io.IOException;
import java.util.*;

import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.utils.Pair;
import static nl.uu.cs.aplib.utils.CSVUtility.* ;

/**
 * A logical map of an LR-level. For now this assumes that a single flat floor.
 * 
 * <p>Currently it cannot deal objects that overlap with floor, like color-screen and
 * book case.
 */
public class LRFloorMap {
	
	public enum TileType { FLOOR, DOOR, BUTTON, WALLLIKE, GOALFLAG } ;
	
	public static class LRTile {
		public TileType ty ;
		public String id ;
		public Boolean fire = false ;
		public Vec3 center ;
	}
	
	/**
	 * Give a list of rows defining a floor (without leading "|"), and calculate
	 * the dimension of the smallest rectangle that enclose the floor. It is
	 * returned as a pair (w,h) where w is the width (horizontal) of the rectangle,
	 * and h is its height (vertical).
	 */
	private static Pair<Integer,Integer> floorSize(List<String> floor) {
		int depth = floor.size() ;
		int width = 0 ;
		for (String row : floor) {
			int n = row.split(",").length ;
			width = Math.max(width,n) ;
		}
		return new Pair<Integer,Integer>(width,depth) ;
	}
	
	private static boolean isFloor(String cell) {
		return Arrays.asList(cell.split(":")).contains("f") ;
	}
	
	private static boolean isWall(String cell) {
		return Arrays.asList(cell.split(":")).contains("w") ;
	}
	
	private static boolean hasFire(String cell) {
		return cell.contains("dhf") ;
	}
	
	private static String getGoalFlagId(String cell) {
		var components = cell.split(":") ;
		for (var u : components) {
			if (u.startsWith("g")) {
				// does not work! because ^ is a meta-char. Don't know how to
				// recognize that.
				// return u.split("^")[1] ;
				int k = u.indexOf("^") ;
				return u.substring(k+1) ;
			}
		}
		return null ;
	}
	
	private static String getDoorId(String cell) {
		var components = cell.split(":") ;
		for (var u : components) {
			if (!u.equals("dhf") && (u.startsWith("d") || u.startsWith("od"))) {
				//System.out.println(">>> " + u) ;
				int k = u.indexOf("^") ;
				return u.substring(k+1) ;
			}
		}
		return null ;
	}
	
	private static String getButtonId(String cell) {
		var components = cell.split(":") ;
		for (var u : components) {
			if (u.startsWith("b") || u.startsWith("cb")) {
				//System.out.println(">>> " + u) ;
				int k = u.indexOf("^") ;
				return u.substring(k+1) ;
			}
		}
		return null ;
	}
	
	private static List<LRTile> parseRow(String row) {
		var tiles = row.split(",") ;
		List<LRTile> R = new LinkedList<>() ;
		for (String T : tiles) {
			LRTile lrt = new LRTile() ;
			lrt.fire = hasFire(T) ;
			if (isWall(T)) {
				lrt.ty = TileType.WALLLIKE ;	
				R.add(lrt) ;
				continue ;
				
			}
			lrt.id = getDoorId(T) ;
			if (lrt.id != null) {
				lrt.ty = TileType.DOOR ;
				R.add(lrt) ;
				continue ;
			}
			lrt.id = getButtonId(T) ;
			if (lrt.id != null) {
				lrt.ty = TileType.BUTTON ;
				R.add(lrt) ;
				continue ;
			}
			lrt.id = getGoalFlagId(T) ;
			if (lrt.id != null) {
				lrt.ty = TileType.GOALFLAG ;
				R.add(lrt) ;
				continue ;
			}
			// id is still null, so not a button nor door etc
			if (isFloor(T)) {
				lrt.ty = TileType.FLOOR ;
				R.add(lrt) ;
				continue ;
			}
			// other cases non navigable:
			R.add(null) ;				
		}
		return R ;
	}
	
	private static List<String> parseFloor_(List<String> rows) {
		
		List<String> floor = new LinkedList<>() ;
		// drop leading rows that are not part of a floor:
		boolean floorFound = false ;
		while (!floorFound) {
			if (rows.isEmpty())
				return floor ;
			String r = rows.get(0) ;
			if (r.startsWith("|")) {
				floorFound = true ;
			}
			else {
				rows.remove(0) ;
			}
		}
		// at the start of a floor definition.
		// add the first row:
		String r = rows.remove(0) ;
		r = r.substring(1) ;
		floor.add(r) ;
		boolean floorEndFound = false ;
		while (!floorEndFound) {
			if (rows.isEmpty())
				return floor ;
			r = rows.get(0) ;
			if (r.startsWith("|")) {
				floorEndFound = true ;
			}
			else {
				floor.add(r) ;
				rows.remove(0) ;
			}
		}
		return floor ;
	}
	
	private static LRTile[][] parseFloor(List<String> rows, int elevation) {
		var floor = parseFloor_(rows) ;
		if (floor.isEmpty())
			return null ;
		var dim = floorSize(floor) ;
		int width = dim.fst ;
		int height = dim.snd ;
		LRTile[][] floor_ = new LRTile[width][height] ;
		int y = 0 ;
		for (var row : floor) {
			var R = parseRow(row) ;
			int x = 0 ;
			for (var cell : R) {
				floor_[x][y] = cell ;
				if (cell != null) {
					cell.center = new Vec3(x,elevation,y) ;
				}
				x++ ;
			}
			while (x < width) {
				floor_[x][y] = null ;
				x++ ;
			}
			y++ ;
		}
		return floor_ ;
	}
	
	/**
	 * Parse an LR level-definition and return a 2D array M representing
	 * the first floor of the level. The array is organized such that M[x][z]
	 * corresponds to the tile with center-coordinate (x,9,z) in the actual
	 * game.
	 */
	public static LRTile[][] parseFirstFloor(String LRlevelDefinition) {
		int elevation = 0 ;
		// can't use "lines()" ... that is Java-11
		//var rows = LRlevelDefinition.lines().toList() ;
		var rows = LRlevelDefinition.split("\\n") ;
		
		
		// rows is an immutable list! Turn it to mutable:
		List<String> rows_ = new LinkedList<>() ;
		//for (var r : rows) rows_.add(r) ;
		for (var r=0; r<rows.length; r++) 
			rows_.add(rows[r]) ;
		return parseFloor(rows_, elevation) ;
	}
	
	public static void printLRTileMap(LRTile[][] map, boolean invertY) {
		if (map.length == 0) return ;
		int X = map.length ;
		int Y = map[0].length ;
		for (int y=0; y<Y; y++) {
			int y_ = y ;
			if (! invertY) y_ = Y - y - 1 ;
			System.out.print(">   ");
			for (int x=0; x<X; x++) {
				var C = map[x][y_] ;
				String c = " " ;
				if (C != null) {
					switch (C.ty) {
					  case FLOOR    : c = "f" ; break ;
					  case DOOR     : c = "d" ; break ;
					  case BUTTON   : c = "b" ; break ;
					  case WALLLIKE : c = "w" ; break ;
					  case GOALFLAG : c = "g" ;
					}
				}
				System.out.print(c);
			}
			System.out.println("") ;		
		}
	}

	/**
	 * Parse an LR level-definition-file and return a 2D array M representing
	 * the first floor of the level. The array is organized such that M[x][z]
	 * corresponds to the tile with center-coordinate (x,9,z) in the actual
	 * game.
	 */
	public static LRTile[][] parseFirstFloorFromFile(String file) throws IOException {
		var content = readTxtFile(file) ;
		return parseFirstFloor(content) ;
	}
	
	
	public static void main(String[] args) throws IOException {
		String file = "./src/test/resources/levels/buttons_doors_1.csv" ;
		var M = parseFirstFloorFromFile(file) ;
		printLRTileMap(M,false) ;
	}
}
