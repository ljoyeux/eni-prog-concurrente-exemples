package fr.eni.concurrent.examples.gpu;

/**
 *
 * @author ljoyeux
 */
public final class ImageFilter {
    public static IntImage imgFilter(final IntImage inImg, IntImage outImg, 
    								 final double[] xFilter, final double[] yFilter) {
        final int width = inImg.getWidth();
        final int height = inImg.getHeight();

        final int numComp = 3;
        final int nbPixels = width*height;
        final float[] tmpImage = new float[nbPixels*numComp];

        // X filter
        final float[] inPixels = TypeUtils.asFloat(inImg.getRGB());
        for (int j = 0; j < height; j++) {
            for (int n = 0; n < numComp; n++) {
                Filter.filter(inPixels, tmpImage, width, xFilter, j * width * numComp + n, numComp);
            }
        }

        // Y filter
        final float[] outPixels = new float[nbPixels*numComp];
        for (int i = 0; i < width; i++) {
            for (int n = 0; n < numComp; n++) {
                Filter.filter(tmpImage, outPixels, height, yFilter, i * numComp + n, numComp * width);
            }
        }

        if (outImg == null || outImg.getWidth() != width || outImg.getHeight() != height) {
            outImg = new IntImage(width, height);
        }
        
        outImg.setRGB(TypeUtils.asInt(outPixels));
        
        return outImg;
    }
}
