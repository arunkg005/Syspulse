package com.example.sensys;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sensys.collector.SensorDataCollector;
import com.example.sensys.ui.AppChrome;
import com.example.sensys.view.SensorVisualizationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class SensorDetailActivity extends AppCompatActivity {
        private android.view.View sensorDetailCardsContainer;
    private SensorDataCollector sensorDataCollector;
    private Sensor sensor;
    private SensorEventListener listener;
    private SensorVisualizationView visualizationView;
    private MaterialToolbar toolbar;
    private TextView sensorNameView;
    private TextView sensorTypeView;
    private TextView sensorMetaView;
    private TextView primaryValueView;
    private TextView axisXView;
    private TextView axisYView;
    private TextView axisZView;
    private TextView accuracyView;
    private TextView statusView;
    private int sensorType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_detail);

        toolbar = findViewById(R.id.topAppBar);
        SwitchMaterial themeModeSwitch = findViewById(R.id.themeModeSwitch);
        AppChrome.bindToolbar(toolbar, R.string.title_sensor_detail, true, this::finish);
        AppChrome.bindThemeSwitch(this, themeModeSwitch);
        AppChrome.applyEdgeToEdge(this, findViewById(R.id.topBarContainer), findViewById(R.id.detailScrollView));

        sensorType = getIntent().getIntExtra("sensor_type", -1);
        sensorDataCollector = new SensorDataCollector(this);
        sensor = sensorDataCollector.getDefaultSensor(sensorType);

        visualizationView = findViewById(R.id.sensorVisualizationView);
        sensorDetailCardsContainer = findViewById(R.id.sensorDetailCardsContainer);
        sensorNameView = findViewById(R.id.sensorNameView);
        sensorTypeView = findViewById(R.id.sensorTypeView);
        sensorMetaView = findViewById(R.id.sensorMetaView);
        primaryValueView = findViewById(R.id.primaryValueView);
        axisXView = findViewById(R.id.axisXView);
        axisYView = findViewById(R.id.axisYView);
        axisZView = findViewById(R.id.axisZView);
        accuracyView = findViewById(R.id.accuracyView);
        statusView = findViewById(R.id.statusView);

        bindSensorMetadata();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensor != null) {
            listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    renderSensorValues(event.values, event.accuracy);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    accuracyView.setText(getString(
                            R.string.format_sensor_accuracy,
                            SensorDataCollector.describeAccuracy(accuracy)
                    ));
                }
            };
            sensorDataCollector.registerListener(sensor, listener, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (listener != null) {
            sensorDataCollector.unregisterListener(listener);
            listener = null;
        }
    }

    private void bindSensorMetadata() {
        if (sensor == null) {
            sensorNameView.setText(R.string.sensor_unavailable);
            sensorTypeView.setText(getString(
                R.string.format_sensor_meta,
                SensorDataCollector.getSensorTypeLabel(sensorType),
                getString(R.string.not_available)
            ));
            sensorMetaView.setText(R.string.sensor_unavailable_hint);
            // Hide all sensor info views and visualization for unavailable sensor
            sensorDetailCardsContainer.setVisibility(android.view.View.GONE);
            return;
        }

        String title = sensor.getName();
        toolbar.setTitle(title);
        sensorNameView.setText(title);
        sensorTypeView.setText(getString(
                R.string.format_sensor_meta,
                SensorDataCollector.getSensorTypeLabel(sensor.getType()),
                SensorDataCollector.getSensorCategoryLabel(sensor.getType())
        ));
        if (!com.example.sensys.view.SensorVisualizationView.isVisualizationSupported(sensor.getType())) {
            // Hide all sensor info views and visualization if unsupported
            sensorMetaView.setVisibility(android.view.View.VISIBLE);
            sensorMetaView.setText(String.format(
                Locale.getDefault(),
                "Type: %s | Change detected: %s",
                SensorDataCollector.getSensorTypeLabel(sensor.getType()),
                SensorDataCollector.getSensorCategoryLabel(sensor.getType())
            ));
            sensorDetailCardsContainer.setVisibility(android.view.View.GONE);
            return;
        }
        // Show all views if supported
        sensorDetailCardsContainer.setVisibility(android.view.View.VISIBLE);
        sensorMetaView.setVisibility(android.view.View.VISIBLE);
        primaryValueView.setVisibility(android.view.View.VISIBLE);
        axisXView.setVisibility(android.view.View.VISIBLE);
        axisYView.setVisibility(android.view.View.VISIBLE);
        axisZView.setVisibility(android.view.View.VISIBLE);
        accuracyView.setVisibility(android.view.View.VISIBLE);
        statusView.setVisibility(android.view.View.VISIBLE);
        visualizationView.setVisibility(android.view.View.VISIBLE);
        sensorMetaView.setText(String.format(
            Locale.getDefault(),
            "Vendor %s | Range %.1f",
            sensor.getVendor(),
            sensor.getMaximumRange()
        ));
        visualizationView.setSensorMetadata(sensorType, 1f);
        primaryValueView.setText("");
        axisXView.setText("");
        axisYView.setText("");
        axisZView.setText("");
        accuracyView.setText("");
        statusView.setText(R.string.sensor_live_hint);
        visualizationView.setSensorMetadata(sensor.getType(), sensor.getMaximumRange());
    }

    private void renderSensorValues(float[] values, int accuracy) {
        visualizationView.setSensorData(sensor.getType(), values);
        accuracyView.setText(getString(
                R.string.format_sensor_accuracy,
                SensorDataCollector.describeAccuracy(accuracy)
        ));
        statusView.setText(SensorDataCollector.describeSensorEvent(sensor.getType(), values));

        if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            float[] orientation = new float[3];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, values);
            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuth = (float) Math.toDegrees(orientation[0]);
            float pitch = (float) Math.toDegrees(orientation[1]);
            float roll = (float) Math.toDegrees(orientation[2]);

            primaryValueView.setText(getString(R.string.format_heading_value, azimuth));
            axisXView.setText(getString(
                    R.string.format_axis_value,
                    "Azimuth",
                    String.format(Locale.getDefault(), "%.1f deg", azimuth)
            ));
            axisYView.setText(getString(
                    R.string.format_axis_value,
                    "Pitch",
                    String.format(Locale.getDefault(), "%.1f deg", pitch)
            ));
            axisZView.setText(getString(
                    R.string.format_axis_value,
                    "Roll",
                    String.format(Locale.getDefault(), "%.1f deg", roll)
            ));
            return;
        }

        if (com.example.sensys.view.SensorVisualizationView.isVisualizationSupported(sensor.getType())) {
            visualizationView.setSensorData(sensor.getType(), values);
        }
        String primary = values.length > 0
            ? String.format(Locale.getDefault(), "%.2f", values[0])
            : getString(R.string.not_available);
        primaryValueView.setText(getString(R.string.format_primary_value, primary));
        axisXView.setText(getString(
                R.string.format_axis_value,
                "X",
                values.length > 0 ? String.format(Locale.getDefault(), "%.2f", values[0]) : getString(R.string.not_available)
        ));
        axisYView.setText(getString(
                R.string.format_axis_value,
                "Y",
                values.length > 1 ? String.format(Locale.getDefault(), "%.2f", values[1]) : getString(R.string.not_available)
        ));
        axisZView.setText(getString(
                R.string.format_axis_value,
                "Z",
                values.length > 2 ? String.format(Locale.getDefault(), "%.2f", values[2]) : getString(R.string.not_available)
        ));
    }
}
