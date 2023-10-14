/******************************************************************************
 *  Compilation:  javac Mandelbrot.java
 *  Execution:    java Mandelbrot xc yc size
 *  Dependencies: StdDraw.java
 *
 *  Plots the size-by-size region of the Mandelbrot set, centered on (xc, yc)
 *
 *  % java Mandelbrot -0.5 0 2
 *
 ******************************************************************************/

import java.awt.Color;

public class ParallelMandelbrot extends Thread {

    //declare global variables
    private static int procs = 8; // # of processes
    private static double xc;
    private static double yc;
    private static double size;
    private static int n   = 1024;   // create n-by-n image
    private static int max = 255;   // maximum number of iterations

    private static Picture picture = new Picture(n, n);
    private int start, width;

    @Override
    public void run(){
        for (int i = 0; i < n; i++) {
            for (int j = start; j < start+width; j++) {
                double x0 = xc - size/2 + size*i/n;
                double y0 = yc - size/2 + size*j/n;
                Complex z0 = new Complex(x0, y0);
                int gray = max - mand(z0, max);
                Color color = new Color(200, gray, gray);
                picture.set(i, n-1-j, color);
            }
        }
    }

    ParallelMandelbrot (int start, int width) {
        this.start = start;
        this.width = width;
    }

    // return number of iterations to check if c = a + ib is in Mandelbrot set
    public static int mand(Complex z0, int max) {
        Complex z = z0;
        for (int t = 0; t < max; t++) {
            if (z.abs() > 2.0) return t;
            z = z.times(z).plus(z0);
        }
        return max;
    }

    public static void main(String[] args) throws InterruptedException {
        xc   = Double.parseDouble(args[0]);
        yc   = Double.parseDouble(args[1]);
        size = Double.parseDouble(args[2]);

        n   = 1024;
        max = 255;

        picture = new Picture(n, n);
        Stopwatch s = new Stopwatch();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double x0 = xc - size/2 + size*i/n;
                double y0 = yc - size/2 + size*j/n;
                Complex z0 = new Complex(x0, y0);
                int gray = max - mand(z0, max);
                Color color = new Color(gray, gray, gray);
                picture.set(i, n-1-j, color);
            }
        }
        double seq = s.elapsedTime();
        StdOut.println("Sequential time: " + seq);
        picture.show();

        picture = new Picture(n, n);
        s = new Stopwatch();
        //use Java thread class to parallelize process into 4
        ParallelMandelbrot[] Thread = new ParallelMandelbrot[procs];
        for (int i = 0; i < procs; ++i) Thread[i] = new ParallelMandelbrot(i*n/procs, n/procs);
        for (int i = 0; i < procs; ++i) Thread[i].start();
        for (int i = 0; i < procs; ++i) Thread[i].join();
        double par = s.elapsedTime();
        StdOut.println("Parallel time: " + par);
        picture.show();
        StdOut.println("Speedup = " + seq/par);
    }
}