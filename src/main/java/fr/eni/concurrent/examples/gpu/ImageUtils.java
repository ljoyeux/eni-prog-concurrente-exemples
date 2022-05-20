package fr.eni.concurrent.examples.gpu;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author ljoyeux
 */
public class ImageUtils {
    public static IntImage read(final InputStream is) throws IOException {
        final BufferedImage bi = ImageIO.read(is);
        
        final int width = bi.getWidth();
        final int height = bi.getHeight();
        final int[] pixels = new int[width*height];
        
        bi.getRGB(0, 0, width, height, pixels, 0, width);
        
        return new IntImage(width, height, pixels);
    }
    
    public static OutputStream write(final IntImage img, final OutputStream os, final String formatName) throws IOException {
        final int width = img.getWidth();
        final int height = img.getHeight();
        
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0, 0, width, height, img.getPixels(), 0, width);
        
        ImageIO.write(bi, formatName, os);
        
        return os;
    }
}
