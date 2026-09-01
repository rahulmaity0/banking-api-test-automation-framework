package com.rahul.sdet.tests;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.rahul.sdet.client.BankingApiClient;
import com.rahul.sdet.specs.ApiSpecs;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BankingApiTestBase {
    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate rest;

    protected BankingApiClient bankingApi;

    @BeforeEach
    void createApiClient() {
        bankingApi = new BankingApiClient(ApiSpecs.bankingApi(port));
    }
}
