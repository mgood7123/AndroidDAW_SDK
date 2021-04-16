package smallville7123.AndroidDAW.SDK.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.text.LineBreaker;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

import smallville7123.AndroidDAW.SDK.UI.R;

/**
 * SLASET - Single Line Auto Size EditText
 * an EditText that auto adjusts text size to fit on a single line within the view.
 */
@SuppressLint("AppCompatCustomView")
public class SLASET extends PreviewEditText {

    private static final String TAG = "SLASET";

    // Default constructor override
    public SLASET(Context context) {
        super(context);
        init(context, null);
    }

    // Default constructor when inflating from XML file
    public SLASET(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    // Default constructor override
    public SLASET(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public SLASET(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    TextViewResizeUtils textViewResizeUtils;

    void init(Context context, AttributeSet attrs) {
        textViewResizeUtils = new TextViewResizeUtils(this);
        textViewPreviewUtils = textViewResizeUtils;
        // set to single line
        super.setSingleLine(true);

        // make sure we do not break text at all
        super.setLines(1);
        super.setMaxLines(1);
        super.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE);
        super.setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE);

        // display Go instead of Next
        setImeOptions(EditorInfo.IME_ACTION_GO);

        if (attrs != null) {
            TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SingleLineAutoSizeTextView, 0, 0);
            int r = attributes.getInt(R.styleable.SingleLineAutoSizeTextView_autoResizeMode, -1);
            textViewResizeUtils.setResizeModeFromAttributes(r);
            attributes.recycle();
        } else {
            textViewResizeUtils.resizeMode = TextViewResizeUtils.ResizeMode.width;
        }

        setOnPreviewListener(textViewResizeUtils.SLASETListener);
    }

    public TextViewResizeUtils.ResizeMode switchToResizeMode(TextViewResizeUtils.ResizeMode mode) {
        return textViewResizeUtils.switchToResizeMode(mode);
    }

    public TextViewResizeUtils.ResizeMode getResizeMode() {
        return textViewResizeUtils.getResizeMode();
    }

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
}