// Source: https://www.geeksforgeeks.org/merge-sort-using-multi-threading/

import java.lang.System;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.io.PrintWriter;

class MergeSort {
    private static final int N_CORES =  Runtime.getRuntime().availableProcessors();
    public static long time;

    private static class SortThreads extends Thread {
        SortThreads(Integer[] array, int begin, int end) {
            super(()->{
                MergeSort.mergeSort(array, begin, end);
            });
            this.start();
        }
    }

    public static void threadedSort(Integer[] array) {
        long time = System.currentTimeMillis();
        final int length = array.length;
        boolean exact = length % N_CORES == 0;
        int maxlim = exact ? length/N_CORES : length/(N_CORES - 1);
        maxlim = maxlim < N_CORES ? N_CORES : maxlim;
        final ArrayList<SortThreads> threads = new ArrayList<>(); 

        for(int i = 0; i < length; i += maxlim) {
            int beg = i;
            int remain = length - i;
            int end = remain < maxlim ? i + remain - 1 : i + maxlim - 1;
            final SortThreads t = new SortThreads(array, beg, end);
            threads.add(t);
        }

        threads.forEach((t) -> {
            try {
                t.join();
            } catch (InterruptedException e) {}
        });

        for(int i=0; i < length; i+=maxlim){
            int mid = i == 0 ? 0 : i - 1;
            int remain = length - i;
            int end = remain < maxlim ? i + remain - 1 : i + maxlim - 1;
            merge(array, 0, mid, end);
        }
        MergeSort.time = System.currentTimeMillis() - time;
    }

    public static void mergeSort(Integer[] array, int begin, int end) {
        if (begin < end) {
            int mid = (begin + end)/2;
            mergeSort(array, begin, mid);
            mergeSort(array, mid + 1, end);
            merge(array, begin, mid, end);
        }
    }

    public static void merge(Integer[] array, int begin, int mid, int end) {
        Integer[] temp = new Integer[(end - begin) + 1];
        int i = begin, j = mid + 1;
        int k = 0;

        while (i <= mid && j <= end) {
            if (array[i] <= array[j]) {
                temp[k] = array[i];
                i += 1;
            } else {
                temp[k] = array[j];
                j += 1;
            }
            k += 1;
        }

        while (i <= mid) {
            temp[k] = array[i];
            i += 1; k += 1;
        }

        while(j <= end) {
            temp[k] = array[j];
            j += 1; k += 1;
        }

        for (i = begin, k = 0; i <= end; i++, k++) {
            array[i] = temp[k];
        }

    }

    public static void main(String[] args) throws Exception {
        Random random = new Random();

        Integer[] size = new Integer[] {10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000, 2000000, 5000000, 10000000};
        long[] execTimeMulti = new long[20];
        long[] execTime = new long[20];
        PrintWriter outputFile = new PrintWriter("times.dat");

        for(Integer s : size) {
            Integer[] array = new Integer[s];
            for (int i = 0; i < array.length; i++) {
                array[i] = random.nextInt();
            }
            for (int i = 0; i < 20; i++) {
                Integer[] testArray = Arrays.copyOf(array, array.length);
                Integer[] testArray2 = Arrays.copyOf(array, array.length);
                MergeSort.threadedSort(testArray);
                execTimeMulti[i] = MergeSort.time;
                long ti = System.currentTimeMillis();
                MergeSort.mergeSort(testArray2, 0, testArray2.length-1);
                execTime[i] = System.currentTimeMillis() - ti;
            }
            long tMulti = (long) Arrays.stream(execTimeMulti).average().orElse(Double.NaN);
            long t = (long) Arrays.stream(execTime).average().orElse(Double.NaN);
            System.out.println("Size: " + s + " Time: " + t + " ms" + " Time (multi): " + tMulti + " ms");
            outputFile.println(size + "; " + t + "; " + tMulti);
            outputFile.flush();
        }
        outputFile.close();
    }
}