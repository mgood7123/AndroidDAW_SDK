package smallville7123.vstmanager.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class VstScanner {

    public interface StringRunnable {
        void run(String text);
    }

    public interface PackageScanRunnable {
        void run(int progress, ApplicationInfo applicationInfo, int max);
    }

    public interface ClassScanRunnable {
        void run(int progress, String ClassName, int max);
    }

    public interface RunOnUiThreadRunnable {
        void run(Runnable runnable);
    }

    public interface SetMaxRunnable {
        void run(int max);

    }
    RunOnUiThreadRunnable runOnUiThread = runnable -> {};

    public void setRunOnUiThread(RunOnUiThreadRunnable runOnUiThreadRunnable) {
        runOnUiThread = runOnUiThreadRunnable;
    }


    VstCore core = new VstCore(this);
    ArrayList<VST> vstList = new ArrayList<>();
    Runnable onScanStarted = () -> {};
    Runnable onScanComplete = () -> {};

    StringRunnable onDatabaseStatusChanged = (name) -> {};

    StringRunnable onPackageBeingScanned = (name) -> {};

    PackageScanRunnable onPackageScanned = (progress, applicationInfo, max) -> {};
    SetMaxRunnable onPackageScannedSetMax = max -> {};
    SetMaxRunnable onPackageSkipped = max -> {};

    SetMaxRunnable onClassTreeDepth = count -> {};
    SetMaxRunnable onDexFileFound = count -> {};
    SetMaxRunnable onEmptyDexFileFound = count -> {};
    SetMaxRunnable onDexClassFound = count -> {};

    ClassScanRunnable onClassSkipped = (progress, className, max) -> {};
    SetMaxRunnable onClassSkippedSetMax = max -> {};

    ClassScanRunnable onClassQuickScanned = (progress, className, max) -> {};
    SetMaxRunnable onClassQuickScannedSetMax = max -> {};

    ClassScanRunnable onClassFullyScanned = (progress, className, max) -> {};
    SetMaxRunnable onClassFullyScannedSetMax = max -> {};

    ClassScanRunnable onVstFound = (progress, applicationInfo, max) -> {};

    public void setOnScanStarted(Runnable onScanStarted) {
        this.onScanStarted = onScanStarted;
    }

    public void setOnScanComplete(Runnable onScanComplete) {
        this.onScanComplete = onScanComplete;
    }

    public void setOnClassTreeDepth(SetMaxRunnable onClassTreeDepth) {
        this.onClassTreeDepth = onClassTreeDepth;
    }

    public void setOnDatabaseStatusChanged(StringRunnable onDatabaseStatusChanged) {
        this.onDatabaseStatusChanged = onDatabaseStatusChanged;
    }

    public void setOnPackageBeingScanned(StringRunnable onPackageBeingScanned) {
        this.onPackageBeingScanned = onPackageBeingScanned;
    }

    public void setOnPackageScanned(PackageScanRunnable onPackageScanned) {
        this.onPackageScanned = onPackageScanned;
    }

    public void setOnPackageScannedSetMax(SetMaxRunnable onPackageScannedSetMax) {
        this.onPackageScannedSetMax = onPackageScannedSetMax;
    }

    public void setOnPackageSkipped(SetMaxRunnable onPackageSkipped) {
        this.onPackageSkipped = onPackageSkipped;
    }

    public void setOnDexFileFound(SetMaxRunnable onDexFileFound) {
        this.onDexFileFound = onDexFileFound;
    }

    public void setOnEmptyDexFileFound(SetMaxRunnable onEmptyDexFileFound) {
        this.onEmptyDexFileFound = onEmptyDexFileFound;
    }

    public void setOnDexClassFound(SetMaxRunnable onDexClassFound) {
        this.onDexClassFound = onDexClassFound;
    }

    public void setOnClassQuickScanned(ClassScanRunnable onClassQuickScanned) {
        this.onClassQuickScanned = onClassQuickScanned;
    }

    public void setOnClassQuickScannedSetMax(SetMaxRunnable onClassQuickScannedSetMax) {
        this.onClassQuickScannedSetMax = onClassQuickScannedSetMax;
    }

    public void setOnClassFullyScanned(ClassScanRunnable onClassFullyScanned) {
        this.onClassFullyScanned = onClassFullyScanned;
    }

    public void setOnClassFullyScannedSetMax(SetMaxRunnable onClassFullyScannedSetMax) {
        this.onClassFullyScannedSetMax = onClassFullyScannedSetMax;
    }

    public void setOnClassSkipped(ClassScanRunnable onClassSkipped) {
        this.onClassSkipped = onClassSkipped;
    }

    public void setOnClassSkippedSetMax(SetMaxRunnable onClassSkippedSetMax) {
        this.onClassSkippedSetMax = onClassSkippedSetMax;
    }

    public void setOnVstFound(ClassScanRunnable onVstFound) {
        this.onVstFound = onVstFound;
    }

    int skipped = 0;
    final Object scanLock = new Object();
    boolean isScanning = false;
    int vstCount = 0;

    public VST verifyVST(Context context, PackageManager packageManager, ApplicationInfo applicationInfo) {
        VST vst = new VST(core);
        vst.scanner = this;
        if (vst.verify(context, packageManager, applicationInfo)) {
            // TODO: prevent adding duplicates
            vstList.add(vst);
            return vst;
        }
        return null;
    }

    public void resetPackageStats() {
        onClassTreeDepth.run(0);
        onEmptyDexFileFound.run(0);
        onDexFileFound.run(0);
        onDexClassFound.run(0);
        onClassQuickScannedSetMax.run(0);
        onClassQuickScannedSetMax.run(0);
        onClassFullyScannedSetMax.run(0);
        onClassSkippedSetMax.run(0);
    }

    boolean scanComplete = false;

    FileBundle scannerDatabase;

    public void scan(Context context, PackageManager packageManager, List<ApplicationInfo> mInstalledApplications) {
        if (isScanning) {
            return;
        }
        int size = mInstalledApplications.size();
        skipped = 0;
        vstCount = 0;
        onPackageScannedSetMax.run(size);
        onVstFound.run(vstCount, null, -1);
        onPackageSkipped.run(0);
        resetPackageStats();
        new Thread(() -> {
            synchronized (scanLock) {
                scanComplete = false;
                isScanning = true;
                runOnUiThread.run(() -> onScanStarted.run());
                if (scannerDatabase != null) {
                    runOnUiThread.run(() -> onDatabaseStatusChanged.run("Database: Garbage collecting"));
                    scannerDatabase = null;
                    System.gc();
                    runOnUiThread.run(() -> onDatabaseStatusChanged.run("Database: Garbage collected"));
                }
                runOnUiThread.run(() -> onDatabaseStatusChanged.run("Database: Creating"));
                scannerDatabase = new FileBundle(context, "ScannerDatabase");
                runOnUiThread.run(() -> onDatabaseStatusChanged.run("Database: Created"));
                runOnUiThread.run(() -> onDatabaseStatusChanged.run("Database: Reading"));
                scannerDatabase.read();
                runOnUiThread.run(() -> {
                    onDatabaseStatusChanged.run("Database: Read");
                    onDatabaseStatusChanged.run("Database: Updating");
                });
                for (int i = 0; i < size; i++) {
                    ApplicationInfo applicationInfo = mInstalledApplications.get(i);
                    int finalI = i+1;
                    runOnUiThread.run(() -> onPackageBeingScanned.run(applicationInfo.packageName));
                    verifyVST(context, packageManager, applicationInfo);
                    runOnUiThread.run(() -> onPackageScanned.run(finalI, applicationInfo, size));
                    try {
                        Thread.sleep(0, 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread.run(() -> {
                    onDatabaseStatusChanged.run("Database: Updated");
                    onDatabaseStatusChanged.run("Database: Writing");
                });
                scannerDatabase.write();
                runOnUiThread.run(() -> {
                    onDatabaseStatusChanged.run("Database: Written");
                    onScanComplete.run();
                });
                isScanning = false;
                scanComplete = true;
            }
        }).start();
    }

    public void scanInForeground(Context context, PackageManager packageManager, List<ApplicationInfo> mInstalledApplications) {
        if (isScanning) {
            while(true) {
                if (scanComplete) return;
                try {
                    Thread.sleep(0, 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        int size = mInstalledApplications.size();
        skipped = 0;
        vstCount = 0;
        onPackageScannedSetMax.run(size);
        onVstFound.run(vstCount, null, -1);
        onPackageSkipped.run(0);
        resetPackageStats();
        synchronized (scanLock) {
            scanComplete = false;
            isScanning = true;
            runOnUiThread.run(() -> onScanStarted.run());
            if (scannerDatabase != null) {
                runOnUiThread.run(() -> onDatabaseStatusChanged.run("Database: Garbage collecting"));
                scannerDatabase = null;
                System.gc();
                runOnUiThread.run(() -> onDatabaseStatusChanged.run("Database: Garbage collected"));
            }
            runOnUiThread.run(() -> onDatabaseStatusChanged.run("Database: Creating"));
            scannerDatabase = new FileBundle(context, "ScannerDatabase");
            runOnUiThread.run(() -> onDatabaseStatusChanged.run("Database: Created"));
            runOnUiThread.run(() -> onDatabaseStatusChanged.run("Database: Reading"));
            scannerDatabase.read();
            runOnUiThread.run(() -> {
                onDatabaseStatusChanged.run("Database: Read");
                onDatabaseStatusChanged.run("Database: Updating");
            });
            for (int i = 0; i < size; i++) {
                ApplicationInfo applicationInfo = mInstalledApplications.get(i);
                int finalI = i+1;
                runOnUiThread.run(() -> onPackageBeingScanned.run(applicationInfo.packageName));
                verifyVST(context, packageManager, applicationInfo);
                runOnUiThread.run(() -> onPackageScanned.run(finalI, applicationInfo, size));
                try {
                    Thread.sleep(0, 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread.run(() -> {
                onDatabaseStatusChanged.run("Database: Updated");
                onDatabaseStatusChanged.run("Database: Writing");
            });
            scannerDatabase.write();
            runOnUiThread.run(() -> {
                onDatabaseStatusChanged.run("Database: Written");
                onScanComplete.run();
            });
            isScanning = false;
            scanComplete = true;
        }
    }

    public VST getVST(ApplicationInfo applicationInfo) {
        for (VST vst : vstList) {
            if (vst.mApplicationInfo == applicationInfo) return vst;
        }
        return null;
    }
}
