package fr.eni.concurrent.exemple.jee.sql;

import fr.eni.concurrent.examples.jee.sql.UserAccount;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

/**
 * Created by ljoyeux on 04/06/2017.
 */
public class PersistenceTest {
    @Test
    public void test() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("Concurrent-PU");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        transaction.begin();

        UserAccount userAccount = new UserAccount();
        userAccount.setFirstName("Laurent");
        userAccount.setLastName("Joyeux");
        userAccount = em.merge(userAccount);

        transaction.commit();

        em.close();


        em = emf.createEntityManager();
        
        List<UserAccount> accounts = em.createQuery("SELECT ac FROM UserAccount ac").getResultList();
        System.out.println("accounts : " + accounts);
        
        em.close();

        emf.close();
    }
}
