package fr.eni.concurrent.exemple.jni;

import fr.eni.concurrent.examples.jni.NativeExample;
import org.junit.Test;

/**
 * Created by ljoyeux on 14/07/2017.
 */
public class NativeExampleTest {
    @Test
    public void callNative() {
        System.out.println(NativeExample.inc(2));
    }

    @Test
    public void incTest() {

        System.out.println("Increment natively a integer member");

        NativeExample nativeExample = new NativeExample();
        System.out.println(nativeExample.incremente());
        System.out.println(nativeExample.incremente());
        System.out.println(nativeExample.incremente());

    }

    @Test
    public void pthread() throws InterruptedException {
        new NativeExample().launchPThread();


        Thread.sleep(5_000);
    }
}
