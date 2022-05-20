package fr.eni.concurrent.examples.threads;

/**
 * Created by ljoyeux on 26/05/2017.
 */
public class ReentrantMutex {

    private Thread acquireThread;
    private int numAcquires;
    private int numWaitingThreads;


    public void acquire() throws InterruptedException {
        performAcquireOrLock(true);
    }

    public void release() {
        try {
            performAcquireOrLock(false);
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private synchronized void performAcquireOrLock(boolean acquire) throws InterruptedException {
        if (acquire) {
            if (numAcquires == 0 || Thread.currentThread().equals(acquireThread)) {
                numAcquires++;
                if (acquireThread == null) {
                    acquireThread = Thread.currentThread();
                }
            } else {
                numWaitingThreads++;
                wait();
                numWaitingThreads--;
                numAcquires = 1;
                acquireThread = Thread.currentThread();
            }
        } else {
            if (Thread.currentThread().equals(acquireThread)) {
                if (--numAcquires == 0) {
                    acquireThread = null;
                    if (numWaitingThreads > 0) {
                        notify();
                    }
                }
            }
        }
    }
}
