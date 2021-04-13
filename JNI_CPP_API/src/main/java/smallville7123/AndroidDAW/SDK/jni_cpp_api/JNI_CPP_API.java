package smallville7123.AndroidDAW.SDK.jni_cpp_api;

public class JNI_CPP_API {
    public static native String basename(String path);
    public static native String dirname(String path);
    public static class Extras {
        public static native String resolvePath(String cwd, String path);
        public static native String resolveDirname(String cwd, String path);
    }
}
