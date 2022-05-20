package fr.eni.concurrent.examples.threads;

import java.util.Random;

/**
 * Created by ljoyeux on 25/04/2017.
 */
public class InterruptThread {
    private static double v;

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(){
            @Override
            public void run() {
                System.out.println("In child thread");
                v = new Random(System.currentTimeMillis()).nextDouble();

                for (int i = 0; i < 1_000_000_000; i++) {
                    v = Math.tan(v);
                    if(isInterrupted()) {
                        return;
                    }
                }

                throw new IllegalStateException();
            }
        };

        thread.start();

        System.out.println("Waiting 1s sec in parent thread");
        Thread.sleep(1000);

        System.out.println("Interrupt child thread");
        thread.interrupt();

        System.out.println(v);
    }
}
