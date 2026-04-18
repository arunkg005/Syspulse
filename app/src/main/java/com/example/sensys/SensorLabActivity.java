package com.example.sensys;

import android.content.Intent;
import android.hardware.Sensor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sensys.collector.SensorDataCollector;
import com.example.sensys.ui.AppChrome;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SensorLabActivity extends AppCompatActivity {
    private SensorDataCollector sensorDataCollector;
    private final List<Sensor> sensors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_lab);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        SwitchMaterial themeModeSwitch = findViewById(R.id.themeModeSwitch);
        AppChrome.bindToolbar(toolbar, R.string.title_sensors, false, null);
        AppChrome.bindThemeSwitch(this, themeModeSwitch);
        AppChrome.applyEdgeToEdge(this, findViewById(R.id.topBarContainer), findViewById(R.id.bottomBarContainer));

        RecyclerView recyclerView = findViewById(R.id.sensorRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sensorDataCollector = new SensorDataCollector(this);
        List<Sensor> allSensors = sensorDataCollector.getAvailableSensors();
        // Group sensors by category for display order
        List<Sensor> motion = new ArrayList<>();
        List<Sensor> environment = new ArrayList<>();
        List<Sensor> position = new ArrayList<>();
        List<Sensor> virtuals = new ArrayList<>();
        List<Sensor> others = new ArrayList<>();
        for (Sensor s : allSensors) {
            switch (s.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                case Sensor.TYPE_GYROSCOPE:
                case Sensor.TYPE_ROTATION_VECTOR:
                case Sensor.TYPE_LINEAR_ACCELERATION:
                case Sensor.TYPE_GRAVITY:
                    motion.add(s); break;
                case Sensor.TYPE_LIGHT:
                case Sensor.TYPE_PROXIMITY:
                case Sensor.TYPE_PRESSURE:
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    environment.add(s); break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                case Sensor.TYPE_STEP_COUNTER:
                case Sensor.TYPE_STEP_DETECTOR:
                    position.add(s); break;
                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                case Sensor.TYPE_ORIENTATION:
                    virtuals.add(s); break;
                default:
                    others.add(s); break;
            }
        }
        sensors.clear();
        sensors.addAll(motion);
        sensors.addAll(environment);
        sensors.addAll(position);
        sensors.addAll(virtuals);
        sensors.addAll(others);
        recyclerView.setAdapter(new SensorAdapter(sensors));

        TextView sensorCountView = findViewById(R.id.sensorCountView);
        TextView emptyView = findViewById(R.id.emptyStateView);
        sensorCountView.setText(getString(R.string.format_sensor_count, sensors.size()));
        emptyView.setVisibility(sensors.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(sensors.isEmpty() ? View.GONE : View.VISIBLE);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_info) {
                openScreen(DeviceInfoActivity.class);
                return true;
            } else if (id == R.id.nav_lab) {
                return true;
            } else if (id == R.id.nav_report) {
                openScreen(ReportActivity.class);
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_lab);
    }

    private void openScreen(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private class SensorAdapter extends RecyclerView.Adapter<SensorViewHolder> {
        private final List<Sensor> sensors;

        SensorAdapter(List<Sensor> sensors) {
            this.sensors = sensors;
        }

        @Override
        public SensorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sensor, parent, false);
            return new SensorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SensorViewHolder holder, int position) {
            holder.bind(sensors.get(position));
        }

        @Override
        public int getItemCount() {
            return sensors.size();
        }
    }

    private class SensorViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final TextView metaView;
        private final TextView capabilityView;
        private final TextView statusView;

        SensorViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.sensorNameView);
            metaView = itemView.findViewById(R.id.sensorMetaView);
            capabilityView = itemView.findViewById(R.id.sensorCapabilityView);
            statusView = itemView.findViewById(R.id.sensorStatusView);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Sensor sensor = sensors.get(position);
                    Intent intent = new Intent(SensorLabActivity.this, SensorDetailActivity.class);
                    intent.putExtra("sensor_type", sensor.getType());
                    startActivity(intent);
                }
            });
        }

        void bind(Sensor sensor) {
                nameView.setText(sensor.getName());
                metaView.setText(String.format(
                    Locale.getDefault(),
                    "%s | %s",
                    SensorDataCollector.getSensorTypeLabel(sensor.getType()),
                    SensorDataCollector.getSensorCategoryLabel(sensor.getType())
                ));
                capabilityView.setText(String.format(
                    Locale.getDefault(),
                    "Vendor %s | Range %.1f",
                    sensor.getVendor(),
                    sensor.getMaximumRange()
                ));
                statusView.setText("");
        }
    }
}
