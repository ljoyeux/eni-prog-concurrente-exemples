package fr.eni.concurrent.examples.threads;

/**
 * Created by ljoyeux on 27/04/2017.
 */
public class ThreadTimeoutWait extends Thread {
    boolean timeout;

    public synchronized void doWait(long timeout) throws InterruptedException {
        this.timeout = true;
        super.wait(timeout);
        System.out.println("is timeout: " + this.timeout);
    }

    public boolean isTimeout() {
        return timeout;
    }

    public synchronized void doNotify() {
        this.timeout = false;
        notify();
    }
}
