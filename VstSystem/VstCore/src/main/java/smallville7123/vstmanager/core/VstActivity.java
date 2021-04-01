package smallville7123.vstmanager.core;

public abstract class VstActivity extends ReflectionActivity implements VstCallback {
    private static final String TAG = "VstActivity";

    public static long callNewNativeInstance(ReflectionActivity reflectionActivity) {
        Object client = reflectionActivity.getCurrentClient();
        if (client == null) {
            if (!(reflectionActivity instanceof VstActivity)) {
                throw new RuntimeException("ReflectionActivity must be an instance of VstActivity");
            }
            VstActivity vstActivity = (VstActivity) reflectionActivity;
            return vstActivity.newNativeInstance();
        }
        return ReflectionHelpers.callInstanceMethod(
                client,
                "newNativeInstance"
        );
    }

    public abstract long newNativeInstance();
}
