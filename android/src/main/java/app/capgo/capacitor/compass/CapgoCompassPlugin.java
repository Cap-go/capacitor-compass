package app.capgo.capacitor.compass;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "CapgoCompass")
public class CapgoCompassPlugin extends Plugin {

    private final String pluginVersion = "7.1.1";
    private CapgoCompass implementation;
    private boolean isListening = false;

    @Override
    public void load() {
        this.implementation = new CapgoCompass(getActivity());
    }

    @Override
    public void handleOnResume() {
        super.handleOnResume();
        if (this.implementation != null && isListening) {
            this.implementation.registerListeners();
        }
    }

    @Override
    public void handleOnPause() {
        super.handleOnPause();
        if (this.implementation != null) {
            this.implementation.unregisterListeners();
        }
    }

    @PluginMethod
    public void getCurrentHeading(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.getCurrentHeading());
        call.resolve(ret);
    }

    @PluginMethod
    public void getPluginVersion(final PluginCall call) {
        try {
            final JSObject ret = new JSObject();
            ret.put("version", this.pluginVersion);
            call.resolve(ret);
        } catch (final Exception e) {
            call.reject("Could not get plugin version", e);
        }
    }

    @PluginMethod
    public void startListening(PluginCall call) {
        if (isListening) {
            call.resolve();
            return;
        }

        // Parse optional throttling configuration
        Integer minInterval = call.getInt("minInterval");
        Double minHeadingChange = call.getDouble("minHeadingChange");

        long intervalMs = minInterval != null ? minInterval.longValue() : 100L;
        float headingChange = minHeadingChange != null ? minHeadingChange.floatValue() : 2.0f;
        implementation.setThrottling(intervalMs, headingChange);

        isListening = true;
        implementation.setHeadingCallback((heading) -> {
            JSObject ret = new JSObject();
            ret.put("value", heading);
            notifyListeners("headingChange", ret);
        });

        implementation.registerListeners();
        call.resolve();
    }

    @PluginMethod
    public void stopListening(PluginCall call) {
        if (!isListening) {
            call.resolve();
            return;
        }

        isListening = false;
        implementation.setHeadingCallback(null);
        implementation.unregisterListeners();

        call.resolve();
    }

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        // Android does not require any permissions for compass/sensor access
        JSObject ret = new JSObject();
        ret.put("compass", "granted");
        call.resolve(ret);
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        // Android does not require any permissions for compass/sensor access
        JSObject ret = new JSObject();
        ret.put("compass", "granted");
        call.resolve(ret);
    }
}
