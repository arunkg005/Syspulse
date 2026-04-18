package com.example.sensys.model;

import java.io.Serializable;

public class MemoryInfoData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long totalBytes;
    private final long availableBytes;
    private final long usedBytes;
    private final String healthStatus;

    public MemoryInfoData(long totalBytes, long availableBytes, long usedBytes, String healthStatus) {
        this.totalBytes = totalBytes;
        this.availableBytes = availableBytes;
        this.usedBytes = usedBytes;
        this.healthStatus = healthStatus;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getAvailableBytes() {
        return availableBytes;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public String getHealthStatus() {
        return healthStatus;
    }
}
