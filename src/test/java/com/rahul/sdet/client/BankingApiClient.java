package com.rahul.sdet.client;

import com.rahul.sdet.models.PaymentRequest;
import com.rahul.sdet.models.TransferRequest;

import static io.restassured.RestAssured.given;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class BankingApiClient {
    private final RequestSpecification specification;

    public BankingApiClient(RequestSpecification specification) {
        this.specification = specification;
    }

    public Response health() {
        return given(specification).when().get("/health");
    }

    public Response transfer(String idempotencyKey, TransferRequest request) {
        return given(specification)
            .header("Idempotency-Key", idempotencyKey)
            .body(request)
            .when().post("/transfers");
    }

    public Response payment(String idempotencyKey, PaymentRequest request) {
        return given(specification)
            .header("Idempotency-Key", idempotencyKey)
            .body(request)
            .when().post("/payments");
    }
}
