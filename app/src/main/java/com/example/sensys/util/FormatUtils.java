package com.example.sensys.util;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.Formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FormatUtils {
    public static final String NOT_AVAILABLE = "Not Available";

    private FormatUtils() {
    }

    public static String fallback(String value) {
        return TextUtils.isEmpty(value) ? NOT_AVAILABLE : value;
    }

    public static String formatBytes(Context context, long bytes) {
        try {
            return Formatter.formatShortFileSize(context, Math.max(bytes, 0L));
        } catch (Exception ignored) {
            return String.format(Locale.getDefault(), "%d B", Math.max(bytes, 0L));
        }
    }

    public static String formatPercent(int percent) {
        return percent >= 0 ? String.format(Locale.getDefault(), "%d%%", percent) : NOT_AVAILABLE;
    }

    public static String formatTemperature(float temperatureCelsius) {
        if (Float.isNaN(temperatureCelsius) || Float.isInfinite(temperatureCelsius)) {
            return NOT_AVAILABLE;
        }
        return String.format(Locale.getDefault(), "%.1f C", temperatureCelsius);
    }

    public static String formatNumber(float value, String unit) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return NOT_AVAILABLE;
        }
        if (TextUtils.isEmpty(unit)) {
            return String.format(Locale.getDefault(), "%.2f", value);
        }
        return String.format(Locale.getDefault(), "%.2f %s", value, unit);
    }

    public static String formatInteger(float value, String unit) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            return NOT_AVAILABLE;
        }
        if (TextUtils.isEmpty(unit)) {
            return String.format(Locale.getDefault(), "%.0f", value);
        }
        return String.format(Locale.getDefault(), "%.0f %s", value, unit);
    }

    public static String joinValues(float[] values) {
        if (values == null || values.length == 0) {
            return NOT_AVAILABLE;
        }

        List<String> parts = new ArrayList<>();
        for (int index = 0; index < values.length; index++) {
            parts.add(String.format(Locale.getDefault(), "v%d=%.2f", index, values[index]));
        }
        return TextUtils.join(", ", parts);
    }

    public static String safeIp(String value) {
        return TextUtils.isEmpty(value) ? NOT_AVAILABLE : value;
    }

    public static String joinNonEmpty(String separator, String... values) {
        List<String> parts = new ArrayList<>();
        for (String value : values) {
            if (!TextUtils.isEmpty(value) && !NOT_AVAILABLE.equals(value)) {
                parts.add(value);
            }
        }
        return parts.isEmpty() ? NOT_AVAILABLE : TextUtils.join(separator, parts);
    }
}
