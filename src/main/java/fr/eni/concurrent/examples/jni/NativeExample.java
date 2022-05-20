package fr.eni.concurrent.examples.jni;

import java.util.List;

public class NativeExample {
    static {
        String nativeLibFolder = System.getProperty("native.lib.folder");
        nativeLibFolder += System.getProperty("file.separator");
        System.load(nativeLibFolder + "libexample-jni.dylib");
    }

    public static native int inc(int i);

    private int i;

    public native int incremente();

    public native void launchPThread();
//    public void launchPThread() {
//        new Thread(){
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(3_000);
//                    pthreadDone();
//                } catch (InterruptedException ex) {
//                    throw new RuntimeException(ex);
//                }
//            }
//        }.start();
//    }

    public void pthreadDone() {
        System.out.println("Pthread done");
    }
}
