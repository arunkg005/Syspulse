package com.example.sensys.collector;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import com.example.sensys.model.BatteryInfo;
import com.example.sensys.model.MemoryInfoData;
import com.example.sensys.model.NetworkInfoData;
import com.example.sensys.model.StorageInfoData;
import com.example.sensys.model.SystemSnapshot;
import com.example.sensys.util.FormatUtils;

import java.net.Inet4Address;
import java.net.InetAddress;

public class SystemInfoCollector {
    private final Context context;

    public SystemInfoCollector(Context context) {
        this.context = context.getApplicationContext();
    }

    public SystemSnapshot collectSystemSnapshot() {
        return new SystemSnapshot(
                FormatUtils.fallback(Build.MANUFACTURER),
                FormatUtils.fallback(Build.MODEL),
                FormatUtils.fallback(Build.DEVICE),
                FormatUtils.fallback(Build.BOARD),
                FormatUtils.fallback(Build.HARDWARE),
                FormatUtils.fallback(getPrimaryCpuAbi()),
                FormatUtils.fallback(Build.VERSION.RELEASE),
                Build.VERSION.SDK_INT,
                FormatUtils.fallback(Build.VERSION.SECURITY_PATCH),
                collectBatteryInfo(),
                collectMemoryInfo(),
                collectStorageInfo(),
                collectNetworkInfo()
        );
    }

    public BatteryInfo collectBatteryInfo() {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null) {
            return new BatteryInfo(-1, "Unknown", "Unknown", Float.NaN);
        }

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int health = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        int temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Integer.MIN_VALUE);

        int levelPercent = -1;
        if (level >= 0 && scale > 0) {
            levelPercent = Math.round((level / (float) scale) * 100f);
        }

        float temperatureCelsius = temperature == Integer.MIN_VALUE ? Float.NaN : temperature / 10f;

        return new BatteryInfo(
                levelPercent,
                mapBatteryStatus(status),
                mapBatteryHealth(health, levelPercent),
                temperatureCelsius
        );
    }

    public MemoryInfoData collectMemoryInfo() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return new MemoryInfoData(0L, 0L, 0L, "Unavailable");
        }

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long total = memoryInfo.totalMem;
        long available = memoryInfo.availMem;
        long used = Math.max(total - available, 0L);

        double availableRatio = total > 0 ? (available / (double) total) : 0d;
        String status;
        if (availableRatio >= 0.35d) {
            status = "Healthy";
        } else if (availableRatio >= 0.18d) {
            status = "Moderate";
        } else {
            status = "Low";
        }

        return new MemoryInfoData(total, available, used, status);
    }

    public StorageInfoData collectStorageInfo() {
        try {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
            long total = statFs.getTotalBytes();
            long free = statFs.getAvailableBytes();
            long used = Math.max(total - free, 0L);

            double freeRatio = total > 0 ? (free / (double) total) : 0d;
            String status;
            if (freeRatio >= 0.20d) {
                status = "Good";
            } else if (freeRatio >= 0.10d) {
                status = "Watch";
            } else {
                status = "Low";
            }

            return new StorageInfoData(total, free, used, status);
        } catch (Exception ignored) {
            return new StorageInfoData(0L, 0L, 0L, "Unavailable");
        }
    }

    public NetworkInfoData collectNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return new NetworkInfoData(false, "Unavailable", FormatUtils.NOT_AVAILABLE, "Connectivity service unavailable");
        }

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            return new NetworkInfoData(false, "Offline", FormatUtils.NOT_AVAILABLE, "No active connection");
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        LinkProperties linkProperties = connectivityManager.getLinkProperties(activeNetwork);

        String transport = getTransportLabel(capabilities);
        String ipAddress = getIpAddress(linkProperties);
        String detail;
        if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            detail = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                    ? "Validated, unmetered"
                    : "Validated internet";
        } else {
            detail = "Local or limited connectivity";
        }

        return new NetworkInfoData(true, transport, ipAddress, detail);
    }

    private String mapBatteryStatus(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                return "Charging";
            case BatteryManager.BATTERY_STATUS_FULL:
                return "Full";
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return "Discharging";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return "Idle";
            default:
                return "Unknown";
        }
    }

    private String mapBatteryHealth(int health, int levelPercent) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return levelPercent >= 20 ? "Normal" : "Low charge";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return "Overheating";
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return "Critical";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return "Over voltage";
            case BatteryManager.BATTERY_HEALTH_COLD:
                return "Cold";
            default:
                return "Unknown";
        }
    }

    private String getPrimaryCpuAbi() {
        if (Build.SUPPORTED_ABIS != null && Build.SUPPORTED_ABIS.length > 0) {
            return Build.SUPPORTED_ABIS[0];
        }
        return Build.CPU_ABI;
    }

    private String getTransportLabel(NetworkCapabilities capabilities) {
        if (capabilities == null) {
            return "Connected";
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return "Wi-Fi";
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return "Cellular";
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return "Ethernet";
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
            return "Bluetooth";
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            return "VPN";
        }
        return "Other";
    }

    private String getIpAddress(LinkProperties linkProperties) {
        if (linkProperties == null) {
            return FormatUtils.NOT_AVAILABLE;
        }

        String fallback = null;
        for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
            InetAddress address = linkAddress.getAddress();
            if (address == null || address.isLoopbackAddress()) {
                continue;
            }
            if (address instanceof Inet4Address) {
                return address.getHostAddress();
            }
            if (fallback == null) {
                fallback = address.getHostAddress();
            }
        }
        return FormatUtils.safeIp(fallback);
    }
}
