package fr.eni.concurrent.exemple.gpu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Test;

import fr.eni.concurrent.examples.gpu.Filter;
import fr.eni.concurrent.examples.gpu.ImageFilter;
import fr.eni.concurrent.examples.gpu.ImageUtils;
import fr.eni.concurrent.examples.gpu.IntImage;

public class ImageFilterTest {
    @Test
    public void filter() throws IOException {
        try (InputStream is = ImageUtilsTest.class.getResourceAsStream("/Lenna.png");
            OutputStream os = new FileOutputStream(new File(Common.getOutputFolder(), "filter.png"));) {
            assert is != null;

            final double[] gaussFilter = Filter.gauss(5);
            final IntImage img = ImageUtils.read(is);
            IntImage filteredImage = ImageFilter.imgFilter(img, null, gaussFilter, gaussFilter);

            ImageUtils.write(filteredImage, os, "png").close();
        }
    }
    
    @Test
    public void filterX() throws IOException {
        try (InputStream is = ImageUtilsTest.class.getResourceAsStream("/Lenna.png");
            OutputStream os = new FileOutputStream(new File(Common.getOutputFolder(), "filter-X.png"));) {
            assert is != null;

            final double[] gaussFilter = Filter.gauss(5);
            final IntImage img = ImageUtils.read(is);
            IntImage filteredImage = ImageFilter.imgFilter(img, null, gaussFilter, new double[] {1});

            ImageUtils.write(filteredImage, os, "png").close();
        }
    }

}
