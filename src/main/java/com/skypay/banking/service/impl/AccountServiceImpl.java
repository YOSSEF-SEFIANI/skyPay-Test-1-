package com.skypay.banking.service.impl;

import com.skypay.banking.exception.InsufficientFundsException;
import com.skypay.banking.model.Account;
import com.skypay.banking.model.Transaction;
import com.skypay.banking.service.AccountService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.PrintStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Implémentation du service bancaire avec bonnes pratiques Spring
 */
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    private final Account account;
    private final StatementPrinter statementPrinter;

    @Setter
    private Clock clock;

    public AccountServiceImpl() {
        this(System.out, Clock.systemDefaultZone());
    }

    public AccountServiceImpl(PrintStream printStream) {
        this(printStream, Clock.systemDefaultZone());
    }

    public AccountServiceImpl(PrintStream printStream, Clock clock) {
        this.account = new Account();
        this.statementPrinter = new StatementPrinter(printStream);
        this.clock = clock;
    }

    @Override
    public void deposit(int amount) {
        validatePositiveAmount(amount);

        int newBalance = account.getBalance() + amount;
        Transaction transaction = new Transaction(LocalDate.now(clock), amount, newBalance);
        account.addTransaction(transaction);

        log.debug("Deposit of {} completed.  New balance: {}", amount, newBalance);
    }

    @Override
    public void withdraw(int amount) {
        validatePositiveAmount(amount);

        if (account.getBalance() < amount) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Balance: " + account.getBalance() + ", requested: " + amount
            );
        }

        int newBalance = account.getBalance() - amount;
        Transaction transaction =new Transaction(LocalDate.now(clock), -amount, newBalance);
        account.addTransaction(transaction);

        log.debug("Withdrawal of {} completed. New balance: {}", amount, newBalance);
    }

    @Override
    public void printStatement() {
        statementPrinter.print(account.getTransactions());
    }

    private void validatePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    /**
     * Classe interne pour l'impression du statement
     * Tri par séquence décroissante (plus récent en premier)
     */
    static class StatementPrinter {
        private static final String HEADER = "Date || Amount || Balance";
        private final PrintStream printStream;

        StatementPrinter(PrintStream printStream) {
            this.printStream = printStream;
        }

        void print(List<Transaction> transactions) {
            printStream.println(HEADER);

            transactions.stream()
                    .sorted(Comparator.comparing(Transaction::sequence).reversed())
                    .map(Transaction::toStatementLine)
                    .forEach(printStream::println);
        }
    }
}