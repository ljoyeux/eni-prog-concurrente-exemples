package fr.eni.concurrent.examples.atomic;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * Created by ljoyeux on 06/07/2017.
 */
public class SemaphoreExample {
    public static void semaphore() {
        final Semaphore semaphore = new Semaphore(4, true);
        final Random r = new Random(System.nanoTime());

        for(int i=0; i<10; i++) {
            new Thread(Integer.toString(i)) {
                @Override
                public void run() {
                    try {
                        synchronized (semaphore) {
                            semaphore.acquire();
                            System.out.println("Semaphore acquired in thread " + getName() + ", remaining " + semaphore.availablePermits());
                        }

                        final int sleepTime = r.nextInt(1_000) + 200;
                        Thread.sleep(sleepTime);

                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    semaphore.release();
                    System.out.println("Semaphore released in thread " + getName());
                }
            }.start();
        }
    }

    public static void main(String[] args) {
        semaphore();
    }
}
