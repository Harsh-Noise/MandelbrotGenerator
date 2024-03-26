package mandelbrotSet;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SetBuilder {	
	
	
	
	public static Complex iterate(Complex z, Complex c) {
		//This method controls the formula used. This one is specific to the mandelbrot set.
		return(c.plus(z.times(z)));
	}
	
	public static Color doesExplode(Complex c, int reductionFactor) {
		//This method controls the color of the pixel. It can be changed to return greyscale or black and white. It is recommended to change the color scheme if reduced color versions are being used to reduce file sizes.
		
		Complex z = new Complex(0, 0);
		int timer;
		int max = 16777216; //256^3			
		
		for(timer = 0; timer < max; timer+= max/reductionFactor) {
			z = new Complex(iterate(z, c));
			
			if(Math.sqrt(Math.pow(z.re(), 2) + Math.pow(z.im(), 2)) > 2.2) {
				break;
			}
		}
		return(new Color(timer));
	}
	
	public static double pxToNumY(int px, double graphSize, int imageSize, double shiftY) {
		//This method converts a pixel on the y axis to its corresponding unit
		return((((double)graphSize / imageSize) * px) - (graphSize / 2) + shiftY);
	}

	public static double pxToNumX(int px, double graphSize, int imageSize, double shiftX) {
		//This method converts a pixel on the x axis to its corresponding unit
		return((((double)graphSize / imageSize) * px) - (graphSize / 2) + shiftX);
	}
	
	public static void main(String [] args) {
		//Side length of the graph (units)
		double graphSize = 2.5;
		
		//Side length of the image (pixels)
		int imageSize = 720;
		int pixTotal = imageSize * imageSize;
		
		//location of the center of the img
		double shiftX = -0.75;
		double shiftY = 0;
		
		//number of colors colors (up to 256^3)
		int reductionFactor = 10000;
		
		BufferedImage img = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
		
		int loadingTimer = 0;
		
		//loop for every pixel in the img
		for(int x = 0; x < imageSize; x++) {
			//Rudimentary loading bar, counts up to ~100
			if(x%(imageSize/100) == 0) {
				System.out.println(loadingTimer);
				loadingTimer++;
			}
			for(int y = 0; y < imageSize; y++) {
				
				//get complex number represented by the pixel on the img
				Complex c = new Complex(pxToNumX(x, graphSize, imageSize, shiftX), pxToNumY(y, graphSize, imageSize, shiftY));
				
				//test if it explodes off to infinity, and use returned color to paint the pixel
				img.setRGB(x, y, doesExplode(c, reductionFactor).getRGB());
				
			}
		}		
		
		//Write the x, y, and zoom at the top left corner of the img
		NumberWriter writer = new NumberWriter();
		img = writer.write(img, String.valueOf(shiftX) + ", " + String.valueOf(shiftY) + ", " + String.valueOf(graphSize));
		
		//Output file
		File outputfile = new File("brot.png");
	    try {
	    	ImageIO.write(img, "png", outputfile);
	    }catch(IOException e) {
	    	
	    }
	}
}
