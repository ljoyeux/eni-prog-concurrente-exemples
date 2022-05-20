package fr.eni.concurrent.examples.threads;

/**
 * Created by ljoyeux on 24/04/2017.
 */
public class TrivialThread {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                System.out.println("In child thread");
            }
        };

        thread.start();
        System.out.println("Wait for child thread completion");
        thread.join();
    }


}
