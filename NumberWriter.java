package mandelbrotSet;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class NumberWriter {
	public NumberWriter() {
		
	}
	
	private void printArray(BufferedImage image, boolean[][] number, int pointer, int layer) {
		//Prints a character at the position of a layer and pointer
		for(int x = 0; x < 5; x++) {
			for(int y = 0; y < 7; y++) {
				if(number[y][x]) {
					image.setRGB(x + pointer, y + (7*layer), Color.black.getRGB());
				} else {
					image.setRGB(x + pointer, y + (7*layer), Color.white.getRGB());
				}
			}
		}
	}
	
	public BufferedImage write(BufferedImage image, String text){
		boolean[][] number;
		int pointer = 0;
		int layer = 0;
	    for(int x = 0; x < text.length(); x++) {
	    	//System.out.println("letter # " + x);
	    	//System.out.println("distance " + x * 5);

	    	//Detects if the text has run out of space, and increments the layer
	    	if((pointer + 1)*5 > image.getWidth()) {
		    	//System.out.println("will be out of bounds");
	    		layer++;
	    		pointer = 0;
	    	}
	    	
	    	//defines all characters able to be written
	        switch(text.charAt(x)) {
	        case '1':
	           	number = new boolean[][]{
                    {false,false,false,false,false},
                    {false,true,true,false,false},
                    {false,false,true,false,false},
                    {false,false,true,false,false},
                    {false,false,true,false,false},
                    {false,true,true,true,false},
                    {false,false,false,false,false}};
                printArray(image, number, pointer * 5, layer);
                break;
			
			case '2':
				number = new boolean[][]{
					{false,false,false,false,false},
					{false,true,true,true,false},
					{false,false,false,true,false},
					{false,true,true,true,false},
					{false,true,false,false,false},
					{false,true,true,true,false},
					{false,false,false,false,false}};
				printArray(image, number, pointer * 5, layer);
				break;
			
			case '3':
				number = new boolean[][]{
					{false,false,false,false,false},
					{false,true,true,true,false},
					{false,false,false,true,false},
					{false,true,true,true,false},
					{false,false,false,true,false},
					{false,true,true,true,false},
					{false,false,false,false,false}};
				printArray(image, number, pointer * 5, layer);
				break;
			
			case '4':
				number = new boolean[][]{
                    {false,false,false,false,false},
                    {false,true,false,true,false},
                    {false,true,false,true,false},
                    {false,true,true,true,false},
                    {false,false,false,true,false},
                    {false,false,false,true,false},
                    {false,false,false,false,false}};
                printArray(image, number, pointer * 5, layer);
                break;
			
			case '5':
				number = new boolean[][]{
					{false,false,false,false,false},
					{false,true,true,true,false},
					{false,true,false,false,false},
					{false,true,true,true,false},
					{false,false,false,true,false},
					{false,true,true,true,false},
					{false,false,false,false,false}};
				printArray(image, number, pointer * 5, layer);
				break;
			
			case '6':
				number = new boolean[][]{
					{false,false,false,false,false},
					{false,true,true,true,false},
					{false,true,false,false,false},
					{false,true,true,true,false},
					{false,true,false,true,false},
					{false,true,true,true,false},
					{false,false,false,false,false}};
				printArray(image, number, pointer * 5, layer);
				break;
			
			case '7':
				number = new boolean[][]{
                    {false,false,false,false,false},
                    {false,true, true,true,false},
                    {false,false,false,true,false},
                    {false,false,true,true,false},
                    {false,false,false,true,false},
                    {false,false,false,true,false},
                    {false,false,false,false,false}};
                printArray(image, number, pointer * 5, layer);
                break;
			
			case '8':
				number = new boolean[][]{
					{false,false,false,false,false},
					{false,true,true,true,false},
					{false,true,false,true,false},
					{false,true,true,true,false},
					{false,true,false,true,false},
					{false,true,true,true,false},
					{false,false,false,false,false}};
				printArray(image, number, pointer * 5, layer);
				break;
			
			case '9':
				number = new boolean[][]{
					{false,false,false,false,false},
					{false,true,true,true,false},
					{false,true,false,true,false},
					{false,true,true,true,false},
					{false,false,false,true,false},
					{false,true,true,true,false},
					{false,false,false,false,false}};
				printArray(image, number, pointer * 5, layer);
				break;
			
			case '0':
				number = new boolean[][]{
					{false,false,false,false,false},
					{false,true,true,true,false},
					{false,true,false,true,false},
					{false,true,false,true,false},
					{false,true,false,true,false},
					{false,true,true,true,false},
					{false,false,false,false,false}};
				printArray(image, number, pointer * 5, layer);
				break;
				
			case '-':
				number = new boolean[][]{
					{false,false,false,false,false},
					{false,false,false,false,false},
					{false,false,false,false,false},
					{false,true,true,true,false},
					{false,false,false,false,false},
					{false,false,false,false,false},
					{false,false,false,false,false}};
				printArray(image, number, pointer * 5, layer);
				break;
			
			case '.':
				number = new boolean[][]{
                    {false,false,false,false,false},
                    {false,false,false,false,false},
                    {false,false,false,false,false},
                    {false,false,false,false,false},
                    {false,false,true,true,false},
                    {false,false,true,true,false},
                    {false,false,false,false,false}};
                printArray(image, number, pointer * 5, layer);
                break;
                
			case ',':
				number = new boolean[][]{
                    {false,false,false,false,false},
                    {false,false,false,false,false},
                    {false,false,false,false,false},
                    {false,false,false,false,false},
                    {false,false,true,true,false},
                    {false,false,false,true,false},
                    {false,false,false,false,false}};
                printArray(image, number, pointer * 5, layer);
                break;
                
			case 'E':
				number = new boolean[][]{
					{false,false,false,false,false},
					{false,true,true,true,false},
					{false,true,false,false,false},
					{false,true,true,true,false},
					{false,true,false,false,false},
					{false,true,true,true,false},
					{false,false,false,false,false}};
				printArray(image, number, pointer * 5, layer);
				break;
			
			case ' ':
				number = new boolean[][]{
                    {false,false,false,false,false},
                    {false,false,false,false,false},
                    {false,false,false,false,false},
                    {false,false,false,false,false},
                    {false,false,false,false,false},
                    {false,false,false,false,false},
                    {false,false,false,false,false}};
                printArray(image, number, pointer * 5, layer);
                break;
			}
	        
	        //increment the pointer
	        pointer++;
			
		}
		return(image);
	}
	
	public static void main(String[] args) {
		BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		NumberWriter writer = new NumberWriter();
		
		double num = 0.0000016;
		
		//img = writer.write(img, String.valueOf(num));
		img = writer.write(img, "E1.2 345, 67890");
		//System.out.println(String.valueOf(num));
		File outputfile = new File("txt.png");
	    try {
	    	ImageIO.write(img, "png", outputfile);
	    }catch(IOException e) {
	    	
	    }
	}
}
