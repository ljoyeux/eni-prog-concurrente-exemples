package fr.eni.concurrent.examples.jee.sql;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;

/**
 * Created by ljoyeux on 04/06/2017.
 */
@Entity
public class AccountTransaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double currentValue;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionTimeStamp;

    @OneToOne(fetch = FetchType.LAZY)
    private AccountTransaction previousTransaction;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public Date getTransactionTimeStamp() {
        return transactionTimeStamp;
    }

    public void setTransactionTimeStamp(Date transactionTimeStamp) {
        this.transactionTimeStamp = transactionTimeStamp;
    }

    public AccountTransaction getPreviousTransaction() {
        return previousTransaction;
    }

    public void setPreviousTransaction(AccountTransaction previousTransaction) {
        this.previousTransaction = previousTransaction;
    }

    @Override
    public String toString() {
        return "AccountTransaction{" + "id=" + id + ", currentValue=" + currentValue + ", transactionTimeStamp=" + transactionTimeStamp + ", previousTransaction=" + previousTransaction + '}';
    }
}
