package fr.eni.concurrent.exemple.gpu;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;
import com.jogamp.opencl.CLProgram;
import fr.eni.concurrent.examples.gpu.ImageUtils;
import fr.eni.concurrent.examples.gpu.IntImage;
import fr.eni.concurrent.examples.gpu.OpenCL;
import fr.eni.concurrent.examples.gpu.TypeUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import org.junit.Test;

/**
 *
 * @author ljoyeux
 */
public class OpenCLKMeanTest {
    @Test
    public void colorKMean() throws IOException {
        try (InputStream is = ImageUtilsTest.class.getResourceAsStream("/Lenna.png");
            OutputStream os = new FileOutputStream(new File(Common.getOutputFolder(), "cluster-kmeans.png"));
            final OpenCL openCL = new OpenCL(); ) {
            assert is != null;

            final IntImage img = ImageUtils.read(is);
            int[] pixels = img.getPixels();
            int width = img.getWidth();
            int height = img.getHeight();
            
            /*
                Create OpenCL context
              */
            final CLContext context = openCL.getContext();

            /*
                Read program from file and compile it
             */
            final CLProgram program = context.createProgram(OpenCLImageFilterTest.class.getResourceAsStream("/k-mean.cl")).build();
            final CLKernel kMeanStep1Kernel = program.createCLKernel("step1");
            final CLKernel kMeanStep2Kernel = program.createCLKernel("step2");

            /*
                Work Group size
             */
            final CLDevice device = openCL.getDevice();
            int maxWorkGroupSize = device.getMaxWorkGroupSize();

            // the number of pixels in the image is a multiple of max Work Group Size
            assert pixels.length % maxWorkGroupSize == 0;
            
            
            double[] rgb = TypeUtils.asDouble(img.getRGB());
          
                        
            /*
                Create image buffer;
            */
            final CLBuffer<DoubleBuffer> clImgBuffer = context.createDoubleBuffer(rgb.length, CLMemory.Mem.READ_WRITE);
            clImgBuffer.getBuffer().put(rgb).rewind(); // write image
            
            /*
                Init colors
              */
            double[] initColors = new double[]{
        		255, 0, 0,      // one color per line
        		255, 255, 0,
        		255, 255, 255,
        		
        		0, 255, 0,
        		0, 255, 255,
        		0, 0, 255,
        		
        		0, 0, 0,
        		128, 128, 128,
                
        		128, 0, 0,
        		128, 128, 0,
        		
        		0, 128, 0,  
        		0, 128, 128,
        		0, 0, 128
            };
            int nbColors = initColors.length / 3;


            /*
                Allocate OpenCL buffers
             */
            final CLBuffer<DoubleBuffer> clColorBuffer = context.createDoubleBuffer(initColors.length, CLMemory.Mem.READ_WRITE);
            
            clColorBuffer.getBuffer().put(initColors).rewind();
            
            final CLBuffer<IntBuffer> clAssignmenBuffer = context.createIntBuffer(pixels.length, CLMemory.Mem.READ_WRITE);
                        

            CLCommandQueue clCommandQueue = device.createCommandQueue();

            /*
                Set arguments for step1 and step2 OpenCL functions.
             */
            
            kMeanStep1Kernel.putArg(clImgBuffer).putArg(pixels.length).putArg(clColorBuffer).putArg(nbColors).putArg(clAssignmenBuffer);
            
            kMeanStep2Kernel.putArg(clImgBuffer).putArg(pixels.length).putArg(clColorBuffer).putArg(nbColors).putArg(clAssignmenBuffer);

            /*
                Send data to the GPU
             */
            clCommandQueue.putWriteBuffer(clImgBuffer, false).putWriteBuffer(clColorBuffer, true);

            /*
                K-Mean algorithm
             */
            for(int i=0; i<1000; i++) {
                // step 1 : assign each color to a group
                clCommandQueue.put1DRangeKernel(kMeanStep1Kernel, 0, pixels.length, maxWorkGroupSize);
                // step 2 : compute the new center of each group using all assigned colors to the group
                clCommandQueue.put1DRangeKernel(kMeanStep2Kernel, 0, nbColors, nbColors);

                // Both previous commands are in the queue. Flushing the queue make both steps to be executed
                clCommandQueue.flush();

                if(i%100==0) {
                    System.out.println("iter " + i);
                } 
            }


            /*
                Read back the color assignments to each color group
              */
            clCommandQueue.putReadBuffer(clAssignmenBuffer, true);
            int[] assignments = new int[pixels.length];
            IntBuffer buffer = clAssignmenBuffer.getBuffer();
            buffer.rewind();
            buffer.get(assignments);


            /*
                Read back color groups (group center) into initColors
              */
            clCommandQueue.putReadBuffer(clColorBuffer, true);
            DoubleBuffer buffer1 = clColorBuffer.getBuffer();
            buffer1.rewind();
            buffer1.get(initColors);
            

            /*
                Release OpenCL resources
              */
            clCommandQueue.release();
            clAssignmenBuffer.release();
            clColorBuffer.release();
            clImgBuffer.release();
            kMeanStep1Kernel.release();
            kMeanStep2Kernel.release();

            /*
             To speed the process, precompute rgb colors.
              */
            int[] colors = new int[nbColors];
            for(int i=0; i<colors.length; i++) {
                int r = (int) initColors[i*3+0];
                int g = (int) initColors[i*3+1];
                int b = (int) initColors[i*3+2];
                colors[i] = (((r<<8) | g)<<8)| b;
            }


            /*
                Color mapping.
              */

            IntImage outImage = new IntImage(width, height);
            int[] outPixels = outImage.getPixels();
            // map the color. A color is assignment to a group, the group has an rgb color.
            for(int i=0; i<outPixels.length; i++) {
                outPixels[i]  = colors[assignments[i]];
            }

            /*
                Write the new image
             */
            ImageUtils.write(outImage, os, "png");                
            
        }
    }
}
