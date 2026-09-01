package com.rahul.sdet.api;

import com.rahul.sdet.domain.BankingService;
import com.rahul.sdet.domain.BankingService.ApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * The four endpoints of the banking API.
 *
 * Everything sits under /api/v1, so fetching an account really looks like
 * GET /api/v1/accounts/ACC-1001
 */
@RestController
@RequestMapping("/api/v1")
public class BankingController {

    private final BankingService service;

    public BankingController(BankingService service) {
        this.service = service;
    }

    /** GET /api/v1/health - a simple "am I alive" check. */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "banking-api");
    }

    /** GET /api/v1/accounts/ACC-1001 - the balance of one account. */
    @GetMapping("/accounts/{id}")
    public BankingService.AccountView account(@PathVariable String id) {
        return service.account(id);
    }

    /**
     * POST /api/v1/transfers - move money between two accounts.
     *
     * The Idempotency-Key header is required. Sending the same key twice
     * returns the original result instead of moving the money again.
     */
    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public BankingService.TransferView transfer(
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody TransferRequest request) {

        return service.transfer(
                key,
                request.fromAccount(),
                request.toAccount(),
                request.amount());
    }

    /** POST /api/v1/payments - pay a merchant from an account. */
    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public BankingService.PaymentView payment(
            @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody PaymentRequest request) {

        return service.payment(
                key,
                request.accountId(),
                request.merchant(),
                request.amount());
    }

    /**
     * The JSON body of a transfer request.
     *
     * @DecimalMin("0.01") means the amount must be at least one paisa - zero
     * or negative is refused. Spring checks these rules before the method
     * above runs, because of the @Valid annotation on the parameter.
     */
    public record TransferRequest(
            @NotBlank String fromAccount,
            @NotBlank String toAccount,
            @NotNull @DecimalMin("0.01") BigDecimal amount) {
    }

    /** The JSON body of a payment request. */
    public record PaymentRequest(
            @NotBlank String accountId,
            @NotBlank String merchant,
            @NotNull @DecimalMin("0.01") BigDecimal amount) {
    }

    /**
     * Turns any ApiException thrown by the service into a 400 with this body:
     *
     *   { "error": "INSUFFICIENT_FUNDS", "message": "Insufficient funds" }
     *
     * The tests assert on that "error" field, which is why it matters that the
     * code is a fixed string rather than free-form text.
     */
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> apiError(ApiException e) {
        return Map.of("error", e.code(), "message", e.getMessage());
    }
}
