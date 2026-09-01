package com.rahul.sdet.tests;

import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Epic("Banking API")
@Feature("Service health")
class HealthApiTest extends BankingApiTestBase {
    @Test
    @Story("Service health")
    @Description("The API exposes a machine-readable health check")
    void healthIsUp() {
        bankingApi.health().then().statusCode(200).body("status", equalTo("UP"));
    }
}
