package smallville7123.vstmanager.core.Views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import smallville7123.taggable.Taggable;
import smallville7123.vstmanager.core.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class WindowView extends FrameLayout {

    public boolean randomized = false;
    private OnDragTouchListener draggable;

    public WindowView(Context context) {
        super(context);
        init(context, null);
    }

    public WindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WindowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public WindowView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public float offsetLeft;
    public float offsetRight;
    public float offsetTop;
    public float offsetBottom;
    public int marginTop;
    public int marginLeft;


    public void setDrag(VstView parent) {
        draggable = new OnDragTouchListener(this, parent);
        draggable.widthLeft = touchZoneWidthLeft;
        draggable.widthRight = touchZoneWidthRight;
        draggable.heightTop = touchZoneHeightTop;
        draggable.heightBottom = touchZoneHeightBottom;
        draggable.marginTop = marginTop;
        draggable.marginLeft = marginLeft;

        draggable.offsetTop = offsetTop;
        draggable.offsetBottom = offsetBottom;
        draggable.offsetLeft = offsetLeft;
        draggable.offsetRight = offsetRight;
        draggable.onlyDragWithinWidthAndHeightRegions = true;
    }

    Drawable icon;
    CharSequence title;

    public void setIcon(Drawable icon) {
        this.icon = icon;
        if (titleBarContent != null)
            ((ImageView) titleBarContent.findViewById(R.id.icon)).setImageDrawable(icon);
    }

    public void setTitle(CharSequence title) {
        if (title == null) title = mContext.getPackageName();
        Log.d(TAG, "title = [" + (title) + "]");
        this.title = title;
        if (titleBarContent != null)
            ((TextView) titleBarContent.findViewById(R.id.title)).setText(title);
    }

    private static class Internal {}
    Internal Internal = new Internal();
    public float widthLeft = 10.0f;
    public float widthRight = 10.0f;
    public float heightTop = 10.0f;
    public float heightBottom = 10.0f;
    public float touchZoneWidthLeft = 80.0f;
    public float touchZoneWidthRight = 80.0f;
    public float touchZoneHeightTop = 80.0f;
    public float touchZoneHeightBottom = 80.0f;
    public float touchZoneWidth;
    public float touchZoneHeight;
    private float titlebarOffset;
    public int titlebarHeight = 80;

    void getAttributeParameters(Context context, AttributeSet attrs, Resources.Theme theme) {
        if (attrs != null) {
//            TypedArray attributes = theme.obtainStyledAttributes(attrs, R.styleable.ExpandableLayout, 0, 0);
//            text = attributes.getString(R.styleable.ExpandableLayout_android_text);
//            textSize = LayoutUtils.getTextSizeAttributesSuitableForTextView(attributes, R.styleable.ExpandableLayout_android_textSize, 30f);
//            textColor = attributes.getColor(R.styleable.ExpandableLayout_android_textColor, Color.WHITE);
//            expanded = attributes.getBoolean(R.styleable.ExpandableLayout_android_state_expanded, false);
//            background = attributes.getDrawable(R.styleable.ExpandableLayout_android_background);
//            chevronColor = attributes.getColor(R.styleable.ExpandableLayout_chevron_color, -1);
//            attributes.recycle();
        }
    }

    LayoutParams windowContentLayout;

    public String TAG = Taggable.getTag(this);

    Paint highlightPaint;
    Paint highlightCornerPaint;
    Paint touchZonePaint;
    Paint regionPaint;
    Paint titleBarPaint;


    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        int width = getWidth();
        int height = getHeight();
        Paint rp = regionPaint;
        if (draggable != null) {
            if (draggable.isResizing) {
                if (draggable.corner) {
                    rp = highlightCornerPaint;
                } else {
                    rp = highlightPaint;
                }
                drawHighlight(canvas, width, height, rp);
            }
        }
        drawBorders(canvas, width, height, rp);
//        drawTouchZones(canvas, width, height, touchZonePaint);
    }

    void drawHighlight(Canvas canvas, int width, int height, Paint paint) {
        canvas.drawRect(offsetLeft, offsetTop, width-offsetRight, height-offsetBottom, paint);
    }

    void drawBorders(Canvas canvas, int width, int height, Paint paint) {
        // left
        canvas.drawRect(touchZoneWidthLeft, touchZoneHeightTop, touchZoneWidthLeft-widthLeft, height-touchZoneHeightBottom, paint);
        // right
        canvas.drawRect(width - touchZoneWidthRight, touchZoneHeightTop, width-touchZoneWidthRight+widthRight, height-touchZoneHeightBottom, paint);
        // top
        canvas.drawRect(touchZoneWidthLeft-widthLeft, touchZoneHeightTop, width-touchZoneWidthRight+widthRight, touchZoneHeightTop-heightTop, paint);
        // bottom
        canvas.drawRect(touchZoneWidthLeft-widthLeft, height-touchZoneHeightBottom, width-touchZoneWidthRight+widthRight, height-touchZoneHeightBottom+heightBottom, paint);
    }

    void drawTouchZones(Canvas canvas, int width, int height, Paint paint) {
        // left
        canvas.drawRect(0, 0, touchZoneWidthLeft, height, paint);
        // right
        canvas.drawRect(width - touchZoneWidthRight, 0, width, height, paint);
        // top
        canvas.drawRect(0, 0, width, touchZoneHeightTop, paint);
        // bottom
        canvas.drawRect(0, height - touchZoneHeightBottom, width, height, paint);
    }

    ConstraintLayout root;
    LayoutParams rootLayoutParams;
    Context mContext;
    FrameLayout titleBarContent;

    boolean maximized = false;

    float savedX;
    float savedY;
    int savedWidth;
    int savedHeight;

    boolean minimized = false;
    View restore;
    View maximize;

    private void init(Context context, AttributeSet attrs) {
        getRootLayout(context);
        rootLayoutParams = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        addView(root, rootLayoutParams);

        titleBarContent = setupTitleBarContent(root);
        View titleBar = inflate(context, R.layout.titlebar, null);
        restore = titleBar.findViewById(R.id.restore);
        maximize = titleBar.findViewById(R.id.maximize);
        restore.setVisibility(GONE);
        maximize.setOnClickListener(v -> maximize());
        restore.setOnClickListener(v -> restore());
        titleBar.findViewById(R.id.minimize).setOnClickListener(v -> minimize());
        Log.d(TAG, "attrs = [" + (attrs) + "]");
        if (attrs != null) {
            TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WindowView, 0, 0);
            CharSequence text = attributes.getString(R.styleable.WindowView_android_text);
            Log.d(TAG, "text = [" + (text) + "]");
            setTitleBar(titleBar, text);
            attributes.recycle();
        } else setTitleBar(titleBar);
        setWindowContent(root);
        setPaint();

        switch (getVisibility()) {
            case INVISIBLE:
            case GONE:
                if (!isInEditMode()) {
                    setVisibility(VISIBLE);
                    invalidate();
                    post(this::minimize);
                }
                break;
        }

        // we could do:
        // toolkit.currentFrame().provideCustomCloseButton(new myCloseButton(myResources));
    }

    boolean isMaximizedWidth = false;
    boolean isMaximizedHeight = false;

    void setMaximizeWidth() {
        if (isMaximizedHeight) {
            isMaximizedHeight = false;
            maximize();
        } else {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            savedX = getX();

            setX(-offsetLeft);
            int width = ((ViewGroup) getParent()).getWidth();

            layoutParams.width = (int) (width - widthRight - (touchZoneWidthRight - offsetRight));
            setLayoutParams(layoutParams);
            isMaximizedWidth = true;
        }
    }

    void setMaximizeHeight() {
        if (isMaximizedWidth) {
            isMaximizedWidth = false;
            maximize();
        } else {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            savedY = getY();

            setY(-offsetTop);
            int height = ((ViewGroup) getParent()).getHeight();

            layoutParams.height = (int) (height - heightBottom - (touchZoneHeightBottom - offsetBottom));
            setLayoutParams(layoutParams);
            isMaximizedHeight = true;
        }
    }

    void maximize() {
        isMaximizedWidth = false;
        isMaximizedHeight = false;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        savedX = getX();
        savedY = getY();
        savedWidth = layoutParams.width;
        savedHeight = layoutParams.height;

        setX(-offsetLeft);
        setY(-offsetTop);
        int width = ((ViewGroup) getParent()).getWidth();
        int height = ((ViewGroup) getParent()).getHeight();

        layoutParams.width = (int) (width - widthRight - (touchZoneWidthRight - offsetRight));
        layoutParams.height = (int) (height - heightBottom - (touchZoneHeightBottom - offsetBottom));
        setLayoutParams(layoutParams);
        restore.setVisibility(VISIBLE);
        maximize.setVisibility(GONE);

        maximized = true;
    }

    void restore() {
        isMaximizedWidth = false;
        isMaximizedHeight = false;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        setX(savedX);
        setY(savedY);

        layoutParams.width = savedWidth;
        layoutParams.height = savedHeight;
        setLayoutParams(layoutParams);

        restore.setVisibility(GONE);
        maximize.setVisibility(VISIBLE);
        maximized = false;
    }

    void minimize() {
        setVisibility(GONE);
        minimized = true;
    }

    private void setPaint() {
        highlightPaint = new Paint();
        highlightCornerPaint = new Paint();
        regionPaint = new Paint();
        touchZonePaint = new Paint();
        titleBarPaint = new Paint();

        highlightPaint.setARGB(70, 0, 0, 255);
        highlightCornerPaint.setARGB(70, 255, 90, 0);
        touchZonePaint.setARGB(60, 0, 90, 0);
        regionPaint.setARGB(255, 168, 168, 168);
        titleBarPaint.setARGB(255, 0,0,255);
    }

    private void getRootLayout(Context context) {
        mContext = context;
        Resources.Theme theme = context.getTheme();
        root = (ConstraintLayout) inflate(context, R.layout.window, null);
        root.setTag(Internal);
        setBackgroundColor(Color.TRANSPARENT);

        setVariables();
        setX(-offsetLeft);
        setY(-offsetTop);
    }

    private void setVariables() {
        touchZoneWidth = touchZoneWidthLeft+touchZoneWidthRight;
        touchZoneHeight = touchZoneHeightTop+touchZoneHeightBottom;
        titlebarOffset = touchZoneHeightTop;
        marginTop = (int) (titlebarOffset+titlebarHeight);
        marginLeft = (int) titlebarHeight;


        offsetTop = touchZoneHeightTop-heightTop;
        offsetBottom = touchZoneHeightBottom-heightBottom;
        offsetLeft = touchZoneWidthLeft-widthLeft;
        offsetRight = touchZoneWidthRight-widthRight;
    }

    private void setTitleBar(View titleBar, CharSequence title) {
        Log.d(TAG, "title = [" + (title) + "]");
        titleBar.setBackgroundColor(Color.BLUE);
        if (titleBarContent.getChildAt(0) != null) {
            titleBarContent.removeViewAt(0);
        }
        titleBarContent.addView(titleBar);
        setTitle(title);
    }

    private void setTitleBar(View titleBar) {
        setTitleBar(titleBar, title);
    }

    private FrameLayout setupTitleBarContent(ConstraintLayout root) {
        FrameLayout titlebar_content = root.findViewById(R.id.titlebar_content);
        ConstraintLayout.LayoutParams titlebar_contentLayoutParams = (ConstraintLayout.LayoutParams) titlebar_content.getLayoutParams();
        titlebar_contentLayoutParams.height = titlebarHeight;
        titlebar_contentLayoutParams.topMargin = (int) titlebarOffset;
        titlebar_contentLayoutParams.leftMargin = (int) (touchZoneWidthLeft);
        titlebar_contentLayoutParams.rightMargin = (int) (touchZoneWidth+touchZoneWidthRight);
        titlebar_content.setLayoutParams(titlebar_contentLayoutParams);
        return titlebar_content;
    }

    FrameLayout window_content;

    public FrameLayout getWindowContent() {
        return window_content;
    }

    private void setWindowContent(ConstraintLayout root) {
        window_content = root.findViewById(R.id.window_content);
        window_content.setBackgroundColor(Color.BLACK);
        ConstraintLayout.LayoutParams window_contentLayout = (ConstraintLayout.LayoutParams) window_content.getLayoutParams();
        window_contentLayout.bottomMargin = (int) (touchZoneHeightBottom);
        window_contentLayout.leftMargin = (int) (touchZoneWidthLeft);
        window_contentLayout.rightMargin = (int) (touchZoneWidth+touchZoneWidthRight);
        window_content.setLayoutParams(window_contentLayout);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(
                        MeasureSpec.getSize(widthMeasureSpec)+(int)(touchZoneWidth),
                        MeasureSpec.getMode(widthMeasureSpec)
                ),
                MeasureSpec.makeMeasureSpec(
                        MeasureSpec.getSize(heightMeasureSpec)+(int)(touchZoneHeight),
                        MeasureSpec.getMode(heightMeasureSpec)
                )
        );
    }

    boolean broughtToFront = false;

    public boolean bringThisWindowToFront(VstView vstView) {
        if (
                vstView.currentTop == this
                        || vstView.currentTop == null
                        || minimized || !vstView.childHasBeenBroughtToFront
        ) {
            if (minimized) {
                if (vstView.currentTop != null) {
                    vstView.currentTop.broughtToFront = false;
                }
                setVisibility(VISIBLE);
            }
            bringToFront();
            broughtToFront = true;
            vstView.childHasBeenBroughtToFront = true;
            vstView.currentTop = this;
            minimized = false;
            invalidate();
            // process input
            return false;
        } else {
            // do not process input
            return true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (broughtToFront) {
            // process input
            return false;
        }
        ViewParent parent = getParent();
        if (parent instanceof VstView) {
            return bringThisWindowToFront((VstView) parent);
        } else throw new RuntimeException("invalid parent");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (broughtToFront) {
            if (!maximized) {
                // lazy
                isMaximizedWidth = false;
                isMaximizedHeight = false;
                draggable.onTouch(event);
            }
            return true;
        }
        return false;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        Object tag = child.getTag();
        if (tag instanceof Internal) super.addView(child, index, params);
        else window_content.addView(child, -1, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }
}