package com.example.sensys.collector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.sensys.model.SensorSnapshot;
import com.example.sensys.util.FormatUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SensorDataCollector {
    private static final int[] CORE_SENSOR_TYPES = new int[] {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PROXIMITY,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_STEP_DETECTOR,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_RELATIVE_HUMIDITY
    };

    private final SensorManager sensorManager;

    public SensorDataCollector(Context context) {
        this.sensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
    }

    public List<Sensor> getAvailableSensors() {
        if (sensorManager == null) {
            return Collections.emptyList();
        }
        return sensorManager.getSensorList(Sensor.TYPE_ALL);
    }

    public Sensor getDefaultSensor(int sensorType) {
        return sensorManager == null ? null : sensorManager.getDefaultSensor(sensorType);
    }

    public boolean registerListener(Sensor sensor, SensorEventListener listener, int delay) {
        return sensor != null && sensorManager != null && sensorManager.registerListener(listener, sensor, delay);
    }

    public void unregisterListener(SensorEventListener listener) {
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }
    }

    public List<SensorSnapshot> collectCoreSensorSnapshots(long captureMillis) {
        Map<Integer, SnapshotReading> readings = new LinkedHashMap<>();
        List<Sensor> trackedSensors = new ArrayList<>();

        if (sensorManager == null) {
            List<SensorSnapshot> snapshots = new ArrayList<>();
            for (int sensorType : CORE_SENSOR_TYPES) {
                snapshots.add(new SensorSnapshot(
                        sensorType,
                        getSensorTypeLabel(sensorType),
                        getSensorTypeLabel(sensorType),
                        getSensorCategoryLabel(sensorType),
                        "Not Available",
                        "Sensor service unavailable",
                        "Unknown"
                ));
            }
            return snapshots;
        }

        SensorEventListener snapshotListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                SnapshotReading reading = readings.get(event.sensor.getType());
                if (reading != null) {
                    reading.values = event.values.clone();
                    reading.accuracy = event.accuracy;
                    reading.receivedEvent = true;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                SnapshotReading reading = readings.get(sensor.getType());
                if (reading != null) {
                    reading.accuracy = accuracy;
                }
            }
        };

        for (int sensorType : CORE_SENSOR_TYPES) {
            Sensor sensor = sensorManager.getDefaultSensor(sensorType);
            SnapshotReading reading = new SnapshotReading(sensor);
            readings.put(sensorType, reading);

            if (sensor != null) {
                trackedSensors.add(sensor);
                sensorManager.registerListener(snapshotListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        if (!trackedSensors.isEmpty()) {
            try {
                Thread.sleep(Math.max(250L, captureMillis));
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
            sensorManager.unregisterListener(snapshotListener);
        }

        List<SensorSnapshot> snapshots = new ArrayList<>();
        for (int sensorType : CORE_SENSOR_TYPES) {
            SnapshotReading reading = readings.get(sensorType);
            if (reading == null || reading.sensor == null) {
                snapshots.add(new SensorSnapshot(
                        sensorType,
                        getSensorTypeLabel(sensorType),
                        getSensorTypeLabel(sensorType),
                        getSensorCategoryLabel(sensorType),
                        "Not Available",
                        "Hardware not present on this device",
                        "Unknown"
                ));
                continue;
            }

            snapshots.add(new SensorSnapshot(
                    sensorType,
                    reading.sensor.getName(),
                    getSensorTypeLabel(sensorType),
                    getSensorCategoryLabel(sensorType),
                    reading.receivedEvent ? "Active" : "Available",
                    reading.receivedEvent
                            ? describeSensorEvent(sensorType, reading.values)
                            : "No live event captured in snapshot window",
                    describeAccuracy(reading.accuracy)
            ));
        }

        return snapshots;
    }

    public static String getSensorTypeLabel(int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                return "Accelerometer";
            case Sensor.TYPE_GYROSCOPE:
                return "Gyroscope";
            case Sensor.TYPE_ROTATION_VECTOR:
                return "Rotation Vector";
            case Sensor.TYPE_LIGHT:
                return "Light";
            case Sensor.TYPE_PROXIMITY:
                return "Proximity";
            case Sensor.TYPE_PRESSURE:
                return "Pressure";
            case Sensor.TYPE_STEP_COUNTER:
                return "Step Counter";
            case Sensor.TYPE_STEP_DETECTOR:
                return "Step Detector";
            case Sensor.TYPE_LINEAR_ACCELERATION:
                return "Linear Acceleration";
            case Sensor.TYPE_GRAVITY:
                return "Gravity";
            case Sensor.TYPE_MAGNETIC_FIELD:
                return "Magnetic Field";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "Ambient Temperature";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Relative Humidity";
            default:
                return String.format(Locale.getDefault(), "Sensor Type %d", sensorType);
        }
    }

    public static String getSensorCategoryLabel(int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_ROTATION_VECTOR:
            case Sensor.TYPE_LINEAR_ACCELERATION:
            case Sensor.TYPE_GRAVITY:
                return "Motion";
            case Sensor.TYPE_LIGHT:
            case Sensor.TYPE_PROXIMITY:
            case Sensor.TYPE_PRESSURE:
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "Environmental";
            case Sensor.TYPE_STEP_COUNTER:
            case Sensor.TYPE_STEP_DETECTOR:
                return "Activity";
            default:
                return "Other";
        }
    }

    public static String describeSensorEvent(int sensorType, float[] values) {
        if (values == null || values.length == 0) {
            return "No data";
        }

        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
            case Sensor.TYPE_LINEAR_ACCELERATION:
            case Sensor.TYPE_GRAVITY:
                float mag = vectorMagnitude(values);
                String motionStatus = mag > 0.5f ? "Movement detected" : "Stable";
                if (sensorType == Sensor.TYPE_GRAVITY) motionStatus = "Orientation sense";
                return String.format(
                        Locale.getDefault(),
                        "%s | magnitude %.2f m/s2",
                        motionStatus,
                        mag
                );
            case Sensor.TYPE_GYROSCOPE:
                float rotMag = vectorMagnitude(values);
                String rotStatus = rotMag > 0.1f ? "Rotating" : "Static";
                return String.format(
                        Locale.getDefault(),
                        "%s | %.2f rad/s",
                        rotStatus,
                        rotMag
                );
            case Sensor.TYPE_ROTATION_VECTOR:
                return "Device Orientation tracking active";
            case Sensor.TYPE_LIGHT:
                float lux = values[0];
                String lightLevel;
                if (lux < 50) lightLevel = "Dark";
                else if (lux < 500) lightLevel = "Normal";
                else lightLevel = "Bright";
                return String.format(Locale.getDefault(), "%s (%s)", lightLevel, FormatUtils.formatNumber(lux, "lx"));
            case Sensor.TYPE_PROXIMITY:
                return values[0] <= 1f
                        ? String.format(Locale.getDefault(), "Near (%.2f cm)", values[0])
                        : String.format(Locale.getDefault(), "Far (%.2f cm)", values[0]);
            case Sensor.TYPE_PRESSURE:
                return String.format(Locale.getDefault(), "Air pressure: %s", FormatUtils.formatNumber(values[0], "hPa"));
            case Sensor.TYPE_STEP_COUNTER:
                return FormatUtils.formatInteger(values[0], "total steps");
            case Sensor.TYPE_STEP_DETECTOR:
                return values[0] == 1f ? "Step detected" : "Listening for a step event";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return String.format(Locale.getDefault(), "Ambient: %.1f °C", values[0]);
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return String.format(Locale.getDefault(), "Humidity: %.1f %%", values[0]);
            case Sensor.TYPE_MAGNETIC_FIELD:
                return String.format(Locale.getDefault(), "Magnetic: %.1f μT", vectorMagnitude(values));
            default:
                return FormatUtils.joinValues(values);
        }
    }

    public static String describeAccuracy(int accuracy) {
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                return "High";
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                return "Medium";
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                return "Low";
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                return "Unreliable";
            default:
                return "Unknown";
        }
    }

    private static float getValue(float[] values, int index) {
        return index < values.length ? values[index] : 0f;
    }

    private static float vectorMagnitude(float[] values) {
        float sum = 0f;
        for (float value : values) {
            sum += value * value;
        }
        return (float) Math.sqrt(sum);
    }

    private static class SnapshotReading {
        private final Sensor sensor;
        private float[] values;
        private int accuracy = SensorManager.SENSOR_STATUS_UNRELIABLE;
        private boolean receivedEvent;

        SnapshotReading(Sensor sensor) {
            this.sensor = sensor;
        }
    }
}
