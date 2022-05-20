package fr.eni.concurrent.examples.gpu;

import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import java.io.Closeable;
import java.io.IOException;

/**
 *
 * @author ljoyeux
 */
public class OpenCL implements Closeable {
    private final CLContext context;
    private final CLDevice[] devices;
    
    private CLDevice device;
    
    public OpenCL() {
      context = CLContext.create();
      devices = context.getDevices();
      device = null;
      for(CLDevice dev: devices)
      {
//          System.out.println("Device: " + device);
          if(CLDevice.Type.GPU.equals(dev.getType()))
          {
              device = dev;
              break;
          }            
      }
      
    }

    public CLContext getContext() {
        return context;
    }

    public CLDevice getDevice() {
        return device;
    }
    
    @Override
    public void close() throws IOException {
        context.release();
    }
    
}
