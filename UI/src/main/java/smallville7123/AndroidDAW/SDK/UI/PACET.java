package smallville7123.AndroidDAW.SDK.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.QwertyKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Filter;
import android.widget.MultiAutoCompleteTextView;

/**
 * PACET - Preview Auto Complete EditText
 * an EditText that auto adjusts text size to fit on a single line within the view.
 */
@SuppressLint("AppCompatCustomView")
public class PACET extends FocusAutoCompleteTextView {

    private static final String TAG = "PACET";

    TextViewPreviewUtils textViewPreviewUtils;

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        Log.d(TAG, "onFocusChanged() called with: focused = [" + focused + "], direction = [" + direction + "], previouslyFocusedRect = [" + previouslyFocusedRect + "]");
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        textViewPreviewUtils.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    public void setOnPreviewListener(TextViewPreviewUtils.OnPreviewListener mOnPreviewListener) {
        textViewPreviewUtils.setOnPreviewListener(mOnPreviewListener);
    }

    // Default constructor override
    public PACET(Context context) {
        super(context);
        init(context, null);
    }

    // Default constructor when inflating from XML file
    public PACET(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    // Default constructor override
    public PACET(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public PACET(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    /**
     * retains cursor position
      */
    @Override
    public void setText(CharSequence text, BufferType type) {
        int position = getSelectionStart();
        super.setText(text, type);
        setSelection(Math.min(text.length(), position));
    }


    private MultiAutoCompleteTextView.Tokenizer mTokenizer;

    /**
     * Sets the Tokenizer that will be used to determine the relevant
     * range of the text where the user is typing.
     */
    public void setTokenizer(MultiAutoCompleteTextView.Tokenizer t) {
        mTokenizer = t;
    }

    /**
     * Instead of filtering on the entire contents of the edit box,
     * this subclass method filters on the range from
     * {@link MultiAutoCompleteTextView.Tokenizer#findTokenStart} to {@link #getSelectionEnd}
     * if the length of that range meets or exceeds {@link #getThreshold}.
     */
    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        if (mTokenizer == null) {
            super.performFiltering(text, keyCode);
            return;
        }

        if (enoughToFilter()) {
            int end = getSelectionEnd();
            int start = mTokenizer.findTokenStart(text, end);

            performFiltering(text, start, end, keyCode);
        } else {
            dismissDropDown();

            Filter f = getFilter();
            if (f != null) {
                f.filter(null);
            }
        }
    }

    /**
     * Instead of filtering whenever the total length of the text
     * exceeds the threshhold, this subclass filters only when the
     * length of the range from
     * {@link MultiAutoCompleteTextView.Tokenizer#findTokenStart} to {@link #getSelectionEnd}
     * meets or exceeds {@link #getThreshold}.
     */
    @Override
    public boolean enoughToFilter() {
        if (mTokenizer == null) {
            return super.enoughToFilter();
        }

        Editable text = getText();

        int end = getSelectionEnd();
        if (end < 0 || mTokenizer == null) {
            return false;
        }

        int start = mTokenizer.findTokenStart(text, end);

        if (end - start >= getThreshold()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Instead of validating the entire text, this subclass method validates
     * each token of the text individually.  Empty tokens are removed.
     */
    @Override
    public void performValidation() {
        if (mTokenizer == null) {
            super.performValidation();
            return;
        }

        Validator v = getValidator();

        if (v == null || mTokenizer == null) {
            return;
        }

        Editable e = getText();
        int i = getText().length();
        while (i > 0) {
            int start = mTokenizer.findTokenStart(e, i);
            int end = mTokenizer.findTokenEnd(e, start);

            CharSequence sub = e.subSequence(start, end);
            if (TextUtils.isEmpty(sub)) {
                e.replace(start, i, "");
            } else if (!v.isValid(sub)) {
                e.replace(start, i,
                        mTokenizer.terminateToken(v.fixText(sub)));
            }

            i = start;
        }
    }

    /**
     * <p>Starts filtering the content of the drop down list. The filtering
     * pattern is the specified range of text from the edit box. Subclasses may
     * override this method to filter with a different pattern, for
     * instance a smaller substring of <code>text</code>.</p>
     */
    protected void performFiltering(CharSequence text, int start, int end,
                                    int keyCode) {
        getFilter().filter(text.subSequence(start, end), this);
    }

    /**
     * <p>Performs the text completion by replacing the range from
     * {@link MultiAutoCompleteTextView.Tokenizer#findTokenStart} to {@link #getSelectionEnd} by the
     * the result of passing <code>text</code> through
     * {@link MultiAutoCompleteTextView.Tokenizer#terminateToken}.
     * In addition, the replaced region will be marked as an AutoText
     * substition so that if the user immediately presses DEL, the
     * completion will be undone.
     * Subclasses may override this method to do some different
     * insertion of the content into the edit box.</p>
     *
     * @param text the selected suggestion in the drop down list
     */
    @Override
    protected void replaceText(CharSequence text) {
        if (mTokenizer == null) {
            super.replaceText(text);
            return;
        }

        clearComposingText();

        int end = getSelectionEnd();
        int start = mTokenizer.findTokenStart(getText(), end);

        Editable editable = getText();
        String original = TextUtils.substring(editable, start, end);

        QwertyKeyListener.markAsReplaced(editable, start, end, original);
        editable.replace(start, end, mTokenizer.terminateToken(text));
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return PACET.class.getName();
    }
}