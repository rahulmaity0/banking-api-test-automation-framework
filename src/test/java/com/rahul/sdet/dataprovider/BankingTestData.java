package com.rahul.sdet.dataprovider;

import java.math.BigDecimal;

import com.rahul.sdet.models.PaymentRequest;
import com.rahul.sdet.models.TransferRequest;

public final class BankingTestData {
    private BankingTestData() {
    }

    public static TransferRequest validTransfer() {
        return new TransferRequest("ACC-1001", "ACC-2002", new BigDecimal("250.00"));
    }

    public static TransferRequest insufficientFundsTransfer() {
        return new TransferRequest("ACC-2002", "ACC-1001", new BigDecimal("5000.00"));
    }

    public static PaymentRequest repeatablePayment() {
        return new PaymentRequest("ACC-1001", "BookStore", new BigDecimal("25.00"));
    }

    public static PaymentRequest highValuePayment() {
        return new PaymentRequest("ACC-1001", "LuxuryStore", new BigDecimal("2500.00"));
    }
}
