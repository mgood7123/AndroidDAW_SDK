package smallville7123.AndroidDAW.SDK.jni_cpp_api;

public class JNI_CPP_API {

    private static JNI_CPP_API instance;

    static {
        System.loadLibrary("AndroidDAW_JniHelpers");
        System.loadLibrary("AndroidDAW_JNI_CPP_API");
    }

    public final Extras Extras;

    private JNI_CPP_API() {
        Extras = new Extras();
    }

    public static JNI_CPP_API getInstance() {
        if (instance == null) {
            instance = new JNI_CPP_API();
        }
        return instance;
    }

    public native String basename(String path);

    public native String dirname(String path);

    public class Extras {
        public native String resolvePath(String cwd, String path);

        public native String resolveDirname(String cwd, String path);
    }
}
