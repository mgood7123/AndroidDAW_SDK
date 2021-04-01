package smallville7123.taggable;

public class Taggable {
    public final static String getTag(Object object) {
        return getName(object) + "@" + getHash(object);
    }

    public final static String getShortTag(Object object) {
        return getLastClassName(object) + "@" + getHash(object);
    }

    public final static String getName(Object object) {
        return object.getClass().getName();
    }

    public final static String getHash(Object object) {
        return Integer.toHexString(object.hashCode());
    }

    public static String getLastClassName(Object view) {
        return getLastClassName(view.getClass().getName());
    }

    private static String getLastClassName(String name) {
        int idx = name.lastIndexOf(".");
        return idx == -1 ? name : name.substring(idx + 1);
    }

}