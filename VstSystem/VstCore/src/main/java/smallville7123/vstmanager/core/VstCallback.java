package smallville7123.vstmanager.core;

import android.util.Log;

public interface VstCallback {
    static final String className = VstCallback.class.getName();
    static final String TAG = "VstCallback";

    public default void VST_CALLBACK_ON_CREATE() {
        Log.d(TAG, "VST_CALLBACK_ON_CREATE() called");
    }

    public default void VST_CALLBACK_ON_DESTROY() {
        Log.d(TAG, "VST_CALLBACK_ON_DESTROY() called");
    }
}
