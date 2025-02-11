package harsh.mandelbrotgenerator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;
import javax.imageio.ImageIO;

interface ProgressListener {

    void onProgressUpdate(int progress); // Interface to send progress updates
}

public class MandelbrotGenerator implements Runnable {
    //EDITABLE SETTINGS

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

    //UNEDITABLE
    //Subgraph and subimage size for each thread
    static double subGraphSize;
    static int subImageSize;

    //Counters for threads
    static int imageNum = 0;
    static volatile int finished = 0;

    //Centerpoint of each sub-image
    static double calcShiftX;
    static double calcShiftY;

    //x and y offsets for the final image
    static int xOffset = 0;
    static int yOffset = 0;

    //shared buffered image
    static volatile BufferedImage img;

    static int startPoint = 0;
    static int endPoint = 0;
    static volatile int threadsDone = 0;
    static volatile int pixDone = 0;
    static boolean CLI = false;
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
            if (Math.sqrt(Math.pow(z.re(), 2) + Math.pow(z.im(), 2)) > 2.2) {
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
                System.err.println("Error reading settings file: " + e.getMessage());
            }
        } else {
            // File does not exist, so initialize with default data
            System.out.println("Settings file not found. Initialized with default values.");
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
            System.err.println("Error writing settings file: " + e.getMessage());
        }
    }

    public void render() {
        threadsDone = 0;
        pixDone = 0;
        img = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        subGraphSize = graphSize / threads;
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

    public static void main(String[] args) throws IOException {
        CLI = true;
        Scanner scan = new Scanner(System.in);
        DataInputStream dis;
        DataOutputStream dos;
        String input = "";
        Boolean inMainMenu = true;

        loadSettings();

        System.out.println("Mandelbrot Generator - by Harsh Noise");
        while (inMainMenu) {

            System.out.println("Would you like to [R]ender, visit [S]ettings, or [E]xit?");
            input = scan.nextLine();
            switch (input) {
                case "R":
                case "r":
                    System.out.println("Starting render.");
                    System.out.println("|------------------------------------------------|");
                    threadsDone = 0;
                    pixDone = 0;
                    img = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
                    subGraphSize = graphSize / threads;
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
                            System.out.println(pixDone / ((double) imageSize * imageSize));
                            Thread.sleep(1000);
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
                    break;
                case "S":
                case "s":

                    System.out.println("Current settings:");
                    System.out.println("[S] Side length of the graph (units): " + graphSize + "\n"
                            + "[R] Resolution of the image (pixels): " + imageSize + "\n"
                            + "[X] Image centered on XCoord: " + shiftX + "\n"
                            + "[Y] Image centered on YCoord: " + shiftY + "\n"
                            + "[C] Color mode (B&W = 0, Full = 1, ROYGBIV = 2): " + colorMode + "\n"
                            + "[F] Color contrast: " + reductionFactor + "\n"
                            + "[D] Max calculation depth (-1 for color status's default): " + maxDepth + "\n"
                            + "[T] Number of threads in use: " + threads + "\n"
                            + "[N] Export file name: " + fileName + "\n"
                            + "[U] Reprint settings table\n"
                            + "[E] Exit");
                    boolean inSettingsMenu = true;
                    while (inSettingsMenu) {
                        System.out.println("Type the setting code you wish to edit.");
                        input = scan.nextLine();
                        switch (input) {
                            case "S":
                            case "s":
                                try {
                                    System.out.println("Enter a new value");
                                    graphSize = scan.nextDouble();
                                    scan.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("[???] Incorect data type for " + ((Object) graphSize).getClass().getName());
                                    scan.nextLine();
                                }
                                break;
                            case "R":
                            case "r":
                                try {
                                    System.out.println("Enter a new value");
                                    imageSize = scan.nextInt();
                                    scan.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("[???] Incorect data type for " + ((Object) imageSize).getClass().getName());
                                    scan.nextLine();
                                }
                                break;
                            case "X":
                            case "x":
                                try {
                                    System.out.println("Enter a new value");
                                    shiftX = scan.nextDouble();
                                    scan.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("[???] Incorect data type for " + ((Object) shiftX).getClass().getName());
                                    scan.nextLine();
                                }
                                break;
                            case "Y":
                            case "y":
                                try {
                                    System.out.println("Enter a new value");
                                    shiftY = scan.nextDouble();
                                    scan.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("[???] Incorect data type for " + ((Object) shiftY).getClass().getName());
                                    scan.nextLine();
                                }
                                break;
                            case "C":
                            case "c":
                                try {
                                    System.out.println("Enter a new value");
                                    colorMode = scan.nextInt();
                                    if (colorMode == 0 && maxDepth > 256) {
                                        maxDepth = 256;
                                        System.out.println("[!!!] Max depth is set too high for this color mode, changed to 256.");
                                    }
                                    scan.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("[???] Incorect data type for " + ((Object) colorMode).getClass().getName());
                                    scan.nextLine();
                                }
                                break;
                            case "F":
                            case "f":
                                try {
                                    System.out.println("Enter a new value");
                                    reductionFactor = scan.nextDouble();
                                    scan.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("[???] Incorect data type for " + ((Object) reductionFactor).getClass().getName());
                                    scan.nextLine();
                                }
                                break;
                            case "D":
                            case "d":
                                try {
                                    System.out.println("Enter a new value");
                                    maxDepth = scan.nextInt();

                                    if (maxDepth == -1) {
                                        switch (colorMode) {
                                            case 0:
                                                maxDepth = 256;
                                                System.out.println("Depth defaulted to 256");
                                                break;
                                            case 1:
                                                maxDepth = (int) Math.pow(256, 3);
                                                System.out.println("Depth defaulted to " + (int) Math.pow(256, 3));
                                                System.out.println("[!!!] Warning: this will run through all possible 8 bit colors and may take a long time. It is advised you set a smaller depth limit.");
                                                break;
                                            case 2:
                                                maxDepth = (int) Math.pow(256, 3);
                                                System.out.println("Depth defaulted to " + (int) Math.pow(256, 3));
                                                System.out.println("ROYGBIV mode will infinitely loop through the rainbow, and has no maximum depth.");
                                                break;
                                        }
                                    }
                                    scan.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("[???] Incorect data type for " + ((Object) maxDepth).getClass().getName());
                                    scan.nextLine();
                                }
                                break;
                            case "T":
                            case "t":
                                try {
                                    System.out.println("Enter a new value");
                                    threads = scan.nextInt();
                                    scan.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("[???] Incorect data type for " + ((Object) threads).getClass().getName());
                                    scan.nextLine();
                                }
                                break;
                            case "N":
                            case "n":
                                try {
                                    System.out.println("Enter a new value");
                                    fileName = scan.nextLine();
                                } catch (InputMismatchException e) {
                                    System.out.println("[???] Incorect data type for " + ((Object) fileName).getClass().getName());
                                }
                                break;
                            case "U":
                            case "u":
                                System.out.println("Current settings:");
                                System.out.println("[S] Side length of the graph (units): " + graphSize + "\n"
                                        + "[R] Resolution of the image (pixels): " + imageSize + "\n"
                                        + "[X] Image centered on XCoord: " + shiftX + "\n"
                                        + "[Y] Image centered on YCoord: " + shiftY + "\n"
                                        + "[C] Color mode (B&W = 0, Full = 1, ROYGBIV = 2): " + colorMode + "\n"
                                        + "[F] Color contrast: " + reductionFactor + "\n"
                                        + "[D] Max calculation depth (-1 for color status's default): " + maxDepth + "\n"
                                        + "[T] Number of threads in use: " + threads + "\n"
                                        + "[N] Export file name: " + fileName + "\n"
                                        + "[U] Reprint settings table\n"
                                        + "[E] Exit");
                                break;
                            case "E":
                            case "e":
                                inSettingsMenu = false;
                                break;
                            default:
                                System.out.println("You entered '" + input + "', accepted values are 'S', 'R', 'X', 'Y', 'C', 'F', 'T', 'N', 'U', or 'E'");
                                break;
                        }
                    }
                    saveSettings();
                    break;
                case "E":
                case "e":
                    inMainMenu = false;
                    break;
                default:
                    System.out.println("You entered '" + input + "', accepted values are 'R', 'S', or 'E'");
                    break;

            }
        }
        scan.close();
    }
}
