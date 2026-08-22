package com.rahul.sdet;

import io.qameta.allure.*;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.math.BigDecimal;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("Banking API") @Feature("Money movement")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankingApiTest {
    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @BeforeEach void setup() { RestAssured.port = port; RestAssured.basePath = "/api/v1"; }

    @Test @Story("Service health") @Description("The API exposes a machine-readable health check")
    void healthIsUp() { given().when().get("/health").then().statusCode(200).body("status", equalTo("UP")); }

    @Test @Story("Transfer") @Description("A valid transfer debits one account and credits another")
    void transferCompletesAndMatchesContract() {
        float amount = given().header("Idempotency-Key", "transfer-001").contentType("application/json")
            .body(Map.of("fromAccount","ACC-1001","toAccount","ACC-2002","amount",250.00))
            .when().post("/transfers").then().statusCode(201).body("status", equalTo("COMPLETED"))
            .extract().path("amount");
        Assertions.assertEquals(250.0f, amount, 0.001f);
    }

    @Test @Story("Negative testing")
    void transferRejectsInsufficientFunds() {
        given().header("Idempotency-Key", "transfer-002").contentType("application/json")
            .body(Map.of("fromAccount","ACC-2002","toAccount","ACC-1001","amount",5000.00))
            .when().post("/transfers").then().statusCode(400).body("error", equalTo("INSUFFICIENT_FUNDS"));
    }

    @Test @Story("Idempotency")
    void repeatedPaymentKeyDoesNotChargeTwice() {
        String body = "{\"accountId\":\"ACC-1001\",\"merchant\":\"BookStore\",\"amount\":25.00}";
        String first = given().header("Idempotency-Key","payment-001").contentType("application/json").body(body).post("/payments").then().statusCode(201).extract().path("paymentId");
        String second = given().header("Idempotency-Key","payment-001").contentType("application/json").body(body).post("/payments").then().statusCode(201).extract().path("paymentId");
        Assertions.assertEquals(first, second);
    }

    @Test @Story("Fraud rules")
    void highValuePaymentGoesToFraudReview() {
        given().header("Idempotency-Key","payment-002").contentType("application/json")
            .body(Map.of("accountId","ACC-1001","merchant","LuxuryStore","amount",2500.00))
            .when().post("/payments").then().statusCode(400).body("error", equalTo("FRAUD_REVIEW"));
    }
}
