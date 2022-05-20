package fr.eni.concurrent.exemple.jee.ejb;

import fr.eni.concurrent.examples.jee.ejb.SingletonBean;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 * Created by ljoyeux on 04/06/2017.
 */
public class SingletonBeanTest {
    @Test
    public void test() throws NamingException {
        EJBContainer container = EJBContainer.createEJBContainer();
        Context context = container.getContext();
        SingletonBean bean = (SingletonBean) context.lookup("java:global/classes/SingletonBean");

        System.out.println(bean.hello("Laurent"));
    }
}
