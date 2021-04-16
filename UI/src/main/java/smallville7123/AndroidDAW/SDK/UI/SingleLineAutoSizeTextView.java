package smallville7123.AndroidDAW.SDK.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.text.LineBreaker;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

import smallville7123.AndroidDAW.SDK.UI.R;

/**
 * Text view that auto adjusts text size to fit on a single line within the view.
 */
@SuppressLint("AppCompatCustomView")
public class SingleLineAutoSizeTextView extends TextView {

    // Default constructor override
    public SingleLineAutoSizeTextView(Context context) {
        super(context);
        init(context, null);
    }

    // Default constructor when inflating from XML file
    public SingleLineAutoSizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    // Default constructor override
    public SingleLineAutoSizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public SingleLineAutoSizeTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    TextViewResizeUtils textViewResizeUtils;

    void init(Context context, AttributeSet attrs) {
        textViewResizeUtils = new TextViewResizeUtils(this);

        // set to single line
        super.setSingleLine(true);

        // make sure we do not break text at all
        super.setLines(1);
        super.setMaxLines(1);
        super.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE);
        super.setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE);

        if (attrs != null) {
            TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SingleLineAutoSizeTextView, 0, 0);
            int r = attributes.getInt(R.styleable.SingleLineAutoSizeTextView_autoResizeMode, -1);
            textViewResizeUtils.setResizeModeFromAttributes(r);
            attributes.recycle();
        } else {
            textViewResizeUtils.resizeMode = TextViewResizeUtils.ResizeMode.width;
        }

        textViewResizeUtils.setOnPreviewListener(textViewResizeUtils.SLASETListener);
    }

    public TextViewResizeUtils.ResizeMode switchToResizeMode(TextViewResizeUtils.ResizeMode mode) {
        return textViewResizeUtils.switchToResizeMode(mode);
    }

    public TextViewResizeUtils.ResizeMode getResizeMode() {
        return textViewResizeUtils.getResizeMode();
    }

    // Text view line spacing multiplier
    private float mSpacingMult = 1.0f;

    // Text view additional line spacing
    private float mSpacingAdd = 0.0f;

    // no-op these to prevent accidental wrapping

    @Override
    public void setBreakStrategy(int breakStrategy) {
    }

    @Override
    public void setHyphenationFrequency(int hyphenationFrequency) {
    }

    @Override
    public void setSingleLine() {
    }

    @Override
    public void setSingleLine(boolean singleLine) {
    }

    @Override
    public void setLines(int lines) {
    }

    @Override
    public void setMaxLines(int maxLines) {
    }

    /**
     * When text changes, set the force resize flag to true and reset the text size.
     */
    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        if (textViewResizeUtils != null) {
            textViewResizeUtils.onTextChanged(text, start, before, after);
        }
        super.onTextChanged(text, start, before, after);
    }

    /**
     * If the text view size changed, set the force resize flag to true
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        textViewResizeUtils.onSizeChanged(w, h, oldw, oldh);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Resize text after measuring
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        textViewResizeUtils.onLayout(changed, left, top, right, bottom);
        textViewResizeUtils.setTextToPreviewText();
        super.onLayout(changed, left, top, right, bottom);
    }
}