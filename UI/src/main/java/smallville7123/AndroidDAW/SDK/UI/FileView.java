package smallville7123.AndroidDAW.SDK.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

import smallville7123.AndroidDAW.SDK.jni_cpp_api.JNI_CPP_API;

import static android.widget.LinearLayout.VERTICAL;

public class FileView extends FrameLayout {
    private static final String TAG = "FileView";
    JNI_CPP_API jniCppApi = JNI_CPP_API.getInstance();

    public FileView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public FileView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FileView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public FileView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    GridView fileList;
    Context mContext;
    AttributeSet mAttrs;

    LayoutUtils.TextViewSize textSize;
    int textColor;
    Drawable background;
    SLACASET header;
    int chevronColor;

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mAttrs = attrs;
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        setup_Attrs(context, attrs);
        setup_Content(context, attrs);
        setup_IME_ACTION_handler();
        setup_AutoCompletion();
        setup_Filtering();
        if (isInEditMode()) {
            enterDirectory("/");
        }
    }

    void setup_Attrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FileView, 0, 0);
            textSize = LayoutUtils.getTextSizeAttributesSuitableForTextView(attributes, R.styleable.FileView_android_textSize, 30f);
            textColor = attributes.getColor(R.styleable.FileView_android_textColor, Color.WHITE);
            chevronColor = attributes.getColor(R.styleable.FileView_chevronColor, Color.WHITE);
            background = attributes.getDrawable(R.styleable.FileView_android_background);
            if (background == null) {
                background = new ColorDrawable(Color.DKGRAY);
            }
            attributes.recycle();
        } else {
            textColor = Color.WHITE;
            chevronColor = Color.WHITE;
            textSize = LayoutUtils.new_TextViewSize(30f);
            background = new ColorDrawable(Color.DKGRAY);
        }
    }

    void setup_Content(Context context, AttributeSet attrs) {
        setBackground(background);
        inflate(context, R.layout.fileview_content, this);
        fileList = new GridView(context, attrs);
        fileList.setOrientation(VERTICAL);
        fileList.setColumns(1);
        fileList.setRows(20);
        header = findViewById(R.id.FileView_header_EditText);
        header.setImeOptions(EditorInfo.IME_ACTION_GO);
        FrameLayout fl = findViewById(R.id.FileView_container);
        fl.addView(fileList);
    }

    void setChecks(
            View view,
            CompoundButton.OnCheckedChangeListener read,
            CompoundButton.OnCheckedChangeListener write,
            CompoundButton.OnCheckedChangeListener execute
    ) {
        ((CheckBox) view.findViewById(R.id.checkBoxRead))
                .setOnCheckedChangeListener(read);
        ((CheckBox) view.findViewById(R.id.checkBoxWrite))
                .setOnCheckedChangeListener(write);
        ((CheckBox) view.findViewById(R.id.checkBoxExecute))
                .setOnCheckedChangeListener(execute);
    }

    void setCheckWithModeSpec(View view, ModeSpec spec) {
        ((CheckBox) view.findViewById(R.id.checkBoxRead)).setChecked(spec.read == Mode.BIT_ALLOW);
        ((CheckBox) view.findViewById(R.id.checkBoxWrite)).setChecked(spec.write == Mode.BIT_ALLOW);
        ((CheckBox) view.findViewById(R.id.checkBoxExecute)).setChecked(spec.execute == Mode.BIT_ALLOW);
    }

    ModeSpec directoryFilter;
    ModeSpec fileFilter;

    public void applyDirectoryFilter(ModeSpec modeSpec) {
        directoryFilter = modeSpec;
    }

    public void applyFileFilter(ModeSpec modeSpec) {
        fileFilter = modeSpec;
    }

    /*
        Allow Read  true  && Deny Read  false  -> BIT_ALLOW
        Allow Read  false && Deny Read  true   -> BIT_DENY
        Allow Read  true  && Deny Read  true   -> BIT_ANY
        Allow Read  false && Deny Read  false  -> ?
     */

    ModeSpec directoryFilterAllow;
    ModeSpec directoryFilterDeny;
    ModeSpec fileFilterAllow;
    ModeSpec fileFilterDeny;

    void setRead(ModeSpec modeSpec, boolean value) {
        modeSpec.read = value ? Mode.BIT_ALLOW : Mode.BIT_DENY;
    }

    void setWrite(ModeSpec modeSpec, boolean value) {
        modeSpec.write = value ? Mode.BIT_ALLOW : Mode.BIT_DENY;
    }

    void setExecute(ModeSpec modeSpec, boolean value) {
        modeSpec.execute = value ? Mode.BIT_ALLOW : Mode.BIT_DENY;
    }

    public void applyFilter(ModeSpec toApplyTo, ModeSpec allow, ModeSpec deny) {
        if (allow.read == Mode.BIT_ALLOW && deny.read == Mode.BIT_DENY) {
            toApplyTo.read = Mode.BIT_ALLOW;
        } else if (allow.read == Mode.BIT_DENY && deny.read == Mode.BIT_ALLOW) {
            toApplyTo.read = Mode.BIT_DENY;
        } else if (allow.read == Mode.BIT_ALLOW && deny.read == Mode.BIT_ALLOW) {
            toApplyTo.read = Mode.BIT_ANY;
        } else if (allow.read == Mode.BIT_DENY && deny.read == Mode.BIT_DENY) {
            // should this be valid?
            toApplyTo.read = Mode.BIT_ANY;
        }

        if (allow.write == Mode.BIT_ALLOW && deny.write == Mode.BIT_DENY) {
            toApplyTo.write = Mode.BIT_ALLOW;
        } else if (allow.write == Mode.BIT_DENY && deny.write == Mode.BIT_ALLOW) {
            toApplyTo.write = Mode.BIT_DENY;
        } else if (allow.write == Mode.BIT_ALLOW && deny.write == Mode.BIT_ALLOW) {
            toApplyTo.write = Mode.BIT_ANY;
        } else if (allow.write == Mode.BIT_DENY && deny.write == Mode.BIT_DENY) {
            // should this be valid?
            toApplyTo.write = Mode.BIT_ANY;
        }

        if (allow.execute == Mode.BIT_ALLOW && deny.execute == Mode.BIT_DENY) {
            toApplyTo.execute = Mode.BIT_ALLOW;
        } else if (allow.execute == Mode.BIT_DENY && deny.execute == Mode.BIT_ALLOW) {
            toApplyTo.execute = Mode.BIT_DENY;
        } else if (allow.execute == Mode.BIT_ALLOW && deny.execute == Mode.BIT_ALLOW) {
            toApplyTo.execute = Mode.BIT_ANY;
        } else if (allow.execute == Mode.BIT_DENY && deny.execute == Mode.BIT_DENY) {
            // should this be valid?
            toApplyTo.execute = Mode.BIT_ANY;
        }
    }

    public void applyDirectoryFilter() {
        applyFilter(directoryFilter, directoryFilterAllow, directoryFilterDeny);
        dps.setText(directoryFilter.toPermissionString());
        updateList(originalData);
    }

    public void applyFileFilter() {
        applyFilter(fileFilter, fileFilterAllow, fileFilterDeny);
        fps.setText(fileFilter.toPermissionString());
        updateList(originalData);
    }

    public void applyDirectoryAndFileFilters() {
        applyFilter(directoryFilter, directoryFilterAllow, directoryFilterDeny);
        dps.setText(directoryFilter.toPermissionString());
        applyFilter(fileFilter, fileFilterAllow, fileFilterDeny);
        fps.setText(fileFilter.toPermissionString());
        updateList(originalData);
    }

    TextView dps;
    TextView fps;

    void setup_Filtering() {
        directoryFilterAllow = new ModeSpec("rwx");
        directoryFilterDeny = new ModeSpec("rwx");
        fileFilterAllow = new ModeSpec("rwx");
        fileFilterDeny = new ModeSpec("rwx");
        directoryFilter = new ModeSpec("***");
        fileFilter = new ModeSpec("***");

        View allowedDirectories = findViewById(R.id.allowedDirectories);
        View deniedDirectories = findViewById(R.id.deniedDirectories);
        View allowedFiles = findViewById(R.id.allowedFiles);
        View deniedFiles = findViewById(R.id.deniedFiles);

        setCheckWithModeSpec(allowedDirectories, directoryFilterAllow);
        setCheckWithModeSpec(deniedDirectories, directoryFilterDeny);
        setCheckWithModeSpec(allowedFiles, fileFilterAllow);
        setCheckWithModeSpec(deniedFiles, fileFilterDeny);

        dps = findViewById(R.id.directoryPermissionsString);
        fps = findViewById(R.id.filePermissionsString);

        applyDirectoryAndFileFilters();


        setChecks(
                findViewById(R.id.allowedDirectories),
                (buttonView, isChecked) -> {
                    setRead(directoryFilterAllow, isChecked);
                    applyDirectoryFilter();
                },
                (buttonView, isChecked) -> {
                    setWrite(directoryFilterAllow, isChecked);
                    applyDirectoryFilter();
                },
                (buttonView, isChecked) -> {
                    setExecute(directoryFilterAllow, isChecked);
                    applyDirectoryFilter();
                }
        );
        setChecks(
                findViewById(R.id.deniedDirectories),
                (buttonView, isChecked) -> {
                    setRead(directoryFilterDeny, isChecked);
                    applyDirectoryFilter();
                },
                (buttonView, isChecked) -> {
                    setWrite(directoryFilterDeny, isChecked);
                    applyDirectoryFilter();
                },
                (buttonView, isChecked) -> {
                    setExecute(directoryFilterDeny, isChecked);
                    applyDirectoryFilter();
                }
        );

        setChecks(
                findViewById(R.id.allowedFiles),
                (buttonView, isChecked) -> {
                    setRead(fileFilterAllow, isChecked);
                    applyFileFilter();
                },
                (buttonView, isChecked) -> {
                    setWrite(fileFilterAllow, isChecked);
                    applyFileFilter();
                },
                (buttonView, isChecked) -> {
                    setExecute(fileFilterAllow, isChecked);
                    applyFileFilter();
                }
        );
        setChecks(
                findViewById(R.id.deniedFiles),
                (buttonView, isChecked) -> {
                    setRead(fileFilterDeny, isChecked);
                    applyFileFilter();
                },
                (buttonView, isChecked) -> {
                    setWrite(fileFilterDeny, isChecked);
                    applyFileFilter();
                },
                (buttonView, isChecked) -> {
                    setExecute(fileFilterDeny, isChecked);
                    applyFileFilter();
                }
        );
    }

    InputMethodManager inputMethodManager;

    public void dismissSoftKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        clearFocus();
    }

    void setup_IME_ACTION_handler() {
        header.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                enterDirectory(v.getText().toString());
                dismissSoftKeyboard();
                return true;
            }
            return true;
        });
    }

    abstract static class ConvertRunnable<F, T> {
        abstract T convert(F input);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    <F, T> T[] convertArrayType(F[] from, Class to, ConvertRunnable<F, T> convertRunnable) {
        T toArray[] = (T[]) Array.newInstance(to, from.length);
        for (int i = 0; i < toArray.length; i++) {
            toArray[i] = convertRunnable.convert(from[i]);
        }
        return toArray;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (header.isAttachedToWindow()) header.showDropDown();
            else post(this);
        }
    };

    enum Mode {
        BIT_ANY,
        BIT_DENY,
        BIT_ALLOW,
        BIT_INVALID
    }

    static Mode getModeBit(char bit) {
        switch (bit) {
            case '*':
                return Mode.BIT_ANY;
            case '-':
                return Mode.BIT_DENY;
            case 'R':
            case 'r':
            case 'W':
            case 'w':
            case 'X':
            case 'x':
                return Mode.BIT_ALLOW;
            default:
                return Mode.BIT_INVALID;
        }
    }

    /**
     * transforms a mode string, or a File, into a ModeSpec <br><br>
     *
     * mode strings are any combination of the following: <br>
     *
     * <li>--- <br>
     * <li>*** <br>
     * <li>rwx <br>
     * <li>RWX <br>
     */
    public static class ModeSpec {
        Mode read;
        Mode write;
        Mode execute;

        /**
         * transforms a mode string into a ModeSpec <br><br>
         *
         * mode strings are any combination of the following: <br>
         *
         * <li>--- <br>
         * <li>*** <br>
         * <li>rwx <br>
         * <li>RWX <br>
         */
        ModeSpec(String mode) {
            if (mode.length() != 3) {
                throw new RuntimeException("length must be 3");
            }
            switch (mode.charAt(0)) {
                case '*':
                    read = Mode.BIT_ANY;
                    break;
                case '-':
                    read = Mode.BIT_DENY;
                    break;
                case 'R':
                case 'r':
                    read = Mode.BIT_ALLOW;
                    break;
                default:
                    throw new RuntimeException(
                            "invalid bit, the only bits that are accepted are " +
                                    "-, R, r"
                    );
            }
            switch (mode.charAt(1)) {
                case '*':
                    write = Mode.BIT_ANY;
                    break;
                case '-':
                    write = Mode.BIT_DENY;
                    break;
                case 'W':
                case 'w':
                    write = Mode.BIT_ALLOW;
                    break;
                default:
                    throw new RuntimeException(
                            "invalid bit, the only bits that are accepted are " +
                                    "-, W, w"
                    );
            }
            switch (mode.charAt(2)) {
                case '*':
                    execute = Mode.BIT_ANY;
                    break;
                case '-':
                    execute = Mode.BIT_DENY;
                    break;
                case 'X':
                case 'x':
                    execute = Mode.BIT_ALLOW;
                    break;
                default:
                    throw new RuntimeException(
                            "invalid bit, the only bits that are accepted are " +
                                    "-, X, x"
                    );
            }
        }

        /**
         * obtains a mode spec from a file
         */
        ModeSpec(File file) {
            read = file.canRead() ? Mode.BIT_ALLOW : Mode.BIT_DENY;
            write = file.canWrite() ? Mode.BIT_ALLOW : Mode.BIT_DENY;
            execute = file.canExecute() ? Mode.BIT_ALLOW : Mode.BIT_DENY;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ModeSpec)) return false;
            ModeSpec modeSpec = (ModeSpec) o;
            return read == modeSpec.read &&
                    write == modeSpec.write &&
                    execute == modeSpec.execute;
        }

        public boolean equals(File file) {
            Objects.requireNonNull(file);
            return (read == Mode.BIT_ANY || read == (file.canRead() ? Mode.BIT_ALLOW : Mode.BIT_DENY)) &&
                    (write == Mode.BIT_ANY || write == (file.canWrite() ? Mode.BIT_ALLOW : Mode.BIT_DENY)) &&
                    (execute == Mode.BIT_ANY || execute == (file.canExecute() ? Mode.BIT_ALLOW : Mode.BIT_DENY));
        }

        @Override
        public int hashCode() {
            return Objects.hash(read, write, execute);
        }

        public static String toPermissionString(File file) {
            Objects.requireNonNull(file);
            return (file.canRead() ? "r" : "-") +
                    (file.canWrite() ? "w" : "-") +
                    (file.canExecute() ? "x" : "-");
        }

        public String toPermissionString() {
            return (read == Mode.BIT_ANY ? "*" : read == Mode.BIT_ALLOW ? "r" : read == Mode.BIT_DENY ? "-" : "<INVALID>") +
                   (write == Mode.BIT_ANY ? "*" : write == Mode.BIT_ALLOW ? "w" : write == Mode.BIT_DENY ? "-" : "<INVALID>") +
                   (execute == Mode.BIT_ANY ? "*" : execute == Mode.BIT_ALLOW ? "x" : execute == Mode.BIT_DENY ? "-" : "<INVALID>");
        }

        @Override
        public String toString() {
            return "ModeSpec{" +
                    "read=" + read +
                    ", write=" + write +
                    ", execute=" + execute +
                    '}';
        }
    }

    void setup_AutoCompletion() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: start");
                String path = s.toString();
                String abs = cwdFile.getAbsolutePath();
                Log.i(TAG, "abs = [" + abs + "]");
                Log.i(TAG, "path = [" + path + "]");
                int len = path.length();
                Log.i(TAG, "len = [" + len + "]");
                if (len == 0) {
                    path = abs + "/";
                    Log.i(TAG, "modified path = [" + path + "]");
                    len = path.length();
                    Log.i(TAG, "modified len = [" + len + "]");
                }
                String dirname = jniCppApi.Extras.resolveDirname(abs, path);
                Log.i(TAG, "dirname = [" + dirname + "]");
                File[] files = getDirectories(new File(dirname));
                if (files == null) {
                    Log.i(TAG, "files = [" + files + "]");
                    Log.d(TAG, "afterTextChanged: end");
                    return;
                }
                sortAlphabeticallyIgnoreCase(files);
                files = keepIf(files, (File file) -> {
                    if (file.isDirectory()) {
                        if (directoryFilter == null) return true;
                        return directoryFilter.equals(file);
                    } else {
                        if (fileFilter == null) return true;
                        return fileFilter.equals(file);
                    }
                });
                if (path.charAt(len-1) != '/') {
                    String basename = jniCppApi.basename(path);
                    Log.i(TAG, "basename = [" + basename + "]");
                    files = keepIf(files, (File file) -> jniCppApi.basename(file.getPath()).startsWith(basename));
                }
                String[] names = convertArrayType(files, String.class, new ConvertRunnable<File, String>() {
                    @Override
                    String convert(File input) {
                        return input.getName();
                    }
                });

                header.setAdapter(
                        new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_item, names)
                );
                Log.d(TAG, "afterTextChanged: end");
                post(runnable);
            }
        };

        header.setTokenizer(new StringTokenizer('/', '/'));
        header.addTextChangedListener(header.convertToTextWatcher(textWatcher));
        header.setThreshold(1);
        // setValidator causes EditView to hang
    }

    private static final int ROTATION_LEFT = 180;
    private static final int ROTATION_DOWN = 90;
    private static final int ROTATION_RIGHT = 0;
    private static final int ROTATION_UP = 270;

    class FileInfo {
        final int index;
        final FileInfo parent;
        final File file;
        final int depth;

        FileInfo(int index, FileInfo parent, File file, int depth) {
            this.index = index;
            this.parent = parent;
            this.file = file;
            this.depth = depth;
        }
    }

    ConstraintLayout add(FileInfo fileInfo) {
        ConstraintLayout r = (ConstraintLayout) inflate(mContext, R.layout.fileview_row, null);
        ConstraintLayout row = r.findViewById(R.id.FileView_row);
        LinearLayout depth = row.findViewById(R.id.FileView_row_depth);
        for (int i = 0; i < fileInfo.depth; i++) {
            inflate(mContext, R.layout.fileview_depth_content, depth);
        }
        ImageView chevron = row.findViewById(R.id.FileView_row_chevron);
        boolean isDir = fileInfo.file.isDirectory();
        if (isDir) {
            chevron.setColorFilter(chevronColor);
            row.setOnClickListener(unused -> {
                Boolean expanded = (Boolean) row.getTag();
                if (expanded == null || expanded == false) {
                    row.setTag(true);
                    expandDirectory(fileInfo);
                    chevron.setRotation(ROTATION_DOWN);
                } else {
                    row.setTag(false);
                    collapseDirectory(fileInfo);
                    chevron.setRotation(ROTATION_RIGHT);
                }
            });
        } else {
            chevron.setVisibility(INVISIBLE);
        }
        TextView textView = row.findViewById(R.id.FileView_row_text);
        String name = fileInfo.file.getName();
        String text = (name.isEmpty() ? fileInfo.file.getAbsolutePath() : name);
        textView.setText(text);
        textView.setTextColor(textColor);
        originalData.add(fileInfo.index, new Pair<>(r, fileInfo));
        updateList(originalData);
        return row;
    }

    void clear() {
        originalData.clear();
        updateList(originalData);
    }

    void update() {
        fileList.adapter.notifyDataSetChanged();
    }

    ArrayList<Pair<View, Object>> originalData = new ArrayList<>();

    ArrayList<Pair<View, Object>> applyFiltersToList(ArrayList<Pair<View, Object>> mData) {
        ArrayList<Pair<View, Object>> data = new ArrayList<>(mData);
        data.removeIf((Pair<View, Object> file) -> {
            FileInfo fileInfo = (FileInfo) file.second;
            if (fileInfo.file.isDirectory()) {
                if (directoryFilter == null) return false;
                return !directoryFilter.equals(fileInfo.file);
            } else {
                if (fileFilter == null) return false;
                return !fileFilter.equals(fileInfo.file);
            }
        });
        return data;
    }

    void updateList(ArrayList<Pair<View, Object>> data) {
        fileList.data = applyFiltersToList(data);
        update();
    }

    private boolean exists(File root) {
        return root != null && root.exists();
    }

    private boolean isReadable(File root) {
        return exists(root) && root.canRead();
    }

    private boolean isDirectory(File root) {
        return isReadable(root) && root.isDirectory();
    }

    private File[] keepIf(File[] files, Predicate<File> filter) {
        if (files == null) return null;
        ArrayList<File> directories = new ArrayList<>(Arrays.asList(files));
        directories.removeIf(filter.negate());
        return directories.toArray(new File[0]);
    }

    private File[] removeIf(File[] files, Predicate<File> filter) {
        return keepIf(files, filter.negate());
    }

    private File[] getDirectories(File root) {
        if (isDirectory(root)) {
            return keepIf(root.listFiles(), File::isDirectory);
        }
        return null;
    }

    private File[] getFiles(File root) {
        if (isDirectory(root)) {
            return removeIf(root.listFiles(), File::isDirectory);
        }
        return null;
    }

    private File[] getDirectoriesAndFiles(File root) {
        return isDirectory(root) ? root.listFiles() : null;
    }

    private void sortAlphabeticallyIgnoreCase(File[] files) {
        if (files == null) return;
        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
    }

    int indexOf(File file) {
        String abs = file.getAbsolutePath();
        ArrayList<Pair<View, Object>> data = originalData;
        for (int i = 0, dataSize = data.size(); i < dataSize; i++) {
            Pair<View, Object> datum = data.get(i);
            String abs_ = ((FileInfo) datum.second).file.getAbsolutePath();
            if (abs_.contentEquals(abs)) return i;
        }
        return -1;
    }

    private boolean expandDirectory(FileInfo root) {
        File[] files = getDirectoriesAndFiles(root.file);
        if (files == null) return false;
        sortAlphabeticallyIgnoreCase(files);
        int index = indexOf(root.file);
        for (File file : files) {
            add(new FileInfo(++index, root, file, root.depth+1));
        }
        return true;
    }

    private boolean collapseDirectory(FileInfo root) {
        originalData.removeIf(viewObjectPair -> {
            FileInfo current = ((FileInfo) viewObjectPair.second);
            while (current != null) {
                FileInfo parent = current.parent;
                if (parent == null) {
                    return false;
                } else {
                    if (parent.file.getAbsolutePath()
                            .contentEquals(root.file.getAbsolutePath())) {
                        return true;
                    } else {
                        current = parent;
                    }
                }
            }
            return false;
        });
        updateList(originalData);
        return true;
    }

    String cwdString;
    File cwdFile;

    public boolean enterDirectory(String root) {
        return enterDirectory(root, new File(root));
    }

    public boolean enterDirectory(File root) {
        return enterDirectory(root.getAbsolutePath(), root);
    }

    public boolean enterDirectory(String rootS, File rootF) {
        try {
            rootS = rootF.getCanonicalPath();
            rootF = new File(rootS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (rootS.charAt(rootS.length()-1) != '/') {
            rootS += '/';
            rootF = new File(rootS);
        }

        File[] files = getDirectoriesAndFiles(rootF);
        if (files == null) {
            header.setText(cwdString);
            return false;
        }
        clear();
        cwdString = rootS;
        cwdFile = rootF;
        header.setText(cwdString);
        add(new FileInfo(0, null, rootF, 0)).callOnClick();
        return true;
    }
}
