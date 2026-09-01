package com.rahul.sdet.specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class ApiSpecs {
    private ApiSpecs() {
    }

    public static RequestSpecification bankingApi(int port) {
        return new RequestSpecBuilder()
            .setPort(port)
            .setBasePath("/api/v1")
            .setContentType(ContentType.JSON)
            .build();
    }
}
