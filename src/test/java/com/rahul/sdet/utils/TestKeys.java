package com.rahul.sdet.utils;

import java.util.concurrent.atomic.AtomicInteger;

public final class TestKeys {
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private TestKeys() {
    }

    public static String next(String prefix) {
        return prefix + "-" + SEQUENCE.incrementAndGet();
    }
}
