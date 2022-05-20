package fr.eni.concurrent.examples.misc;

/**
 * Created by ljoyeux on 25/06/2017.
 */
public class VolatileVar {

    private volatile  int stampId = 0;

    public void modification() {
        int j = ++stampId;
        stampId = j;

        // perform modifications
        if(j!=stampId) {
            throw new IllegalStateException();
        }
    }
}
