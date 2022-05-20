package fr.eni.concurrent.exemple.gpu;

import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;

import org.junit.Test;

/**
 *
 * @author joyeuxl
 */
public class OpenCLTest {
  @Test
  public void openCLContext() {
      final CLContext context = CLContext.create();
      final CLDevice[] devices = context.getDevices();

      CLDevice device = null;
      for(CLDevice dev: devices)
      {
          System.out.println("Device: " + device);
          if(CLDevice.Type.GPU.equals(dev.getType()))
          {
              device = dev;
              break;
          }            
      }
      
      if(device==null) {
        System.err.println("No GPU Device found");
        return;
      }
      
      System.out.println(device);
      context.release();
  }
}
