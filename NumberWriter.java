package harsh.mandelbrotgenerator;

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
					image.setRGB(x + pointer, y + layer, Color.black.getRGB());
				} else {
					image.setRGB(x + pointer, y + layer, Color.white.getRGB());
				}
			}
		}
	}
	
	public BufferedImage write(BufferedImage image, String text){
            int pointer = 0;
            int layer = 0;
            //Check once to make sure there is room for the label, and return an unlabled image otherwise.
            for(int x = 0; x < text.length(); x++) {
	    	if((pointer + 1)*5 > image.getWidth()) {
                    if((layer + 2)*7 > image.getHeight()){
                        System.out.println("[???] Not enough space available to label the image. Please disable the label writer.");
                        return(image);
                    }
                    layer++;
                    pointer = 0;    
	    	}
            	//increment the pointer
	        pointer++;
            }
            
            //Get each character array and place it in the correct spot.
            pointer = 0;
            layer = 0;
	    for(int x = 0; x < text.length(); x++) {
	    	if((pointer + 1)*5 > image.getWidth()) {
                    if((layer + 2)*7 > image.getHeight()){
                        System.out.println("[!!!] Label writer ran out of space unexpectedly! Send Harsh Noise your settings and command, and tell him it's borked!");
                        break;
                    }
                    layer++;
                    pointer = 0;    
	    	}
                printArray(image, getCharMatrix(text.charAt(x)), pointer * 5, layer * 7);
	        
	        //increment the pointer
	        pointer++;
            }
            return(image);
	}
        
        private boolean[][] getCharMatrix(char next) {
            //defines all characters able to be written
            boolean[][] number;
            switch (next){
                case '1':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, true, false, false},
                        {false, false, true, false, false},
                        {false, false, true, false, false},
                        {false, false, true, false, false},
                        {false, true, true, true, false},
                        {false, false, false, false, false}};
                    return(number);

                case '2':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, true, true, false},
                        {false, false, false, true, false},
                        {false, true, true, true, false},
                        {false, true, false, false, false},
                        {false, true, true, true, false},
                        {false, false, false, false, false}};
                    return(number);

                case '3':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, true, true, false},
                        {false, false, false, true, false},
                        {false, true, true, true, false},
                        {false, false, false, true, false},
                        {false, true, true, true, false},
                        {false, false, false, false, false}};
                    return(number);

                case '4':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, false, true, false},
                        {false, true, false, true, false},
                        {false, true, true, true, false},
                        {false, false, false, true, false},
                        {false, false, false, true, false},
                        {false, false, false, false, false}};
                    return(number);

                case '5':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, true, true, false},
                        {false, true, false, false, false},
                        {false, true, true, true, false},
                        {false, false, false, true, false},
                        {false, true, true, true, false},
                        {false, false, false, false, false}};
                    return(number);

                case '6':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, true, true, false},
                        {false, true, false, false, false},
                        {false, true, true, true, false},
                        {false, true, false, true, false},
                        {false, true, true, true, false},
                        {false, false, false, false, false}};
                    return(number);

                case '7':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, true, true, false},
                        {false, false, false, true, false},
                        {false, false, true, true, false},
                        {false, false, false, true, false},
                        {false, false, false, true, false},
                        {false, false, false, false, false}};
                    return(number);

                case '8':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, true, true, false},
                        {false, true, false, true, false},
                        {false, true, true, true, false},
                        {false, true, false, true, false},
                        {false, true, true, true, false},
                        {false, false, false, false, false}};
                    return(number);
                case '9':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, true, true, false},
                        {false, true, false, true, false},
                        {false, true, true, true, false},
                        {false, false, false, true, false},
                        {false, true, true, true, false},
                        {false, false, false, false, false}};
                    return(number);

                case '0':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, true, true, false},
                        {false, true, false, true, false},
                        {false, true, false, true, false},
                        {false, true, false, true, false},
                        {false, true, true, true, false},
                        {false, false, false, false, false}};
                    return(number);

                case '-':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, true, true, true, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false}};
                    return(number);

                case '.':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, true, true, false, false},
                        {false, true, true, false, false},
                        {false, false, false, false, false}};
                    return(number);

                case ',':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, true, true, false, false},
                        {false, false, true, false, false},
                        {false, false, false, false, false}};
                    return(number);

                case 'E':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, true, true, false},
                        {false, true, false, false, false},
                        {false, true, true, true, false},
                        {false, true, false, false, false},
                        {false, true, true, true, false},
                        {false, false, false, false, false}};
                    return(number);

                case ' ':
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false},
                        {false, false, false, false, false}};
                    return(number);

                default:
                    number = new boolean[][]{
                        {false, false, false, false, false},
                        {false, true, true, true, false},
                        {false, false, false, true, false},
                        {false, false, true, true, false},
                        {false, false, false, false, false},
                        {false, false, true, false, false},
                        {false, false, false, false, false}};
                    return(number);
            }
        }
	
	public static void main(String[] args) {
		BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		NumberWriter writer = new NumberWriter();
		
		double num = 0.0000016;
		
		//img = writer.write(img, String.valueOf(num));
		img = writer.write(img, "E1.2 345, 67890 HELLO WORLD");
		//System.out.println(String.valueOf(num));
		File outputfile = new File("txt.png");
	    try {
	    	ImageIO.write(img, "png", outputfile);
	    }catch(IOException e) {
	    	
	    }
	}
}
