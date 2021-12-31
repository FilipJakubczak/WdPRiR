import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;


public class ScraperThreaded {
    String url;
    String target;
    Document doc;
    Elements images;
    Boolean blur;

    public ScraperThreaded(String url, String target) throws IOException {
        this.url = url;
        doc = Jsoup.connect(this.url).get();
        this.images = doc.select("a[href$=.png]");
        this.target = target;
    }

    public static BufferedImage blur(BufferedImage image, int[] filter, int filterWidth) {
        if (filter.length % filterWidth != 0) {
            throw new IllegalArgumentException("Niekompletny rząd pikseli.");
        }
    
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int sum = IntStream.of(filter).sum();
    
        int[] input = image.getRGB(0, 0, width, height, null, 0, width);
    
        int[] output = new int[input.length];
    
        final int pixelIndexOffset = width - filterWidth;
        final int centerOffsetX = filterWidth / 2;
        final int centerOffsetY = filter.length / filterWidth / 2;
    
        for (int h = height - filter.length / filterWidth + 1, w = width - filterWidth + 1, y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int r = 0;
                int g = 0;
                int b = 0;
                for (int filterIndex = 0, pixelIndex = y * width + x;
                        filterIndex < filter.length;
                        pixelIndex += pixelIndexOffset) {
                    for (int fx = 0; fx < filterWidth; fx++, pixelIndex++, filterIndex++) {
                        int col = input[pixelIndex];
                        int factor = filter[filterIndex];
                        r += ((col >>> 16) & 0xFF) * factor;
                        g += ((col >>> 8) & 0xFF) * factor;
                        b += (col & 0xFF) * factor;
                    }
                }
                r /= sum;
                g /= sum;
                b /= sum;
                output[x + centerOffsetX + (y + centerOffsetY) * width] = (r << 16) | (g << 8) | b | 0xFF000000;
            }
        }
    
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        result.setRGB(0, 0, width, height, output, 0, width);
        return result;
    }

    public static class ScraperThreadedCall implements Callable<Boolean> {
        String url;
        String target;
        String img;
        Boolean blur;

        public ScraperThreadedCall(String url, String target, String img, Boolean blur) {
            this.url = url;
            this.target = target;
            this.img = img;
            this.blur = blur;
        }

        @Override
        public Boolean call() {
            BufferedImage image;
            try{
                URL url =new URL(this.url + this.img);
                image = ImageIO.read(url);
                if (!blur) ImageIO.write(image, "png",new File(this.target+this.img));
                else {
                    int[] filter = new int[] {1, 2, 1, 2, 4, 2, 1, 2, 1};
                    BufferedImage imgBlured = blur(image, filter, 3);
                    ImageIO.write(imgBlured, "png", new File(this.target+this.img));
                }
                return true;
            } catch(IOException e){
                e.printStackTrace();
                return false;
            }
        }
    } 


    public void download() throws InterruptedException {
        ArrayList<ScraperThreaded.ScraperThreadedCall> threads = new ArrayList<>();
        for (Element img : this.images) {
            threads.add(new ScraperThreaded.ScraperThreadedCall(this.url, this.target, img.attr("href"), this.blur));
        }
        ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Boolean>> futures = ex.invokeAll(threads);
        ex.shutdown();
    }

    public static Double time(Object object, Method method, int n, Boolean blur) throws InvocationTargetException, IllegalAccessException {
        double total = 0.0;
        for(int i = 0; i < n; i++){
            long startTime = System.currentTimeMillis();
            method.invoke(object, blur);
            long endTime = System.currentTimeMillis();
            total += (endTime - startTime);
        }
    return total/n;
    }


    public static void main(String[] args) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Scraper scraper = new Scraper("http://www.if.pw.edu.pl/~mrow/dyd/wdprir/",
                "img/");

        Double timeBlur;
        Double time;
        Method download = Scraper.class.getMethod("download", Boolean.class);
        time = time(scraper, download, 10, false);
        timeBlur = time(scraper, download, 10, true);
        PrintWriter pw = new PrintWriter(new FileOutputStream("times_thread.dat"));
        pw.println(time.toString());
        pw.close();
        PrintWriter pw_blur = new PrintWriter(new FileOutputStream("times_thread_blur.dat"));
        pw_blur.println(timeBlur.toString());
        pw_blur.close();
    }
}