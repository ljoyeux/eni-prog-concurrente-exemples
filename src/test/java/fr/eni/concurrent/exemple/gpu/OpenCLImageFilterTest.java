package fr.eni.concurrent.exemple.gpu;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory;
import com.jogamp.opencl.CLProgram;
import fr.eni.concurrent.examples.gpu.Filter;
import fr.eni.concurrent.examples.gpu.ImageUtils;
import fr.eni.concurrent.examples.gpu.IntImage;
import fr.eni.concurrent.examples.gpu.OpenCL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.junit.Test;

/**
 *
 * @author ljoyeux
 */
public class OpenCLImageFilterTest {

    @Test
    public void loadImage() throws IOException {
        try (final InputStream is = ImageUtilsTest.class.getResourceAsStream("/Lenna.png");
            final OpenCL openCL = new OpenCL(); ) {
            
            /*
                Read Image
            */
            final IntImage img = ImageUtils.read(is);
            
            int[] pixels = img.getPixels();
            int imgSize = pixels.length*Integer.SIZE;
            int width = img.getWidth();
            int height = img.getHeight();
            
            
            // Create Context
            final CLContext context = openCL.getContext();
                        
            /*
                Create image buffer;
            */
            final CLBuffer<ByteBuffer> clImgBuffer = context.createByteBuffer(imgSize, CLMemory.Mem.READ_WRITE);
            // Each color component is stored as Byte (unsigned). A pixel stores 4 components (Alpha, Red, Green, Blue)
            clImgBuffer.getBuffer().asIntBuffer().put(pixels).rewind(); // write image
            
            final CLBuffer<FloatBuffer> clTmpBuffer = context.createFloatBuffer(imgSize, CLMemory.Mem.READ_WRITE);
            
            /*
                Create filter
            */
            final double[] gaussFilter = Filter.gauss(5);
            final CLBuffer<DoubleBuffer> clFilterBuffer = context.createDoubleBuffer(gaussFilter.length, CLMemory.Mem.READ_ONLY);
            clFilterBuffer.getBuffer().put(gaussFilter).rewind();
            
            /*
                Create programs to filter the image
            */
            CLProgram program = context.createProgram(OpenCLImageFilterTest.class.getResourceAsStream("/filter.cl")).build();
            final CLKernel xFilterKernel = program.createCLKernel("xFilter");
            final CLKernel yFilterKernel = program.createCLKernel("yFilter");
            
            /*
                Create queue to send commands to the GPU
            */
            final CLDevice device = openCL.getDevice();
            CLCommandQueue clCommandQueue = device.createCommandQueue();
            
            /*
                Compute block computation. Work Group Size gives the number of processor.
                Group size is chosen to be a square .
            */
            int maxWorkGroupSize = device.getMaxWorkGroupSize();
            int squareMaxGroupSize = (int) Math.sqrt(maxWorkGroupSize);
            
            
            assert squareMaxGroupSize*squareMaxGroupSize == maxWorkGroupSize;
            // width and height are multiple of squareMaxGroupSize
            assert width % squareMaxGroupSize == 0;
            assert height % squareMaxGroupSize == 0;
            
            /*
                X filter
            */
            // set parameters
            xFilterKernel.putArgs(clImgBuffer, clTmpBuffer).putArg(img.getWidth()).putArg(img.getHeight()).putArg(clFilterBuffer).putArg(gaussFilter.length);
            // compute
            clCommandQueue.putWriteBuffer(clImgBuffer, false) // write image to GPU
                          .putWriteBuffer(clFilterBuffer, false) // write filter to GPU
                          .put2DRangeKernel(xFilterKernel, 0, 0, width, height, squareMaxGroupSize, squareMaxGroupSize); // execute x Filtering
            
            /*
                Y filter
            */
            yFilterKernel.putArgs(clTmpBuffer, clImgBuffer).putArg(img.getWidth()).putArg(img.getHeight()).putArg(clFilterBuffer).putArg(gaussFilter.length);
            clCommandQueue.put2DRangeKernel(yFilterKernel, 0, 0, width, height, squareMaxGroupSize, squareMaxGroupSize).putReadBuffer(clImgBuffer, true);
            
            /*
                Read output image
            */
            IntBuffer outputIntBuffer = clImgBuffer.getBuffer().asIntBuffer();
            outputIntBuffer.rewind();
            
            IntImage outImage = new IntImage(width, height);
            outputIntBuffer.get(outImage.getPixels());
            
            /*
                Release OpenCL resources
            */
            clCommandQueue.release();
            xFilterKernel.release();
            yFilterKernel.release();
            
            clTmpBuffer.release();
            clImgBuffer.release();
            
            /*
                Write filtered image
            */
            try(OutputStream os = new FileOutputStream(new File(Common.getOutputFolder(), "filter-opencl.png"))) {
                ImageUtils.write(outImage, os, "png");                
            }
            
        }
    }
}
