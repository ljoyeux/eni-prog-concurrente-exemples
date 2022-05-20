package fr.eni.concurrent.examples.gui;

/**
 * Created by ljoyeux on 12/05/2017.
 */
public class Mandelbrot {

    public static int mandelbrot(final double x, final double y, int numIter, final double limit) {
        double x0, x1, y0, y1;
        double a;

        x1 = y1 = 0;
        do {
            double xSq = x1 * x1;
            double ySq = y1 * y1;
            a = xSq + ySq;
            x0 = xSq - ySq + x;
            y0 = 2 * x1 * y1 + y;
            x1 = x0;
            y1 = y0;
        } while (a < limit && --numIter > 0);
        return numIter;
    }

    public static int[] mandelbrot(double x, double y, final double deltaX, final double deltaY, final int numIter, final double limit, final int[] results) {
        if (results == null) {
            return null;
        }

        for (int i = 0; i < results.length; i++) {
            results[i] = mandelbrot(x, y, numIter, limit);
            x += deltaX;
            y += deltaY;
        }

        return results;
    }

    public static class MandelbrotLine<U> {

        private final double x;
        private final double y;
        private final double deltaX;
        private final double deltaY;
        private final U ref;

        public MandelbrotLine(double x, double y, double deltaX, double deltaY, U ref) {
            this.x = x;
            this.y = y;
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.ref = ref;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getDeltaX() {
            return deltaX;
        }

        public double getDeltaY() {
            return deltaY;
        }

        public U getRef() {
            return ref;
        }
    }
}
