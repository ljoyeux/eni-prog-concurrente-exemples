package fr.eni.concurrent.examples.threads;

/**
 * Created by ljoyeux on 24/04/2017.
 */
public class TrivialRunnableThread {
    public static void main(String[] args) throws InterruptedException {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                System.out.println("In child thread");
            }
        };

        Thread thread = new Thread(run);
        thread.start();
        thread.join();
    }
}
