package app.capgo.capacitor.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import androidx.appcompat.app.AppCompatActivity;

public class CapgoCompass implements SensorEventListener {

    private static final String TAG = "CapgoCompass";

    // Default throttling constants
    private static final long DEFAULT_MIN_INTERVAL_MS = 100; // Max 10 events/sec
    private static final float DEFAULT_MIN_HEADING_CHANGE = 2.0f; // Minimum 2° change

    // Configurable throttling values
    private long minIntervalMs = DEFAULT_MIN_INTERVAL_MS;
    private float minHeadingChange = DEFAULT_MIN_HEADING_CHANGE;

    private AppCompatActivity activity;
    private SensorManager sensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;
    private float[] gravityValues = new float[3];
    private float[] magneticValues = new float[3];
    private HeadingCallback headingCallback;

    // Background thread for sensor processing
    private HandlerThread sensorThread;
    private Handler sensorHandler;

    // Throttling state
    private volatile long lastNotifyTime = 0;
    private volatile float lastNotifiedHeading = -1;

    public interface HeadingCallback {
        void onHeadingChanged(float heading);
    }

    public CapgoCompass(AppCompatActivity activity) {
        Log.d(TAG, "Initializing CapgoCompass");
        this.activity = activity;
        this.sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        this.magnetometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        Log.d(TAG, "SensorManager: " + (this.sensorManager != null) + " Magnetometer: " + (this.magnetometer != null));

        if (accelerometer == null || magnetometer == null) {
            Log.e(TAG, "Accelerometer or magnetometer sensor not found on this device.");
        }
    }

    public float getCurrentHeading() {
        return this.calculateCurrentHeading();
    }

    public void setHeadingCallback(HeadingCallback callback) {
        this.headingCallback = callback;
    }

    /**
     * Configure throttling parameters.
     * @param minIntervalMs Minimum interval between events in milliseconds (default: 100)
     * @param minHeadingChange Minimum heading change in degrees to trigger an event (default: 2.0)
     */
    public void setThrottling(long minIntervalMs, float minHeadingChange) {
        this.minIntervalMs = minIntervalMs > 0 ? minIntervalMs : DEFAULT_MIN_INTERVAL_MS;
        this.minHeadingChange = minHeadingChange > 0 ? minHeadingChange : DEFAULT_MIN_HEADING_CHANGE;
    }

    public void registerListeners() {
        // Create background thread for sensor processing
        sensorThread = new HandlerThread("CompassSensorThread");
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());

        // Reset throttling state
        lastNotifyTime = 0;
        lastNotifiedHeading = -1;

        if (this.magnetometer != null) {
            this.sensorManager.registerListener(this, this.magnetometer, SensorManager.SENSOR_DELAY_NORMAL, sensorHandler);
        }
        if (this.accelerometer != null) {
            this.sensorManager.registerListener(this, this.accelerometer, SensorManager.SENSOR_DELAY_NORMAL, sensorHandler);
        }
    }

    public void unregisterListeners() {
        this.sensorManager.unregisterListener(this);

        // Clean up background thread
        if (sensorThread != null) {
            sensorThread.quitSafely();
            sensorThread = null;
            sensorHandler = null;
        }
    }

    private DisplayRotation getDisplayRotation() {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        switch (rotation) {
            case Surface.ROTATION_90:
                return DisplayRotation.ROTATION_90;
            case Surface.ROTATION_180:
                return DisplayRotation.ROTATION_180;
            case Surface.ROTATION_270:
                return DisplayRotation.ROTATION_270;
            case Surface.ROTATION_0:
            default:
                return DisplayRotation.ROTATION_0;
        }
    }

    private float calculateCurrentHeading() {
        float bearing;

        Vector fieldVector = new Vector(this.magneticValues.clone());
        Vector gravityVector = new Vector(this.gravityValues.clone());
        gravityVector.normalize();
        Vector gravityDownVector = new Vector(0.0f, 0.0f, 1.0f);
        Vector axisVector = gravityVector.crossProduct(gravityDownVector);
        axisVector.normalize();
        double angle = Math.acos(gravityVector.dotProduct(gravityDownVector));

        Vector fieldRotatedVector = new Vector(axisVector);
        fieldRotatedVector.multiply(axisVector.dotProduct(fieldVector));
        Vector axisCrossProductField = new Vector(axisVector).crossProduct(fieldVector);
        Vector axisCrossProductFieldCosAngle = new Vector(axisCrossProductField);
        axisCrossProductFieldCosAngle.multiply(Math.cos(angle));
        Vector axisCrossProductFieldSinAngle = new Vector(axisCrossProductField);
        axisCrossProductFieldSinAngle.multiply(Math.sin(angle));
        fieldRotatedVector.add(axisCrossProductFieldCosAngle.crossProduct(axisVector));
        fieldRotatedVector.add(axisCrossProductFieldSinAngle);

        bearing = fieldRotatedVector.getYaw() - 90.0f;

        DisplayRotation displayRotation = getDisplayRotation();
        switch (displayRotation) {
            case ROTATION_90:
                bearing += 90.0f;
                break;
            case ROTATION_180:
                bearing += 180.0f;
                break;
            case ROTATION_270:
                bearing += 270.0f;
                break;
            case ROTATION_0:
            default:
                break;
        }

        float normalized = (bearing + 360.0f) % 360.0f;

        return normalized;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelerometer) {
            gravityValues = event.values.clone();
        } else if (event.sensor == magnetometer) {
            magneticValues = event.values.clone();
        }

        if (headingCallback != null) {
            float heading = calculateCurrentHeading();

            // Time-based throttle
            long now = System.currentTimeMillis();
            if (now - lastNotifyTime < minIntervalMs) {
                return; // Skip - too soon
            }

            // Heading change threshold (with wraparound handling)
            if (lastNotifiedHeading >= 0) {
                float diff = Math.abs(heading - lastNotifiedHeading);
                if (diff > 180) diff = 360 - diff;
                if (diff < minHeadingChange) {
                    return; // Skip - heading hasn't changed enough
                }
            }

            lastNotifyTime = now;
            lastNotifiedHeading = heading;

            // Post to main thread for WebView communication
            final float finalHeading = heading;
            activity.runOnUiThread(() -> {
                if (headingCallback != null) {
                    headingCallback.onHeadingChanged(finalHeading);
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
