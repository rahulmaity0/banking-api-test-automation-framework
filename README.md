# Banking API Test Automation Framework

This is a portfolio SDET project: a small banking API is the **system under test**, and the test suite behaves like a real QA automation repository. It covers account lookup, transfers, payments, insufficient funds, fraud review, response contracts, and idempotency.

## Run it

```text
mvn clean test
```

The tests start the API on a random local port, send real HTTP requests with REST Assured, and produce JUnit/Allure results under `target/`.

## Why this is an SDET project

Software Development Engineer in Test means building software that tests software. QA is the wider discipline: understand risk, design checks, report defects, and decide whether a release is safe. Automation is one tool inside QA; SDET work adds programming, API knowledge, test architecture, CI, diagnostics, and maintainability.

The request flow is:

`test -> HTTP request -> controller -> banking rules -> response -> assertion/report`

The test suite is intentionally layered: happy-path tests prove normal business behavior; negative tests prove safe rejection; idempotency tests protect against duplicate money movement; contract assertions protect response shape; CI runs the suite on every change.

## Interview talking points

- Why test APIs? They are faster and less brittle than testing only through a browser.
- Why negative tests? Financial systems fail safely: no overdraft, no same-account transfer, no high-risk payment.
- Why idempotency? A retried network request must not charge a customer twice.
- Why CI? A green build gives the team immediate feedback before release.
- What would be next? Replace the in-memory store with PostgreSQL, add Testcontainers, persist transaction history, add Docker Compose, and add a small UI smoke suite.

## Resume bullet

Built a Java/Spring Boot banking API automation framework using REST Assured, JUnit 5, Allure, and GitHub Actions; automated transfer/payment workflows with contract, negative, fraud-rule, and idempotency coverage and published test artifacts in CI.
