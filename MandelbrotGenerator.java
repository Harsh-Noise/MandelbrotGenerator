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

    //Color settings b&w = 0, full = 1, roygbiv = 2
    static double reductionFactor = 1;
    static int colorMode = 0;
    static int maxDepth = 256;

    //Images per side (Square root of the number of threads started)
    static int threads = 4;

    //fileName
    static String fileName = "myMandelbrot";

    //Variables edited by the algorithm
    //Subimage size for each thread
    static int startPoint = 0;
    static int endPoint = 0;

    //Counters for threads
    static volatile int threadsDone = 0;
    static volatile int pixDone = 0;

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

    public static Complex iterate(Complex z, Complex c) {
        //This method controls the formula used. This one is specific to the mandelbrot set.
        return (c.plus(z.times(z)));
    }

    public static Color getColor(Complex c) {
        //Takes a complex number, and converts the 'getStability' function into a Color object depending on the colormode
        int color;
        switch (colorMode) {
            case 0:
                color = getStability(c, maxDepth);
                if (color >= 256) {
                    color = 255;
                }
                color = 255 - color;
                return (new Color(color, color, color));
            case 1:
                color = getStability(c, maxDepth);

                return (new Color(color));
            case 2:
                color = getStability(c, maxDepth);
                return (new Color(Color.getHSBColor(((float) color / 256), (float) 1, (float) 1).getRGB()));
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
    
    public static int getP(){
        return((int)(((double)pixDone/(imageSize*imageSize))*100));
    }

    public static void buildBrot(int startPix, int endPix) {
        //System.out.println("Building " + startPix + ", " + endPix);

        //loop for every pixel in the img
        for (int x = startPix; x < endPix; x++) {
            //loading bar
            if(CLI){
                if(x%(imageSize/50) == 0) {
                    System.out.print('#');
                }
            }
            
            for (int y = 0; y < imageSize; y++) {
                //get complex number represented by the pixel on the img
                Complex c = new Complex(pxToNumX(x, graphSize, imageSize, shiftX), pxToNumY(y, graphSize, imageSize, shiftY));

                //test if it explodes off to infinity, and use returned color to paint the pixel
                img.setRGB(x, y, getColor(c).getRGB());
                
                pixDone += 1;
            }
        }
    }

    public static void loadSettings() {
        File file = new File(settingsFile);
        if (file.exists()) {
            try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
                graphSize = dis.readDouble();
                imageSize = dis.readInt();
                shiftX = dis.readDouble();
                shiftY = dis.readDouble();
                reductionFactor = dis.readDouble();
                colorMode = dis.readInt();
                maxDepth = dis.readInt();
                threads = dis.readInt();
                fileName = dis.readUTF();
                System.out.println("Settings loaded.");
            } catch (IOException e) {
                System.err.println("[!!!] Error reading settings file: " + e.getMessage());
            }
        } else {
            // File does not exist, so initialize with default data
            System.out.println("[???] Settings file not found. Initialized with default values.");
            saveSettings();
        }
    }

    public static void saveSettings() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(settingsFile))) {
            dos.writeDouble(graphSize);
            dos.writeInt(imageSize);
            dos.writeDouble(shiftX);
            dos.writeDouble(shiftY);
            dos.writeDouble(reductionFactor);
            dos.writeInt(colorMode);
            dos.writeInt(maxDepth);
            dos.writeInt(threads);
            dos.writeUTF(fileName);
            System.out.println("Settings saved.");
        } catch (IOException e) {
            System.err.println("[!!!] Error writing settings file: " + e.getMessage());
        }
    }

    public static void render() {
        //System.out.println("Rendering");
        threadsDone = 0;
        pixDone = 0;
        img = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        double subImageSize = (double) imageSize / threads;
        for (int x = 0; x < threads; x++) {
            startPoint = (int) (x * subImageSize);
            endPoint = (int) ((x + 1) * subImageSize);
            if (imageSize - endPoint < subImageSize) {
                endPoint = imageSize;
            }
            //System.out.println(startPoint + ", " + endPoint);

            Thread object = new Thread(new MandelbrotGenerator());
            object.start();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        //System.out.println("Waiting for threads to finish");
        while (threadsDone != threads) {

            try {
                //System.out.println(threadsDone);
                //System.out.println(pixDone / ((double) imageSize * imageSize));

                if (listener != null) {
                    listener.onProgressUpdate(getP()); // Notify progress
                }

                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        //System.out.println("Threads finished");
        //Number Writer
        NumberWriter writer = new NumberWriter();
        img = writer.write(img, String.valueOf(shiftX) + ", " + String.valueOf(shiftY) + ", " + String.valueOf(graphSize));

        //Output file 
        File outputfile = new File(fileName + ".png");
        try {
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {

        }
        System.out.println("\nFile saved to " + outputfile.getAbsolutePath());
    }

    @Override
    public void run() {
        buildBrot(startPoint, endPoint);
        threadsDone++;
    }

    public void setgraphSize(double x) {
        graphSize = x;
    }

    public double getgraphSize() {
        return (graphSize);
    }

    public void setimageSize(int x) {
        imageSize = x;
    }

    public int getimageSize() {
        return (imageSize);
    }

    public void setshiftX(double x) {
        shiftX = x;
    }

    public double getshiftX() {
        return (shiftX);
    }

    public void setshiftY(double x) {
        shiftY = x;
    }

    public double getshiftY() {
        return (shiftY);
    }

    public void setreductionFactor(double x) {
        reductionFactor = x;
    }

    public double getreductionFactor() {
        return (reductionFactor);
    }

    public void setcolorMode(int x) {
        colorMode = x;
    }

    public int getcolorMode() {
        return (colorMode);
    }

    public void setmaxDepth(int x) {
        maxDepth = x;
    }

    public int getmaxDepth() {
        return (maxDepth);
    }

    public void setthreads(int x) {
        threads = x;
    }

    public int getthreads() {
        return (threads);
    }

    public void setfileName(String x) {
        fileName = x;
    }

    public String getfileName() {
        return (fileName);
    }

    public int getPixDone() {
        return (pixDone);
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
                           \t-C\tThe mode which the mandelbrot set is colored. Black & white = 0, All colors = 1, Rainbow = 2. (Integer - Default 0) 
                           \t-F\tThe color contrast between different depths of the mandelbrot (Decimal - Default 1.0)
                           \t-D\tThe maximum calculation depth.(Integer - Default varies by color mode. Set to -1 for the color mode's default.) 
                           \t-T\tThe number of threads used for calculation. (Integer - Default 4) 
                           \t-N\tThe name of the exported image file. (String - Default myMandelbrot) """);
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
                           
    public static void main(String[] args) throws IOException {
        try{
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
                            System.out.println("Starting render.");
                            System.out.println("|------------------------------------------------|");
                            render();
                        }
                        break;
                    case "load":
                        loadSettings();
                        if(setSettingsValues(args)){
                            System.out.println("Starting render.");
                            System.out.println("|------------------------------------------------|");
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
            System.out.print("[!!!] An unexpected exception has occured. Send Harsh Noise a copy of this stack trace and tell him it's borked!\n");
            System.out.print(e);
            e.printStackTrace();
        }
    }
}
