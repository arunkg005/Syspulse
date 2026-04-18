package com.example.sensys.report;

import android.content.Context;

import com.example.sensys.model.BatteryInfo;
import com.example.sensys.model.DeviceReport;
import com.example.sensys.model.MemoryInfoData;
import com.example.sensys.model.NetworkInfoData;
import com.example.sensys.model.SensorSnapshot;
import com.example.sensys.model.StorageInfoData;
import com.example.sensys.model.SystemSnapshot;
import com.example.sensys.model.TestResult;
import com.example.sensys.util.FormatUtils;

public class ReportBuilder {
    private final Context context;

    public ReportBuilder(Context context) {
        this.context = context.getApplicationContext();
    }

    public String build(DeviceReport report) {
        StringBuilder builder = new StringBuilder();
        SystemSnapshot system = report.getSystemSnapshot();

        builder.append("DEVICE HEALTH REPORT\n");
        builder.append("Generated: ").append(report.getGeneratedAt()).append('\n');
        builder.append("Overall Status: ").append(report.getOverallStatus()).append("\n\n");

        builder.append("DEVICE\n");
        appendLine(builder, "Manufacturer", system.getManufacturer());
        appendLine(builder, "Model", system.getModel());
        appendLine(builder, "Device Code", system.getDeviceCode());
        appendLine(builder, "Board", system.getBoard());
        appendLine(builder, "Hardware", system.getHardware());
        appendLine(builder, "CPU ABI", system.getPrimaryCpuAbi());
        builder.append('\n');

        builder.append("SOFTWARE\n");
        appendLine(builder, "Android Version", system.getAndroidVersion());
        appendLine(builder, "API Level", String.valueOf(system.getApiLevel()));
        appendLine(builder, "Security Patch", system.getSecurityPatch());
        builder.append('\n');

        BatteryInfo batteryInfo = system.getBatteryInfo();
        builder.append("BATTERY\n");
        appendLine(builder, "Level", FormatUtils.formatPercent(batteryInfo.getLevelPercent()));
        appendLine(builder, "Charging", batteryInfo.getChargingStatus());
        appendLine(builder, "Health", batteryInfo.getHealthStatus());
        appendLine(builder, "Temperature", FormatUtils.formatTemperature(batteryInfo.getTemperatureCelsius()));
        builder.append('\n');

        MemoryInfoData memoryInfo = system.getMemoryInfo();
        builder.append("MEMORY\n");
        appendLine(builder, "Total RAM", FormatUtils.formatBytes(context, memoryInfo.getTotalBytes()));
        appendLine(builder, "Available RAM", FormatUtils.formatBytes(context, memoryInfo.getAvailableBytes()));
        appendLine(builder, "Used RAM", FormatUtils.formatBytes(context, memoryInfo.getUsedBytes()));
        appendLine(builder, "RAM Status", memoryInfo.getHealthStatus());
        builder.append('\n');

        StorageInfoData storageInfo = system.getStorageInfo();
        builder.append("STORAGE\n");
        appendLine(builder, "Total Storage", FormatUtils.formatBytes(context, storageInfo.getTotalBytes()));
        appendLine(builder, "Free Storage", FormatUtils.formatBytes(context, storageInfo.getFreeBytes()));
        appendLine(builder, "Used Storage", FormatUtils.formatBytes(context, storageInfo.getUsedBytes()));
        appendLine(builder, "Storage Status", storageInfo.getHealthStatus());
        builder.append('\n');

        NetworkInfoData networkInfo = system.getNetworkInfo();
        builder.append("NETWORK\n");
        appendLine(builder, "Connected", networkInfo.isConnected() ? "Yes" : "No");
        appendLine(builder, "Transport", networkInfo.getTransportLabel());
        appendLine(builder, "IP Address", networkInfo.getIpAddress());
        appendLine(builder, "Detail", networkInfo.getDetail());
        builder.append('\n');

        builder.append("SENSORS\n");
        for (SensorSnapshot snapshot : report.getSensorSnapshots()) {
            builder.append(snapshot.getSensorTypeLabel()).append(" - ").append(snapshot.getStatus()).append('\n');
            builder.append("  ").append(snapshot.getSensorName()).append('\n');
            builder.append("  ").append(snapshot.getValueSummary()).append('\n');
        }
        builder.append('\n');

        builder.append("TESTS\n");
        for (TestResult result : report.getTestResults()) {
            builder.append(result.getName())
                    .append(": ")
                    .append(result.getStatus())
                    .append(" - ")
                    .append(result.getDetails())
                    .append('\n');
        }

        return builder.toString().trim();
    }

    private void appendLine(StringBuilder builder, String label, String value) {
        builder.append(label).append(": ").append(FormatUtils.fallback(value)).append('\n');
    }
}
