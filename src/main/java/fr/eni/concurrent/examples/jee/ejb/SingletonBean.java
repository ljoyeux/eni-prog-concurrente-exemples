package fr.eni.concurrent.examples.jee.ejb;

//import javax.ejb.Singleton;

/**
 * Created by ljoyeux on 04/06/2017.
 */
//@Singleton
public class SingletonBean {
    public String hello(String name) {
        return "hello " + name;
    }
}
