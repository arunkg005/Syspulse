package com.example.sensys.collector;

import android.content.Context;

import com.example.sensys.diagnostics.UserTestManager;
import com.example.sensys.model.BatteryInfo;
import com.example.sensys.model.DeviceReport;
import com.example.sensys.model.SensorSnapshot;
import com.example.sensys.model.SystemSnapshot;
import com.example.sensys.model.TestResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportDataCollector {
    private final SystemInfoCollector systemInfoCollector;
    private final SensorDataCollector sensorDataCollector;

    public ReportDataCollector(Context context) {
        this.systemInfoCollector = new SystemInfoCollector(context);
        this.sensorDataCollector = new SensorDataCollector(context);
    }

    public DeviceReport collectDeviceReport(List<TestResult> userTestResults) {
        SystemSnapshot systemSnapshot = systemInfoCollector.collectSystemSnapshot();
        ArrayList<TestResult> normalizedTests = UserTestManager.ensureCoverage(userTestResults);
        List<SensorSnapshot> sensorSnapshots = sensorDataCollector.collectCoreSensorSnapshots(700L);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        String overallStatus = evaluateOverallStatus(systemSnapshot, sensorSnapshots, normalizedTests);

        return new DeviceReport(
                timestamp,
                systemSnapshot,
                sensorSnapshots,
                normalizedTests,
                overallStatus
        );
    }

    private String evaluateOverallStatus(
            SystemSnapshot systemSnapshot,
            List<SensorSnapshot> sensorSnapshots,
            List<TestResult> testResults
    ) {
        int issues = 0;
        int warnings = 0;

        BatteryInfo batteryInfo = systemSnapshot.getBatteryInfo();
        if (batteryInfo.getLevelPercent() >= 0 && batteryInfo.getLevelPercent() < 15) {
            warnings++;
        }
        if ("Overheating".equals(batteryInfo.getHealthStatus()) || "Critical".equals(batteryInfo.getHealthStatus())) {
            issues++;
        }

        if ("Low".equals(systemSnapshot.getMemoryInfo().getHealthStatus())) {
            issues++;
        } else if ("Moderate".equals(systemSnapshot.getMemoryInfo().getHealthStatus())) {
            warnings++;
        }

        if ("Low".equals(systemSnapshot.getStorageInfo().getHealthStatus())) {
            issues++;
        } else if ("Watch".equals(systemSnapshot.getStorageInfo().getHealthStatus())) {
            warnings++;
        }

        boolean hasActiveCoreSensor = false;
        for (SensorSnapshot snapshot : sensorSnapshots) {
            if ("Active".equals(snapshot.getStatus())) {
                hasActiveCoreSensor = true;
                break;
            }
        }
        if (!hasActiveCoreSensor) {
            warnings++;
        }

        for (TestResult testResult : testResults) {
            if (TestResult.STATUS_ISSUE.equals(testResult.getStatus())) {
                issues++;
            } else if (TestResult.STATUS_PENDING.equals(testResult.getStatus()) || TestResult.STATUS_SKIPPED.equals(testResult.getStatus())) {
                warnings++;
            }
        }

        if (issues > 0) {
            return "ATTENTION NEEDED";
        }
        if (warnings > 0) {
            return "MONITOR";
        }
        return "HEALTHY";
    }
}
