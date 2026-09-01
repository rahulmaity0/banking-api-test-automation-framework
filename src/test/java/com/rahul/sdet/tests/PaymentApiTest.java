package com.rahul.sdet.tests;

import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.rahul.sdet.dataprovider.BankingTestData;
import com.rahul.sdet.utils.TestKeys;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Epic("Banking API")
@Feature("Payments")
class PaymentApiTest extends BankingApiTestBase {
    @Test
    @Story("Idempotency")
    void repeatedPaymentKeyDoesNotChargeTwice() {
        String key = TestKeys.next("payment");
        String first = bankingApi.payment(key, BankingTestData.repeatablePayment())
            .then().statusCode(201).extract().path("paymentId");
        String second = bankingApi.payment(key, BankingTestData.repeatablePayment())
            .then().statusCode(201).extract().path("paymentId");

        Assertions.assertEquals(first, second);
    }

    @Test
    @Story("Fraud rules")
    void highValuePaymentGoesToFraudReview() {
        bankingApi.payment(TestKeys.next("payment"), BankingTestData.highValuePayment())
            .then().statusCode(400)
            .body("error", equalTo("FRAUD_REVIEW"));
    }
}
