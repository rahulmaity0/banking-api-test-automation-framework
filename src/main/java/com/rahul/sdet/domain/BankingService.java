package com.rahul.sdet.domain;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BankingService {
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, Object> idempotentResponses = new ConcurrentHashMap<>();
    public BankingService() { accounts.put("ACC-1001", new Account("ACC-1001", new BigDecimal("5000.00"))); accounts.put("ACC-2002", new Account("ACC-2002", new BigDecimal("1000.00"))); }
    public AccountView account(String id) { Account a = get(id); return new AccountView(a.id, a.balance); }
    public synchronized TransferView transfer(String key, String from, String to, BigDecimal amount) {
        if (idempotentResponses.containsKey(key)) return (TransferView) idempotentResponses.get(key);
        Account source = get(from), destination = get(to); validateDifferent(source, destination); validateFunds(source, amount);
        source.balance = source.balance.subtract(amount); destination.balance = destination.balance.add(amount);
        TransferView result = new TransferView("TRX-" + key, "COMPLETED", source.id, destination.id, amount);
        idempotentResponses.put(key, result); return result;
    }
    public synchronized PaymentView payment(String key, String accountId, String merchant, BigDecimal amount) {
        if (idempotentResponses.containsKey(key)) return (PaymentView) idempotentResponses.get(key);
        Account account = get(accountId);
        if (amount.compareTo(new BigDecimal("2000.00")) > 0) throw new ApiException("FRAUD_REVIEW", "Payment exceeds the single-payment risk limit");
        validateFunds(account, amount); account.balance = account.balance.subtract(amount);
        PaymentView result = new PaymentView("PAY-" + key, "APPROVED", account.id, merchant, amount);
        idempotentResponses.put(key, result); return result;
    }
    private Account get(String id) { Account a = accounts.get(id); if (a == null) throw new ApiException("ACCOUNT_NOT_FOUND", "Unknown account: " + id); return a; }
    private void validateFunds(Account a, BigDecimal amount) { if (amount.signum() <= 0) throw new ApiException("INVALID_AMOUNT", "Amount must be positive"); if (a.balance.compareTo(amount) < 0) throw new ApiException("INSUFFICIENT_FUNDS", "Insufficient funds"); }
    private void validateDifferent(Account a, Account b) { if (a.id.equals(b.id)) throw new ApiException("SAME_ACCOUNT", "Source and destination must differ"); }
    private static class Account { String id; BigDecimal balance; Account(String id, BigDecimal balance) { this.id=id; this.balance=balance; } }
    public record AccountView(String accountId, BigDecimal balance) {}
    public record TransferView(String transactionId, String status, String fromAccount, String toAccount, BigDecimal amount) {}
    public record PaymentView(String paymentId, String status, String accountId, String merchant, BigDecimal amount) {}
    public static class ApiException extends RuntimeException { private final String code; public ApiException(String code, String message) { super(message); this.code=code; } public String code(){return code;} }
}
