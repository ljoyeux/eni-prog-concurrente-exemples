package fr.eni.concurrent.examples.gui;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ljoyeux on 12/05/2017.
 */
public class SharedIterator<T> implements Iterable<T> {
    private final Iterator<T> srcIterator;
    private final Iterator<T> sharedIterator;
    private final ReentrantLock lock;

    public SharedIterator(Iterable<T> srcIterable) {
        this.srcIterator = srcIterable.iterator();
        lock = new ReentrantLock();
        sharedIterator = new Iterator<T>() {
            @Override
            public boolean hasNext() {
                boolean hasNext = false;
                lock.lock();
                try {
                    hasNext = srcIterator.hasNext();
                    return hasNext;
                } finally {
                    if (!hasNext)
                        lock.unlock();
                }
            }

            @Override
            public T next() {
                try {
                    return srcIterator.next();
                } finally {
                    lock.unlock();
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
    }

    @Override
    public Iterator<T> iterator() {
        return sharedIterator;
    }

}
