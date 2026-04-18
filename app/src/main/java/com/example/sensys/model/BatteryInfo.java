package com.example.sensys.model;

import java.io.Serializable;

public class BatteryInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int levelPercent;
    private final String chargingStatus;
    private final String healthStatus;
    private final float temperatureCelsius;

    public BatteryInfo(int levelPercent, String chargingStatus, String healthStatus, float temperatureCelsius) {
        this.levelPercent = levelPercent;
        this.chargingStatus = chargingStatus;
        this.healthStatus = healthStatus;
        this.temperatureCelsius = temperatureCelsius;
    }

    public int getLevelPercent() {
        return levelPercent;
    }

    public String getChargingStatus() {
        return chargingStatus;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public float getTemperatureCelsius() {
        return temperatureCelsius;
    }
}
