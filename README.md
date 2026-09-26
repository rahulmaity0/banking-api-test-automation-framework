# Payments API Test Automation Framework

[![CI](https://github.com/rahulmaity0/banking-api-test-automation-framework/actions/workflows/ci.yml/badge.svg)](https://github.com/rahulmaity0/banking-api-test-automation-framework/actions/workflows/ci.yml)

A small Spring Boot payments API and a layered REST Assured test framework built against it. I wrote both sides, as the developer of the service and as the tester of it.

## System under test

| Method | Path | Behaviour |
|---|---|---|
| GET | `/api/v1/health` | Liveness check |
| GET | `/api/v1/accounts/{id}` | Account and balance |
| POST | `/api/v1/transfers` | Move money between two accounts |
| POST | `/api/v1/payments` | Pay a merchant |

Business rules the tests pin down:

- **Idempotency.** Transfers and payments require an `Idempotency-Key` header. Replaying a key returns the original response and never charges twice.
- **Insufficient funds.** Transfers and payments are rejected, and no balance changes.
- **Fraud review.** A single payment over 2,000 is refused with `FRAUD_REVIEW`, even when the account could afford it.
- **Validation.** Transfers to the same account and malformed requests are rejected.

## Framework layout

```
src/test/java/com/rahul/sdet
├── client/        BankingApiClient – every HTTP call lives here
├── specs/         ApiSpecs – shared request/response specifications
├── models/        TransferRequest, PaymentRequest – request bodies
├── dataprovider/  BankingTestData – one place for test data
├── utils/         TestKeys – unique idempotency keys per test
└── tests/         BankingApiTestBase + Health, Transfer and Payment tests
```

The test classes contain only scenarios and assertions; the HTTP plumbing sits in the client and the specs. Response contracts are checked with JSON Schema (`src/test/resources/schemas`).

Tests boot the real application on a random port (`@SpringBootTest(webEnvironment = RANDOM_PORT)`) rather than mocking the web layer.

## Tech stack

Java 17, Spring Boot, REST Assured, JUnit 5, JSON Schema Validator, Allure, GitHub Actions

## Running

```bash
mvn clean test            # run the suite
mvn allure:serve          # open the Allure report
```

CI runs the suite on every push and uploads the Allure results as a build artifact.
