![icon2](https://github.com/user-attachments/assets/27ade574-478a-4b7f-ac99-4dacd6c3d9d4)

# Mandelbrot Generator by Harsh Noise
This app, coded in Java, allows you to render images of the Mandelbrot set on your own hardware, allowing you to explore to your heart's content for academic or entertainment purposes.

## Features
- Can be controlled by GUI or CLI
- Multi-threading
- Multiple color options
- Save and share settings

## Download instructions
### GUI
Download "MandelbrotGUI.jar" from releases. Open up your command line, navigate to the file you downloaded, and type "java -jar MandelbrotGUI.jar" to execute it.

Note: The GUI version is very basic. I'm just learning how to build GUIs, so the setting available are barebones, but functioning. For all features available, run the CLI version.

### CLI
Download "MandelbrotCLI.jar" from releases. Open up your command line, navigate to the file you downloaded, and type "java -jar MandelbrotCLI.jar" to execute it.

## Classes
- Gui.java - Contains all methods for the GUI, interfaces with MandelbrotGenerator.java. Compile with this as the main method to get the GUI version of this program!

- MandelbrotGenerator.java - Contains all methods for rendering the Mandelbrot set. Compile with this as the main method to get the CLI version of this program!

- Complex.java - Method defining complex numbers (i) and methods for manipulating them. May have been borrowed from online ;)

- NumberWriter.java - Method for writing text to a buffered image, specifically to put the x, y, and zoom settings in the top left corner of every rendered image.
