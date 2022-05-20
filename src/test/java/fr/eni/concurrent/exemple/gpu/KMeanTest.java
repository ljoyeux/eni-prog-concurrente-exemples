package fr.eni.concurrent.exemple.gpu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fr.eni.concurrent.examples.gpu.ImageUtils;
import fr.eni.concurrent.examples.gpu.IntImage;
import fr.eni.concurrent.examples.gpu.KMean;

public class KMeanTest {
	
	@Test
	public void colorVectorise() throws IOException {
	    
        try (InputStream is = ImageUtilsTest.class.getResourceAsStream("/Lenna.png");
            OutputStream os = new FileOutputStream(new File(Common.getOutputFolder(), "cluster.png"));) {
            assert is != null;

            final IntImage img = ImageUtils.read(is);
            
            final List<double[]> vectors = new ArrayList<>();
            final int[] rgb = img.getRGB();
            for(int i=0; i<rgb.length; i+=3) {
            	vectors.add(new double[]{rgb[i], rgb[i+1], rgb[i+2]});
            }
            double[][] initColors = new double[][]{
        		new double[] {255, 0, 0},      // one color per line
        		new double[] {255, 255, 0},
        		new double[] {255, 255, 255},
        		
        		new double[] {0, 255, 0},
        		new double[] {0, 255, 255},
        		new double[] {0, 0, 255},
        		
        		new double[] {0, 0, 0},
        		new double[] {128, 128, 128},
                
        		new double[] {128, 0, 0},
        		new double[] {128, 128, 0},
        		
        		new double[] {0, 128, 0},  
        		new double[] {0, 128, 128},
        		new double[] {0, 0, 128}
            };

            
            final List<double[]> centers = new ArrayList<>(Arrays.asList(initColors));
            final KMean.Distance d2 = new KMean.Distance() {
				
				@Override
				public double d(double[] v, double[] c) {
					double d2 = 0;
					for(int i=0; i<v.length; i++) {
						double delta = v[i] - c[i];
						d2 += delta*delta;
					}
					
					return d2;
				}
			};
			
            KMean kMean = new KMean(vectors, centers, d2);

            for(int i=0; i<1000; i++) {
            	kMean.step1();
                List<double[]> colors = kMean.step2();
                if(i%100==0) {
                    System.out.println("iter " + i + " nb colors: " + colors.size() );
                }
            }
            
            int[] colorIndex = kMean.step1();
//            List<double[]> colors = kMean.step2();
//            System.out.println("num colors : " + colors.size());
            for(int i=0; i<colorIndex.length; i++) {
            	double[] c = initColors[colorIndex[i]];
//            	double[] c = colors.get(colorIndex[i]);
            	rgb[i*3+0] = (int) c[0];
            	rgb[i*3+1] = (int) c[1];
            	rgb[i*3+2] = (int) c[2];
            }
            
            IntImage outImg = new IntImage(img.getWidth(), img.getHeight(), null);
            outImg.setRGB(rgb);
            ImageUtils.write(outImg, os, "png").close();
        }
	}
}
