package com.rahul.sdet.api;

import com.rahul.sdet.domain.BankingService;
import com.rahul.sdet.domain.BankingService.ApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class BankingController {
    private final BankingService service;
    public BankingController(BankingService service) { this.service = service; }

    @GetMapping("/health") public Map<String, String> health() { return Map.of("status", "UP", "service", "banking-api"); }
    @GetMapping("/accounts/{id}") public BankingService.AccountView account(@PathVariable String id) { return service.account(id); }
    @PostMapping("/transfers") @ResponseStatus(HttpStatus.CREATED)
    public BankingService.TransferView transfer(@RequestHeader("Idempotency-Key") String key, @Valid @RequestBody TransferRequest request) { return service.transfer(key, request.fromAccount(), request.toAccount(), request.amount()); }
    @PostMapping("/payments") @ResponseStatus(HttpStatus.CREATED)
    public BankingService.PaymentView payment(@RequestHeader("Idempotency-Key") String key, @Valid @RequestBody PaymentRequest request) { return service.payment(key, request.accountId(), request.merchant(), request.amount()); }

    public record TransferRequest(@NotBlank String fromAccount, @NotBlank String toAccount, @NotNull @DecimalMin("0.01") BigDecimal amount) {}
    public record PaymentRequest(@NotBlank String accountId, @NotBlank String merchant, @NotNull @DecimalMin("0.01") BigDecimal amount) {}

    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> apiError(ApiException e) { return Map.of("error", e.code(), "message", e.getMessage()); }
}
