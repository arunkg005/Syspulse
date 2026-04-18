package com.example.sensys;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        // Top bar
        TextView title = new TextView(this);
        title.setText("Sensys");
        title.setTextSize(24);
        title.setPadding(32, 32, 32, 16);
        root.addView(title);

        // Device summary (optional, can be improved)
        TextView summary = new TextView(this);
        summary.setText("Welcome to Sensys!\nChoose a module below.");
        summary.setPadding(24, 24, 24, 24);
        root.addView(summary);

        // Spacer
        View spacer = new View(this);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(spacer, spacerParams);

        // Bottom navigation bar
        LinearLayout navBar = new LinearLayout(this);
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setPadding(16, 16, 16, 16);
        navBar.setBackgroundColor(0xFFE0E0E0); // light gray

        Button deviceInfoBtn = new Button(this);
        deviceInfoBtn.setText("Device Info");
        deviceInfoBtn.setOnClickListener(v -> startActivity(new Intent(this, DeviceInfoActivity.class)));
        navBar.addView(deviceInfoBtn, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button sensorsBtn = new Button(this);
        sensorsBtn.setText("Sensors");
        sensorsBtn.setOnClickListener(v -> startActivity(new Intent(this, SensorLabActivity.class)));
        navBar.addView(sensorsBtn, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button reportBtn = new Button(this);
        reportBtn.setText("Report");
        reportBtn.setOnClickListener(v -> startActivity(new Intent(this, ReportActivity.class)));
        navBar.addView(reportBtn, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        root.addView(navBar);

        setContentView(root);
    }
}