package com.rahul.sdet.tests;

import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;

import com.rahul.sdet.dataprovider.BankingTestData;
import com.rahul.sdet.utils.TestKeys;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Epic("Banking API")
@Feature("Transfers")
class TransferApiTest extends BankingApiTestBase {
    @Test
    @Story("Transfer")
    void transferCompletesAndMatchesContract() {
        bankingApi.transfer(TestKeys.next("transfer"), BankingTestData.validTransfer())
            .then().statusCode(201)
            .body("status", equalTo("COMPLETED"))
            .body("amount", equalTo(250.0f));
    }

    @Test
    @Story("Negative testing")
    void transferRejectsInsufficientFunds() {
        bankingApi.transfer(TestKeys.next("transfer"), BankingTestData.insufficientFundsTransfer())
            .then().statusCode(400)
            .body("error", equalTo("INSUFFICIENT_FUNDS"));
    }
}
