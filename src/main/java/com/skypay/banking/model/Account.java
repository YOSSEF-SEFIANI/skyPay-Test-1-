package com.skypay.banking.model;

import lombok. Getter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entité Account avec encapsulation correcte
 */
@Getter
public class Account {
    private int balance;
    private final List<Transaction> transactions;

    public Account() {
        this.balance = 0;
        this.transactions = new ArrayList<>();
    }

    /**
     * Retourne une copie immuable des transactions
     */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * Ajoute une transaction et met à jour le solde
     */
    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        this.balance = transaction.balance();
    }
}