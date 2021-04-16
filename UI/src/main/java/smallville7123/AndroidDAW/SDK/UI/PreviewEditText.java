package smallville7123.AndroidDAW.SDK.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import static android.view.KeyEvent.KEYCODE_BACK;

@SuppressLint("AppCompatCustomView")
public class PreviewEditText extends EditText {

    private static final String TAG = "PreviewEditText";

    TextViewPreviewUtils textViewPreviewUtils;

    public void setOnPreviewListener(TextViewPreviewUtils.OnPreviewListener mOnPreviewListener) {
        textViewPreviewUtils.setOnPreviewListener(mOnPreviewListener);
    }

    // Default constructor override
    public PreviewEditText(Context context) {
        super(context);
        init(context, null);
    }

    // Default constructor when inflating from XML file
    public PreviewEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    // Default constructor override
    public PreviewEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public PreviewEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    void init(Context context, AttributeSet attrs) {
        textViewPreviewUtils = new TextViewPreviewUtils(this);
    }

    /**
     * converts a {@link android.text.TextWatcher} into a {@link TextViewPreviewUtils.TextWatcher}
     * @param textWatcher the {@link android.text.TextWatcher} to convert
     * @return a new {@link TextViewPreviewUtils.TextWatcher}
     */
    public TextViewPreviewUtils.TextWatcher convertToTextWatcher(android.text.TextWatcher textWatcher) {
        return textViewPreviewUtils.convertToTextWatcher(textWatcher);
    }


    /**
     * converts a {@link android.text.TextWatcher} into a {@link TextViewPreviewUtils.PreviewTextWatcher}
     * @param textWatcher the {@link android.text.TextWatcher} to convert
     * @return a new {@link TextViewPreviewUtils.PreviewTextWatcher}
     */
    public TextViewPreviewUtils.PreviewTextWatcher convertToPreviewTextWatcher(android.text.TextWatcher textWatcher) {
        return textViewPreviewUtils.convertToPreviewTextWatcher(textWatcher);
    }

    public void addTextChangedListener(TextViewPreviewUtils.TextWatcher watcher) {
        super.addTextChangedListener(watcher);
    }

    public void addTextChangedListener(TextViewPreviewUtils.PreviewTextWatcher watcher) {
        super.addTextChangedListener(watcher);
    }

    /**
     * This method is deprecated. <br><br>
     * Please call
     * {@link #addTextChangedListener(TextViewPreviewUtils.TextWatcher)} or
     * {@link #addTextChangedListener(TextViewPreviewUtils.PreviewTextWatcher)} instead
     *
     * @see #convertToTextWatcher(android.text.TextWatcher)
     * @see #convertToPreviewTextWatcher(android.text.TextWatcher)
     */
    @Override
    @Deprecated
    final public void addTextChangedListener(android.text.TextWatcher watcher) {
        super.addTextChangedListener(watcher);
    }

    /**
     * When text changes, set the force resize flag to true and reset the text size.
     */
    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        if (textViewPreviewUtils != null) {
            textViewPreviewUtils.onTextChanged(text, start, before, after);
        }
        super.onTextChanged(text, start, before, after);
    }

    /**
     * If the text view size changed, set the force resize flag to true
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        textViewPreviewUtils.onSizeChanged(w, h, oldw, oldh);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Resize text after measuring
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        textViewPreviewUtils.onLayout(changed, left, top, right, bottom);
        super.onLayout(changed, left, top, right, bottom);
    }

    InputMethodManager inputMethodManager;

    // clear focus on keyboard back
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_BACK) clearFocus();
        return super.onKeyPreIme(keyCode, event);
    }

    public void dismissSoftKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        clearFocus();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        Log.d(TAG, "onFocusChanged() called with: focused = [" + focused + "], direction = [" + direction + "], previouslyFocusedRect = [" + previouslyFocusedRect + "]");
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        textViewPreviewUtils.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    /**
     * retains cursor position
     */
    @Override
    public void setText(CharSequence text, BufferType type) {
        int position = getSelectionStart();
        super.setText(text, type);
        setSelection(Math.min(text.length(), position));
    }
}