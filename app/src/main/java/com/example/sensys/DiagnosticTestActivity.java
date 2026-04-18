package com.example.sensys;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sensys.diagnostics.UserTestManager;
import com.example.sensys.model.TestResult;
import com.example.sensys.ui.AppChrome;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DiagnosticTestActivity extends AppCompatActivity {
    private final List<DiagnosticStep> steps = Arrays.asList(
            new DiagnosticStep(UserTestManager.TEST_DISPLAY, "Cycle through high-contrast colors and inspect the panel for tint, dead pixels, or uneven rendering."),
            new DiagnosticStep(UserTestManager.TEST_TOUCH, "Tap every zone in the grid to confirm touch registration across the display surface."),
            new DiagnosticStep(UserTestManager.TEST_AUDIO, "Play the built-in test tone, then confirm the speaker output is clean and audible."),
            new DiagnosticStep(UserTestManager.TEST_BUTTONS, "Press volume up and volume down while this screen is active.")
    );

    private final ArrayList<TestResult> results = new ArrayList<>();

    private TextView testStepView;
    private TextView testTitleView;
    private TextView testInstructionView;
    private TextView liveStatusView;
    private FrameLayout dynamicContent;
    private MaterialButton primaryActionButton;
    private MaterialButton passButton;
    private MaterialButton issueButton;

    private int currentIndex = 0;

    private final int[] displayColors = new int[] {
            0xFFFF0000, // Pure Red
            0xFF00FF00, // Pure Green
            0xFF0000FF, // Pure Blue
            0xFFFFFFFF, // Pure White
            0xFF000000  // Pure Black
    };
    private final String[] displayLabels = new String[] {"Red", "Green", "Blue", "White", "Black"};
    private int displayColorIndex = -1;
    private View displayPreview;
    private TextView displayCaption;
    private View fullScreenOverlay;
    private float originalBrightness = -1f;

    private int touchedCells = 0;
    private boolean audioPlayed = false;
    private ToneGenerator toneGenerator;

    private boolean volumeUpPressed = false;
    private boolean volumeDownPressed = false;
    private TextView volumeUpStatusView;
    private TextView volumeDownStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_test);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        SwitchMaterial themeModeSwitch = findViewById(R.id.themeModeSwitch);
        AppChrome.bindToolbar(toolbar, R.string.title_diagnostics, true, this::finishWithCoverage);
        AppChrome.bindThemeSwitch(this, themeModeSwitch);
        AppChrome.applyEdgeToEdge(this, findViewById(R.id.topBarContainer), findViewById(R.id.bottomActionContainer));

        testStepView = findViewById(R.id.testStepView);
        testTitleView = findViewById(R.id.testTitleView);
        testInstructionView = findViewById(R.id.testInstructionView);
        liveStatusView = findViewById(R.id.liveStatusView);
        dynamicContent = findViewById(R.id.dynamicContent);
        primaryActionButton = findViewById(R.id.primaryActionButton);
        passButton = findViewById(R.id.passButton);
        issueButton = findViewById(R.id.issueButton);

        renderCurrentStep();
    }

    @Override
    public void onBackPressed() {
        finishWithCoverage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (currentIndex < steps.size() && UserTestManager.TEST_BUTTONS.equals(steps.get(currentIndex).name)) {
            if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP) {
                volumeUpPressed = true;
                updateButtonKeyStatus();
                return true;
            }
            if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) {
                volumeDownPressed = true;
                updateButtonKeyStatus();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void renderCurrentStep() {
        if (currentIndex >= steps.size()) {
            finishWithCoverage();
            return;
        }

        DiagnosticStep step = steps.get(currentIndex);
        testStepView.setText(String.format(Locale.getDefault(), "Step %d of %d", currentIndex + 1, steps.size()));
        testTitleView.setText(step.name + " Test");
        testInstructionView.setText(step.instructions);
        dynamicContent.removeAllViews();
        primaryActionButton.setOnClickListener(null);
        passButton.setOnClickListener(null);
        issueButton.setOnClickListener(null);
        passButton.setEnabled(true);
        issueButton.setEnabled(true);

        switch (step.name) {
            case UserTestManager.TEST_DISPLAY:
                setupDisplayTest();
                break;
            case UserTestManager.TEST_TOUCH:
                setupTouchTest();
                break;
            case UserTestManager.TEST_AUDIO:
                setupAudioTest();
                break;
            case UserTestManager.TEST_BUTTONS:
                setupButtonsTest();
                break;
            default:
                liveStatusView.setText("Unknown test step.");
                completeCurrentStep(TestResult.STATUS_SKIPPED, "Unsupported diagnostic step.");
                break;
        }
    }

    private void setupDisplayTest() {
        LinearLayout container = createVerticalContainer();
        displayPreview = new View(this);
        displayPreview.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(190)
        ));

        displayCaption = new TextView(this);
        displayCaption.setTextSize(16f);
        displayCaption.setPadding(0, dp(12), 0, 0);

        container.addView(displayPreview);
        container.addView(displayCaption);
        dynamicContent.addView(container);

        primaryActionButton.setVisibility(View.VISIBLE);
        primaryActionButton.setText(R.string.btn_cycle_color);
        primaryActionButton.setOnClickListener(v -> cycleDisplayColor());

        displayPreview.setOnClickListener(v -> enterFullScreenDisplay());

        passButton.setText(R.string.btn_mark_passed);
        issueButton.setText(R.string.btn_mark_issue);
        passButton.setOnClickListener(v -> completeCurrentStep(TestResult.STATUS_PASSED, "Display colors rendered cleanly."));
        issueButton.setOnClickListener(v -> completeCurrentStep(TestResult.STATUS_ISSUE, "User observed a display rendering issue."));

        liveStatusView.setText("Tap the preview to enter Full Screen test.");
        cycleDisplayColor();
    }

    private void enterFullScreenDisplay() {
        fullScreenOverlay = new View(this);
        fullScreenOverlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        fullScreenOverlay.setBackgroundColor(displayColors[displayColorIndex]);

        // Max brightness
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (originalBrightness < 0) originalBrightness = lp.screenBrightness;
        lp.screenBrightness = 1.0f;
        window.setAttributes(lp);

        // Immersive mode
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        fullScreenOverlay.setOnClickListener(v -> {
            displayColorIndex = (displayColorIndex + 1) % displayColors.length;
            if (displayColorIndex == 0) {
                // Exit full screen after one full cycle
                exitFullScreen();
            } else {
                fullScreenOverlay.setBackgroundColor(displayColors[displayColorIndex]);
            }
        });

        ((ViewGroup) findViewById(android.R.id.content)).addView(fullScreenOverlay);
    }

    private void exitFullScreen() {
        if (fullScreenOverlay != null) {
            ((ViewGroup) findViewById(android.R.id.content)).removeView(fullScreenOverlay);
            fullScreenOverlay = null;

            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            if (originalBrightness >= 0) lp.screenBrightness = originalBrightness;
            window.setAttributes(lp);

            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            AppChrome.applyEdgeToEdge(this, findViewById(R.id.topBarContainer), findViewById(R.id.bottomActionContainer));
            
            if (UserTestManager.TEST_DISPLAY.equals(steps.get(currentIndex).name)) {
                cycleDisplayColor();
            } else if (UserTestManager.TEST_TOUCH.equals(steps.get(currentIndex).name)) {
                updateTouchStatus();
            }
        }
    }

    private void setupTouchTest() {
        touchedCells = 0;

        LinearLayout container = createVerticalContainer();
        TextView infoView = new TextView(this);
        infoView.setText("Test full-screen touch registration to ensure no dead zones exist on the panel.");
        infoView.setTextSize(16f);
        container.addView(infoView);
        dynamicContent.addView(container);

        primaryActionButton.setVisibility(View.VISIBLE);
        primaryActionButton.setText("Start Full Screen Touch");
        primaryActionButton.setOnClickListener(v -> enterFullScreenTouch());

        passButton.setText(R.string.btn_mark_passed);
        passButton.setEnabled(false);
        passButton.setOnClickListener(v -> completeCurrentStep(
                TestResult.STATUS_PASSED,
                String.format(Locale.getDefault(), "Touch grid completed with %d of 12 zones activated.", touchedCells)
        ));
        issueButton.setText(R.string.btn_mark_issue);
        issueButton.setOnClickListener(v -> completeCurrentStep(
                TestResult.STATUS_ISSUE,
                String.format(Locale.getDefault(), "Touch issue reported after %d of 12 zones.", touchedCells)
        ));

        liveStatusView.setText("Tap 'Start' to test complete display area.");
    }

    private void enterFullScreenTouch() {
        touchedCells = 0;
        int totalCells = 12; // 3x4 grid for better coverage

        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setColumnCount(3);
        gridLayout.setRowCount(4);
        gridLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        gridLayout.setBackgroundColor(Color.BLACK);

        for (int i = 0; i < totalCells; i++) {
            MaterialCardView cell = new MaterialCardView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            cell.setLayoutParams(params);
            cell.setRadius(0f);
            cell.setStrokeWidth(dp(1));
            cell.setStrokeColor(0x44FFFFFF);
            cell.setCardBackgroundColor(Color.TRANSPARENT);

            cell.setTag(false);
            cell.setOnClickListener(v -> {
                if (Boolean.FALSE.equals(cell.getTag())) {
                    cell.setTag(true);
                    cell.setCardBackgroundColor(0x884EA66E);
                    touchedCells++;
                    if (touchedCells == totalCells) {
                        exitFullScreen();
                    }
                }
            });
            gridLayout.addView(cell);
        }

        fullScreenOverlay = gridLayout;

        // Immersive mode
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        ((ViewGroup) findViewById(android.R.id.content)).addView(fullScreenOverlay);
    }

    private void setupAudioTest() {
        audioPlayed = false;

        LinearLayout container = createVerticalContainer();
        TextView infoView = new TextView(this);
        infoView.setText(R.string.test_audio_hint);
        infoView.setTextSize(16f);
        infoView.setLineSpacing(0f, 1.1f);
        container.addView(infoView);
        dynamicContent.addView(container);

        primaryActionButton.setVisibility(View.VISIBLE);
        primaryActionButton.setText(R.string.btn_play_tone);
        primaryActionButton.setOnClickListener(v -> {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 90);
            }
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 800);
            audioPlayed = true;
            passButton.setEnabled(true);
            liveStatusView.setText(R.string.test_audio_played);
        });

        passButton.setText(R.string.btn_mark_passed);
        passButton.setEnabled(false);
        passButton.setOnClickListener(v -> completeCurrentStep(
                TestResult.STATUS_PASSED,
                audioPlayed ? "Speaker tone was audible and clear." : "User passed audio without replay."
        ));
        issueButton.setText(R.string.btn_mark_issue);
        issueButton.setOnClickListener(v -> completeCurrentStep(TestResult.STATUS_ISSUE, "User reported distorted or missing speaker output."));

        liveStatusView.setText(R.string.test_audio_status);
    }

    private void setupButtonsTest() {
        volumeUpPressed = false;
        volumeDownPressed = false;

        LinearLayout container = createVerticalContainer();
        volumeUpStatusView = buildStatusTile(container, getString(R.string.test_buttons_up_label));
        volumeDownStatusView = buildStatusTile(container, getString(R.string.test_buttons_down_label));
        dynamicContent.addView(container);

        primaryActionButton.setVisibility(View.VISIBLE);
        primaryActionButton.setText(R.string.btn_reset_keys);
        primaryActionButton.setOnClickListener(v -> renderCurrentStep());
        passButton.setText(R.string.btn_mark_passed);
        passButton.setEnabled(false);
        passButton.setOnClickListener(v -> completeCurrentStep(TestResult.STATUS_PASSED, "Volume up and volume down were both detected."));
        issueButton.setText(R.string.btn_mark_issue);
        issueButton.setOnClickListener(v -> completeCurrentStep(TestResult.STATUS_ISSUE, "Hardware key press was missed or inconsistent."));

        updateButtonKeyStatus();
    }

    private void cycleDisplayColor() {
        displayColorIndex = (displayColorIndex + 1) % displayColors.length;
        displayPreview.setBackgroundColor(displayColors[displayColorIndex]);
        displayCaption.setText(getString(R.string.format_test_color, displayLabels[displayColorIndex]));
    }

    private void updateTouchStatus() {
        int total = 12;
        liveStatusView.setText(getString(R.string.format_touch_progress, touchedCells, total));
        passButton.setEnabled(touchedCells == total);
    }

    private void updateButtonKeyStatus() {
        if (volumeUpStatusView != null) {
            volumeUpStatusView.setText(getString(
                    R.string.format_key_status,
                    getString(R.string.test_buttons_up_label),
                    volumeUpPressed ? getString(R.string.key_detected) : getString(R.string.key_waiting)
            ));
        }
        if (volumeDownStatusView != null) {
            volumeDownStatusView.setText(getString(
                    R.string.format_key_status,
                    getString(R.string.test_buttons_down_label),
                    volumeDownPressed ? getString(R.string.key_detected) : getString(R.string.key_waiting)
            ));
        }

        passButton.setEnabled(volumeUpPressed && volumeDownPressed);
        liveStatusView.setText(volumeUpPressed && volumeDownPressed
                ? getString(R.string.test_buttons_ready)
                : getString(R.string.test_buttons_status));
    }

    private TextView buildStatusTile(LinearLayout container, String title) {
        MaterialCardView cardView = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(12);
        cardView.setLayoutParams(cardParams);
        cardView.setCardBackgroundColor(0xFFEFF4F5);

        TextView textView = new TextView(this);
        textView.setPadding(dp(16), dp(16), dp(16), dp(16));
        textView.setTextSize(16f);
        cardView.addView(textView);
        container.addView(cardView);

        textView.setText(title);
        return textView;
    }

    private void completeCurrentStep(String status, String details) {
        results.add(new TestResult(steps.get(currentIndex).name, status, details));
        currentIndex++;
        renderCurrentStep();
    }

    private void finishWithCoverage() {
        Intent data = new Intent();
        UserTestManager.putResults(data, UserTestManager.ensureCoverage(results));
        setResult(RESULT_OK, data);
        finish();
    }

    private LinearLayout createVerticalContainer() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return layout;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class DiagnosticStep {
        private final String name;
        private final String instructions;

        DiagnosticStep(String name, String instructions) {
            this.name = name;
            this.instructions = instructions;
        }
    }
}
