package mandelbrotSet;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.Color;


public class colorTesting {
	public static void main(String[] args) {
		int max = 4160;
		BufferedImage img = new BufferedImage(max, max, BufferedImage.TYPE_INT_RGB);
		int numPrev = 0;
		for(int x = 0; x < max; x++) {
			for(int y = 0; y < max; y++) {
				
				Color color = new Color(y + (x * max));
				//if(numPrev > color.getRGB()) {
				//	System.out.println(y + (x * max));
				//}
				img.setRGB(x, y, color.getRGB()); 
				//numPrev = color.getRGB();
			}
		}
		
		File outputfile = new File("test.png");
	    try {
	    	ImageIO.write(img, "png", outputfile);
	    }catch(IOException e) {
	    	
	    }
	}
}
