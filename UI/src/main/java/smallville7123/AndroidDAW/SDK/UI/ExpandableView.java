package smallville7123.AndroidDAW.SDK.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ExpandableView extends LinearLayout {

    private static final String TAG = "ExpandableView";
    Runnable onHeaderClicked;
    Runnable onContentClicked;
    boolean expanded;
    boolean replaceHeaderWhenExpanded;
    private FrameLayout header;
    private FrameLayout content;


    public ExpandableView(Context context) {
        this(context, null);
    }

    public ExpandableView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    boolean shouldExpand;

    public ExpandableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (attrs != null) {
            final TypedArray a = getTypedArray(attrs, defStyleAttr, defStyleRes);
            shouldExpand = a.getBoolean(R.styleable.ExpandableView_expanded, false);
            replaceHeaderWhenExpanded = a.getBoolean(R.styleable.ExpandableView_replaceHeaderWhenExpanded, false);
            a.recycle();
        } else {
            shouldExpand = false;
            replaceHeaderWhenExpanded = false;
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        int childCount = getChildCount();
        switch (childCount) {
            case 0:
                setHeader(child, params);
                break;
            case 1:
                setContent(child, params);
                break;
            default:
                throw new RuntimeException("This view cannot have more than 2 views");
        }
    }

    boolean hasView(View view) {
        return view != null && indexOfChild(view) != -1;
    }

    public boolean hasHeader() {
        return hasView(header);
    }

    public boolean hasContent() {
        return hasView(content);
    }

    @NonNull
    private TypedArray getTypedArray(@NonNull AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ExpandableView, defStyleAttr, defStyleRes);
        return a;
    }

    public void expand(boolean animate) {
        if (content == null || expanded) return;
        expanded = true;
        if (animate) {

        } else {
            header.setVisibility(replaceHeaderWhenExpanded ? GONE : VISIBLE);
            content.setVisibility(VISIBLE);
        }
    }

    void collapse(boolean animate) {
        if (content == null || !expanded) return;
        expanded = false;
        if (animate) {

        } else {
            header.setVisibility(VISIBLE);
            content.setVisibility(GONE);
        }
    }

    public void setOnHeaderClicked(Runnable onHeaderClicked) {
        this.onHeaderClicked = onHeaderClicked;
    }

    public void setOnContentClicked(Runnable onContentClicked) {
        this.onContentClicked = onContentClicked;
    }


    public void toggleExpandedState(boolean animate) {
        if (expanded) {
            collapse(animate);
        } else {
            expand(animate);
        }
    }

    public void setHeader(View view, ViewGroup.LayoutParams params) {
        if (header != null) {
            header.removeViewAt(0);
            super.removeView(header);
        }
        header = new FrameLayout(getContext());
        header.setOnClickListener(this::headerOnClick);
        header.addView(view, -1, params);
        super.addView(header, 0, params);
    }

    public void setContent(View view, ViewGroup.LayoutParams params) {
        if (content != null) {
            content.removeViewAt(0);
            super.removeView(content);
        }
        content = new FrameLayout(getContext());
        if (!expanded) content.setVisibility(GONE);
        content.setOnClickListener(this::contentOnClick);
        content.addView(view, -1, params);
        super.addView(content, 1, params);
        if (shouldExpand) expand(false);
    }

    /**
     * Returns the view at the specified position in the group.
     *
     * @param index the position at which to get the view from
     * @return the view at the specified position or null if the position
     *         does not exist within the group
     */
    private  <T extends View> T getChild(ViewGroup viewGroup, int index) {
        return (T) viewGroup.getChildAt(0);
    }

    public <T extends View> T getHeader() {
        return getChild(header, 0);
    }

    public <T extends View> T getContent() {
        return getChild(content, 0);
    }

    public void setHeaderTag(Object object) {
        header.setTag(object);
    }

    public void setHeaderTag(int key, Object object) {
        header.setTag(key, object);
    }

    public Object getHeaderTag() {
        return header.getTag();
    }

    public Object getHeaderTag(int key) {
        return header.getTag(key);
    }

    public void setContentTag(Object object) {
        content.setTag(object);
    }

    public void setContentTag(int key, Object object) {
        content.setTag(key, object);
    }

    public Object getContentTag() {
        return content.getTag();
    }

    public Object getContentTag(int key) {
        return content.getTag(key);
    }

    private void contentOnClick(View v) {
        if (onContentClicked != null) onContentClicked.run();
    }

    private void headerOnClick(View v) {
        toggleExpandedState(false);
        if (onHeaderClicked != null) onHeaderClicked.run();
    }
}
