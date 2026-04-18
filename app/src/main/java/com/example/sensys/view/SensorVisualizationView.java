package com.example.sensys.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import com.example.sensys.R;

import java.util.Locale;

public class SensorVisualizationView extends View {
        /**
         * Returns true if the given sensor type is supported for visualization.
         */
        public static boolean isVisualizationSupported(int sensorType) {
            switch (sensorType) {
                case Sensor.TYPE_LIGHT:
                case Sensor.TYPE_PROXIMITY:
                case Sensor.TYPE_PRESSURE:
                case Sensor.TYPE_STEP_COUNTER:
                case Sensor.TYPE_STEP_DETECTOR:
                case Sensor.TYPE_GYROSCOPE:
                case Sensor.TYPE_ROTATION_VECTOR:
                case Sensor.TYPE_MAGNETIC_FIELD:
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                case Sensor.TYPE_GRAVITY:
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    return true;
                default:
                    return false;
            }
        }
    private final Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint secondaryPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private int sensorType = -1;
    private float sensorMaximumRange = 1f;
    private float[] values = new float[0];
    private long lastPulseAt = 0L;

    public SensorVisualizationView(Context context) {
        super(context);
        init();
    }

    public SensorVisualizationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SensorVisualizationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        framePaint.setStyle(Paint.Style.FILL);
        framePaint.setColor(Color.parseColor("#E8EEF0"));

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.parseColor("#1F6D78"));

        accentPaint.setStyle(Paint.Style.FILL);
        accentPaint.setColor(Color.parseColor("#F47C48"));

        secondaryPaint.setStyle(Paint.Style.STROKE);
        secondaryPaint.setStrokeWidth(6f);
        secondaryPaint.setColor(Color.parseColor("#23414A"));

        textPaint.setColor(Color.parseColor("#183036"));
        textPaint.setTextSize(38f);
        textPaint.setFakeBoldText(true);
    }

    public void setSensorMetadata(int sensorType, float sensorMaximumRange) {
        this.sensorType = sensorType;
        this.sensorMaximumRange = sensorMaximumRange <= 0f ? 1f : sensorMaximumRange;
        invalidate();
    }

    public void setSensorData(int sensorType, float[] values) {
        this.sensorType = sensorType;
        this.values = values == null ? new float[0] : values.clone();
        if (sensorType == Sensor.TYPE_STEP_DETECTOR && this.values.length > 0 && this.values[0] == 1f) {
            lastPulseAt = SystemClock.uptimeMillis();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float padding = 18f;
        rect.set(padding, padding, getWidth() - padding, getHeight() - padding);
        canvas.drawRoundRect(rect, 34f, 34f, framePaint);

        switch (sensorType) {
            case Sensor.TYPE_LIGHT:
                drawLight(canvas);
                break;
            case Sensor.TYPE_PROXIMITY:
                drawProximity(canvas);
                break;
            case Sensor.TYPE_PRESSURE:
                drawPressure(canvas);
                break;
            case Sensor.TYPE_STEP_COUNTER:
                drawStepCounter(canvas);
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                drawStepPulse(canvas);
                break;
            case Sensor.TYPE_GYROSCOPE:
            case Sensor.TYPE_ROTATION_VECTOR:
            case Sensor.TYPE_MAGNETIC_FIELD:
                drawRotation(canvas);
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                drawTemperature(canvas);
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                drawHumidity(canvas);
                break;
            case Sensor.TYPE_GRAVITY:
                drawGravity(canvas);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                drawLinearAcceleration(canvas);
                break;
            default:
                drawMotionDot(canvas);
                break;
        }
    }

    private void drawGravity(Canvas canvas) {
        float x = getValue(0);
        float y = getValue(1);
        float z = getValue(2);
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
        if (magnitude == 0) magnitude = 1f;

        float centerX = rect.centerX();
        float centerY = rect.centerY();
        float arrowLength = Math.min(rect.width(), rect.height()) * 0.35f;

        // Draw background circle
        secondaryPaint.setStyle(Paint.Style.STROKE);
        secondaryPaint.setStrokeWidth(2f);
        canvas.drawCircle(centerX, centerY, arrowLength, secondaryPaint);

        // Draw arrow
        accentPaint.setColor(Color.parseColor("#F47C48"));
        accentPaint.setStrokeWidth(8f);
        float endX = centerX + (x / magnitude) * arrowLength;
        float endY = centerY + (y / magnitude) * arrowLength;
        canvas.drawLine(centerX, centerY, endX, endY, accentPaint);
        canvas.drawCircle(endX, endY, 12f, accentPaint);

        canvas.drawText("Gravity vector", rect.left + 36f, rect.bottom - 40f, textPaint);
    }

    private void drawLinearAcceleration(Canvas canvas) {
        float mag = (float) Math.sqrt(Math.pow(getValue(0), 2) + Math.pow(getValue(1), 2) + Math.pow(getValue(2), 2));
        float normalized = clamp(mag / 10f, 0f, 1f);

        float barWidth = rect.width() * 0.7f;
        float barHeight = 40f;
        float left = rect.centerX() - barWidth / 2f;
        float top = rect.centerY() - barHeight / 2f;

        fillPaint.setColor(Color.parseColor("#CCE2E7"));
        canvas.drawRoundRect(left, top, left + barWidth, top + barHeight, 20f, 20f, fillPaint);

        accentPaint.setColor(Color.parseColor("#1F6D78"));
        canvas.drawRoundRect(left, top, left + (barWidth * normalized), top + barHeight, 20f, 20f, accentPaint);

        canvas.drawText(String.format(Locale.getDefault(), "Intensity: %.2f m/s2", mag), rect.left + 36f, rect.bottom - 40f, textPaint);
    }

    private void drawLight(Canvas canvas) {
        float lux = getValue(0);
        float normalized = clamp(lux / 1000f, 0f, 1f);
        int brightness = (int) (85 + normalized * 170);
        fillPaint.setColor(Color.rgb(brightness, Math.min(255, brightness + 20), 160));
        canvas.drawRoundRect(rect.left + 24f, rect.top + 24f, rect.right - 24f, rect.bottom - 24f, 28f, 28f, fillPaint);

        accentPaint.setColor(Color.parseColor("#FFF4B8"));
        float radius = 36f + (normalized * 38f);
        canvas.drawCircle(getWidth() * 0.5f, getHeight() * 0.42f, radius, accentPaint);
        canvas.drawText(getContext().getString(R.string.format_lux_value, lux), rect.left + 36f, rect.bottom - 40f, textPaint);
    }

    private void drawProximity(Canvas canvas) {
        float value = getValue(0);
        boolean near = value <= Math.max(1f, sensorMaximumRange * 0.35f);
        fillPaint.setColor(near ? Color.parseColor("#F47C48") : Color.parseColor("#4EA66E"));
        canvas.drawRoundRect(rect.left + 24f, rect.top + 24f, rect.right - 24f, rect.bottom - 24f, 28f, 28f, fillPaint);
        textPaint.setTextSize(64f);
        canvas.drawText(getContext().getString(near ? R.string.label_near : R.string.label_far), rect.left + 36f, rect.centerY() + 16f, textPaint);
        textPaint.setTextSize(38f);
        canvas.drawText(getContext().getString(R.string.format_cm_value, value), rect.left + 36f, rect.bottom - 40f, textPaint);
    }

    private void drawPressure(Canvas canvas) {
        float pressure = getValue(0);
        float normalized = clamp((pressure - 900f) / 200f, 0f, 1f);
        float barLeft = rect.left + 42f;
        float barRight = rect.right - 42f;
        float barBottom = rect.bottom - 42f;
        float barTop = rect.top + 42f;

        fillPaint.setColor(Color.parseColor("#CCE2E7"));
        canvas.drawRoundRect(barLeft, barTop, barRight, barBottom, 24f, 24f, fillPaint);

        accentPaint.setColor(Color.parseColor("#1F6D78"));
        float activeTop = barBottom - ((barBottom - barTop) * normalized);
        canvas.drawRoundRect(barLeft, activeTop, barRight, barBottom, 24f, 24f, accentPaint);
        canvas.drawText(getContext().getString(R.string.format_hpa_value, pressure), rect.left + 36f, rect.top + 68f, textPaint);
    }

    private void drawStepCounter(Canvas canvas) {
        float count = getValue(0);
        accentPaint.setColor(Color.parseColor("#4EA66E"));
        canvas.drawCircle(rect.centerX(), rect.centerY(), Math.min(rect.width(), rect.height()) * 0.24f, accentPaint);
        textPaint.setTextSize(56f);
        canvas.drawText(String.format(Locale.getDefault(), "%.0f", count), rect.centerX() - 34f, rect.centerY() + 16f, textPaint);
        textPaint.setTextSize(32f);
        canvas.drawText(getContext().getString(R.string.label_total_steps), rect.left + 36f, rect.bottom - 38f, textPaint);
        textPaint.setTextSize(38f);
    }

    private void drawStepPulse(Canvas canvas) {
        long elapsed = SystemClock.uptimeMillis() - lastPulseAt;
        boolean pulseVisible = elapsed < 450L;

        fillPaint.setColor(pulseVisible ? Color.parseColor("#F47C48") : Color.parseColor("#A8C8D0"));
        float radius = pulseVisible ? Math.min(rect.width(), rect.height()) * 0.28f : Math.min(rect.width(), rect.height()) * 0.18f;
        canvas.drawCircle(rect.centerX(), rect.centerY(), radius, fillPaint);
        canvas.drawText(getContext().getString(pulseVisible ? R.string.label_step : R.string.label_ready), rect.left + 36f, rect.bottom - 40f, textPaint);
    }

    private void drawRotation(Canvas canvas) {
        float x = getValue(0);
        float y = getValue(1);
        float magnitude = (float) Math.sqrt((x * x) + (y * y));
        float angle = (float) Math.atan2(y, x);
        float radius = Math.min(rect.width(), rect.height()) * 0.30f;

        secondaryPaint.setStyle(Paint.Style.STROKE);
        secondaryPaint.setStrokeWidth(8f);
        canvas.drawCircle(rect.centerX(), rect.centerY(), radius, secondaryPaint);

        accentPaint.setColor(Color.parseColor("#1F6D78"));
        float lineLength = radius * clamp(0.4f + (magnitude / 6f), 0.4f, 1f);
        float endX = rect.centerX() + ((float) Math.cos(angle) * lineLength);
        float endY = rect.centerY() + ((float) Math.sin(angle) * lineLength);
        canvas.drawCircle(endX, endY, 18f, accentPaint);
        canvas.drawLine(rect.centerX(), rect.centerY(), endX, endY, secondaryPaint);
        canvas.drawText(getContext().getString(R.string.format_motion_magnitude, magnitude), rect.left + 36f, rect.bottom - 40f, textPaint);
    }

    private void drawTemperature(Canvas canvas) {
        float temp = getValue(0);
        float normalized = clamp((temp + 10f) / 60f, 0f, 1f); // Range -10 to 50
        float barLeft = rect.centerX() - 30f;
        float barRight = rect.centerX() + 30f;
        float barBottom = rect.bottom - 80f;
        float barTop = rect.top + 60f;

        fillPaint.setColor(Color.parseColor("#CCE2E7"));
        canvas.drawRoundRect(barLeft, barTop, barRight, barBottom, 30f, 30f, fillPaint);

        accentPaint.setColor(temp > 30 ? Color.RED : Color.BLUE);
        float activeTop = barBottom - ((barBottom - barTop) * normalized);
        canvas.drawRoundRect(barLeft, activeTop, barRight, barBottom, 30f, 30f, accentPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.1f °C", temp), rect.left + 36f, rect.bottom - 40f, textPaint);
    }

    private void drawHumidity(Canvas canvas) {
        float humidity = getValue(0);
        float normalized = clamp(humidity / 100f, 0f, 1f);
        accentPaint.setColor(Color.parseColor("#3498db"));
        canvas.drawArc(rect.centerX() - 100f, rect.centerY() - 100f, rect.centerX() + 100f, rect.centerY() + 100f, 180, 180 * normalized, true, accentPaint);
        canvas.drawText(String.format(Locale.getDefault(), "%.1f %%", humidity), rect.left + 36f, rect.bottom - 40f, textPaint);
    }

    private void drawMotionDot(Canvas canvas) {
        float x = getValue(0);
        float y = getValue(1);
        float normalizedX = clamp(x / 10f, -1f, 1f);
        float normalizedY = clamp(y / 10f, -1f, 1f);
        float centerX = rect.centerX();
        float centerY = rect.centerY();

        secondaryPaint.setStyle(Paint.Style.STROKE);
        secondaryPaint.setStrokeWidth(4f);
        canvas.drawLine(rect.left + 30f, centerY, rect.right - 30f, centerY, secondaryPaint);
        canvas.drawLine(centerX, rect.top + 30f, centerX, rect.bottom - 30f, secondaryPaint);

        accentPaint.setColor(Color.parseColor("#F47C48"));
        float dotX = centerX + (normalizedX * rect.width() * 0.28f);
        float dotY = centerY + (normalizedY * rect.height() * 0.24f);
        canvas.drawCircle(dotX, dotY, 22f, accentPaint);
        canvas.drawText(getContext().getString(R.string.label_motion_map), rect.left + 36f, rect.bottom - 40f, textPaint);
    }

    private float getValue(int index) {
        return index < values.length ? values[index] : 0f;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
