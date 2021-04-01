package smallville7123.vstmanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import dalvik.system.DexFile;
import smallville7123.vstmanager.core.VST;

public final class PackageViewerFragment extends Fragment {
    private static final String EXTRA_TARGET_PACKAGE_NAME = "target_package_name";
    private static final String EXTRA_TARGET_PACKAGE_NAME_FRAGMENT_ACTIVITY = "package_name_fragment_activity";
    private static final String EXTRA_TARGET_PACKAGE_LIST = "target_package_list";
    private final PackageViewerAdapter.Listener mAdapterListener = new PackageViewerAdapter.Listener() {
        @Override
        public void onItemClick(@NonNull ObjectInfo objectInfo) {
            if (isVisible()) {
                switch (objectInfo.getType()) {
                    case VST:
                        if (manager != null) {
                            if (manager.load(objectInfo.vst) != null) {
                                activity.getSupportFragmentManager().popBackStack();
                            }
                        }
                        break;
                    case APPLICATION:
                        // no action available for Applications's yet
                        break;
                    default:
                        if (objectInfo.getType().isClass()) {
                            // TODO: show class properties.
                        } else {
                            if (getFragmentManager() != null) {
                                getFragmentManager().beginTransaction()
                                        .replace(android.R.id.content, newInstance(objectInfo.getName()))
                                        .addToBackStack("tag")
                                        .setTransition(FragmentTransaction.TRANSIT_NONE)
                                        .commit();
                            }
                        }
                        break;
                }
            }
        }
    };

    @Nullable RecyclerView mRecyclerView = null;
    @Nullable LinearLayout mProgressContainer = null;
    @Nullable TextView mDatabaseStatusText = null;
    @Nullable ProgressBar mPackageProgressBar = null;
    @Nullable TextView mPackageProgressBarText = null;
    @Nullable TextView mPackagesSkippedText = null;
    @Nullable TextView mPackageBeingScanned = null;
    @Nullable TextView mClassTreeDepth = null;
    @Nullable TextView mDexFilesFound = null;
    @Nullable TextView mEmptyDexFilesFound = null;
    @Nullable TextView mDexClassesFound = null;
    @Nullable ProgressBar mClassQuickProgressBar = null;
    @Nullable TextView mClassQuickProgressBarText = null;
    @Nullable ProgressBar mClassFullProgressBar = null;
    @Nullable TextView mClassFullyProgressBarText = null;
    @Nullable ProgressBar mClassSkippedProgressBar = null;
    @Nullable TextView mClassSkippedProgressBarText = null;
    @Nullable TextView mVstFound = null;
    @Nullable TextView mTextView = null;
    @Nullable FragmentActivity activity = null;
    @Nullable VstManager manager = null;

