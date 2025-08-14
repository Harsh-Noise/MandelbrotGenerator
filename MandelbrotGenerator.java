package harsh.mandelbrotgenerator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

interface ProgressListener {
    void onProgressUpdate(int progress); // Interface to send progress updates
}

public class MandelbrotGenerator implements Runnable {
    //Default settings
    //Side length of the graph (units)
    static double graphSize = 2.5;

    //Side length of the image (pixels)
    static int imageSize = 1000;

    //location of the center of the img
    static double shiftX = -0.75;
    static double shiftY = 0;

    //alters the color contrast by reducing/increasing color change per depth
    static double reductionFactor = 1;

    //Color settings b&w = 0, full = 1, roygbiv = 2
    static int colorMode = 0;

    //max calculation iterations
    static int maxDepth = 255;

    //Number of threads in use
    static int threads = 1;

    //Whether ot not to write a label on the image
    static boolean numberWriter = true;
    
    //fileName
    static String fileName = "myMandelbrot";

    //Variables edited by the algorithm
    //Subimage size for each thread
    static int startPoint = 0;
    static int endPoint = 0;

    //Counters for threads
    static volatile int threadsDone = 0;
    static volatile int pixDone = 0;
    static volatile double loadBarChunks = 0.0;

    //shared buffered image
    static volatile BufferedImage img;

    //Detector for CLI use
    static boolean CLI = false;
    
    //Settings file name
    static String settingsFile = "mandelbrotSettings.dat";

    public MandelbrotGenerator() {

    }

    private static ProgressListener listener;

    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
    
    public int getPixDone() {
        return (pixDone);
    }

    public static Complex iterate(Complex z, Complex c) {
        //This method controls the formula used. This one is specific to the mandelbrot set.
        return (c.plus(z.times(z)));
    }

    public static Color getColor(Complex c) {
        //Takes a complex number, and converts the 'getStability' function into a Color object depending on the colormode
        int color;
        switch (colorMode) {
            case 0:
                //Black and white
                if(maxDepth > 255){
                    maxDepth = 255;
                    System.out.println("[???] Max depth out of range for this color mode. Setting calculation depth to 255. Resuming render.");
                }
                
                color = getStability(c, maxDepth);
                    
                color = 255 - color;
                return (new Color(color, color, color));
            case 1:
                //Full
                if(maxDepth > 16581375){
                    maxDepth = 16581375;
                    System.out.println("[???] Max depth out of range for this color mode. Setting calculation depth to 16,581,375. Resuming render.");
                    System.out.println("[¿¿¿] Wtf are you doing?");
                }
                color = getStability(c, maxDepth);

                return (new Color(color));
            case 2:
                //Hue
                color = getStability(c, maxDepth);
                return (new Color(Color.getHSBColor(((float) color / 256), (float) 1, (float) 1).getRGB()));
            case 3:
                //Black and white inv
                if(maxDepth > 255){
                    maxDepth = 255;
                    System.out.println("[???] Max depth out of range for this color mode. Setting calculation depth to 255. Resuming render.");
                }
                                
                color = getStability(c, maxDepth);

                return (new Color(color, color, color));
            default:
                return (new Color(0, 0, 0));
        }
    }

    public static int getStability(Complex c, int limit) {
        //returns the stability of the complex number
        Complex z = new Complex(0, 0);
        int timer;
        for (timer = 0; timer < limit / reductionFactor; timer++) {
            z = new Complex(iterate(z, c));
            //Check if the complex number has exploded off to infinity by seeing if it is greater than 2.2 units away from the origin (an acceptable distance)
            //if (Math.sqrt(Math.pow(z.re(), 2) + Math.pow(z.im(), 2)) > 2.2) {
            if (Math.pow(z.re(), 2) + Math.pow(z.im(), 2) > 4.84) {
                break;
            }
        }
        return ((int) (timer * reductionFactor));
    }
    
    public static double pxToNumY(int px, double graphSize, int imageSize, double shiftY) {
        //This method converts a pixel on the y axis to its corresponding unit
        return ((((double) graphSize / imageSize) * px) - (graphSize / 2) - shiftY);
    }

    public static double pxToNumX(int px, double graphSize, int imageSize, double shiftX) {
        //This method converts a pixel on the x axis to its corresponding unit
        return ((((double) graphSize / imageSize) * px) - (graphSize / 2) + shiftX);
    }
    
    public static int getPercentDone(){
        return((int)(((double)pixDone/(imageSize*imageSize))*100));
    }

