package com.example.sensys.model;

import java.io.Serializable;

public class StorageInfoData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long totalBytes;
    private final long freeBytes;
    private final long usedBytes;
    private final String healthStatus;

    public StorageInfoData(long totalBytes, long freeBytes, long usedBytes, String healthStatus) {
        this.totalBytes = totalBytes;
        this.freeBytes = freeBytes;
        this.usedBytes = usedBytes;
        this.healthStatus = healthStatus;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getFreeBytes() {
        return freeBytes;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public String getHealthStatus() {
        return healthStatus;
    }
}
