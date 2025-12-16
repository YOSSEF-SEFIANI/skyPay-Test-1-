package com.skypay.banking.service;

import com.skypay.banking.exception.InsufficientFundsException;
import com.skypay.banking.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires avec JUnit 5 et AssertJ - Gestion complète des erreurs
 */
@DisplayName("Account Service Tests - Error Handling")
class AccountServiceTest {

    private AccountServiceImpl accountService;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        Clock fixedClock = Clock.fixed(
                Instant.parse("2025-01-01T10:00:00Z"),
                ZoneId.systemDefault()
        );

        accountService = new AccountServiceImpl(printStream, fixedClock);
        originalOut = System.out;
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ========== TESTS DES CAS D'ERREUR - DÉPÔTS ==========

    @Nested
    @DisplayName("Deposit Error Cases")
    class DepositErrorTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when depositing zero")
        void shouldThrowExceptionWhenDepositingZero() {
            // When & Then
            assertThatThrownBy(() -> accountService.deposit(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when depositing negative amount")
        void shouldThrowExceptionWhenDepositingNegative() {
            // When & Then
            assertThatThrownBy(() -> accountService.deposit(-100))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100, -1000, -10000, 0})
        @DisplayName("Should reject all invalid deposit amounts")
        void shouldRejectAllInvalidDepositAmounts(int invalidAmount) {
            // When & Then
            assertThatThrownBy(() -> accountService.deposit(invalidAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");

            // Vérifier que le solde n'a pas changé
            accountService.printStatement();
            String output = outputStream.toString();
            String[] lines = output.split("\\r?\\n");
            assertThat(lines).hasSize(1); // Seulement le header, pas de transaction
        }

        @Test
        @DisplayName("Should not create transaction when deposit fails")
        void shouldNotCreateTransactionWhenDepositFails() {
            // Given
            accountService.deposit(1000); // Transaction valide

            // When - Tentative de dépôt invalide
            try {
                accountService.deposit(-500);
            } catch (IllegalArgumentException e) {
                // Exception attendue
            }

            // Then - Vérifier qu'il n'y a toujours qu'une seule transaction
            accountService.printStatement();
            String output = outputStream.toString();
            String[] lines = output.split("\\r?\\n");
            assertThat(lines).hasSize(2); // Header + 1 transaction valide seulement
        }

        @Test
        @DisplayName("Should maintain correct balance after failed deposit")
        void shouldMaintainCorrectBalanceAfterFailedDeposit() {
            // Given
            accountService.deposit(1000);
            accountService.deposit(500);

            // When - Tentative de dépôt invalide
            try {
                accountService.deposit(-200);
            } catch (IllegalArgumentException e) {
                // Exception attendue
            }

            // Then - Le solde doit rester à 1500
            accountService.printStatement();
            String output = outputStream.toString();
            assertThat(output).contains("|| 1500");
        }
    }

    // ========== TESTS DES CAS D'ERREUR - RETRAITS ==========

    @Nested
    @DisplayName("Withdrawal Error Cases")
    class WithdrawalErrorTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when withdrawing zero")
        void shouldThrowExceptionWhenWithdrawingZero() {
            // Given
            accountService.deposit(1000);

            // When & Then
            assertThatThrownBy(() -> accountService.withdraw(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when withdrawing negative amount")
        void shouldThrowExceptionWhenWithdrawingNegative() {
            // Given
            accountService.deposit(1000);

            // When & Then
            assertThatThrownBy(() -> accountService.withdraw(-100))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -10, -100, -1000, 0})
        @DisplayName("Should reject all invalid withdrawal amounts")
        void shouldRejectAllInvalidWithdrawalAmounts(int invalidAmount) {
            // Given
            accountService.deposit(5000);

            // When & Then
            assertThatThrownBy(() -> accountService.withdraw(invalidAmount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive");
        }

        @Test
        @DisplayName("Should throw InsufficientFundsException when balance is zero")
        void shouldThrowExceptionWhenBalanceIsZero() {
            // When & Then
            assertThatThrownBy(() -> accountService.withdraw(100))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("Insufficient funds")
                    .hasMessageContaining("Balance: 0")
                    .hasMessageContaining("requested: 100");
        }

        @Test
        @DisplayName("Should throw InsufficientFundsException when withdrawal exceeds balance")
        void shouldThrowExceptionWhenWithdrawalExceedsBalance() {
            // Given
            accountService.deposit(500);

            // When & Then
            assertThatThrownBy(() -> accountService.withdraw(1000))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("Insufficient funds")
                    .hasMessageContaining("Balance: 500")
                    .hasMessageContaining("requested: 1000");
        }

        @ParameterizedTest
        @CsvSource({
                "100, 101",
                "500, 501",
                "1000, 1001",
                "999, 1000",
                "1, 2"
        })
        @DisplayName("Should throw exception when withdrawal is 1 euro more than balance")
        void shouldThrowExceptionWhenWithdrawalExceedsByOne(int balance, int withdrawal) {
            // Given
            accountService.deposit(balance);

            // When & Then
            assertThatThrownBy(() -> accountService.withdraw(withdrawal))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("Insufficient funds");
        }

        @Test
        @DisplayName("Should not create transaction when withdrawal fails")
        void shouldNotCreateTransactionWhenWithdrawalFails() {
            // Given
            accountService.deposit(1000); // Transaction 1

            // When - Tentative de retrait avec fonds insuffisants
            try {
                accountService.withdraw(2000);
            } catch (InsufficientFundsException e) {
                // Exception attendue
            }

            // Then - Vérifier qu'il n'y a toujours qu'une seule transaction
            accountService.printStatement();
            String output = outputStream.toString();
            String[] lines = output.split("\\r?\\n");
            assertThat(lines).hasSize(2); // Header + 1 transaction valide seulement

            // Vérifier que le solde n'a pas changé
            assertThat(output).contains("|| 1000 || 1000");
        }

        @Test
        @DisplayName("Should preserve balance after failed withdrawal")
        void shouldPreserveBalanceAfterFailedWithdrawal() {
            // Given
            accountService.deposit(1000);
            accountService.deposit(500);

            // When - Tentative de retrait invalide
            try {
                accountService.withdraw(2000);
            } catch (InsufficientFundsException e) {
                // Exception attendue
            }

            // Then - Le solde doit rester à 1500
            accountService.printStatement();
            String output = outputStream.toString();

            // La dernière ligne doit montrer le solde de 1500
            String[] lines = output.split("\\r?\\n");
            assertThat(lines[1]).contains("|| 1500"); // Dernière transaction
        }
    }

    // ========== TESTS DES CAS D'ERREUR - SCÉNARIOS COMPLEXES ==========

    @Nested
    @DisplayName("Complex Error Scenarios")
    class ComplexErrorScenarios {

        @Test
        @DisplayName("Should handle multiple failed transactions")
        void shouldHandleMultipleFailedTransactions() {
            // Given
            accountService.deposit(1000);

            // When - Plusieurs tentatives échouées
            assertThatThrownBy(() -> accountService.withdraw(2000))
                    .isInstanceOf(InsufficientFundsException.class);

            assertThatThrownBy(() -> accountService.deposit(-500))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> accountService.withdraw(1500))
                    .isInstanceOf(InsufficientFundsException.class);

            // Then - Une seule transaction valide
            accountService.printStatement();
            String output = outputStream.toString();
            String[] lines = output.split("\\r?\\n");  // ✅ CORRECTION

            System.setOut(originalOut);
            System.out.println("=== DEBUG Multiple Failed Transactions ===");
            System.out.println("Output: " + output);


            assertThat(lines).hasSize(2); // Header + 1 transaction
            assertThat(output).contains("|| 1000 || 1000");
        }

        @Test
        @DisplayName("Should handle alternating valid and invalid transactions")
        void shouldHandleAlternatingValidAndInvalidTransactions() {
            // Given & When
            accountService.deposit(1000);

            try {
                accountService.withdraw(-100);
            } catch (IllegalArgumentException ignored) {
            }

            accountService.deposit(500);

            try {
                accountService.withdraw(2000);
            } catch (InsufficientFundsException ignored) {
            }

            accountService.withdraw(300);

            // Then - 3 transactions valides
            accountService.printStatement();
            String output = outputStream.toString();
            String[] lines = output.split("\\r?\\n");
            assertThat(lines).hasSize(4); // Header + 3 transactions

            // Vérifier le solde final:  1000 + 500 - 300 = 1200
            assertThat(output).contains("|| 1200");
        }

        @Test
        @DisplayName("Should handle withdrawal at exact balance limit")
        void shouldHandleWithdrawalAtExactBalanceLimit() {
            // Given
            accountService.deposit(1000);

            // When - Retrait exact du solde (devrait réussir)
            assertThatCode(() -> accountService.withdraw(1000))
                    .doesNotThrowAnyException();

            // Then - Solde doit être 0
            accountService.printStatement();
            String output = outputStream.toString();
            assertThat(output).contains("|| 0");
        }

        @Test
        @DisplayName("Should not allow withdrawal after balance reaches zero")
        void shouldNotAllowWithdrawalAfterBalanceReachesZero() {
            // Given
            accountService.deposit(1000);
            accountService.withdraw(1000); // Solde = 0

            // When & Then
            assertThatThrownBy(() -> accountService.withdraw(1))
                    .isInstanceOf(InsufficientFundsException.class)
                    .hasMessageContaining("Balance: 0")
                    .hasMessageContaining("requested: 1");
        }

        @Test
        @DisplayName("Should handle consecutive failed withdrawals")
        void shouldHandleConsecutiveFailedWithdrawals() {
            // Given
            accountService.deposit(500);

            // When & Then - Plusieurs retraits échoués consécutifs
            assertThatThrownBy(() -> accountService.withdraw(600))
                    .isInstanceOf(InsufficientFundsException.class);

            assertThatThrownBy(() -> accountService.withdraw(1000))
                    .isInstanceOf(InsufficientFundsException.class);

            assertThatThrownBy(() -> accountService.withdraw(501))
                    .isInstanceOf(InsufficientFundsException.class);

            // Then - Le solde doit rester inchangé
            accountService.printStatement();
            String output = outputStream.toString();
            assertThat(output).contains("|| 500 || 500");
        }

        @Test
        @DisplayName("Should handle mix of all error types")
        void shouldHandleMixOfAllErrorTypes() {
            // Given
            accountService.deposit(1000);

            // When - Mix d'erreurs
            try {
                accountService.deposit(0);
            } catch (Exception ignored) {
            }
            try {
                accountService.deposit(-100);
            } catch (Exception ignored) {
            }
            try {
                accountService.withdraw(0);
            } catch (Exception ignored) {
            }
            try {
                accountService.withdraw(-50);
            } catch (Exception ignored) {
            }
            try {
                accountService.withdraw(2000);
            } catch (Exception ignored) {
            }

            // Then - Une seule transaction valide
            accountService.printStatement();
            String output = outputStream.toString();
            String[] lines = output.split("\\r?\\n");
            assertThat(lines).hasSize(2); // Header + 1 transaction
            assertThat(output).contains("|| 1000 || 1000");
        }
    }

    // ========== TEST DE SCÉNARIO RÉALISTE AVEC ERREURS ==========

    @Nested
    @DisplayName("Realistic Scenario with Errors")
    class RealisticScenarioWithErrors {

        @Test
        @DisplayName("Should handle realistic monthly scenario with errors")
        void shouldHandleRealisticMonthlyScenarioWithErrors() {
            // Given
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(out);
            AccountServiceImpl service = new AccountServiceImpl(ps);

            // Scénario: Salarié avec tentatives d'erreurs

            // Jour 1 - Salaire
            Clock clock1 = Clock.fixed(
                    LocalDate.parse("2025-01-01").atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault()
            );
            service.setClock(clock1);
            service.deposit(3500); // Salaire OK

            // Jour 2 - Tentative de retrait excessif (ERREUR)
            Clock clock2 = Clock.fixed(
                    LocalDate.parse("2025-01-02").atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault()
            );
            service.setClock(clock2);
            assertThatThrownBy(() -> service.withdraw(5000))
                    .isInstanceOf(InsufficientFundsException.class);

            // Jour 5 - Loyer OK
            Clock clock3 = Clock.fixed(
                    LocalDate.parse("2025-01-05").atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault()
            );
            service.setClock(clock3);
            service.withdraw(800);

            // Jour 10 - Tentative de dépôt négatif (ERREUR)
            Clock clock4 = Clock.fixed(
                    LocalDate.parse("2025-01-10").atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault()
            );
            service.setClock(clock4);
            assertThatThrownBy(() -> service.deposit(-100))
                    .isInstanceOf(IllegalArgumentException.class);

            // Jour 15 - Courses OK
            Clock clock5 = Clock.fixed(
                    LocalDate.parse("2025-01-15").atStartOfDay(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault()
            );
            service.setClock(clock5);
            service.withdraw(200);

            // When
            service.printStatement();

            // Then
            String output = out.toString();

            System.setOut(originalOut);
            System.out.println("=== SCÉNARIO AVEC ERREURS ===");
            System.out.println(output);

            // Vérifications:  seulement 3 transactions valides
            String[] lines = output.split("\\r?\\n");
            assertThat(lines).hasSize(4); // Header + 3 transactions

            // Solde final: 3500 - 800 - 200 = 2500
            assertThat(output).contains("|| 2500");

            // Vérifier que les transactions échouées ne sont pas présentes
            assertThat(output).doesNotContain("5000");
            assertThat(output).doesNotContain("-100");
        }
    }

    // ========== TEST D'ACCEPTATION ==========

    @Nested
    @DisplayName("Acceptance Test")
    class AcceptanceTest {

        @Test
        @DisplayName("Should match the exact specification from PDF")
        void shouldMatchSpecification() {
            // Given
            Clock clock1 = Clock.fixed(Instant.parse("2012-01-10T10:00:00Z"), ZoneId.systemDefault());
            Clock clock2 = Clock.fixed(Instant.parse("2012-01-13T10:00:00Z"), ZoneId.systemDefault());
            Clock clock3 = Clock.fixed(Instant.parse("2012-01-14T10:00:00Z"), ZoneId.systemDefault());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(out);

            AccountServiceImpl service = new AccountServiceImpl(ps, clock1);

            // When
            service.setClock(clock1);
            service.deposit(1000);

            service.setClock(clock2);
            service.deposit(2000);

            service.setClock(clock3);
            service.withdraw(500);

            service.printStatement();

            // Then
            String output = out.toString();

            System.setOut(originalOut);
            System.out.println("=== SPECIFICATION OUTPUT ===");
            System.out.println(output);

            assertThat(output).contains("Date || Amount || Balance");
            assertThat(output).contains("14/01/2012 || -500 || 2500");
            assertThat(output).contains("13/01/2012 || 2000 || 3000");
            assertThat(output).contains("10/01/2012 || 1000 || 1000");
        }
    }
}