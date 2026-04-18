package com.example.sensys.model;

import java.io.Serializable;

public class TestResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_PASSED = "Passed";
    public static final String STATUS_ISSUE = "Issue";
    public static final String STATUS_SKIPPED = "Skipped";
    public static final String STATUS_PENDING = "Pending";

    private final String name;
    private final String status;
    private final String details;

    public TestResult(String name, String status, String details) {
        this.name = name;
        this.status = status;
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getDetails() {
        return details;
    }
}