    public PackageViewerFragment(FragmentActivity mOrigin, VstManager vstManager) {
        activity = mOrigin;
        manager = vstManager;
        if (mOrigin instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) mOrigin).getSupportActionBar();
            if (actionBar != null) {
                final Bundle bundle = new Bundle();
                bundle.putString(EXTRA_TARGET_PACKAGE_NAME_FRAGMENT_ACTIVITY, actionBar.getTitle().toString());
                setArguments(bundle);
            }
        } else {
            android.app.ActionBar actionBar = mOrigin.getActionBar();
            if (actionBar != null) {
                final Bundle bundle = new Bundle();
                bundle.putString(EXTRA_TARGET_PACKAGE_NAME_FRAGMENT_ACTIVITY, actionBar.getTitle().toString());
                setArguments(bundle);
            }
        }
    }

    public PackageViewerFragment newInstance(@NonNull final String targetPackageName) {
        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TARGET_PACKAGE_NAME, targetPackageName);
        final PackageViewerFragment fragment = new PackageViewerFragment(activity, manager);
        fragment.setArguments(bundle);
        return fragment;
    }

    public PackageViewerFragment newInstance(@NonNull final List<String> targetPackageList) {
        final Bundle bundle = new Bundle();
        bundle.putStringArrayList(EXTRA_TARGET_PACKAGE_LIST, new ArrayList<>(targetPackageList));
        final PackageViewerFragment fragment = new PackageViewerFragment(activity, manager);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_package_viewer, container, false);

        mProgressContainer = view.findViewById(R.id.progress);
        mDatabaseStatusText = mProgressContainer.findViewById(R.id.DatabaseStatus);
        mPackageProgressBar = mProgressContainer.findViewById(R.id.PackageProgressBar);
        mPackageProgressBarText = mProgressContainer.findViewById(R.id.PackagesScannedProgressBarText);
        mPackagesSkippedText = mProgressContainer.findViewById(R.id.PackagesSkippedProgressBarText);
        mPackageBeingScanned = mProgressContainer.findViewById(R.id.PackageBeingScanned);
        mClassTreeDepth = mProgressContainer.findViewById(R.id.ClassTreeDepth);
        mDexFilesFound = mProgressContainer.findViewById(R.id.DexFilesFound);
        mEmptyDexFilesFound = mProgressContainer.findViewById(R.id.EmptyDexFilesFound);
        mDexClassesFound = mProgressContainer.findViewById(R.id.DexClassesFound);
        mClassQuickProgressBar = mProgressContainer.findViewById(R.id.ClassQuickProgressBar);
        mClassQuickProgressBarText = mProgressContainer.findViewById(R.id.ClassesQuickScannedProgressBarText);
        mClassFullProgressBar = mProgressContainer.findViewById(R.id.ClassFullProgressBar);
        mClassFullyProgressBarText = mProgressContainer.findViewById(R.id.ClassesFullyScannedProgressBarText);
        mClassSkippedProgressBar = mProgressContainer.findViewById(R.id.ClassSkipProgressBar);
        mClassSkippedProgressBarText = mProgressContainer.findViewById(R.id.ClassesSkippedProgressBarText);
        mVstFound = mProgressContainer.findViewById(R.id.VstFound);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mTextView = view.findViewById(R.id.error_text);

        mRecyclerView.setVisibility(View.GONE);
        mTextView.setVisibility(View.GONE);
        mProgressContainer.setVisibility(View.VISIBLE);


        manager.mVstHost.vstScanner.setOnDatabaseStatusChanged(text -> mDatabaseStatusText.setText(text));
        manager.mVstHost.vstScanner.setOnPackageBeingScanned(packageName -> mPackageBeingScanned.setText(packageName));

        manager.mVstHost.vstScanner.setOnPackageScannedSetMax(max -> {
            mPackageProgressBar.setMax(max);
            mPackageProgressBarText.setText(0 + "/" + max);
        });
        manager.mVstHost.vstScanner.setOnPackageScanned((progress, applicationInfo, max) -> {
            mPackageProgressBar.setProgress(progress);
            mPackageProgressBarText.setText(progress + "/" + max);
        });

        manager.mVstHost.vstScanner.setOnPackageSkipped(count -> mPackagesSkippedText.setText(String.valueOf(count)));

        manager.mVstHost.vstScanner.setOnClassTreeDepth(count -> mClassTreeDepth.setText(String.valueOf(count)));

        manager.mVstHost.vstScanner.setOnDexFileFound(count -> mDexFilesFound.setText(String.valueOf(count)));
        manager.mVstHost.vstScanner.setOnEmptyDexFileFound(count -> mEmptyDexFilesFound.setText(String.valueOf(count)));
        manager.mVstHost.vstScanner.setOnDexClassFound(count -> mDexClassesFound.setText(String.valueOf(count)));

        manager.mVstHost.vstScanner.setOnClassQuickScannedSetMax(max -> {
            mClassQuickProgressBar.setMax(max);
            mClassQuickProgressBarText.setText(0 + "/" + max);
        });
        manager.mVstHost.vstScanner.setOnClassQuickScanned((progress, ClassName, max) -> {
            mClassQuickProgressBar.setProgress(progress);
            mClassQuickProgressBarText.setText(progress + "/" + max);
        });

        manager.mVstHost.vstScanner.setOnClassFullyScannedSetMax(max -> {
            mClassFullProgressBar.setMax(max);
            mClassFullyProgressBarText.setText(0 + "/" + max);
        });
        manager.mVstHost.vstScanner.setOnClassFullyScanned((progress, ClassName, max) -> {
            mClassFullProgressBar.setProgress(progress);
            mClassFullyProgressBarText.setText(progress + "/" + max);
        });

        manager.mVstHost.vstScanner.setOnClassSkippedSetMax(max -> {
            mClassSkippedProgressBar.setMax(max);
            mClassSkippedProgressBarText.setText(0 + "/" + max);
        });
        manager.mVstHost.vstScanner.setOnClassSkipped((progress, ClassName, max) -> {
            mClassSkippedProgressBar.setProgress(progress);
            mClassSkippedProgressBarText.setText(progress + "/" + max);
        });

        manager.mVstHost.vstScanner.setOnVstFound((count, className, unused) -> {
            mVstFound.setText(String.valueOf(count));
        });

        setupRecyclerView();
        setupParentActivityTitle();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // package list is scanned here
        manager.mVstHost.vstScanner.setOnScanComplete(() -> {
            mProgressContainer.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.VISIBLE);
            setupData();
        });

        manager.mVstHost.scan(manager.mContext, manager.mPackageManager, manager.mInstalledApplications);
    }

    @SuppressWarnings("ConstantConditions")
    private void setupRecyclerView() {
        PackageViewerAdapter adapter = new PackageViewerAdapter(mAdapterListener);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @NonNull
    private List<ObjectInfo> getTargetPackageList() {
        if (manager.mInstalledApplications.isEmpty()) {
            return null;
        } else {
            final List<ObjectInfo> retValues = new ArrayList<>();

//            for (ApplicationInfo installedApplication : manager.mInstalledApplications) {
//                retValues.add(new ObjectInfo(installedApplication, manager.mPackageManager));
//            }

            for (VST vst : manager.mVstHost.getVstList()) {
                retValues.add(new ObjectInfo(vst));
            }

            return retValues;
        }
    }

    private List<ObjectInfo> getClassListFromPackage(String packageName) {
        final List<ObjectInfo> list = new ArrayList<>();
        if (getContext() == null) return list;

        try {
            final String packageCodePath = getContext().getPackageCodePath();
            final DexFile df = new DexFile(packageCodePath);
            for (Enumeration<String> iterator = df.entries(); iterator.hasMoreElements(); ) {
                final String fullClassPath = iterator.nextElement();
                final ObjectInfo additionalObject;
                final String className;
                if (TextUtils.isEmpty(packageName)) {
                    className = fullClassPath;
                } else {
                    if (!fullClassPath.startsWith(packageName) || fullClassPath.contains("$"))
                        continue;
                    className = fullClassPath.replace(packageName + ".", "");
                }

                final String[] splitClassName = className.split(Pattern.quote("."), 0);
                if (splitClassName.length == 1) {
                    // file
                    additionalObject = new ObjectInfo(className, packageName);
                } else if (splitClassName.length > 1) {
                    // directory
                    additionalObject = new ObjectInfo(splitClassName[0], packageName, ObjectInfo.Type.DIRECTORY);
                } else {
                    continue;
                }
                if (!list.contains(additionalObject)) list.add(additionalObject);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void setupData() {
        if (mRecyclerView == null || mTextView == null) return;

        List<ObjectInfo> displayList = getTargetPackageList();

        mRecyclerView.setVisibility(displayList.isEmpty() ? View.GONE : View.VISIBLE);
        mTextView.setVisibility(displayList.isEmpty() ? View.VISIBLE : View.GONE);

        PackageViewerAdapter adapter = (PackageViewerAdapter) mRecyclerView.getAdapter();
        adapter.setItems(displayList);
    }

    private void setupParentActivityTitle() {
        final String packageName = getTitle();
        if (getActivity() instanceof AppCompatActivity) {
            final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) actionBar.setTitle(packageName);
        }
    }

    @NonNull
    private String getTitle() {
        if (getArguments() == null) {
            return "";
        }
        return getArguments().getString(EXTRA_TARGET_PACKAGE_NAME, getArguments().getString(EXTRA_TARGET_PACKAGE_NAME_FRAGMENT_ACTIVITY, ""));
    }

    @NonNull
    private String getPackageName() {
        if (getArguments() == null) {
            return "";
        }
        return getArguments().getString(EXTRA_TARGET_PACKAGE_NAME, "");
    }

    @NonNull
    private List<String> getPackageList() {
        if (getArguments() == null) {
            return new ArrayList<>();
        }

        List<String> ret = getArguments().getStringArrayList(EXTRA_TARGET_PACKAGE_LIST);

        if (ret == null) {
            return new ArrayList<>();
        }

        return ret;
    }
}
