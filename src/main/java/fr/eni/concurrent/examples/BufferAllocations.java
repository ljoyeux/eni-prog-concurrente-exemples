package fr.eni.concurrent.examples;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 *
 * @author ljoyeux
 */
public class BufferAllocations {
    public static void main(String[] args) {
        byte[] content = new byte[1024];
        Random r = new Random(System.nanoTime());
        r.nextBytes(content);

        // Buffer allocation
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(content.length);

        // fill the buffer
        directBuffer.put(content);
        directBuffer.rewind();

        /*
            use the Buffer
          */

        // Read an integer
        int i = directBuffer.getInt();
        System.out.println(i);

        // Next, read a double
        double d = directBuffer.getDouble();
        System.out.println(d);

        // Finally, write a double
        directBuffer.putDouble(Math.PI);


        try {
            // rewind first
            directBuffer.rewind();

	        directBuffer.put(content);
	        directBuffer.put(content); // BufferOverflowException
            assert false;
        } catch (BufferOverflowException ex) {
        }
	        
        try {
	        byte[] directBufferArray = directBuffer.array(); // UnsupportedOperationException
	        directBufferArray[0] = (byte) 0;
	        assert false;
        } catch (UnsupportedOperationException ex){
        }
        
        ByteBuffer javaBuffer = ByteBuffer.allocate(content.length);
        javaBuffer.put(content);
        javaBuffer.rewind();
        
        byte[] javaBufferArray = javaBuffer.array(); // Ok
        javaBufferArray[0] = (byte) 0; // Ok
    }
}
