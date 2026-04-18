package com.example.sensys.model;

import java.io.Serializable;

public class SensorSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int sensorType;
    private final String sensorName;
    private final String sensorTypeLabel;
    private final String sensorCategoryLabel;
    private final String status;
    private final String valueSummary;
    private final String accuracyLabel;

    public SensorSnapshot(
            int sensorType,
            String sensorName,
            String sensorTypeLabel,
            String sensorCategoryLabel,
            String status,
            String valueSummary,
            String accuracyLabel
    ) {
        this.sensorType = sensorType;
        this.sensorName = sensorName;
        this.sensorTypeLabel = sensorTypeLabel;
        this.sensorCategoryLabel = sensorCategoryLabel;
        this.status = status;
        this.valueSummary = valueSummary;
        this.accuracyLabel = accuracyLabel;
    }

    public int getSensorType() {
        return sensorType;
    }

    public String getSensorName() {
        return sensorName;
    }

    public String getSensorTypeLabel() {
        return sensorTypeLabel;
    }

    public String getSensorCategoryLabel() {
        return sensorCategoryLabel;
    }

    public String getStatus() {
        return status;
    }

    public String getValueSummary() {
        return valueSummary;
    }

    public String getAccuracyLabel() {
        return accuracyLabel;
    }
}
