package smallville7123.vstmanager.core;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.lang.reflect.Method;

import static smallville7123.vstmanager.core.ReflectionHelpers.ClassParameter.from;
import static smallville7123.vstmanager.core.ReflectionHelpers.callInstanceMethod;
import static smallville7123.vstmanager.core.ReflectionHelpers.setField;

public class ReflectionActivityController {

    ReflectionActivity reflectionActivity;
    Object reflectionInstance;
    ReflectionActivity localInstance = null;
    private boolean reflectionRequired = false;

    public ReflectionActivityController(ReflectionActivity reflectionActivity, Class callback) {
        this.reflectionActivity = reflectionActivity;
        reflectionInstance = ReflectionHelpers.newInstance(callback);
        try {
            localInstance = (ReflectionActivity) reflectionInstance;
        } catch (ClassCastException e) {
            reflectionRequired = true;
        }
    }

    public void setUiThread(Handler uiThread) {
        if (reflectionRequired) {
            setField(reflectionInstance, "UiThread", uiThread);
        } else {
            localInstance.UiThread = uiThread;
        }
    }

    public void setContentRoot(ViewGroup contentRoot) {
        if (reflectionRequired) {
            setField(reflectionInstance, "mContentRoot", contentRoot);
        } else {
            localInstance.mContentRoot = contentRoot;
        }
    }

    public void attachBaseContext(Context base) {
        if (reflectionRequired) {
            callInstanceMethod(reflectionInstance, "attachBaseContext", from(Context.class, base));
        } else {
            callInstanceMethod(localInstance, "attachBaseContext", from(Context.class, base));
        }
    }

    public void setLayoutInflater(LayoutInflater layoutInflater) {
        if (reflectionRequired) {
            setField(reflectionInstance, "layoutInflater", layoutInflater);
        } else {
            localInstance.layoutInflater = layoutInflater;
        }
    }

    public void onCreate(Bundle bundle) {
        if (reflectionRequired) {
            reflectionActivity.runOnUIThread(() -> callInstanceMethod(reflectionInstance, "onCreate", from(Bundle.class, bundle)));
        } else {
            reflectionActivity.runOnUIThread(() -> localInstance.onCreate(bundle));
        }
    }

    private void runOnUIThread(Runnable r) {
        if (reflectionRequired) {
            ReflectionHelpers.traverseClass(reflectionInstance.getClass(), new ReflectionHelpers.TraversalRunnable() {
                @Override
                boolean run(Object argument) {
                    try {
                        Class C = ((Class)argument);
                        Method m = C.getMethod("runOnUIThread", Runnable.class);
                        m.invoke(reflectionInstance, r);
                        return true;
                    } catch (ReflectiveOperationException e) {
                        return false;
                    }
                }
            });
        } else {
            localInstance.runOnUIThread(r);
        }
    }
}
