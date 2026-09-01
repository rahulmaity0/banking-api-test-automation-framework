package com.rahul.sdet;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests for the banking API.
 *
 * webEnvironment = RANDOM_PORT starts a REAL Tomcat server on a free port,
 * and the tests send REAL HTTP requests to it using REST Assured. That is the
 * difference from MockMvc, which fakes the request inside Spring without ever
 * opening a network port.
 *
 * A random port is used so the tests never clash with something already
 * running on 8080.
 *
 * The @Epic / @Feature / @Story annotations are Allure's. They do not affect
 * whether a test passes - they group the tests in the generated report.
 */
@Epic("Banking API")
@Feature("Money movement")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankingApiTest {

    /** Spring fills this in with whichever port Tomcat actually got. */
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    /**
     * Runs before every test.
     *
     * REST Assured needs telling where to send requests. Setting basePath
     * here means each test can just write "/health" instead of the full
     * "http://localhost:54321/api/v1/health".
     */
    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
    }

    /**
     * Every REST Assured test reads as given / when / then:
     *
     *   given()  - set up the request (headers, body)
     *   when()   - send it
     *   then()   - assert on the response
     */
    @Test
    @Story("Service health")
    @Description("The API exposes a machine-readable health check")
    void healthIsUp() {

        given()
        .when()
                .get("/health")
        .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    @Story("Transfer")
    @Description("A valid transfer debits one account and credits another")
    void transferCompletesAndMatchesContract() {

        float amount =
                given()
                        .header("Idempotency-Key", "transfer-001")
                        .contentType("application/json")
                        .body(Map.of(
                                "fromAccount", "ACC-1001",
                                "toAccount", "ACC-2002",
                                "amount", 250.00))
                .when()
                        .post("/transfers")
                .then()
                        .statusCode(201)
                        .body("status", equalTo("COMPLETED"))
                        // extract() pulls a value out of the response so we can
                        // assert on it with plain JUnit below.
                        .extract().path("amount");

        Assertions.assertEquals(250.0f, amount, 0.001f);
    }

    /**
     * A negative test: ACC-2002 only holds 1000, so moving 5000 must fail.
     *
     * Note we assert on the error CODE, not the message. Codes are part of the
     * API contract; messages are wording that might be reworded tomorrow.
     */
    @Test
    @Story("Negative testing")
    void transferRejectsInsufficientFunds() {

        given()
                .header("Idempotency-Key", "transfer-002")
                .contentType("application/json")
                .body(Map.of(
                        "fromAccount", "ACC-2002",
                        "toAccount", "ACC-1001",
                        "amount", 5000.00))
        .when()
                .post("/transfers")
        .then()
                .statusCode(400)
                .body("error", equalTo("INSUFFICIENT_FUNDS"));
    }

    /**
     * The idempotency test - the most interesting one here.
     *
     * The same payment is sent twice with the same Idempotency-Key. Both
     * requests must come back with the SAME paymentId, which proves the second
     * one returned the stored result rather than charging the account again.
     */
    @Test
    @Story("Idempotency")
    void repeatedPaymentKeyDoesNotChargeTwice() {

        String body = "{\"accountId\":\"ACC-1001\",\"merchant\":\"BookStore\",\"amount\":25.00}";

        String first =
                given()
                        .header("Idempotency-Key", "payment-001")
                        .contentType("application/json")
                        .body(body)
                .when()
                        .post("/payments")
                .then()
                        .statusCode(201)
                        .extract().path("paymentId");

        String second =
                given()
                        .header("Idempotency-Key", "payment-001")
                        .contentType("application/json")
                        .body(body)
                .when()
                        .post("/payments")
                .then()
                        .statusCode(201)
                        .extract().path("paymentId");

        Assertions.assertEquals(first, second);
    }

    /** Anything over 2000 in one payment is refused, even with funds available. */
    @Test
    @Story("Fraud rules")
    void highValuePaymentGoesToFraudReview() {

        given()
                .header("Idempotency-Key", "payment-002")
                .contentType("application/json")
                .body(Map.of(
                        "accountId", "ACC-1001",
                        "merchant", "LuxuryStore",
                        "amount", 2500.00))
        .when()
                .post("/payments")
        .then()
                .statusCode(400)
                .body("error", equalTo("FRAUD_REVIEW"));
    }
}
