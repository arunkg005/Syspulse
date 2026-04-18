package com.example.sensys;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sensys.collector.SensorDataCollector;
import com.example.sensys.collector.SystemInfoCollector;
import com.example.sensys.model.SystemSnapshot;
import com.example.sensys.ui.AppChrome;
import com.example.sensys.util.FormatUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class DeviceInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        SwitchMaterial themeModeSwitch = findViewById(R.id.themeModeSwitch);
        AppChrome.bindToolbar(toolbar, R.string.app_name, false, null);
        AppChrome.bindThemeSwitch(this, themeModeSwitch);
        AppChrome.applyEdgeToEdge(this, findViewById(R.id.topBarContainer), findViewById(R.id.bottomBarContainer));

        SystemSnapshot snapshot = new SystemInfoCollector(this).collectSystemSnapshot();
        int sensorCount = new SensorDataCollector(this).getAvailableSensors().size();
        bindSnapshot(snapshot, sensorCount);
        bindActions();
        setupBottomNavigation();
    }

    private void bindSnapshot(SystemSnapshot snapshot, int sensorCount) {
        ((TextView) findViewById(R.id.summaryTitleView)).setText(
                String.format(Locale.getDefault(), "%s %s", snapshot.getManufacturer(), snapshot.getModel())
        );
        ((TextView) findViewById(R.id.summarySubtitleView)).setText(
                getString(R.string.format_summary_subtitle, snapshot.getAndroidVersion(), snapshot.getApiLevel(), snapshot.getDeviceCode())
        );
        ((TextView) findViewById(R.id.summaryStatusView)).setText(
                getString(
                        R.string.format_summary_status,
                        FormatUtils.formatPercent(snapshot.getBatteryInfo().getLevelPercent()),
                        sensorCount
                )
        );

        ((TextView) findViewById(R.id.hardwareManufacturer)).setText(
                getString(R.string.format_label_value, getString(R.string.label_manufacturer), snapshot.getManufacturer())
        );
        ((TextView) findViewById(R.id.hardwareModel)).setText(
                getString(R.string.format_label_value, getString(R.string.label_model), snapshot.getModel())
        );
        ((TextView) findViewById(R.id.hardwareBoard)).setText(
                getString(R.string.format_label_value, getString(R.string.label_board), snapshot.getBoard())
        );
        ((TextView) findViewById(R.id.hardwareCpu)).setText(
                getString(R.string.format_label_value, getString(R.string.label_cpu), snapshot.getPrimaryCpuAbi())
        );

        ((TextView) findViewById(R.id.softwareVersion)).setText(
                getString(R.string.format_label_value, getString(R.string.label_version), "Android " + snapshot.getAndroidVersion())
        );
        ((TextView) findViewById(R.id.softwareApiLevel)).setText(
                getString(R.string.format_label_value, getString(R.string.label_api_level), String.valueOf(snapshot.getApiLevel()))
        );
        ((TextView) findViewById(R.id.softwareSecurityPatch)).setText(
                getString(R.string.format_label_value, getString(R.string.label_security_patch), snapshot.getSecurityPatch())
        );
        ((TextView) findViewById(R.id.softwareHardware)).setText(
                getString(R.string.format_label_value, getString(R.string.label_hardware_core), snapshot.getHardware())
        );

        ((TextView) findViewById(R.id.ramTotal)).setText(
                getString(R.string.format_label_value, getString(R.string.label_ram_total), FormatUtils.formatBytes(this, snapshot.getMemoryInfo().getTotalBytes()))
        );
        ((TextView) findViewById(R.id.ramAvailable)).setText(
                getString(R.string.format_label_value, getString(R.string.label_ram_available), FormatUtils.formatBytes(this, snapshot.getMemoryInfo().getAvailableBytes()))
        );
        ((TextView) findViewById(R.id.storageTotal)).setText(
                getString(R.string.format_label_value, getString(R.string.label_storage_total), FormatUtils.formatBytes(this, snapshot.getStorageInfo().getTotalBytes()))
        );
        ((TextView) findViewById(R.id.storageFree)).setText(
                getString(R.string.format_label_value, getString(R.string.label_storage_free), FormatUtils.formatBytes(this, snapshot.getStorageInfo().getFreeBytes()))
        );
        ((LinearProgressIndicator) findViewById(R.id.ramProgressIndicator)).setProgress(calculateUsageProgress(
                snapshot.getMemoryInfo().getUsedBytes(),
                snapshot.getMemoryInfo().getTotalBytes()
        ));
        ((LinearProgressIndicator) findViewById(R.id.storageProgressIndicator)).setProgress(calculateUsageProgress(
                snapshot.getStorageInfo().getUsedBytes(),
                snapshot.getStorageInfo().getTotalBytes()
        ));

        ((TextView) findViewById(R.id.batteryLevel)).setText(
                getString(R.string.format_label_value, getString(R.string.label_battery_level), FormatUtils.formatPercent(snapshot.getBatteryInfo().getLevelPercent()))
        );
        ((TextView) findViewById(R.id.batteryStatus)).setText(
                getString(R.string.format_label_value, getString(R.string.label_battery_status), snapshot.getBatteryInfo().getChargingStatus())
        );
        ((TextView) findViewById(R.id.batteryHealth)).setText(
                getString(R.string.format_label_value, getString(R.string.label_battery_health), snapshot.getBatteryInfo().getHealthStatus())
        );
        ((TextView) findViewById(R.id.batteryTemperature)).setText(
                getString(R.string.format_label_value, getString(R.string.label_battery_temperature), FormatUtils.formatTemperature(snapshot.getBatteryInfo().getTemperatureCelsius()))
        );

        ((TextView) findViewById(R.id.networkStatus)).setText(
                getString(
                        R.string.format_label_value,
                        getString(R.string.label_network_status),
                        snapshot.getNetworkInfo().isConnected() ? getString(R.string.connected) : getString(R.string.not_connected)
                )
        );
        ((TextView) findViewById(R.id.networkType)).setText(
                getString(R.string.format_label_value, getString(R.string.label_network_type), snapshot.getNetworkInfo().getTransportLabel())
        );
        ((TextView) findViewById(R.id.networkIp)).setText(
                getString(R.string.format_label_value, getString(R.string.label_ip_address), snapshot.getNetworkInfo().getIpAddress())
        );
        ((TextView) findViewById(R.id.networkDetail)).setText(
                getString(R.string.format_label_value, getString(R.string.label_network_detail), snapshot.getNetworkInfo().getDetail())
        );
    }

        private void bindActions() {
                // Buttons removed from layout, nothing to bind here
        }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_info) {
                return true;
            }
            if (itemId == R.id.nav_lab) {
                openScreen(SensorLabActivity.class);
                overridePendingTransition(0, 0);
                return true;
            }
            if (itemId == R.id.nav_report) {
                openScreen(ReportActivity.class);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_info);
    }

    private void openScreen(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private int calculateUsageProgress(long usedBytes, long totalBytes) {
        if (totalBytes <= 0L) {
            return 0;
        }
        double ratio = usedBytes / (double) totalBytes;
        return (int) Math.max(0d, Math.min(100d, Math.round(ratio * 100d)));
    }
}
