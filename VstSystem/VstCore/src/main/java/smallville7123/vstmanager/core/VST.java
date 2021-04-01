package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

public class VST {
    static String TAG = "VST";

    VstCore core;
    VstScanner scanner;

    String packageName;
    CharSequence label;
    Drawable icon;
    Drawable logo;
    Drawable banner;

    ApplicationInfo mApplicationInfo;

    Context applicationContext;
    ClassLoader classLoader;
    String callbackClassName;
    ArrayList<String> classFiles;
    ArrayList<Pair<Class, Integer>> callbacks;
    PackageManager mPackageManager;
    private ArrayList<Integer> cachedCallbacks;

    VST() {
        core = new VstCore();
    }

    VST(VstCore core) {
        this.core = core;
    }

    VST(VstScanner scanner) {
        core = new VstCore(scanner);
        this.scanner = scanner;
    }

    VST(VstCore core, VstScanner scanner) {
        this(core);
        if (core.scanner == null) {
            core.scanner = scanner;
            this.scanner = scanner;
        } else {
            this.scanner = core.scanner;
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public CharSequence getLabel() {
        return label;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Drawable getLogo() {
        return logo;
    }

    public Drawable getBanner() {
        return banner;
    }

    public boolean verify(Context context, PackageManager packageManager, ApplicationInfo mApplicationInfo) {
        scanner.runOnUiThread.run(() -> scanner.resetPackageStats());
        core.classTreeDepth = 0;
        this.mApplicationInfo = mApplicationInfo;
        mPackageManager = packageManager;
        packageName = mApplicationInfo.packageName;
        FileBundle packageInfo = scanner.scannerDatabase.getFileBundle(packageName);
        boolean[] hasVstCallback = null;
        if (packageInfo == null) {
            packageInfo = new FileBundle();
            scanner.scannerDatabase.putFileBundle(packageName, packageInfo);
            label = packageManager.getApplicationLabel(mApplicationInfo);
            if (label == null) {
                Log.e(TAG, "verify: packageManager returned a null label");
            }
            packageInfo.putCharSequence("label", label);
            icon = packageManager.getApplicationIcon(mApplicationInfo);
            logo = packageManager.getApplicationLogo(mApplicationInfo);
            banner = packageManager.getApplicationBanner(mApplicationInfo);
        } else {
            label = packageInfo.getCharSequence("label");
            if (label == null) {
                Log.e(TAG, "verify: cached label is null, re-obtaining label");
                label = packageManager.getApplicationLabel(mApplicationInfo);
                if (label == null) {
                    Log.e(TAG, "verify: packageManager returned a null label");
                }
                packageInfo.putCharSequence("label", label);
            }
            icon = packageManager.getApplicationIcon(mApplicationInfo);
            logo = packageManager.getApplicationLogo(mApplicationInfo);
            banner = packageManager.getApplicationBanner(mApplicationInfo);
            hasVstCallback = packageInfo.getBooleanArray("hasVstCallback");
            if (hasVstCallback == null || !hasVstCallback[0]) return false;
        }
        applicationContext = core.createContextForPackage(context, mApplicationInfo);
        if (applicationContext == null) return false;
        classLoader = applicationContext.getClassLoader();
        callbackClassName = VstCallback.className;
        classFiles = packageInfo.getStringArrayList("classFiles");
        if (classFiles == null) {
            classFiles = core.getClassFiles(classLoader);
            packageInfo.putStringArrayList("classFiles", classFiles);
        }
        hasVstCallback = new boolean[]{core.hasVstCallback(classFiles, callbackClassName)};
        packageInfo.putBooleanArray("hasVstCallback", hasVstCallback);
        if (hasVstCallback[0]) {
            cachedCallbacks = packageInfo.getIntegerArrayList("callbacks");
            if (cachedCallbacks == null) {
                callbacks = core.getCallbacks(classLoader, classFiles, callbackClassName);
                if (callbacks.isEmpty()) {
                    hasVstCallback[0] = false;
                    packageInfo.putBooleanArray("hasVstCallback", hasVstCallback);
                } else {
                    cachedCallbacks = new ArrayList<Integer>();
                    for (Pair<Class, Integer> callback : callbacks) {
                        if (core.debug) Log.d(core.TAG, "vst callback = [" + callback + "]");
                        cachedCallbacks.add(callback.second);
                    }
                    packageInfo.putIntegerArrayList("callbacks", cachedCallbacks);
                }
            } else {
                int cachedCallbacksSize = cachedCallbacks.size();
                scanner.runOnUiThread.run(() -> scanner.onClassFullyScannedSetMax.run(cachedCallbacksSize));
                callbacks = new ArrayList<>(cachedCallbacksSize);
                for (int i = 0; i < cachedCallbacksSize; i++) {
                    Integer cachedCallback = cachedCallbacks.get(i);
                    String className = classFiles.get(cachedCallback);
                    Class c = null;
                    try {
                        if (core.debug)
                            Log.d(core.TAG, "VstCore: getCallbacks: loading class: " + className);
                        c = classLoader.loadClass(className);
                    } catch (ClassNotFoundException e) {
                        if (core.debug)
                            Log.d(core.TAG, "VstCore: getCallbacks: class could not be loaded: " + className);
                        continue;
                    }
                    if (core.debug)
                        Log.d(TAG, "VstCore: getCallbacks: found callback: " + className);
                    callbacks.add(new Pair<>(c, i));
                    scanner.runOnUiThread.run(() -> scanner.onVstFound.run(++scanner.vstCount, className, -1));
                    int finalI = i + 1;
                    scanner.runOnUiThread.run(() -> scanner.onClassFullyScanned.run(finalI, className, cachedCallbacksSize));
                }
                if (callbacks.isEmpty()) {
                    hasVstCallback[0] = false;
                    packageInfo.putBooleanArray("hasVstCallback", hasVstCallback);
                } else {
                    for (Pair<Class, Integer> callback : callbacks) {
                        if (core.debug) Log.d(TAG, "vst callback = [" + callback + "]");
                    }
                }
            }
            return hasVstCallback[0];
        }
        return false;
    }
}
