package helperclasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.imageio.ImageIO;

import eu.iv4xr.framework.spatial.Vec3;

public class GraphPlotter {
	
	public static void mkScatterGraph(Map<Vec3,Float> data, 
			String fileToSave,
			int width, int height, 
			float scale,
			int dotSize) throws IOException {
		
		BufferedImage image = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB) ;
		Graphics2D g2d = image.createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(0,0, width, height);
		
		for(var p : data.entrySet()) {
			
			int x = Math.round(p.getKey().x * scale) ;
			int y = Math.round(p.getKey().z * scale) ;
			
			Color c = new Color(p.getValue(),0.5f,0.5f) ;
			g2d.setColor(c); 
			g2d.fillOval(x, y, dotSize, dotSize);
		}
		
		g2d.dispose();
	    File file = new File(fileToSave);
	    ImageIO.write(image, "png", file);		
	}
	

}
