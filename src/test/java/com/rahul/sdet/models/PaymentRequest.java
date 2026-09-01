package com.rahul.sdet.models;

import java.math.BigDecimal;

public record PaymentRequest(String accountId, String merchant, BigDecimal amount) {
}
