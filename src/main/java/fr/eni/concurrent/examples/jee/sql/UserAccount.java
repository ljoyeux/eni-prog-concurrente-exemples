package fr.eni.concurrent.examples.jee.sql;

import java.io.Serializable;
import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Created by ljoyeux on 04/06/2017.
 */
@Entity
public class UserAccount implements Serializable {

    @Id @GeneratedValue(strategy = IDENTITY)
    private long id;

    private String firstName;

    private String lastName;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private AccountTransaction accountTransaction;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public AccountTransaction getAccountTransaction() {
        return accountTransaction;
    }

    public void setAccountTransaction(AccountTransaction accountTransaction) {
        this.accountTransaction = accountTransaction;
    }

    @Override
    public String toString() {
        return "UserAccount{" + "id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", accountTransaction=" + accountTransaction + '}';
    }    
}
