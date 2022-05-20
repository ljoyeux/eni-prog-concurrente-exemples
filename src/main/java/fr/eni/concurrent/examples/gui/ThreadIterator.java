package fr.eni.concurrent.examples.gui;

/**
 *
 * @author ljoyeux
 * @param <T>
 */
public class ThreadIterator<T> extends Thread {
    private final Iterable<T> iterable;
    private final ParamaterRunnable<T> parameterRunnable;

    public ThreadIterator(Iterable<T> iterable, ParamaterRunnable<T> target) {
        this.iterable = iterable;
        this.parameterRunnable = target;
    }

    @Override
    public void run() {
        for(T i: iterable) {
            if(isInterrupted()) {
                return;
            }
            parameterRunnable.run(i);
        }
    }
}
