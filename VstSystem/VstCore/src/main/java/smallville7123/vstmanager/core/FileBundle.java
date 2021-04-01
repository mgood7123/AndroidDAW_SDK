package smallville7123.vstmanager.core;

import android.content.Context;
import android.os.BadParcelableException;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileBundle implements Serializable {
    private static final String TAG = "FileBundle";
    transient String location;
    transient FileBundle parent;

    ArrayMap<String, Object> mMap;

    /**
     * Constructs a new, empty FileBundle. useful for {@link #putFileBundle(String, FileBundle)}.
     */
    public FileBundle() {
        this(0);
    }

    /**
     * Constructs a new, empty FileBundle sized to hold the given number of
     * elements. The FileBundle will grow as needed. useful for
     * {@link #putFileBundle(String, FileBundle)}.
     *
     * @param capacity the initial capacity of the FileBundle
     */
    public FileBundle(int capacity) {
        mMap = capacity > 0 ?
                new ArrayMap<String, Object>(capacity) : new ArrayMap<String, Object>();
    }

    /**
     * Constructs a new, empty FileBundle. the FileBundle will use the given path
     * to read from, and write to.
     *
     * @param absolutePath an absolute path of the FileBundle location
     */
    public FileBundle(String absolutePath) {
        this(absolutePath, 0);
    }

    /**
     * Constructs a new, empty FileBundle sized to hold the given number of
     * elements. The FileBundle will grow as needed. the FileBundle will use
     * the given path to read from, and write to.
     *
     * @param capacity the initial capacity of the FileBundle
     * @param absolutePath an absolute path of the FileBundle location
     */
    public FileBundle(String absolutePath, int capacity) {
        this(capacity);
        location = absolutePath;
    }

    /**
     * Constructs a new, empty FileBundle. the FileBundle will use the given path,
     * which is relative to the applications Files directory, to read from, and write to.
     *
     * @param context the content to retrieve the Files Directory path from
     * @param relativePath a relative path of the FileBundle location
     */
    public FileBundle(Context context, String relativePath) {
        this(context.getFilesDir() + "/" + relativePath, 0);
    }

    /**
     * Constructs a new, empty FileBundle sized to hold the given number of
     * elements. The FileBundle will grow as needed. the FileBundle will use
     * the given path, which is relative to the applications Files directory,
     * to read from, and write to.
     *
     * @param context the content to retrieve the Files Directory path from
     * @param relativePath a relative path of the FileBundle location
     * @param capacity the initial capacity of the FileBundle
     */
    public FileBundle(Context context, String relativePath, int capacity) {
        this(context.getFilesDir() + "/" + relativePath, capacity);
    }

    /**
     * Constructs a FileBundle containing a copy of the mappings from the given
     * FileBundle.
     *
     * @param b a FileBundle to be copied.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public FileBundle(FileBundle b) {
        copyInternal(b, false);
    }

    /**
     * Clones the current FileBundle. The internal map is cloned, but the keys and
     * values to which it refers are copied by reference.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Object clone() {
        return new FileBundle(this);
    }

    /**
     * Make a deep copy of the given parcelableBundle.  Traverses into inner containers and copies
     * them as well, so they are not shared across parcelableBundles.  Will traverse in to
     * {@link FileBundle}, {@link Bundle}, {@link PersistableBundle}, {@link ArrayList},
     * and all types of primitive arrays.  Other types of objects
     * (such as Parcelable or Serializable) are referenced as-is and not copied in any way.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public FileBundle deepCopy() {
        FileBundle b = new FileBundle(location, 0);
        b.copyInternal(this, true);
        return b;
    }

    /** @hide */
    ArrayMap<String, Object> getMap() {
        return mMap;
    }

    /**
     * obtains the current map from the given FileBundle.
     *
     * @param fileBundle a FileBundle
     * @return
     */
    ArrayMap<String, Object> getMap(FileBundle fileBundle) {
        return fileBundle.mMap;
    }

    /**
     * Returns the number of mappings contained in this FileBundle.
     *
     * @return the number of mappings as an int.
     */
    public int size() {
        return mMap.size();
    }

    /**
     * Returns true if the mapping of this FileBundle is empty, false otherwise.
     */
    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    /**
     * Does a loose equality check between two given {@link FileBundle} objects.
     * Returns {@code true} if both are {@code null}, or if both are equal as per
     * {@link #kindofEquals(FileBundle)}
     *
     * @param a A {@link FileBundle} object
     * @param b Another {@link FileBundle} to compare with a
     * @return {@code true} if both are the same, {@code false} otherwise
     *
     * @see #kindofEquals(FileBundle)
     *
     * @hide
     */
    public static boolean kindofEquals(FileBundle a, FileBundle b) {
        return (a == b) || (a != null && a.kindofEquals(b));
    }

    /**
     * @hide This kind-of does an equality comparison.  Kind-of.
     */
    public boolean kindofEquals(FileBundle other) {
        if (other == null) {
            return false;
        }
        return mMap.equals(other.mMap);
    }

    /**
     * Removes all elements from the mapping of this FileBundle.
     */
    public void clear() {
        mMap.clear();
    }

    private static final boolean DEBUG_ARRAY_MAP = false;
    private static final int VAL_NULL = -1;
    private static final int VAL_STRING = 0;
    private static final int VAL_INTEGER = 1;
    private static final int VAL_MAP = 2;
    private static final int VAL_BUNDLE = 3;
    private static final int VAL_SHORT = 5;
    private static final int VAL_LONG = 6;
    private static final int VAL_FLOAT = 7;
    private static final int VAL_DOUBLE = 8;
    private static final int VAL_BOOLEAN = 9;
    private static final int VAL_CHARSEQUENCE = 10;
    private static final int VAL_LIST  = 11;
    private static final int VAL_SPARSEARRAY = 12;
    private static final int VAL_BYTEARRAY = 13;
    private static final int VAL_STRINGARRAY = 14;
    private static final int VAL_ARRAY_MAP = 15;
    private static final int VAL_PARCELABLEARRAY = 16;
    private static final int VAL_OBJECTARRAY = 17;
    private static final int VAL_INTARRAY = 18;
    private static final int VAL_LONGARRAY = 19;
    private static final int VAL_BYTE = 20;
    private static final int VAL_SERIALIZABLE = 21;
    private static final int VAL_SPARSEBOOLEANARRAY = 22;
    private static final int VAL_BOOLEANARRAY = 23;
    private static final int VAL_CHARSEQUENCEARRAY = 24;
    private static final int VAL_PERSISTABLEBUNDLE = 25;
    private static final int VAL_SIZE = 26;
    private static final int VAL_SIZEF = 27;
    private static final int VAL_DOUBLEARRAY = 28;

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
    {
        mMap = (ArrayMap<String, Object>) readValue(aInputStream);
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException
    {
        writeValue(aOutputStream, mMap);
    }

    /**
     * Please use {@link #writeBundle} instead.  Flattens a Map into the parcel
     * at the current dataPosition(),
     * growing dataCapacity() if needed.  The Map keys must be String objects.
     * The Map values are written using {@link #writeValue} and must follow
     * the specification there.
     *
     * <p>It is strongly recommended to use {@link #writeBundle} instead of
     * this method, since the Bundle class provides a type-safe API that
     * allows you to avoid mysterious type errors at the point of marshalling.
     */
    public final void writeMap(ObjectOutputStream aOutputStream, @Nullable Map val) throws IOException {
        writeMapInternal(aOutputStream, (Map<String, Object>) val);
    }

    /**
     * Flatten a Map into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.  The Map keys must be String objects.
     */
    /* package */ void writeMapInternal(ObjectOutputStream aOutputStream, @Nullable Map<String,Object> val) throws IOException {
        if (val == null) {
            aOutputStream.writeInt(-1);
            return;
        }
        Set<Map.Entry<String,Object>> entries = val.entrySet();
        int size = entries.size();
        aOutputStream.writeInt(size);

        for (Map.Entry<String,Object> e : entries) {
            writeValue(aOutputStream, e.getKey());
            writeValue(aOutputStream, e.getValue());
            size--;
        }

        if (size != 0) {
            throw new BadParcelableException("Map size does not match number of entries!");
        }
    }

    public ArrayMap readArrayMap(ObjectInputStream aInputStream) throws IOException {
        int N = aInputStream.readInt();
        if (N < 0) {
            return null;
        }
        ArrayMap a = new ArrayMap(N);
        readArrayMapInternal(aInputStream, (ArrayMap<String, Object>) a, N);
        return a;
    }

    /* package */ void readArrayMapInternal(ObjectInputStream aInputStream, @NonNull ArrayMap<String, Object> outVal, int N) throws IOException {
        if (DEBUG_ARRAY_MAP) {
            RuntimeException here =  new RuntimeException("here");
            here.fillInStackTrace();
            Log.d(TAG, "Reading " + N + " ArrayMap entries", here);
        }
        while (N > 0) {
            String key = aInputStream.readUTF();
            Object value = readValue(aInputStream);
            outVal.append(key, value);
            N--;
        }
        outVal.validate();
    }

    /**
     * Please use {@link #writeBundle} instead.  Flattens a Map into the parcel
     * at the current dataPosition(),
     * growing dataCapacity() if needed.  The Map keys must be String objects.
     * The Map values are written using {@link #writeValue} and must follow
     * the specification there.
     *
     * <p>It is strongly recommended to use {@link #writeBundle} instead of
     * this method, since the Bundle class provides a type-safe API that
     * allows you to avoid mysterious type errors at the point of marshalling.
     */
    public final void writeArrayMap(ObjectOutputStream aOutputStream, @Nullable ArrayMap val) throws IOException {
        writeArrayMapInternal(aOutputStream, (ArrayMap<String, Object>) val);
    }

    /**
     * Flatten an ArrayMap into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.  The Map keys must be String objects.
     */
    /* package */ void writeArrayMapInternal(ObjectOutputStream aOutputStream, @Nullable ArrayMap<String, Object> val) throws IOException {
        if (val == null) {
            aOutputStream.writeInt(-1);
            return;
        }

        final int N = val.size();
        aOutputStream.writeInt(N);
        for (int i=0; i<N; i++) {
            aOutputStream.writeUTF(val.keyAt(i));
            writeValue(aOutputStream, val.valueAt(i));
        }
    }

    void writeFileBundle(ObjectOutputStream aOutputStream, @Nullable FileBundle val) throws IOException {
        writeValue(aOutputStream, val.mMap);
    }

    FileBundle readFileBundle(ObjectInputStream aInputStream) throws IOException {
        FileBundle bundle = new FileBundle();
        bundle.mMap = (ArrayMap<String, Object>) readValue(aInputStream);
        return bundle;
    }

    /**
     * Flatten a List into the parcel at the current dataPosition(), growing
     * dataCapacity() if needed.  The List values are written using
     * {@link #writeValue} and must follow the specification there.
     */
    public final void writeList(ObjectOutputStream aOutputStream, @Nullable List val) throws IOException {
        if (val == null) {
            aOutputStream.writeInt(-1);
            return;
        }
        int N = val.size();
        int i=0;
        aOutputStream.writeInt(N);
        while (i < N) {
            writeValue(aOutputStream, val.get(i));
            i++;
        }
    }

    /**
     * Flatten an Object array into the parcel at the current dataPosition(),
     * growing dataCapacity() if needed.  The array values are written using
     * {@link #writeValue} and must follow the specification there.
     */
    public final void writeArray(ObjectOutputStream aOutputStream, @Nullable Object[] val) throws IOException {
        if (val == null) {
            aOutputStream.writeInt(-1);
            return;
        }
        int N = val.length;
        int i=0;
        aOutputStream.writeInt(N);
        while (i < N) {
            writeValue(aOutputStream, val[i]);
            i++;
        }
    }

    /**
     * Flatten a generic SparseArray into the parcel at the current
     * dataPosition(), growing dataCapacity() if needed.  The SparseArray
     * values are written using {@link #writeValue} and must follow the
     * specification there.
     */
    public final <T> void writeSparseArray(ObjectOutputStream aOutputStream, @Nullable SparseArray<T> val) throws IOException {
        if (val == null) {
            aOutputStream.writeInt(-1);
            return;
        }
        int N = val.size();
        aOutputStream.writeInt(N);
        int i=0;
        while (i < N) {
            aOutputStream.writeInt(val.keyAt(i));
            writeValue(aOutputStream, val.valueAt(i));
            i++;
        }
    }

    public final void writeBooleanArray(ObjectOutputStream aOutputStream, @Nullable boolean[] val) throws IOException {
        if (val != null) {
            int N = val.length;
            aOutputStream.writeInt(N);
            for (int i=0; i<N; i++) {
                aOutputStream.writeBoolean(val[i]);
            }
        } else {
            aOutputStream.writeInt(-1);
        }
    }

    @Nullable
    public final boolean[] createBooleanArray(ObjectInputStream aInputStream) throws IOException {
        int N = aInputStream.readInt();
        // >>2 as a fast divide-by-4 works in the create*Array() functions
        // because dataAvail() will never return a negative number.  4 is
        // the size of a stored boolean in the stream.
        if (N >= 0) {
            boolean[] val = new boolean[N];
            for (int i=0; i<N; i++) {
                val[i] = aInputStream.readBoolean();
            }
            return val;
        } else {
            return null;
        }
    }

    public final void writeIntArray(ObjectOutputStream aOutputStream, @Nullable int[] val) throws IOException {
        if (val != null) {
            int N = val.length;
            aOutputStream.writeInt(N);
            for (int i=0; i<N; i++) {
                aOutputStream.writeInt(val[i]);
            }
        } else {
            aOutputStream.writeInt(-1);
        }
    }

    @Nullable
    public final int[] createIntArray(ObjectInputStream aInputStream) throws IOException {
        int N = aInputStream.readInt();
        if (N >= 0) {
            int[] val = new int[N];
            for (int i=0; i<N; i++) {
                val[i] = aInputStream.readInt();
            }
            return val;
        } else {
            return null;
        }
    }

    public final void writeLongArray(ObjectOutputStream aOutputStream, @Nullable long[] val) throws IOException {
        if (val != null) {
            int N = val.length;
            aOutputStream.writeInt(N);
            for (int i=0; i<N; i++) {
                aOutputStream.writeLong(val[i]);
            }
        } else {
            aOutputStream.writeInt(-1);
        }
    }

    @Nullable
    public final long[] createLongArray(ObjectInputStream aInputStream) throws IOException {
        int N = aInputStream.readInt();
        // >>3 because stored longs are 64 bits
        if (N >= 0) {
            long[] val = new long[N];
            for (int i=0; i<N; i++) {
                val[i] = aInputStream.readLong();
            }
            return val;
        } else {
            return null;
        }
    }

    public final void writeDoubleArray(ObjectOutputStream aOutputStream, @Nullable double[] val) throws IOException {
        if (val != null) {
            int N = val.length;
            aOutputStream.writeInt(N);
            for (int i=0; i<N; i++) {
                aOutputStream.writeDouble(val[i]);
            }
        } else {
            aOutputStream.writeInt(-1);
        }
    }

    @Nullable
    public final double[] createDoubleArray(ObjectInputStream aInputStream) throws IOException {
        int N = aInputStream.readInt();
        // >>3 because stored doubles are 8 bytes
        if (N >= 0) {
            double[] val = new double[N];
            for (int i=0; i<N; i++) {
                val[i] = aInputStream.readDouble();
            }
            return val;
        } else {
            return null;
        }
    }

    public final void writeStringArray(ObjectOutputStream aOutputStream, @Nullable String[] val) throws IOException {
        if (val != null) {
            int N = val.length;
            aOutputStream.writeInt(N);
            for (int i=0; i<N; i++) {
                aOutputStream.writeUTF(val[i]);
            }
        } else {
            aOutputStream.writeInt(-1);
        }
    }

    /**
     * Read and return a String[] object from the parcel.
     */
    @Nullable
    public final String[] readStringArray(ObjectInputStream aInputStream) throws IOException {
        String[] array = null;

        int length = aInputStream.readInt();
        if (length >= 0)
        {
            array = new String[length];

            for (int i = 0 ; i < length ; i++)
            {
                array[i] = aInputStream.readUTF();
            }
        }

        return array;
    }

    public final void writeByteArray(ObjectOutputStream aOutputStream, @Nullable byte[] b) throws IOException {
        if (b == null) {
            aOutputStream.writeInt(-1);
            return;
        }
        int N = b.length;
        int i=0;
        aOutputStream.writeInt(N);
        while (i < N) {
            aOutputStream.writeByte(b[i]);
            i++;
        }
    }

    @Nullable
    public final byte[] createByteArray(ObjectInputStream aInputStream) throws IOException {
        int N = aInputStream.readInt();
        if (N >= 0) {
            byte[] val = new byte[N];
            for (int i=0; i<N; i++) {
                val[i] = aInputStream.readByte();
            }
            return val;
        } else {
            return null;
        }
    }

    public final void writeCharSequenceArray(ObjectOutputStream aOutputStream, @Nullable CharSequence[] val) throws IOException {
        if (val != null) {
            int N = val.length;
            aOutputStream.writeInt(N);
            for (int i=0; i<N; i++) {
                writeValue(aOutputStream, val[i]);
            }
        } else {
            aOutputStream.writeInt(-1);
        }
    }

    /**
     * Write a generic serializable object in to a Parcel.  It is strongly
     * recommended that this method be avoided, since the serialization
     * overhead is extremely large, and this approach will be much slower than
     * using the other approaches to writing data in to a Parcel.
     */
    public final void writeSerializable(ObjectOutputStream aOutputStream, @Nullable Serializable s) throws IOException {
        if (s == null) {
            aOutputStream.writeInt(-1);
            return;
        }
        String name = s.getClass().getName();
        aOutputStream.writeUTF(name);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(s);
            oos.close();

            writeByteArray(aOutputStream, baos.toByteArray());
        } catch (IOException ioe) {
            throw new RuntimeException("Parcelable encountered " +
                    "IOException writing serializable object (name = " + name +
                    ")", ioe);
        }
    }

    /**
     * Flatten a generic object in to a parcel.  The given Object value may
     * currently be one of the following types:
     *
     * <ul>
     * <li> null
     * <li> String
     * <li> Byte
     * <li> Short
     * <li> Integer
     * <li> Long
     * <li> Float
     * <li> Double
     * <li> Boolean
     * <li> String[]
     * <li> boolean[]
     * <li> byte[]
     * <li> int[]
     * <li> long[]
     * <li> Object[] (supporting objects of the same type defined here).
     * <li> Map (as supported by {@link #writeMap}).
     * <li> CharSequence.
     * <li> List (as supported by {@link #writeList}).
     * <li> {@link SparseArray} (as supported by {@link #writeSparseArray(ObjectOutputStream, SparseArray) writeSparseArray(SparseArray)}).
     * <li> Any object that implements Serializable (but see
     *      {@link #writeSerializable} for caveats).  Note that all of the
     *      previous types have relatively efficient implementations for
     *      writing to a Parcel; having to rely on the generic serialization
     *      approach is much less efficient and should be avoided whenever
     *      possible.
     * </ul>
     */
    public final void writeValue(ObjectOutputStream aOutputStream, @Nullable Object v) throws IOException {
        if (v == null) {
            aOutputStream.writeInt(VAL_NULL);
        } else if (v instanceof String) {
            aOutputStream.writeInt(VAL_STRING);
            aOutputStream.writeUTF((String) v);
        } else if (v instanceof Integer) {
            aOutputStream.writeInt(VAL_INTEGER);
            aOutputStream.writeInt((Integer) v);
        } else if (v instanceof ArrayMap) {
            aOutputStream.writeInt(VAL_ARRAY_MAP);
            writeArrayMap(aOutputStream, (ArrayMap) v);
        } else if (v instanceof Map) {
            aOutputStream.writeInt(VAL_MAP);
            writeMap(aOutputStream, (Map) v);
        } else if (v instanceof FileBundle) {
            aOutputStream.writeInt(VAL_BUNDLE);
            writeFileBundle(aOutputStream, (FileBundle) v);
        } else if (v instanceof Short) {
            aOutputStream.writeInt(VAL_SHORT);
            aOutputStream.writeShort((Short) v);
        } else if (v instanceof Long) {
            aOutputStream.writeInt(VAL_LONG);
            aOutputStream.writeLong((Long) v);
        } else if (v instanceof Float) {
            aOutputStream.writeInt(VAL_FLOAT);
            aOutputStream.writeFloat((Float) v);
        } else if (v instanceof Double) {
            aOutputStream.writeInt(VAL_DOUBLE);
            aOutputStream.writeDouble((Double) v);
        } else if (v instanceof Boolean) {
            aOutputStream.writeInt(VAL_BOOLEAN);
            aOutputStream.writeInt((Boolean) v ? 1 : 0);
        } else if (v instanceof CharSequence) {
            // Must be after String
            aOutputStream.writeInt(VAL_CHARSEQUENCE);
            aOutputStream.writeUTF(((CharSequence) v).toString());
        } else if (v instanceof List) {
            aOutputStream.writeInt(VAL_LIST);
            writeList(aOutputStream, (List) v);
        } else if (v instanceof SparseArray) {
            aOutputStream.writeInt(VAL_SPARSEARRAY);
            writeSparseArray(aOutputStream, (SparseArray) v);
        } else if (v instanceof boolean[]) {
            aOutputStream.writeInt(VAL_BOOLEANARRAY);
            writeBooleanArray(aOutputStream, (boolean[]) v);
        } else if (v instanceof byte[]) {
            aOutputStream.writeInt(VAL_BYTEARRAY);
            writeByteArray(aOutputStream, (byte[]) v);
        } else if (v instanceof String[]) {
            aOutputStream.writeInt(VAL_STRINGARRAY);
            writeStringArray(aOutputStream, (String[]) v);
        } else if (v instanceof CharSequence[]) {
            // Must be after String[] and before Object[]
            aOutputStream.writeInt(VAL_CHARSEQUENCEARRAY);
            writeCharSequenceArray(aOutputStream, (CharSequence[]) v);
        } else if (v instanceof int[]) {
            aOutputStream.writeInt(VAL_INTARRAY);
            writeIntArray(aOutputStream, (int[]) v);
        } else if (v instanceof long[]) {
            aOutputStream.writeInt(VAL_LONGARRAY);
            writeLongArray(aOutputStream, (long[]) v);
        } else if (v instanceof Byte) {
            aOutputStream.writeInt(VAL_BYTE);
            aOutputStream.writeInt((Byte) v);
        } else if (v instanceof double[]) {
            aOutputStream.writeInt(VAL_DOUBLEARRAY);
            writeDoubleArray(aOutputStream, (double[]) v);
        } else {
            Class<?> clazz = v.getClass();
            if (clazz.isArray() && clazz.getComponentType() == Object.class) {
                // Only pure Object[] are written here, Other arrays of non-primitive types are
                // handled by serialization as this does not record the component type.
                aOutputStream.writeInt(VAL_OBJECTARRAY);
                writeArray(aOutputStream, (Object[]) v);
            } else if (v instanceof Serializable) {
                // Must be last
                aOutputStream.writeInt(VAL_SERIALIZABLE);
                writeSerializable(aOutputStream, (Serializable) v);
            } else {
                throw new RuntimeException("Parcel: unable to marshal value " + v);
            }
        }
    }

    /**
     * Read and return a new SparseArray object from the parcel at the current
     * dataPosition().  Returns null if the previously written list object was
     * null.  The given class loader will be used to load any enclosed
     * Parcelables.
     */
    @Nullable
    public final <T> SparseArray<T> readSparseArray(ObjectInputStream aInputStream) throws IOException {
        int N = aInputStream.readInt();
        if (N < 0) {
            return null;
        }
        SparseArray sa = new SparseArray(N);
        readSparseArrayInternal(aInputStream, sa, N);
        return sa;
    }

    private void readSparseArrayInternal(ObjectInputStream aInputStream, @NonNull SparseArray outVal, int N) throws IOException {
        while (N > 0) {
            int key = aInputStream.readInt();
            Object value = readValue(aInputStream);
            //Log.i(TAG, "Unmarshalling key=" + key + " value=" + value);
            outVal.append(key, value);
            N--;
        }
    }

    /**
     * Read and return a new HashMap object from the parcel at the current
     * dataPosition(), using the given class loader to load any enclosed
     * Parcelables. Returns null if the previously written map object was null.
     */
    @Nullable
    public final HashMap readHashMap(ObjectInputStream aInputStream) throws IOException {
        int N = aInputStream.readInt();
        if (N < 0) {
            return null;
        }
        HashMap m = new HashMap(N);
        readMapInternal(aInputStream, m, N);
        return m;
    }

    /* package */ void readMapInternal(ObjectInputStream aInputStream, @NonNull Map outVal, int N) throws IOException {
        while (N > 0) {
            Object key = readValue(aInputStream);
            Object value = readValue(aInputStream);
            outVal.put(key, value);
            N--;
        }
    }

    /**
     * Read and return a new ArrayList object from the parcel at the current
     * dataPosition().  Returns null if the previously written list object was
     * null.  The given class loader will be used to load any enclosed
     * Parcelables.
     */
    @Nullable
    public final ArrayList readArrayList(ObjectInputStream aInputStream) throws IOException {
        int N = aInputStream.readInt();
        if (N < 0) {
            return null;
        }
        ArrayList l = new ArrayList(N);
        readListInternal(aInputStream, l, N);
        return l;
    }

    private void readListInternal(ObjectInputStream aInputStream, @NonNull List outVal, int N) throws IOException {
        while (N > 0) {
            Object value = readValue(aInputStream);
            outVal.add(value);
            N--;
        }
    }

    /**
     * Read and return a CharSequence[] object from the parcel.
     */
    @Nullable
    public final CharSequence[] readCharSequenceArray(ObjectInputStream aInputStream) throws IOException {
        CharSequence[] array = null;

        int length = aInputStream.readInt();
        if (length >= 0)
        {
            array = new CharSequence[length];

            for (int i = 0 ; i < length ; i++)
            {
                array[i] = aInputStream.readUTF();
            }
        }

        return array;
    }

    /**
     * Read and return a new Object array from the parcel at the current
     * dataPosition().  Returns null if the previously written array was
     * null.  The given class loader will be used to load any enclosed
     * Parcelables.
     */
    @Nullable
    public final Object[] readArray(ObjectInputStream aInputStream) throws IOException {
        int N = aInputStream.readInt();
        if (N < 0) {
            return null;
        }
        Object[] l = new Object[N];
        readArrayInternal(aInputStream, l, N);
        return l;
    }

    private void readArrayInternal(ObjectInputStream aInputStream, @NonNull Object[] outVal, int N) throws IOException {
        for (int i = 0; i < N; i++) {
            Object value = readValue(aInputStream);
            outVal[i] = value;
        }
    }

    @Nullable
    private final Serializable readSerializable(ObjectInputStream aInputStream) throws IOException {
        String name = aInputStream.readUTF();
        if (name == null) {
            // For some reason we were unable to read the name of the Serializable (either there
            // is nothing left in the Parcel to read, or the next value wasn't a String), so
            // return null, which indicates that the name wasn't found in the parcel.
            return null;
        }

        byte[] serializedData = createByteArray(aInputStream);
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedData);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Serializable) ois.readObject();
        } catch (IOException ioe) {
            throw new RuntimeException("Parcelable encountered " +
                    "IOException reading a Serializable object (name = " + name +
                    ")", ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Parcelable encountered " +
                    "ClassNotFoundException reading a Serializable object (name = "
                    + name + ")", cnfe);
        }
    }

    /**
     * Read a typed object from a parcel.  The given class loader will be
     * used to load any enclosed Parcelables.  If it is null, the default class
     * loader will be used.
     * @param aInputStream
     */
    @Nullable
    public final Object readValue(ObjectInputStream aInputStream) throws IOException {
        int type = aInputStream.readInt();

        switch (type) {
            case VAL_NULL:
                return null;

            case VAL_STRING:
                return aInputStream.readUTF();

            case VAL_INTEGER:
                return aInputStream.readInt();

            case VAL_ARRAY_MAP:
                return readArrayMap(aInputStream);

            case VAL_MAP:
                return readHashMap(aInputStream);

            case VAL_SHORT:
                return aInputStream.readShort();

            case VAL_LONG:
                return aInputStream.readLong();

            case VAL_FLOAT:
                return aInputStream.readFloat();

            case VAL_DOUBLE:
                return aInputStream.readDouble();

            case VAL_BOOLEAN:
                return aInputStream.readBoolean();

            case VAL_CHARSEQUENCE:
                //noinspection DuplicateBranchesInSwitch
                return aInputStream.readUTF();

            case VAL_LIST:
                return readArrayList(aInputStream);

            case VAL_BOOLEANARRAY:
                return createBooleanArray(aInputStream);

            case VAL_BYTEARRAY:
                return createByteArray(aInputStream);

            case VAL_STRINGARRAY:
                return readStringArray(aInputStream);

            case VAL_CHARSEQUENCEARRAY:
                return readCharSequenceArray(aInputStream);

            case VAL_OBJECTARRAY:
                return readArray(aInputStream);

            case VAL_INTARRAY:
                return createIntArray(aInputStream);

            case VAL_LONGARRAY:
                return createLongArray(aInputStream);

            case VAL_BYTE:
                return aInputStream.readByte();

            case VAL_BUNDLE:
                return readFileBundle(aInputStream);

            case VAL_SERIALIZABLE:
                return readSerializable(aInputStream);

            case VAL_SPARSEARRAY:
                return readSparseArray(aInputStream);

            case VAL_DOUBLEARRAY:
                return createDoubleArray(aInputStream);

            default:
                throw new RuntimeException(
                        "Parcel " + this + ": Unmarshalling unknown type code " + type);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void copyInternal(FileBundle from, boolean deep) {
        synchronized (from) {
            if (from.mMap != null) {
                if (!deep) {
                    mMap = new ArrayMap<>(from.mMap);
                } else {
                    final ArrayMap<String, Object> fromMap = from.mMap;
                    final int N = fromMap.size();
                    mMap = new ArrayMap<>(N);
                    for (int i = 0; i < N; i++) {
                        mMap.append(fromMap.keyAt(i), deepCopyValue(fromMap.valueAt(i)));
                    }
                }
            } else {
                mMap = null;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    Object deepCopyValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof FileBundle) {
            return ((FileBundle)value).deepCopy();
        } else if (value instanceof ArrayList) {
            return deepcopyArrayList((ArrayList) value);
        } else if (value.getClass().isArray()) {
            if (value instanceof int[]) {
                return ((int[])value).clone();
            } else if (value instanceof long[]) {
                return ((long[])value).clone();
            } else if (value instanceof float[]) {
                return ((float[])value).clone();
            } else if (value instanceof double[]) {
                return ((double[])value).clone();
            } else if (value instanceof Object[]) {
                return ((Object[])value).clone();
            } else if (value instanceof byte[]) {
                return ((byte[])value).clone();
            } else if (value instanceof short[]) {
                return ((short[])value).clone();
            } else if (value instanceof char[]) {
                return ((char[]) value).clone();
            }
        }
        return value;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    ArrayList deepcopyArrayList(ArrayList from) {
        final int N = from.size();
        ArrayList out = new ArrayList(N);
        for (int i=0; i<N; i++) {
            out.add(deepCopyValue(from.get(i)));
        }
        return out;
    }



    /**
     * Returns true if the given key is contained in the mapping
     * of this FileBundle.
     *
     * @param key a String key
     * @return true if the key is part of the mapping, false otherwise
     */
    public boolean containsKey(String key) {
        return mMap.containsKey(key);
    }

    /**
     * Returns the entry with the given key as an object.
     *
     * @param key a String key
     * @return an Object, or null
     */
    @Nullable
    public Object get(String key) {
        return mMap.get(key);
    }

    /**
     * Removes any entry with the given key from the mapping of this FileBundle.
     *
     * @param key a String key
     */
    public void remove(String key) {
        mMap.remove(key);
    }

    /**
     * Inserts all mappings from the given PersistableFileBundle into this FileBundle.
     *
     * @param fileBundle a PersistableFileBundle
     */
    public void putAll(FileBundle fileBundle) {
        mMap.putAll(getMap(fileBundle));
    }

    /**
     * Inserts all mappings from the given Map into this FileBundle.
     *
     * @param map a Map
     */
    void putAll(ArrayMap map) {
        mMap.putAll(map);
    }

    /**
     * Returns a Set containing the Strings used as keys in this FileBundle.
     *
     * @return a Set of String keys
     */
    public Set<String> keySet() {
        return mMap.keySet();
    }

    /**
     * Inserts a Boolean value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a boolean
     */
    public void putBoolean(@Nullable String key, boolean value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a byte value into the mapping of this FileBundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a byte
     */
    void putByte(@Nullable String key, byte value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a char value into the mapping of this FileBundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a char
     */
    void putChar(@Nullable String key, char value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a short value into the mapping of this FileBundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a short
     */
    void putShort(@Nullable String key, short value) {
        mMap.put(key, value);
    }

    /**
     * Inserts an int value into the mapping of this FileBundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value an int
     */
    public void putInt(@Nullable String key, int value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a long value into the mapping of this FileBundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a long
     */
    public void putLong(@Nullable String key, long value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a float value into the mapping of this FileBundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a float
     */
    void putFloat(@Nullable String key, float value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a double value into the mapping of this FileBundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a double
     */
    public void putDouble(@Nullable String key, double value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a String value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a String, or null
     */
    public void putString(@Nullable String key, @Nullable String value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a CharSequence value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a CharSequence, or null
     */
    void putCharSequence(@Nullable String key, @Nullable CharSequence value) {
        mMap.put(key, value);
    }

    /**
     * Inserts an ArrayList<Integer> value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<Integer> object, or null
     */
    void putIntegerArrayList(@Nullable String key, @Nullable ArrayList<Integer> value) {
        mMap.put(key, value);
    }

    /**
     * Inserts an ArrayList<String> value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<String> object, or null
     */
    void putStringArrayList(@Nullable String key, @Nullable ArrayList<String> value) {
        mMap.put(key, value);
    }

    /**
     * Inserts an ArrayList<CharSequence> value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an ArrayList<CharSequence> object, or null
     */
    void putCharSequenceArrayList(@Nullable String key, @Nullable ArrayList<CharSequence> value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a Serializable value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Serializable object, or null
     */
    void putSerializable(@Nullable String key, @Nullable Serializable value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a boolean array value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a boolean array object, or null
     */
    public void putBooleanArray(@Nullable String key, @Nullable boolean[] value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a byte array value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a byte array object, or null
     */
    void putByteArray(@Nullable String key, @Nullable byte[] value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a short array value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a short array object, or null
     */
    void putShortArray(@Nullable String key, @Nullable short[] value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a char array value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a char array object, or null
     */
    void putCharArray(@Nullable String key, @Nullable char[] value) {
        mMap.put(key, value);
    }

    /**
     * Inserts an int array value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value an int array object, or null
     */
    public void putIntArray(@Nullable String key, @Nullable int[] value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a long array value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a long array object, or null
     */
    public void putLongArray(@Nullable String key, @Nullable long[] value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a float array value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a float array object, or null
     */
    void putFloatArray(@Nullable String key, @Nullable float[] value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a double array value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a double array object, or null
     */
    public void putDoubleArray(@Nullable String key, @Nullable double[] value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a String array value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a String array object, or null
     */
    public void putStringArray(@Nullable String key, @Nullable String[] value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a CharSequence array value into the mapping of this FileBundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a CharSequence array object, or null
     */
    void putCharSequenceArray(@Nullable String key, @Nullable CharSequence[] value) {
        mMap.put(key, value);
    }

    /**
     * Inserts a FileBundle value into the mapping of this FileBundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a FileBundle
     */
    void putFileBundle(@Nullable String key, FileBundle value) {
        mMap.put(key, value);
    }

    /**
     * Returns the value associated with the given key, or false if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a boolean value
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    // Log a message if the value was non-null but not of the expected type
    void typeWarning(String key, Object value, String className,
                     Object defaultValue, ClassCastException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Key ");
        sb.append(key);
        sb.append(" expected ");
        sb.append(className);
        sb.append(" but value was a ");
        sb.append(value.getClass().getName());
        sb.append(".  The default value ");
        sb.append(defaultValue);
        sb.append(" was returned.");
        Log.w(TAG, sb.toString());
        Log.w(TAG, "Attempt to cast generated internal exception:", e);
    }

    void typeWarning(String key, Object value, String className,
                     ClassCastException e) {
        typeWarning(key, value, className, "<null>", e);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a boolean value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Boolean) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Boolean", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or (byte) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a byte value
     */
    byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a byte value
     */
    Byte getByte(String key, byte defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Byte) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Byte", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or (char) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a char value
     */
    char getChar(String key) {
        return getChar(key, (char) 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a char value
     */
    char getChar(String key, char defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Character) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Character", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or (short) 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a short value
     */
    short getShort(String key) {
        return getShort(key, (short) 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a short value
     */
    short getShort(String key, short defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Short) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Short", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or 0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return an int value
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return an int value
     */
    public int getInt(String key, int defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Integer) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Integer", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or 0L if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a long value
     */
    public long getLong(String key) {
        return getLong(key, 0L);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a long value
     */
    public long getLong(String key, long defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Long) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Long", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or 0.0f if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a float value
     */
    float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a float value
     */
    float getFloat(String key, float defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Float) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Float", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or 0.0 if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @return a double value
     */
    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key.
     *
     * @param key a String
     * @param defaultValue Value to return if key does not exist
     * @return a double value
     */
    public double getDouble(String key, double defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Double) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Double", defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a String value, or null
     */
    @Nullable
    public String getString(@Nullable String key) {
        final Object o = mMap.get(key);
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key or if a null
     * value is explicitly associated with the given key.
     *
     * @param key a String, or null
     * @param defaultValue Value to return if key does not exist or if a null
     *     value is associated with the given key.
     * @return the String value associated with the given key, or defaultValue
     *     if no valid String object is currently mapped to that key.
     */
    public String getString(@Nullable String key, String defaultValue) {
        final String s = getString(key);
        return (s == null) ? defaultValue : s;
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a CharSequence value, or null
     */
    @Nullable
    CharSequence getCharSequence(@Nullable String key) {
        final Object o = mMap.get(key);
        try {
            return (CharSequence) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or defaultValue if
     * no mapping of the desired type exists for the given key or if a null
     * value is explicitly associated with the given key.
     *
     * @param key a String, or null
     * @param defaultValue Value to return if key does not exist or if a null
     *     value is associated with the given key.
     * @return the CharSequence value associated with the given key, or defaultValue
     *     if no valid CharSequence object is currently mapped to that key.
     */
    CharSequence getCharSequence(@Nullable String key, CharSequence defaultValue) {
        final CharSequence cs = getCharSequence(key);
        return (cs == null) ? defaultValue : cs;
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a Serializable value, or null
     */
    @Nullable
    Serializable getSerializable(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (Serializable) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Serializable", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<String> value, or null
     */
    @Nullable
    ArrayList<Integer> getIntegerArrayList(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList<Integer>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<Integer>", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<String> value, or null
     */
    @Nullable
    ArrayList<String> getStringArrayList(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList<String>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<String>", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an ArrayList<CharSequence> value, or null
     */
    @Nullable
    ArrayList<CharSequence> getCharSequenceArrayList(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList<CharSequence>) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<CharSequence>", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a boolean[] value, or null
     */
    @Nullable
    public boolean[] getBooleanArray(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (boolean[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "byte[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a byte[] value, or null
     */
    @Nullable
    byte[] getByteArray(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (byte[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "byte[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a short[] value, or null
     */
    @Nullable
    short[] getShortArray(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (short[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "short[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a char[] value, or null
     */
    @Nullable
    char[] getCharArray(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (char[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "char[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return an int[] value, or null
     */
    @Nullable
    public int[] getIntArray(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (int[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "int[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a long[] value, or null
     */
    @Nullable
    public long[] getLongArray(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (long[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "long[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a float[] value, or null
     */
    @Nullable
    float[] getFloatArray(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (float[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "float[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a double[] value, or null
     */
    @Nullable
    public double[] getDoubleArray(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (double[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "double[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a String[] value, or null
     */
    @Nullable
    public String[] getStringArray(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (String[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a CharSequence[] value, or null
     */
    @Nullable
    CharSequence[] getCharSequenceArray(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (CharSequence[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence[]", e);
            return null;
        }
    }

    /**
     * Returns the value associated with the given key, or null if
     * no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key.
     *
     * @param key a String, or null
     * @return a short[] value, or null
     */
    @Nullable
    FileBundle getFileBundle(@Nullable String key) {
        Object o = mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (FileBundle) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "FileBundle", e);
            return null;
        }
    }

    void read() {
        if (new File(location).exists()) {
            try {
                FileInputStream fis = new FileInputStream(location);
                ObjectInputStream oos = new ObjectInputStream(fis);
                FileBundle fileBundle = (FileBundle) oos.readObject();
                oos.close();
                fis.close();
                this.mMap = fileBundle.mMap;
            } catch (EOFException | StreamCorruptedException e) {
                Log.e(TAG, "Database is corrupted, creating a new database");
                mMap = new ArrayMap<String, Object>();
            } catch (Exception e) {
                throw new RuntimeException("failed to read database file: " + e);
            }
        }
    }

    void write() {
        try {
            FileOutputStream fos = new FileOutputStream(location);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException("failed to write database file: ", e);
        }
    }
}
