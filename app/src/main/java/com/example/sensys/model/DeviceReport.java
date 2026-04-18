package com.example.sensys.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String generatedAt;
    private final SystemSnapshot systemSnapshot;
    private final List<SensorSnapshot> sensorSnapshots;
    private final List<TestResult> testResults;
    private final String overallStatus;

    public DeviceReport(
            String generatedAt,
            SystemSnapshot systemSnapshot,
            List<SensorSnapshot> sensorSnapshots,
            List<TestResult> testResults,
            String overallStatus
    ) {
        this.generatedAt = generatedAt;
        this.systemSnapshot = systemSnapshot;
        this.sensorSnapshots = Collections.unmodifiableList(new ArrayList<>(sensorSnapshots));
        this.testResults = Collections.unmodifiableList(new ArrayList<>(testResults));
        this.overallStatus = overallStatus;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public SystemSnapshot getSystemSnapshot() {
        return systemSnapshot;
    }

    public List<SensorSnapshot> getSensorSnapshots() {
        return sensorSnapshots;
    }

    public List<TestResult> getTestResults() {
        return testResults;
    }

    public String getOverallStatus() {
        return overallStatus;
    }
}
