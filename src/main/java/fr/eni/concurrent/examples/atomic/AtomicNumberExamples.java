package fr.eni.concurrent.examples.atomic;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ljoyeux on 06/07/2017.
 */
public class AtomicNumberExamples {

    public static void set(final AtomicInteger number, Integer i, int a) {
        number.set(a);
        i = a;
    }

    public static void ops() {
        final AtomicInteger i = new AtomicInteger();
        i.set(2);
        if(i.get()!=2) {
            throw new AssertionError();
        }

        /*
            proper way to update
          */
        int result = i.addAndGet(2); // result is the value at the end of the call.
        if(result!=4) {
            throw new AssertionError();
        }

        /*
            wrong way but working in single thread environment
          */
        i.addAndGet(2);
        // the value may have changed in between the two calls.
        if(i.get()!=6) { //
            throw new AssertionError();
        }

        /*
            set new value on expected value
         */
        boolean set = i.compareAndSet(1, 5);
        if(set) {
            throw new AssertionError();
        }

        set = i.compareAndSet(6, 7);
        if(!set) {
            throw new AssertionError();
        }

        /*
            decrement and increment
         */

        i.incrementAndGet(); // pre increment
        i.getAndIncrement(); // post increment

        i.decrementAndGet(); // pre decrement
        i.getAndDecrement(); // post decrement

    }

    public static void main(String[] args) {
        AtomicInteger n = new AtomicInteger(2);
        Integer i = 2;
        set(n, i, 4);
        System.out.println(String.format("%d %d", n.intValue(), i.intValue()));



        ops();

    }
}
