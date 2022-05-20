package fr.eni.concurrent.examples.gpu;

/**
 *
 * @author ljoyeux
 */
public final class TypeUtils {
    
    public static int[] asInt(float... els) {
        final int[] conv = new int[els.length];
        
        int outIndex = 0;
        for(float f : els) {
            conv[outIndex++] = (int) f;
        }
        
        return conv;
    } 
    
    public static float[] asFloat(int... els) {
        final float[] conv = new float[els.length];
        
        int outIndex = 0;
        for(int f : els) {
            conv[outIndex++] = (float) f;
        }
        
        return conv;
    } 
    
    public static double[] asDouble(int... els) {
        final double[] conv = new double[els.length];
        
        int outIndex = 0;
        for(int f : els) {
            conv[outIndex++] = (double) f;
        }
        
        return conv;
    } 
}
