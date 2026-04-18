package com.example.sensys.diagnostics;

import android.content.Intent;

import com.example.sensys.model.TestResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class UserTestManager {
    public static final String EXTRA_TEST_RESULTS = "com.example.sensys.extra.TEST_RESULTS";

    public static final String TEST_DISPLAY = "Display";
    public static final String TEST_TOUCH = "Touch";
    public static final String TEST_AUDIO = "Audio";
    public static final String TEST_BUTTONS = "Buttons";

    private UserTestManager() {
    }

    public static ArrayList<String> getOrderedTestNames() {
        ArrayList<String> names = new ArrayList<>();
        names.add(TEST_DISPLAY);
        names.add(TEST_TOUCH);
        names.add(TEST_AUDIO);
        names.add(TEST_BUTTONS);
        return names;
    }

    public static ArrayList<TestResult> createPendingResults() {
        ArrayList<TestResult> results = new ArrayList<>();
        for (String name : getOrderedTestNames()) {
            results.add(new TestResult(name, TestResult.STATUS_PENDING, "Test not run yet."));
        }
        return results;
    }

    public static ArrayList<TestResult> createSkippedResults() {
        ArrayList<TestResult> results = new ArrayList<>();
        for (String name : getOrderedTestNames()) {
            results.add(new TestResult(name, TestResult.STATUS_SKIPPED, "User skipped this test."));
        }
        return results;
    }

    public static ArrayList<TestResult> ensureCoverage(List<TestResult> incoming) {
        Map<String, TestResult> ordered = new LinkedHashMap<>();
        for (String name : getOrderedTestNames()) {
            ordered.put(name, new TestResult(name, TestResult.STATUS_SKIPPED, "User skipped this test."));
        }

        if (incoming != null) {
            for (TestResult result : incoming) {
                if (result != null && ordered.containsKey(result.getName())) {
                    ordered.put(result.getName(), result);
                }
            }
        }
        return new ArrayList<>(ordered.values());
    }

    public static void putResults(Intent intent, ArrayList<TestResult> results) {
        intent.putExtra(EXTRA_TEST_RESULTS, results);
    }

    public static ArrayList<TestResult> fromIntent(Intent intent) {
        if (intent == null) {
            return createPendingResults();
        }

        Serializable extra = intent.getSerializableExtra(EXTRA_TEST_RESULTS);
        if (!(extra instanceof ArrayList<?>)) {
            return createPendingResults();
        }

        ArrayList<?> rawList = (ArrayList<?>) extra;
        ArrayList<TestResult> results = new ArrayList<>();
        for (Object item : rawList) {
            if (item instanceof TestResult) {
                results.add((TestResult) item);
            }
        }
        return ensureCoverage(results);
    }

    public static boolean hasCompletedDiagnostics(List<TestResult> results) {
        if (results == null) {
            return false;
        }

        for (TestResult result : results) {
            String status = result.getStatus();
            if (TestResult.STATUS_PASSED.equals(status) || TestResult.STATUS_ISSUE.equals(status)) {
                return true;
            }
        }
        return false;
    }

    public static String summarize(List<TestResult> results) {
        int passed = 0;
        int issues = 0;
        int pending = 0;
        int skipped = 0;

        for (TestResult result : ensureCoverage(results)) {
            switch (result.getStatus()) {
                case TestResult.STATUS_PASSED:
                    passed++;
                    break;
                case TestResult.STATUS_ISSUE:
                    issues++;
                    break;
                case TestResult.STATUS_PENDING:
                    pending++;
                    break;
                default:
                    skipped++;
                    break;
            }
        }

        return String.format(
                Locale.getDefault(),
                "%d passed, %d issues, %d pending, %d skipped",
                passed,
                issues,
                pending,
                skipped
        );
    }
}
