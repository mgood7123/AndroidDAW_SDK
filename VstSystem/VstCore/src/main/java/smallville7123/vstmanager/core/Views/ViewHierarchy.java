package smallville7123.vstmanager.core.Views;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static smallville7123.taggable.Taggable.getLastClassName;

public class ViewHierarchy implements Iterable<ViewHierarchy> {
    private static final String TAG = "ViewHierarchy";
    View view;
    View parent;
    float x;
    float y;
    int depth;
    int invertedDepth;
    int maxDepth;
    ArrayList<ViewHierarchy> children;

    ArrayList<ViewHierarchy> toArrayList() {
        ArrayList<ViewHierarchy> arrayList = new ArrayList<>();
        // first, add itself
        arrayList.add(this);
        // next, add each children, if any
        // this will recursively add each children, and then their children
        // for example
        // A {
        //   B, {
        //     C, {
        //       D
        //     }
        //   }
        // }
        // add A, add B, add C, add D, no more children
        for (ViewHierarchy viewHierarchy : this) arrayList.addAll(viewHierarchy.toArrayList());
        return arrayList;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NonNull
    @Override
    public Iterator<ViewHierarchy> iterator() {
        return children != null ? children.iterator() : new Iterator<ViewHierarchy>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public ViewHierarchy next() {
                return null;
            }
        };
    }

    /**
     * Performs the given action for each element of the {@code Iterable}
     * until all elements have been processed or the action throws an
     * exception.  Unless otherwise specified by the implementing class,
     * actions are performed in the order of iteration (if an iteration order
     * is specified).  Exceptions thrown by the action are relayed to the
     * caller.
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @implSpec <p>The default implementation behaves as if:
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     * @since 1.8
     */
    @Override
    public void forEach(@NonNull Consumer<? super ViewHierarchy> action) {
        if (children != null) children.forEach(action);
    }

    /**
     * Creates a {@link Spliterator} over the elements described by this
     * {@code Iterable}.
     *
     * @return a {@code Spliterator} over the elements described by this
     * {@code Iterable}.
     * @implSpec The default implementation creates an
     * <em><a href="Spliterator.html#binding">early-binding</a></em>
     * spliterator from the iterable's {@code Iterator}.  The spliterator
     * inherits the <em>fail-fast</em> properties of the iterable's iterator.
     * @implNote The default implementation should usually be overridden.  The
     * spliterator returned by the default implementation has poor splitting
     * capabilities, is unsized, and does not report any spliterator
     * characteristics. Implementing classes can nearly always provide a
     * better implementation.
     * @since 1.8
     */
    @NonNull
    @Override
    public Spliterator<ViewHierarchy> spliterator() {
        return children != null ? children.spliterator() : new Spliterator<ViewHierarchy>() {
            @Override
            public boolean tryAdvance(Consumer<? super ViewHierarchy> action) {
                return false;
            }

            @Override
            public Spliterator<ViewHierarchy> trySplit() {
                return this;
            }

            @Override
            public long estimateSize() {
                return 0;
            }

            @Override
            public int characteristics() {
                return 0;
            }
        };
    }

    // printing

    String asciiArrow(int depth) {
        return Strings.repeat("-", depth) + ">";
    }



    public void print() {
        // depth is computed during construction of the hierarchy
        Log.d(TAG, "traverse: " + asciiArrow(depth) + " view = [" + (view == null ? "null" : getLastClassName(view)) + "], depth = [" + depth + "], inverted depth = [" + invertedDepth + "]");
        // Attempt to invoke virtual method
        // 'java.util.Iterator java.util.ArrayList.iterator()'
        // on a null object reference
        for (ViewHierarchy child : this) {
            child.print();
        }
    }

    // build up a child hierarchy

    public void analyze(View root) {
        analyzeInternal(root, 1);
    }

    // internal

    void analyzeInternal(View root, int depth) {
        analyze(root, depth);
        computeOffsets(null);
    }

    private void computeOffsets(ViewHierarchy parent) {
        x = view.getX();
        y = view.getY();
        if (parent != null) {
            x += parent.x;
            y += parent.y;
        }
        for (ViewHierarchy hierarchy : this) {
            hierarchy.computeOffsets(this);
        }
    }

    int set(View root, int depth) {
        view = root;
        ViewParent p = view.getParent();
        if (p instanceof View) parent = (View) p;
        this.depth = depth;
        if (depth > maxDepth) maxDepth = depth;
        return maxDepth;
    }

    int analyze(View root, int depth) {
        if (root instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) root;
            int r = set(viewGroup, depth);
            int childCount = viewGroup.getChildCount();
            if (childCount != 0) {
                children = new ArrayList<>();
                for (int i = 0; i < childCount; i++) {
                    ViewHierarchy viewHierarchy = new ViewHierarchy();
                    int r2 = viewHierarchy.analyze(viewGroup.getChildAt(i), depth + 1);
                    if (r2 > r) r = r2;
                    children.add(viewHierarchy);
                }
            }
            return r;
        } else return set(root, depth);
    }

    // painters algorithm

    // 1. Sort all polygons according to z coordinate.

    // Z-order in android, and in the hierarchy, is just the depth count
    ArrayList<ViewHierarchy> sortByDepth() {
        ArrayList<ViewHierarchy> flatList = toArrayList();
        flatList.sort((o1, o2) -> Integer.compare(o1.depth, o2.depth));
        return flatList;
    }
}