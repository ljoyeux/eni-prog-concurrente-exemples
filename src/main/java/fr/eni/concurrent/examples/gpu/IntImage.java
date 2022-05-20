package fr.eni.concurrent.examples.gpu;

/**
 *
 * @author ljoyeux
 */
public final class IntImage {

    private final int[] pixels;
    private final int width;
    private final int height;

    public IntImage(final int width, final int height, final int[] pixels) {
        this.width = width;
        this.height = height;

        this.pixels = (pixels != null && pixels.length == width * height) ? pixels : new int[width * height];
    }

    public IntImage(int width, int height) {
        this(width, height, null);
    }

    public int[] getPixels() {
        return pixels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRed(final int x, final int y) {
        return (pixels[x + y * width] >> 16) & 0xff;
    }

    public int getGreen(final int x, final int y) {
        return (pixels[x + y * width] >> 8) & 0xff;
    }

    public int getBlue(final int x, final int y) {
        return pixels[x + y * width] & 0xff;
    }

    public int[] getReds() {
        return getChannel(0);
    }

    public int[] getGreens() {
        return getChannel(1);
    }

    public int[] getBlues() {
        return getChannel(2);
    }

    public int[] getChannel(final int c) {
        final int[] channel = new int[width * height];

        final int nbShifts = (2 - c) * 8;

        int channelIndex = 0;
        for (int v : pixels) {
            channel[channelIndex++] = (v >> nbShifts) & 0xff;
        }
        return channel;
    }

    public void setRGBChannels(int[]... values) {
        final int nbChannels = 3;

        if (values.length != nbChannels) {
            throw new IllegalStateException();
        }

        for (int pixelIndex = 0; pixelIndex < pixels.length; pixelIndex++) {
            int pixel = 0;
            for (int c = 0; c < nbChannels; c++) {
                pixel = pixel << 8;
                pixel |= values[c][pixelIndex];
            }
            pixels[pixelIndex] = pixel;
        }
    }
    
    public int[] getRGB() {
        
        int[] rgbs = new int[3*width*height];
        int outRGBIndex = 0;
        
        for(int p: pixels) {
            rgbs[outRGBIndex+2] = p & 0xff;
            p >>= 8;
            rgbs[outRGBIndex+1] = p & 0xff;
            p >>= 8;
            rgbs[outRGBIndex+0] = p & 0xff;
            outRGBIndex += 3;
        }
        
        return rgbs;
    }
    
    public void setRGB(int[] rgbs) {
        int pixeIndex = 0;
        
        for(int rgbIndex = 0; rgbIndex<rgbs.length;) {
            int p = rgbs[rgbIndex++]; //red
            p <<= 8;
            p |= rgbs[rgbIndex++]; // green
            p <<= 8;
            p |= rgbs[rgbIndex++]; // blue
            
            pixels[pixeIndex++] = p;
        }
    }
}
