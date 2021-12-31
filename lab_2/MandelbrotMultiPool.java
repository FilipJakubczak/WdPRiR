import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class MandelbrotMultiPool implements Callable<Boolean> {

    static int N = 4096;
    final static int max_iter = 100;

    static int[][] set = new int[N][N];
    static int[] sizes = new int[] { 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192 };

    public static int cores = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws Exception {
        PrintWriter outputFile = new PrintWriter("times_multi_pool.dat");
        for (int size : sizes) {
            MandelbrotMultiPool.N = size;
            MandelbrotMultiPool.set = new int[size][size];
            long[] execTime = new long[20];
            ArrayList<MandelbrotMultiPool> threads = new ArrayList<>();
            for (int j = 0; j < cores; j++) {
                threads.add(new MandelbrotMultiPool(j));
            }
            
            for(int i = 0; i < 20; i++) {
                ExecutorService executorService = Executors.newFixedThreadPool(cores);
                long startTime = System.currentTimeMillis();
                
                List<Future<Boolean>> futures = executorService.invokeAll(threads);
                executorService.shutdown();

                long endTime = System.currentTimeMillis();
                execTime[i] = endTime - startTime;
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

    public MandelbrotMultiPool(int chunk) {
        this.chunk = chunk;
    }

    @Override
    public Boolean call() {

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
        return true;
    }

}
