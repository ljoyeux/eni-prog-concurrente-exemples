package fr.eni.concurrent.exemple.gpu;

import fr.eni.concurrent.examples.gpu.Filter;
import fr.eni.concurrent.examples.gpu.ImageFilter;
import fr.eni.concurrent.examples.gpu.ImageUtils;
import fr.eni.concurrent.examples.gpu.IntImage;
import fr.eni.concurrent.examples.gpu.TypeUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Test;

/**
 *
 * @author ljoyeux
 */
public class ImageUtilsTest {

    @Test
    public void ioTest() throws IOException {
        try (InputStream is = ImageUtilsTest.class.getResourceAsStream("/Lenna.png");
            OutputStream os = new FileOutputStream(new File(Common.getOutputFolder(), "out.png"));) {
            assert is != null;

            final IntImage img = ImageUtils.read(is);

            ImageUtils.write(img, os, "png").close();
        }
    }
    
    @Test
    public void splitAndMerge() throws IOException {
        try (InputStream is = ImageUtilsTest.class.getResourceAsStream("/Lenna.png");
            OutputStream os = new FileOutputStream(new File(Common.getOutputFolder(), "merge.png"));) {
            assert is != null;

            final IntImage img = ImageUtils.read(is);
            final int[] reds = img.getReds();
            final int[] greens = img.getGreens();
            final int[] blues = img.getBlues();

            IntImage outImg = new IntImage(img.getWidth(), img.getHeight(), null);
            outImg.setRGBChannels(reds, greens, blues);
            ImageUtils.write(outImg, os, "png").close();
        }
    }
    
    @Test
    public void splitAndMergeRGB() throws IOException {
        try (InputStream is = ImageUtilsTest.class.getResourceAsStream("/Lenna.png");
            OutputStream os = new FileOutputStream(new File(Common.getOutputFolder(), "merge-1.png"));) {
            assert is != null;

            final IntImage img = ImageUtils.read(is);
            final int[] rgbs = img.getRGB();
            float[] rgbAsFloat = TypeUtils.asFloat(rgbs);

            IntImage outImg = new IntImage(img.getWidth(), img.getHeight());
            outImg.setRGB(TypeUtils.asInt(rgbAsFloat));
            ImageUtils.write(outImg, os, "png").close();
        }
    }
    
}