    public static void buildBrot(int startPix, int endPix) {
        //loop for every pixel in the img
        double loadBarChunk = (50.0/(imageSize*imageSize));
        for (int x = startPix; x < endPix; x++) {
            for (int y = 0; y < imageSize; y++) {
                //get complex number represented by the pixel on the img
                Complex c = new Complex(pxToNumX(x, graphSize, imageSize, shiftX), pxToNumY(y, graphSize, imageSize, shiftY));

                //test if it explodes off to infinity, and use returned color to paint the pixel
                img.setRGB(x, y, getColor(c).getRGB());
                
                pixDone += 1;
                //loading bar
                if(CLI){
                    loadBarChunks += loadBarChunk;
                }
            }
        }
    }

    public static void loadSettings() {
        File file = new File(settingsFile);
        if (file.exists()) {
            try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
                if(dis.readUTF().equals("Harsh was here")){
                    graphSize = dis.readDouble();
                    imageSize = dis.readInt();
                    shiftX = dis.readDouble();
                    shiftY = dis.readDouble();
                    reductionFactor = dis.readDouble();
                    colorMode = dis.readInt();
                    maxDepth = dis.readInt();
                    threads = dis.readInt();
                    fileName = dis.readUTF();
                    numberWriter = dis.readBoolean();
                    System.out.println("Settings loaded.");
                }else{
                    System.out.println("[???] Settings file is corrupted. Initialized with default values.");
                }
            } catch (IOException e) {
                System.out.println("[!!!] Error reading settings file.");
                System.out.println(e);
                e.printStackTrace();
            }
        } else {
            // File does not exist, so initialize with default data
            System.out.println("[???] Settings file not found. Initialized with default values.");
        }
    }

    public static void saveSettings() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(settingsFile))) {
            dos.writeUTF("Harsh was here");
            dos.writeDouble(graphSize);
            dos.writeInt(imageSize);
            dos.writeDouble(shiftX);
            dos.writeDouble(shiftY);
            dos.writeDouble(reductionFactor);
            dos.writeInt(colorMode);
            dos.writeInt(maxDepth);
            dos.writeInt(threads);
            dos.writeUTF(fileName);
            dos.writeBoolean(numberWriter);
            System.out.println("Settings saved.");
        } catch (IOException e) {
            System.out.println("[!!!] Error writing settings file.");
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public static void render() {
        threadsDone = 0;
        pixDone = 0;
        img = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        //double subImageSize = (double) imageSize / threads;
        double subImageSize = (double) imageSize / (4*threads); //cover 1/4 the img
        
        //Loop for starting threads
        System.out.println("Starting " + threads + " threads");
        for (int x = 0; x < threads; x++) {
            startPoint = (int) (x * subImageSize);
            endPoint = (int) ((x + 1) * subImageSize);
            if (imageSize - endPoint < subImageSize) {
                endPoint = imageSize;
            }
            Thread object = new Thread(new MandelbrotGenerator());
            object.start();
            //Small delay such that threads have time to update shared resources 
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("[!!!] An unexpected exception has occured while starting threads. Send Harsh Noise a copy of this stack trace and tell him it's borked!\n");
                System.out.println(e);
                e.printStackTrace();
            }
        }
        
        //Loop while threads are working
        if(CLI){
            System.out.println("Starting render.");
            System.out.println("|---------------Estimated-Progress---------------|");
        }
        int CLIBar = 0;
        do {
            try {
                Thread.sleep(100);
                if (listener != null) {
                    listener.onProgressUpdate(getPercentDone()); // Notify progress
                }
                if(CLI){
                    while(loadBarChunks >= 1 && CLIBar < 50){ //make sure the bar doesn't go over 50 units;
                        System.out.print('#');
                        CLIBar++;
                        loadBarChunks -= 1;
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("[!!!] An unexpected exception has occured while waiting on the threads. Send Harsh Noise a copy of this stack trace and tell him it's borked!\n");
                System.out.print(e);
                e.printStackTrace();
            }
        } while (threadsDone != threads);
        
        while(CLIBar < 50){ //Make sure the bar isn't short of 50
            System.out.print('#');
            CLIBar++;
        }
        
        //Number Writer
        if(numberWriter){
            NumberWriter writer = new NumberWriter();
            img = writer.write(img, String.valueOf(shiftX) + ", " + String.valueOf(shiftY) + ", " + String.valueOf(graphSize));
        }
        
        //Output image file 
        File outputfile = new File(fileName + ".png");
        try {
            ImageIO.write(img, "png", outputfile);
            if(CLI){
                System.out.println("\nFile saved to " + outputfile.getAbsolutePath());
            }
        } catch (IOException e) {

        }
    }
    
    //return small thumbnail of the brot given current settings
    public static BufferedImage getThumbnail(int thumbSize, int depth) {
        threadsDone = 0;
        pixDone = 0;
        img = new BufferedImage(thumbSize, thumbSize, BufferedImage.TYPE_INT_RGB);
        //double subImageSize = (double) imageSize / threads;
        double subImageSize = (double) thumbSize / (4*1); //cover 1/4 the img
        
        //Loop for starting threads
        //System.out.println("Starting " + threads + " threads");
        startPoint = (int) (0);
        endPoint = (int) (subImageSize);
        Thread object = new Thread(new MandelbrotGenerator());
        object.start();
        //Small delay such that threads have time to update shared resources 
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            System.out.println("[!!!] An unexpected exception has occured while starting threads. Send Harsh Noise a copy of this stack trace and tell him it's borked!\n");
            System.out.println(e);
            e.printStackTrace();
        }
        
        
        //Loop while threads are working
        int CLIBar = 0;
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("[!!!] An unexpected exception has occured while waiting on the threads. Send Harsh Noise a copy of this stack trace and tell him it's borked!\n");
                System.out.print(e);
                e.printStackTrace();
            }
        } while (threadsDone != threads);
        
        //Number Writer
        if(numberWriter){
            NumberWriter writer = new NumberWriter();
            img = writer.write(img, String.valueOf(shiftX) + ", " + String.valueOf(shiftY) + ", " + String.valueOf(graphSize));
        }
        
        return(img);
    }
    
    
    //static volatile int threadNum = 0;
    @Override
    public void run() {
        int threadStart = startPoint;
        int threadEnd = endPoint;
        //int thisThread = threadNum;
        //threadNum++;
        buildBrot(threadStart, threadEnd);
        buildBrot(threadStart + (imageSize/4), threadEnd + (imageSize/4));
        buildBrot(threadStart + 2*(imageSize/4), threadEnd + 2*(imageSize/4));
        buildBrot(threadStart + 3*(imageSize/4), threadEnd + 3*(imageSize/4));
        threadsDone++;
        //System.out.println("Thread #" + thisThread + " finished");
    }
    
    //Getters and setters used by the GUI in the future
    public void setGraphSize(double x) {
       graphSize = x;
    }
    public double getGraphSize() {
        return (graphSize);
    }
    public void setImageSize(int x) {
       imageSize = x;
    }
    public int getImageSize() {
        return (imageSize);
    }
    public void setShiftX(double x) {
       shiftX = x;
    }
    public double getShiftX() {
        return (shiftX);
    }
    public void setShiftY(double x) {
       shiftY = x;
    }
    public double getShiftY() {
        return (shiftY);
    }
    public void setReductionFactor(double x) {
       reductionFactor = x;
    }
    public double getReductionFactor() {
        return (reductionFactor);
    }
    public void setColorMode(int x) {
       colorMode = x;
    }
    public int getColorMode() {
        return (colorMode);
    }
    public void setMaxDepth(int x) {
       maxDepth = x;
    }
    public int getMaxDepth() {
        return (maxDepth);
    }
    public void setNumberWriter(boolean x) {
       numberWriter = x;
    }
    public boolean getNumberWriter() {
        return (numberWriter);
    }
    public void setThreads(int x) {
       threads = x;
    }
    public int getThreads() {
        return (threads);
    }
    public void setFileName(String x) {
       fileName = x;
    }
    public String getFileName() {
        return (fileName);
    }
    
    public static void printHelp(){
        System.out.println("""
                           Usage:
                           \tMandelbrotGeneratorCLI [mode] [setting] [value] [setting] [value]...
                           \tExample: 'MandelbrotGeneratorCLI Render -S 0.5 -Y 1.0 -N HarshBrot
                           Mode:
                           \tRender\tLoads the default values of the mandelbrot generator, applies you updated settings, and processes a render job
                           \tLoad\tLoads the settings from a mandelbrotgeneratorsettings.dat file, applies your updated settings, and processes a render job
                           \tSave\tLoads the default settings, applies your updated settings, and generates a mandelbrotgeneratorsettings.dat file
                           
                           Setting:
                           \t-S\tThe side length of the graph, used for zooming the image in and out. (Decimal - Default 2.5) 
                           \t-R\tThe resolution of the resulting image, the number of pixels per side. (Integer - Default 1000) 
                           \t-X\tThe X coordinates of the center of the image. (Decimal - Default -0.75) 
                           \t-Y\tThe Y coordinates of the center of the image. (Decimal - Default 0.0) 
                           \t-F\tThe color contrast between different depths of the mandelbrot (Decimal - Default 1.0)
                           \t-D\tThe maximum calculation depth for each pixel. See color modes for limitations. (Integer - Default 255) 
                           \t-T\tThe number of threads used for calculation. (Integer - Default 1) 
                           \t-L\tWhether or not to print the x, y, and zoom at the top left corner. (Boolean - default true)
                           \t-N\tThe name of the exported image file. (String - Default myMandelbrot) 
                           \t-C\tThe mode which the mandelbrot set is colored. See below for details. (Integer - Default 0) 
                           
                           Color modes:
                           \t0\tBlack mandelbrot on a white background. (Max calculation depth - 255)
                           \t1\tCycles though all possible RGB values. (Max calculation depth - 16,581,375)
                           \t2\tRainbow coloring that infinitely cycles through hue. (Max calculation depth - infinite)
                           \t3\t0's coloring inverted. (Max calculation depth - 255)""");
    }
    
    public static boolean setSettingsValues(String[] args){
        int x = 1;
        try{
            for(x = 1; x < args.length; x+=2){
                switch(args[x].toLowerCase()){
                    case "-s":
                        graphSize = Double.parseDouble(args[x+1]);
                        break;
                    case "-r":
                        imageSize = Integer.parseInt(args[x+1]);
                        break;
                    case "-x":
                        shiftX = Double.parseDouble(args[x+1]);
                        break;
                    case "-y":
                        shiftY = Double.parseDouble(args[x+1]);
                        break;
                    case "-c":
                        colorMode = Integer.parseInt(args[x+1]);
                        break;
                    case "-f":
                        reductionFactor = Double.parseDouble(args[x+1]);
                        break;
                    case "-d":
                        maxDepth = Integer.parseInt(args[x+1]);
                        break;
                    case "-t":
                        threads = Integer.parseInt(args[x+1]);
                        break;
                    case "-n":
                        fileName = args[x+1];
                        break;
                    case "-l":
                        numberWriter = Boolean.parseBoolean(args[x+1]);
                        break;
                    default:
                        System.out.println("[???] Setting '" + args[x].toLowerCase() + "' not recognized as a valid setting. Type 'MandelbrotGeneratorCLI help' to see a list of all valid settings. Make sure to type them exactly as seen. Aborting render.");
                        return(false);
                }
            }
            return(true);
        }catch(NumberFormatException e){
            System.out.println("[???] " + args[x+1] + " is not able to be converted to the proper variable type for " + args[x].toLowerCase() + ". Type 'MandelbrotGeneratorCLI help' to see a list of all valid settings and their associated variable type. Aborting render.");
            return(false);
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("[???] Settings-value mismatch. Make sure there is exactly one value for every setting. Type 'MandelbrotGeneratorCLI help' to see a list of all valid settings and their associated variable type. Aborting render.");
            return(false);
        }
    }
                           
    public static void main(String[] args){
        try{
            //args = new String[]{"render", "-t", "16", "-r", "10000"}; //temp
            CLI = true;
            System.out.println("Mandelbrot Generator - A project by Harsh Noise.\nA tool for rendering images of the mandelbrot set on your device.\n");
            if(args.length == 0){
                System.out.println("[???] No arguments detected. At least one argument is required. Type 'MandelbrotGeneratorGUI help' to see a list of all options. Aborting render.");
            }else{
                switch(args[0].toLowerCase()){
                    case "help":
                        printHelp();
                        break;
                    case "render":
                        if(setSettingsValues(args)){
                            render();
                        }
                        break;
                    case "load":
                        loadSettings();
                        if(setSettingsValues(args)){
                            render();
                        }
                        break;
                    case "save":
                        if(setSettingsValues(args)){
                            saveSettings();
                        }
                        break;
                    default:
                        System.out.println("[???] Mode '" + args[0].toLowerCase() + "' not recognized as a valid mode. Type 'MandelbrotGeneratorCLI help' to see a list of all valid modes. Make sure to type them exactly as seen. Aborting render.");
                }
            }
        }catch(Exception e){
            System.out.println("[!!!] An unexpected exception has occured. Send Harsh Noise a copy of this stack trace and tell him it's borked!");
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
