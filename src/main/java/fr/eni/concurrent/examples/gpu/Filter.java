package fr.eni.concurrent.examples.gpu;

/**
 *
 * @author ljoyeux
 */
public class Filter {
	/**
	 * Create gaussian blur filter
	 * @param sigma
	 * @return
	 */
    public static double[] gauss(final double sigma) {
        int size = (int) Math.ceil(2 * sigma * 3) | 1;

        double[] f = new double[size];
        int center = size / 2;
        double sum = 0;

        // compute gaussian filter (low pass filter)
        for (int i = 0; i < size; i++) {
            double x = i - center;
            double v = Math.exp(-x * x / (2 * sigma * sigma));
            sum += v;
            f[i] = v;
        }

        // normalize the filter
        double inv = 1 / sum;
        for (int i = 0; i < size; i++) {
            f[i] *= inv;
        }

        return f;
    }
    
    /**
     * Filter input data. Data may be sparse (step!=1). 
     * 
     * An information is access by its index : i*step+origin
     * 
     * @param src
     * @param dst
     * @param width
     * @param filter
     * @param origin
     * 	Origin is the offset
     * @param step
     */
    public static void filter(final float[] src, final float[] dst, 
                              final int width, final double[] filter, final int origin, final int step) {
        final int half = filter.length / 2;
        final boolean repeat = true;
        int i, j;
        
        int v = origin;
        int u = origin;

        // left
        for (i = 0, j = half; j > 0; i++, j--) {
            double sum = 0;

            if (repeat) { // always true. For missing pixels, we use the pixel on the left of the image
                for (int l = 0; l < j; l++) {
                    double f = filter[l];
                    sum += src[u] * f;
                }
            }

            for (int k = u, l = j; l < filter.length; k += step, l++) {
                double f = filter[l];
                sum += src[k] * f;
            }
            
            dst[v] = (float) sum;
            v += step;
        }

        // middle
        for (; i < width - half; i++, u += step) {
            double sum = 0;
            for (int k = 0, l = u; k < filter.length; k++, l += step) {
                sum += src[l] * filter[k];
            }

            dst[v] = (float) sum;
            v += step;
        }

        // right
        for (j = filter.length - 1; i < width; i++, j--, u += step) {
            double sum = 0;
            for (int k = u, l = 0; l < j; l++, k += step) {
                double f = filter[l];
                sum += src[k] * f;
            }

            if (repeat) { // always true. For missing pixels, we use the pixel on the right of the image
                int k = origin + (width - 1) * step;
                for (int l = j; l < filter.length; l++) {
                    double f = filter[l];
                    sum += src[k] * f;
                }
            }

            dst[v] = (float) sum;
            v += step;
        }
    }
}
