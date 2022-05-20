package fr.eni.concurrent.examples.threads;

/**
 * Created by ljoyeux on 25/04/2017.
 */
public class DoubleLaunchThread {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                System.out.println("In child thread");
            }
        };

        System.out.println("First launch");
        thread.start();
        System.out.println("Wait for child thread completion");
        thread.join();

        System.out.println("Second launch");
        thread.start();
        System.out.println("Wait for child thread completion");
        thread.join();

    }
}
