package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import smallville7123.vstmanager.core.Views.VstView;
import smallville7123.vstmanager.core.Views.WindowView;

public class VstHost {
    public String TAG = "VstHost";
    public final VstScanner vstScanner = new VstScanner();

    ArrayList<ReflectionActivity> VSTs = new ArrayList<>();

    public VST getVST(ApplicationInfo applicationInfo) {
        return vstScanner.getVST(applicationInfo);
    }

    public ArrayList<VST> getVstList() {
        return vstScanner.vstList;
    }

    public void scan(Context context, PackageManager packageManager, List<ApplicationInfo> mInstalledApplications) {
        vstScanner.scan(context, packageManager, mInstalledApplications);
    }

    public ReflectionActivity launchVst(Context context, String packageName, VST vst, VstView contentRoot) {
        for (Pair<Class, Integer> callback : vst.callbacks) {
            if (ReflectionHelpers.classAextendsB(callback.first, ReflectionActivity.class)) {
                Log.d(TAG, "launchVst: callback [" + callback.first + "] extends ReflectionActivity");
                WindowView window = contentRoot.requestNewWindow();
                window.setTitle(vst.label);
                window.setIcon(vst.icon);
                ReflectionActivity reflectionActivity = new ReflectionActivity(
                        context, packageName, vst.applicationContext,
                        callback.first, window.getWindowContent()
                );
                reflectionActivity.callOnCreate(null);
                VSTs.add(reflectionActivity);
                return reflectionActivity;
            } else {
                Log.d(TAG, "launchVst: callback [" + callback.first + "] does not extend ReflectionActivity");
            }
        }
        return null;
    }

    public ReflectionActivity loadVST(Context context, String packageName, VST vst, VstView contentRoot) {
        return launchVst(context, packageName, vst, contentRoot);
    }

    public ReflectionActivity loadVST(Context context, String packageName, VST vst) {
        return launchVst(context, packageName, vst, contentRoot);
    }

    public Pair<VST, ReflectionActivity> loadVST(Context mContext, String packageName) {
        try {
            PackageManager pm = mContext.getPackageManager();
            ApplicationInfo app = pm.getApplicationInfo(packageName, 0);
            vstScanner.scanInForeground(mContext, pm, Collections.singletonList(app));
            VST vst = getVST(app);
            if (vst.verify(mContext, pm, app)) {
                return new Pair<>(vst, loadVST(mContext, packageName, vst));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    VstView contentRoot = null;

    public void setContentRoot(VstView viewGroup) {
        contentRoot = viewGroup;
    }

    public void setWindows(PackageManager mPackageManager, ApplicationInfo mApplicationInfo) {
        if (contentRoot != null) {
            int childCount = contentRoot.getChildCount();
            if (childCount != 0) {
                for (int count = childCount; count > -1; count--) {
                    View child = contentRoot.getChildAt(count);
                    if (child instanceof WindowView) {
                        WindowView window = ((WindowView) child);
                        window.setIcon(mPackageManager.getApplicationIcon(mApplicationInfo));
                    }
                }
            }
        }
    }

    public void getReflectionActivity(VST vst) {
        for (ReflectionActivity reflectionActivity : VSTs) {
        }
    }
}
