package com.rahul.sdet.domain;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All the banking rules.
 *
 * There is no database here. Accounts live in a Map in memory, so everything
 * resets whenever the application restarts. That is fine for a demo, and it is
 * why the tests can rely on ACC-1001 always starting with 5000.
 */
@Service
public class BankingService {

    /** accountId -> the account. Two accounts are created at startup. */
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    /**
     * Idempotency-Key -> the response we already sent for that key.
     *
     * This is what stops a repeated request from moving money twice.
     */
    private final Map<String, Object> idempotentResponses = new ConcurrentHashMap<>();

    public BankingService() {
        accounts.put("ACC-1001", new Account("ACC-1001", new BigDecimal("5000.00")));
        accounts.put("ACC-2002", new Account("ACC-2002", new BigDecimal("1000.00")));
    }

    /** Reads one account's balance. */
    public AccountView account(String id) {

        Account account = get(id);

        return new AccountView(account.id, account.balance);
    }

    /**
     * Moves money from one account to another.
     *
     * "synchronized" means only one thread can be inside this method at a
     * time. Without it, two simultaneous transfers could both read a balance
     * of 5000 and both decide there was enough money.
     */
    public synchronized TransferView transfer(String key, String from, String to, BigDecimal amount) {

        // Step 1: have we already handled this key? If so, return the same
        // answer as last time and move no money.
        if (idempotentResponses.containsKey(key)) {
            return (TransferView) idempotentResponses.get(key);
        }

        // Step 2: find both accounts and check the rules.
        Account source = get(from);
        Account destination = get(to);

        validateDifferent(source, destination);
        validateFunds(source, amount);

        // Step 3: move the money.
        source.balance = source.balance.subtract(amount);
        destination.balance = destination.balance.add(amount);

        // Step 4: build the response and remember it against this key.
        TransferView result = new TransferView(
                "TRX-" + key,
                "COMPLETED",
                source.id,
                destination.id,
                amount);

        idempotentResponses.put(key, result);

        return result;
    }

    /** Pays a merchant from an account. */
    public synchronized PaymentView payment(String key, String accountId, String merchant, BigDecimal amount) {

        // Same idempotency check as transfer.
        if (idempotentResponses.containsKey(key)) {
            return (PaymentView) idempotentResponses.get(key);
        }

        Account account = get(accountId);

        // The fraud rule: anything over 2000 in a single payment is refused.
        // Note this is checked BEFORE the funds check, so a large payment is
        // flagged for fraud even when the account could afford it.
        BigDecimal fraudLimit = new BigDecimal("2000.00");

        if (amount.compareTo(fraudLimit) > 0) {
            throw new ApiException("FRAUD_REVIEW", "Payment exceeds the single-payment risk limit");
        }

        validateFunds(account, amount);

        account.balance = account.balance.subtract(amount);

        PaymentView result = new PaymentView(
                "PAY-" + key,
                "APPROVED",
                account.id,
                merchant,
                amount);

        idempotentResponses.put(key, result);

        return result;
    }

    // ------------------------------------------------------------------
    // Rules
    // ------------------------------------------------------------------

    /** Finds an account, or fails with ACCOUNT_NOT_FOUND. */
    private Account get(String id) {

        Account account = accounts.get(id);

        if (account == null) {
            throw new ApiException("ACCOUNT_NOT_FOUND", "Unknown account: " + id);
        }

        return account;
    }

    /** The amount must be positive, and the account must be able to cover it. */
    private void validateFunds(Account account, BigDecimal amount) {

        if (amount.signum() <= 0) {
            throw new ApiException("INVALID_AMOUNT", "Amount must be positive");
        }

        // compareTo returns a negative number when balance is less than amount.
        if (account.balance.compareTo(amount) < 0) {
            throw new ApiException("INSUFFICIENT_FUNDS", "Insufficient funds");
        }
    }

    /** You cannot transfer money to the same account you took it from. */
    private void validateDifferent(Account source, Account destination) {

        if (source.id.equals(destination.id)) {
            throw new ApiException("SAME_ACCOUNT", "Source and destination must differ");
        }
    }

    // ------------------------------------------------------------------
    // Data holders
    // ------------------------------------------------------------------

    /**
     * The internal account. Kept private so nothing outside this class can
     * change a balance directly - all changes go through the methods above.
     *
     * BigDecimal is used instead of double because double loses precision on
     * decimal fractions, which is unacceptable for money.
     */
    private static class Account {

        String id;
        BigDecimal balance;

        Account(String id, BigDecimal balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    /** What the API sends back for an account lookup. */
    public record AccountView(String accountId, BigDecimal balance) {
    }

    /** What the API sends back for a completed transfer. */
    public record TransferView(
            String transactionId,
            String status,
            String fromAccount,
            String toAccount,
            BigDecimal amount) {
    }

    /** What the API sends back for an approved payment. */
    public record PaymentView(
            String paymentId,
            String status,
            String accountId,
            String merchant,
            BigDecimal amount) {
    }

    /**
     * A business rule failure.
     *
     * The "code" is a fixed string like INSUFFICIENT_FUNDS. The controller
     * puts it in the response, and the tests assert on it - which is more
     * reliable than matching on a human-readable message that might change.
     */
    public static class ApiException extends RuntimeException {

        private final String code;

        public ApiException(String code, String message) {
            super(message);
            this.code = code;
        }

        public String code() {
            return code;
        }
    }
}
