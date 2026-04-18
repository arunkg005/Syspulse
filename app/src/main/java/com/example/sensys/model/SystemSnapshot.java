package com.example.sensys.model;

import java.io.Serializable;

public class SystemSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String manufacturer;
    private final String model;
    private final String deviceCode;
    private final String board;
    private final String hardware;
    private final String primaryCpuAbi;
    private final String androidVersion;
    private final int apiLevel;
    private final String securityPatch;
    private final BatteryInfo batteryInfo;
    private final MemoryInfoData memoryInfo;
    private final StorageInfoData storageInfo;
    private final NetworkInfoData networkInfo;

    public SystemSnapshot(
            String manufacturer,
            String model,
            String deviceCode,
            String board,
            String hardware,
            String primaryCpuAbi,
            String androidVersion,
            int apiLevel,
            String securityPatch,
            BatteryInfo batteryInfo,
            MemoryInfoData memoryInfo,
            StorageInfoData storageInfo,
            NetworkInfoData networkInfo
    ) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.deviceCode = deviceCode;
        this.board = board;
        this.hardware = hardware;
        this.primaryCpuAbi = primaryCpuAbi;
        this.androidVersion = androidVersion;
        this.apiLevel = apiLevel;
        this.securityPatch = securityPatch;
        this.batteryInfo = batteryInfo;
        this.memoryInfo = memoryInfo;
        this.storageInfo = storageInfo;
        this.networkInfo = networkInfo;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public String getBoard() {
        return board;
    }

    public String getHardware() {
        return hardware;
    }

    public String getPrimaryCpuAbi() {
        return primaryCpuAbi;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public int getApiLevel() {
        return apiLevel;
    }

    public String getSecurityPatch() {
        return securityPatch;
    }

    public BatteryInfo getBatteryInfo() {
        return batteryInfo;
    }

    public MemoryInfoData getMemoryInfo() {
        return memoryInfo;
    }

    public StorageInfoData getStorageInfo() {
        return storageInfo;
    }

    public NetworkInfoData getNetworkInfo() {
        return networkInfo;
    }
}
