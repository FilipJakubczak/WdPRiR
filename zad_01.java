import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.io.PrintWriter;

public class zad_01 {

  // Check, whether point fall within Mandelbrot set.
  public static boolean check(int maxIter, double xval, double yval) {
    double x = 0, y = 0;

    for (int i = 0; i < maxIter; i++) {
      double prevX = x, prevY = y;
      x = Math.pow(prevX, 2) - Math.pow(prevY, 2) + xval;
      y = 2 * prevX * prevY + yval;

      if (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) >= 2) {
        return false;
      }

    }
    return true;
  }

  // Generate Mandelbrot set image. 
  public static BufferedImage mandelbrot(int width, int height, double xmin, double xmax, double ymin, double ymax,
      int N) {
    byte[] bw = { (byte) 0xFF, (byte) 0x00 };
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY,
        new IndexColorModel(1, 2, bw, bw, bw));
    int black = Color.BLACK.getRGB();
    int white = Color.WHITE.getRGB();

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        double xval = xmin + x * (xmax - xmin) / (width - 1);
        double yval = ymin + y * (ymax - ymin) / (height - 1);
        image.setRGB(x, y, check(N, xval, yval) ? black : white);
      }
    }
    return image;
  }

  // Overload with default values. 
  public static BufferedImage mandelbrot(int width, int height) {
    return mandelbrot(width, height, -2.1, 0.6, -1.2, 1.2, 200);
  }

  // Calculate execution time.
  public static long getExecutionTime(int iterations, int width, int height, TimeUnit unit) {
    long[] execTime = new long[iterations];
    for (int i = 0; i < iterations; i++) {
      long start = System.nanoTime();
      mandelbrot(width, height);
      long end = System.nanoTime();
      execTime[i] = end - start;
    }
    return unit.convert((long) Arrays.stream(execTime).average().orElse(Double.NaN), unit);
  }

  // Save image of the Mandelbrot set to a .png file.
  public static void saveImage(BufferedImage image) throws IOException {
    File outputFile = new File("mandelbrot.png");
    ImageIO.write(image, "png", outputFile);
  }

  // Export execution time to a .dat file. 
  public static void exportTime(String fileName) throws IOException {
        PrintWriter outputFile = new PrintWriter(fileName);
        for(int size = 32; size <= Math.pow(2, 13); size *= 2) {
           outputFile.println(size + "; " + getExecutionTime(200, size, size, TimeUnit.MICROSECONDS));
           outputFile.flush();
        }
        outputFile.close();
    }

  public static void main(String[] args) throws IOException {
    int sizeX = 10000;
    int sizeY = 10000;
    saveImage(mandelbrot(sizeX, sizeY));
    // exportTime("times.dat");
  }
}
