package com.skypay.banking.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Record représentant une transaction bancaire
 * Utilise LocalDate + sequence pour le tri
 */
public record Transaction(
        LocalDate date,
        int amount,
        int balance,
        long sequence  // Pour garantir l'ordre d'insertion
) {

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Compact constructor pour validation
     */
    public Transaction {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
    }

    /**
     * Constructeur sans séquence (sera généré automatiquement)
     */
    public Transaction(LocalDate date, int amount, int balance) {
        this(date, amount, balance, System.nanoTime());
    }

    /**
     * Formatage pour l'affichage du statement
     */
    public String toStatementLine() {
        return String.format("%s || %d || %d",
                date.format(DISPLAY_FORMATTER),
                amount,
                balance
        );
    }
}