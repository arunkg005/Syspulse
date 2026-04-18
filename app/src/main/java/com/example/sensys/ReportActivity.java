package com.example.sensys;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sensys.collector.ReportDataCollector;
import com.example.sensys.diagnostics.UserTestManager;
import com.example.sensys.model.DeviceReport;
import com.example.sensys.model.TestResult;
import com.example.sensys.report.ReportBuilder;
import com.example.sensys.ui.AppChrome;
import com.example.sensys.util.ReportExporter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReportActivity extends AppCompatActivity {
    private static final int REQUEST_DIAGNOSTICS = 1001;

    private MaterialButton generateButton;
    private TextView reportStatusView;
    private RecyclerView reportLogRecyclerView;
    private ReportLogAdapter adapter;
    private List<File> reportFiles = new ArrayList<>();
    private ArrayList<TestResult> currentResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        SwitchMaterial themeModeSwitch = findViewById(R.id.themeModeSwitch);
        AppChrome.bindToolbar(toolbar, R.string.title_device_health_report, false, null);
        AppChrome.bindThemeSwitch(this, themeModeSwitch);
        AppChrome.applyEdgeToEdge(this, findViewById(R.id.topBarContainer), findViewById(R.id.bottomBarContainer));

        generateButton = findViewById(R.id.generateButton);
        reportStatusView = findViewById(R.id.reportStatusView);
        reportLogRecyclerView = findViewById(R.id.reportLogRecyclerView);

        reportLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportLogAdapter();
        reportLogRecyclerView.setAdapter(adapter);

        currentResults = UserTestManager.createPendingResults();

        generateButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DiagnosticTestActivity.class);
            startActivityForResult(intent, REQUEST_DIAGNOSTICS);
        });

        loadReports();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_info) {
                openScreen(DeviceInfoActivity.class);
                return true;
            } else if (id == R.id.nav_lab) {
                openScreen(SensorLabActivity.class);
                return true;
            } else if (id == R.id.nav_report) {
                return true;
            }
            return false;
        });
        bottomNav.setSelectedItemId(R.id.nav_report);
    }

    private void openScreen(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void loadReports() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles((d, name) -> name.startsWith("report_") && name.endsWith(".txt"));
            if (files != null) {
                reportFiles = new ArrayList<>(Arrays.asList(files));
                Collections.sort(reportFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DIAGNOSTICS && resultCode == RESULT_OK && data != null) {
            currentResults = UserTestManager.fromIntent(data);
            generateReport();
        }
    }

    private void generateReport() {
        generateButton.setEnabled(false);
        reportStatusView.setText(R.string.report_generating);

        new Thread(() -> {
            ReportDataCollector collector = new ReportDataCollector(this);
            DeviceReport report = collector.collectDeviceReport(currentResults);
            ReportBuilder reportBuilder = new ReportBuilder(this);
            String reportText = reportBuilder.build(report);

            try {
                ReportExporter.exportTextReport(this, reportText);
                runOnUiThread(() -> {
                    reportStatusView.setText(R.string.report_initial_status);
                    generateButton.setEnabled(true);
                    currentResults = UserTestManager.createPendingResults();
                    loadReports();
                    Toast.makeText(ReportActivity.this, R.string.report_generated_success, Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    reportStatusView.setText(R.string.report_generation_failed);
                    generateButton.setEnabled(true);
                    Toast.makeText(ReportActivity.this, getString(R.string.report_save_failed, e.getMessage()), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

        private void showDeleteConfirmation(File file) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Report")
                .setMessage("Are you sure you want to delete this report?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (file.delete()) {
                        loadReports();
                        Toast.makeText(this, "Report deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete report", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class ReportLogAdapter extends RecyclerView.Adapter<ReportLogViewHolder> {
            @NonNull
            @Override
            public ReportLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_log, parent, false);
                return new ReportLogViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ReportLogViewHolder holder, int position) {
                holder.bind(reportFiles.get(position));
            }

            @Override
            public int getItemCount() {
                return reportFiles.size();
            }
        }

        private class ReportLogViewHolder extends RecyclerView.ViewHolder {
            private final TextView fileName;
            private final TextView date;
            private final MaterialButton viewBtn;
            private final MaterialButton shareBtn;

            public ReportLogViewHolder(@NonNull View itemView) {
                super(itemView);
                fileName = itemView.findViewById(R.id.reportFileName);
                date = itemView.findViewById(R.id.reportDate);
                viewBtn = itemView.findViewById(R.id.viewReportButton);
                shareBtn = itemView.findViewById(R.id.shareReportButton);
            }

            public void bind(File file) {
                fileName.setText(file.getName());
                date.setText(new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date(file.lastModified())));

                itemView.setOnLongClickListener(v -> {
                    showDeleteConfirmation(file);
                    return true;
                });

                viewBtn.setOnClickListener(v -> {
                    Uri uri = FileProvider.getUriForFile(ReportActivity.this, getPackageName() + ".provider", file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "text/plain");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(ReportActivity.this, R.string.no_app_text_files, Toast.LENGTH_SHORT).show();
                    }
                });

                shareBtn.setOnClickListener(v -> {
                    Uri uri = FileProvider.getUriForFile(ReportActivity.this, getPackageName() + ".provider", file);
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_report)));
                });
            }
        }
    }
