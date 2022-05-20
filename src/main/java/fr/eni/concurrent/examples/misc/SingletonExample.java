package fr.eni.concurrent.examples.misc;

import java.util.*;

/**
 * Created by ljoyeux on 10/07/2017.
 */
public class SingletonExample {

    /*
        Singleton initialization on class loading
     */
    public static final Map<String, String> CONFIG;

    static {
        CONFIG = Collections.synchronizedMap(new HashMap<String, String>());
    }

    /*
        Singleton initialization on method call
     */
    private static List<String> els;

    public static List<String> getEls() {
        // synchronized block for mutual execution
        synchronized (SingletonExample.class) {
            // if els is not initialized, instantiate it.
            if(els==null) {
                els = Collections.synchronizedList(new ArrayList<String>());
            }

            return els;
        }
    }
}
