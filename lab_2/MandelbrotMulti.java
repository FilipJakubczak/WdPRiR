import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.io.File;
import java.util.Arrays;
import java.io.PrintWriter;

public class MandelbrotMulti extends Thread {

    static int N = 4096;
    final static int max_iter = 100;

    static int[][] set = new int[N][N];
    static int[] sizes = new int[] { 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192 };

    public static int cores = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws Exception {
        PrintWriter outputFile = new PrintWriter("times_multi.dat");
        for (int size : sizes) {
            MandelbrotMulti.N = size;
            MandelbrotMulti.set = new int[size][size];
            long[] execTime = new long[20];
            for(int j=0; j < 20; j++) {
                long startTime = System.currentTimeMillis();
                ArrayList<MandelbrotMulti> threads = new ArrayList<>();
                for (int i = 0; i < cores; i++) {
                    threads.add(new MandelbrotMulti(i));
                }

                threads.forEach((t) -> t.start());
                threads.forEach((t) -> {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                long endTime = System.currentTimeMillis();

                execTime[j] = endTime - startTime;
            }
            
            long time = (long) Arrays.stream(execTime).average().orElse(Double.NaN);
            outputFile.println(size + "; " + time);
            outputFile.flush();

            BufferedImage img = new BufferedImage(N, N,
                    BufferedImage.TYPE_INT_ARGB);

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {

                    int k = set[i][j];

                    float level;
                    if (k < max_iter) {
                        level = (float) k / max_iter;
                    } else {
                        level = 0;
                    }
                    Color c = new Color(level, 0, 0);
                    img.setRGB(i, j, c.getRGB());
                }
            }
            ImageIO.write(img, "PNG", new File(String.format("Mandelbrot_%d.png", size)));
            System.out.println("Size: " + size + " time: " + time + " ms");
        }
        
        outputFile.close();

    }

    int chunk;

    public MandelbrotMulti(int chunk) {
        this.chunk = chunk;
    }

    public void run() {

        int begin = (N / cores) * chunk, end = (N / cores) * (chunk + 1);

        for (int i = begin; i < end; i++) {
            for (int j = 0; j < N; j++) {

                double cr = ((float) cores * i - (float) cores / 2 * N) / N;
                double ci = ((float) cores * j - (float) cores / 2 * N) / N;

                double zr = cr, zi = ci;

                int k = 0;
                while (k < max_iter && zr * zr + zi * zi < (float) cores) {

                    double newr = cr + zr * zr - zi * zi;
                    double newi = ci + (float) cores / 2 * zr * zi;

                    zr = newr;
                    zi = newi;

                    k++;
                }

                set[i][j] = k;
            }
        }
    }

}
