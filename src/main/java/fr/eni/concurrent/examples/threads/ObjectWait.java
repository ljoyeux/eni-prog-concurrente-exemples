package fr.eni.concurrent.examples.threads;

/**
 * Created by ljoyeux on 25/04/2017.
 */
public class ObjectWait {

    public synchronized void synchronisedMethodV1() {

    }

    public void synchronizedMethodV2() {
        synchronized (this) {

        }
    }

    public void waitExample() {
        Object obj = new Object();
        synchronized (obj) {
            try {
                obj.wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    public static void waitInterrupted() {
        final Thread thread = new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        wait();
                        throw new IllegalStateException();
                    } catch (InterruptedException ex) {
                        System.out.println("Thread interrupted");
                    }
                }
            }
        };

        thread.start();

        try {
            Thread.sleep(1000);
            thread.interrupt();
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static void waitInterruptedRunnable() {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        wait();
                        throw new IllegalStateException();
                    } catch (InterruptedException ex) {
                        System.out.println("Thread interrupted");
                    }
                }

            }
        };

        final Thread thread = new Thread(run);

        thread.start();

        try {
            Thread.sleep(1000);
            thread.interrupt();
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static void timeoutWait() {
        new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        wait(1000);
                    } catch (InterruptedException ex) {
                        System.out.println("Thread interrupted");
                    }
                }
            }

        };
    }


    public static void main(String[] args) throws InterruptedException {

        waitInterrupted();

//        Object obj1 = new Object();
//        Object obj2 = new Object();
//        synchronized (obj1) {
//            obj2.wait();
//        }
//
//        synchronized (obj1) {
//            obj1.wait();
//        }

    }
}
