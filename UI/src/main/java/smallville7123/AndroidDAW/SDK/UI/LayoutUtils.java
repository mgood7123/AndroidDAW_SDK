package smallville7123.AndroidDAW.SDK.UI;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.annotation.StyleableRes;

import java.util.ArrayList;

import smallville7123.taggable.Taggable;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class LayoutUtils {
    private static final String TAG = "LayoutUtils";

    public static ViewGroup.LayoutParams getMatchParent() {
        return new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
    }

    public static ViewGroup.LayoutParams getWrapContent() {
        return new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
    }

    public static ViewGroup.LayoutParams getMatchParent_wrapContent() {
        return new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
    }

    public static ViewGroup.LayoutParams getWrapContent_matchParent() {
        return new ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
    }

    public static int toDP(Resources resources, float val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, resources.getDisplayMetrics());
    }

    public static class TextViewSize {
        boolean valid;
        boolean isPx;
        int px;
        boolean isSp;
        float sp;
    }

    public static TextViewSize new_TextViewSize(int px) {
        return new_TextViewSize(new TextViewSize(), px);
    }

    public static TextViewSize new_TextViewSize(TextViewSize s, int px) {
        s.valid = px != -1;
        if (s.valid) {
            s.isPx = true;
            s.px = px;
        }
        return s;
    }

    public static TextViewSize new_TextViewSize(float sp) {
        return new_TextViewSize(new TextViewSize(), sp);
    }

    public static TextViewSize new_TextViewSize(TextViewSize s, float sp) {
        s.valid = true;
        s.isSp = true;
        s.sp = sp;
        return s;
    }

    /**
     * obtains the textSize value or equivilant, from the given attribute, in a manner that ensures
     * correct scaling with TextView itself, since android provides different methods of obtaining
     * this value, it is not always obvious to the user how this value should be obtained
     * @return a TextViewSize object containing information about the current size
     */
    public static TextViewSize getTextSizeAttributesSuitableForTextView(TypedArray attributes, @StyleableRes int index) {
        return new_TextViewSize(attributes.getDimensionPixelSize(index, -1));
    }

    /**
     * obtains the textSize value or equivilant, from the given attribute, in a manner that ensures
     * correct scaling with TextView itself, since android provides different methods of obtaining
     * this value, it is not always obvious to the user how this value should be obtained
     * @param defVal default size in Scaled Pixels should the attribute not be declared/found
     * @return a TextViewSize object containing information about the current size
     */
    public static TextViewSize getTextSizeAttributesSuitableForTextView(TypedArray attributes, @StyleableRes int index, float defVal) {
        TextViewSize s = new_TextViewSize(attributes.getDimensionPixelSize(index, -1));
        if (!s.valid) s = new_TextViewSize(s, defVal);
        return s;
    }

    public static boolean textSizeAttributesIsValid(TextViewSize textSize) {
        return textSize.valid;
    }

    /**
     * uses the given TextViewSize to correctly set the text size for the given TextView object
     */
    public static void setTextSizeAttributesSuitableForTextView(TextView textView, TextViewSize textSize) {
        if (textSizeAttributesIsValid(textSize)) {
            if (textSize.isSp) textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.sp);
            else textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.px);
        }
    }

    /**
     * Finds the first descendant view with the given ID, the view itself if
     * the ID matches {@link View#getId() getId()}, or {@code null} if the ID is invalid
     * (< 0) or there is no matching view in the hierarchy.
     * <p>
     * <strong>Note:</strong> In most cases -- depending on compiler support --
     * the resulting view is automatically cast to the target class type. If
     * the target class type is unconstrained, an explicit cast may be
     * necessary.
     *
     * @return a view with given ID if found, or {@code null} otherwise
     * @see View#requireViewById(int)
     */
    @androidx.annotation.Nullable
    public static final <T extends View> T constructView(Class viewClass, Context context, AttributeSet attrs, Integer defStyleAttr, Integer defStyleRes) {
        if (!View.class.isAssignableFrom(viewClass)) throw new RuntimeException("viewClass must extend View");
        if (context == null) throw new RuntimeException("context must not be null");

        boolean attr = attrs != null;
        boolean style = defStyleAttr != null;
        boolean res = defStyleRes != null;
        View instance = null;

        // any of these can be null
        try {
            if (attr) {
                if (style) {
                    if (res) {
                        instance = (View) viewClass.getConstructor(Context.class, AttributeSet.class, int.class, int.class).newInstance(context, attrs, defStyleAttr, defStyleRes);
                    } else {
                        instance = (View) viewClass.getConstructor(Context.class, AttributeSet.class, int.class).newInstance(context, attrs, defStyleAttr);
                    }
                } else {
                    instance = (View) viewClass.getConstructor(Context.class, AttributeSet.class).newInstance(context, attrs);
                }
            } else {
                instance = (View) viewClass.getConstructor(Context.class).newInstance(context);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        if (instance == null) throw new RuntimeException("failed to construct view");
        instance.setId(View.generateViewId());
        return (T) instance;
    }

    public static String visibilityToString(int visibility) {
        switch (visibility) {
            case VISIBLE:
                return "Visible";
            case INVISIBLE:
                return "Invisible";
            case GONE:
                return "Gone";
            default:
                return "Unknown visibility";
        }
    }

    public static String locationToString(@Size(2) int[] l) {
        String x = "";
        for (int i = 0; i < l.length; i++) {
            x += l[i];
            if (i+1 != l.length) x += ",";
        }
        return x;
    }

    public static Rect addRect(Rect rectA, Rect rectB) {
        return new Rect(rectA.left + rectB.left,rectA.top + rectB.top,rectA.right + rectB.right,rectA.bottom + rectB.bottom);
    }

    private static String rectToString(Rect rect) {
        return rect.left + "," + rect.top + ", " + rect.right + "," + rect.bottom;
    }

    public static String indentString(int indent, String s) {
        String tmp = "";
        for (int i = 0; i < indent; i++) tmp += s;
        return tmp;
    }

    public static void logChildren(ViewGroup root) {
        final View[] children = getChildren(root);
        for (int i = root.getChildCount() - 1; i >= 0; i--) {
            final int childIndex = i;
            final View child = children[childIndex];
            Log.d(TAG, "root = [" + Taggable.getName(root) + "], child = [" + Taggable.getName(child) + "]");
        }
    }

    public static void logChildrenRecursive(ViewGroup root, MotionEvent ev) {
        final View[] children = getChildren(root);
        for (int i = root.getChildCount() - 1; i >= 0; i--) {
            final int childIndex = i;
            final View child = children[childIndex];
            Log.d(TAG, "root = [" + Taggable.getName(root) + "], child = [" + Taggable.getName(child) + "]");
            if (child != null && child instanceof ViewGroup) {
                    logChildrenRecursive((ViewGroup) child, ev);
            }
        }
    }

    /**
     * Adds a touch target for specified child to the beginning of the list.
     * Assumes the target child is not already present.
     */
    private static TouchTarget addTouchTarget(@NonNull View child, int pointerIdBits) {
        final TouchTarget target = TouchTarget.obtain(child, pointerIdBits);
        target.next = mFirstTouchTarget;
        mFirstTouchTarget = target;
        return target;
    }

    // First touch target in the linked list of touch targets.
    /* @UnsupportedAppUsage */
    private static TouchTarget mFirstTouchTarget;

    // For debugging only.  You can see these in hierarchyviewer.
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    @ViewDebug.ExportedProperty(category = "events")
    private static long mLastTouchDownTime;
    @ViewDebug.ExportedProperty(category = "events")
    private static int mLastTouchDownIndex = -1;
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    @ViewDebug.ExportedProperty(category = "events")
    private static float mLastTouchDownX;
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    @ViewDebug.ExportedProperty(category = "events")
    private static float mLastTouchDownY;

    /**
     * Gets the touch target for specified child view.
     * Returns null if not found.
     */
    public static TouchTarget getTouchTarget(@NonNull View child) {
        for (TouchTarget target = mFirstTouchTarget; target != null; target = target.next) {
            if (target.child == child) {
                return target;
            }
        }
        return null;
    }


    private static View getAndVerifyPreorderedView(ArrayList<View> preorderedList, View[] children,
                                                   int childIndex) {
        final View child;
        if (preorderedList != null) {
            child = preorderedList.get(childIndex);
            if (child == null) {
                throw new RuntimeException("Invalid preorderedList contained null child at index "
                        + childIndex);
            }
        } else {
            child = children[childIndex];
        }
        return child;
    }

    /**
     * Provide custom ordering of views in which the touch will be dispatched.
     *
     * This is called within a tight loop, so you are not allowed to allocate objects, including
     * the return array. Instead, you should return a pre-allocated list that will be cleared
     * after the dispatch is finished.
     * @hide
     */
    public static ArrayList<View> buildTouchDispatchChildList(ViewGroup root) {
        return buildOrderedChildList(root);
    }

    // Temporary holder of presorted children, only used for
    // input/software draw dispatch for correctly Z ordering.
    private static ArrayList<View> mPreSortedChildren;

    /**
     * Populates (and returns) mPreSortedChildren with a pre-ordered list of the View's children,
     * sorted first by Z, then by child drawing order (if applicable). This list must be cleared
     * after use to avoid leaking child Views.
     *
     * Uses a stable, insertion sort which is commonly O(n) for ViewGroups with very few elevated
     * children.
     */
    public static ArrayList<View> buildOrderedChildList(ViewGroup root) {
        final int childrenCount = root.getChildCount();
        if (childrenCount <= 1 || !hasChildWithZ(root)) return null;

        if (mPreSortedChildren == null) {
            mPreSortedChildren = new ArrayList<>(childrenCount);
        } else {
            // callers should clear, so clear shouldn't be necessary, but for safety...
            mPreSortedChildren.clear();
            mPreSortedChildren.ensureCapacity(childrenCount);
        }

        // assume children are drawn in the order they appear
        final boolean customOrder =false /*isChildrenDrawingOrderEnabled()*/;
        for (int i = 0; i < childrenCount; i++) {
            // add next child (in child order) to end of list
            final int childIndex = getAndVerifyPreorderedIndex(childrenCount, i, customOrder);
            final View nextChild = root.getChildAt(childIndex);
            final float currentZ = nextChild.getZ();

            // insert ahead of any Views with greater Z
            int insertIndex = i;
            while (insertIndex > 0 && mPreSortedChildren.get(insertIndex - 1).getZ() > currentZ) {
                insertIndex--;
            }
            mPreSortedChildren.add(insertIndex, nextChild);
        }
        return mPreSortedChildren;
    }

    private static int getAndVerifyPreorderedIndex(int childrenCount, int i, boolean customOrder) {
        return i;
    }

    private static boolean hasChildWithZ(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            if (root.getChildAt(i).getZ() != 0) return true;
        }
        return false;
    }

    /* Describes a touched view and the ids of the pointers that it has captured.
     *
     * This code assumes that pointer ids are always in the range 0..31 such that
     * it can use a bitfield to track which pointer ids are present.
     * As it happens, the lower layers of the input dispatch pipeline also use the
     * same trick so the assumption should be safe here...
     */
    private static final class TouchTarget {
        private static final int MAX_RECYCLED = 32;
        private static final Object sRecycleLock = new Object[0];
        private static TouchTarget sRecycleBin;
        private static int sRecycledCount;

        public static final int ALL_POINTER_IDS = -1; // all ones

        // The touched child view.
        /* @UnsupportedAppUsage */
        public View child;

        // The combined bit mask of pointer ids for all pointers captured by the target.
        public int pointerIdBits;

        // The next target in the target list.
        public TouchTarget next;

        /* @UnsupportedAppUsage */
        private TouchTarget() {
        }

        public static TouchTarget obtain(@NonNull View child, int pointerIdBits) {
            if (child == null) {
                throw new IllegalArgumentException("child must be non-null");
            }

            final TouchTarget target;
            synchronized (sRecycleLock) {
                if (sRecycleBin == null) {
                    target = new TouchTarget();
                } else {
                    target = sRecycleBin;
                    sRecycleBin = target.next;
                    sRecycledCount--;
                    target.next = null;
                }
            }
            target.child = child;
            target.pointerIdBits = pointerIdBits;
            return target;
        }

        public void recycle() {
            if (child == null) {
                throw new IllegalStateException("already recycled once");
            }

            synchronized (sRecycleLock) {
                if (sRecycledCount < MAX_RECYCLED) {
                    next = sRecycleBin;
                    sRecycleBin = this;
                    sRecycledCount += 1;
                } else {
                    next = null;
                }
                child = null;
            }
        }
    }

    /* Describes a hovered view. */
    private static final class HoverTarget {
        private static final int MAX_RECYCLED = 32;
        private static final Object sRecycleLock = new Object[0];
        private static HoverTarget sRecycleBin;
        private static int sRecycledCount;

        // The hovered child view.
        public View child;

        // The next target in the target list.
        public HoverTarget next;

        private HoverTarget() {
        }

        public static HoverTarget obtain(@NonNull View child) {
            if (child == null) {
                throw new IllegalArgumentException("child must be non-null");
            }

            final HoverTarget target;
            synchronized (sRecycleLock) {
                if (sRecycleBin == null) {
                    target = new HoverTarget();
                } else {
                    target = sRecycleBin;
                    sRecycleBin = target.next;
                    sRecycledCount--;
                    target.next = null;
                }
            }
            target.child = child;
            return target;
        }

        public void recycle() {
            if (child == null) {
                throw new IllegalStateException("already recycled once");
            }

            synchronized (sRecycleLock) {
                if (sRecycledCount < MAX_RECYCLED) {
                    next = sRecycleBin;
                    sRecycleBin = this;
                    sRecycledCount += 1;
                } else {
                    next = null;
                }
                child = null;
            }
        }
    }

    // a quick grep of android's views reveal's that only
    // View, ViewGroup, Window, WindowCallback and WindowCallbackWrapper
    // override the dispatchTouchEvent method

    public class ViewDispatcher {
    }

    public static View[] getChildren(ViewGroup root) {
        final View[] mChildren = new View[root.getChildCount()];
        for (int i = 0; i < mChildren.length; i++) {
            mChildren[i] = root.getChildAt(i);
        }
        return mChildren;
    }

}
