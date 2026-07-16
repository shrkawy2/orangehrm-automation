package com.orangehrm.utils;

import java.time.Instant;

public final class TestDataGenerator {

    private TestDataGenerator() {
    }

    public static String uniqueUsername() {
        return "qa_auto_" + Instant.now().toEpochMilli();
    }

    public static String strongPassword() {
        return "Automation@123";
    }
}
