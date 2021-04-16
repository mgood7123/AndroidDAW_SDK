package smallville7123.AndroidDAW.SDK.UI;

import android.graphics.Rect;
import android.text.Editable;
import android.widget.TextView;

import androidx.annotation.CallSuper;

public class TextViewPreviewUtils {

    final TextView textView;

    private String preview = "";
    private String realText = "";
    boolean needsRefresh = false;
    private boolean inRefresh = false;
    private boolean isSettingPreview = false;


    TextViewPreviewUtils(TextView textView) {
        this.textView = textView;
    }

    /**
     * When text changes, set the force resize flag to true and reset the text size.
     */
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        needsRefresh = true;
    }

    /**
     * If the text view size changed, set the force resize flag to true
     */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            needsRefresh = true;
        }
    }

    /**
     * Resize text after measuring
     */
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if(changed || needsRefresh) {
            inRefresh = true;
            mOnPreviewListener.onLayout(changed, left, top, right, bottom);
            refresh();
            inRefresh = false;
        }
    }

    public void refresh() {
        inRefresh = true;
        mOnPreviewListener.refreshBegin();
        String r = new String(realText);
        String p = new String(preview);
        preview = mOnPreviewListener.refreshProcess(r, p);
        mOnPreviewListener.refreshEnd();
        inRefresh = false;
    }

    public void setTextToRealText() {
        setText(realText);
    }

    public void setTextToPreviewText() {
        setText(preview);
    }

    /**
     * a version of TextWatcher that gets called when the main text changes
     */
    abstract class TextWatcher implements android.text.TextWatcher {
        final boolean shouldCall() {
            return !isSettingPreview;
        }
    }

    /**
     * a version of TextWatcher that gets called when the preview text changes
     */
    abstract class PreviewTextWatcher implements android.text.TextWatcher {
        final boolean shouldCall() {
            return isSettingPreview;
        }
    }

    /**
     * converts a {@link android.text.TextWatcher} into a {@link TextWatcher}
     * @param textWatcher the {@link android.text.TextWatcher} to convert
     * @return a new {@link TextWatcher}
     */
    public TextWatcher convertToTextWatcher(android.text.TextWatcher textWatcher) {
        return new TextWatcher() {
            final android.text.TextWatcher textWatcher_ = textWatcher;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (shouldCall()) textWatcher_.beforeTextChanged(s, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (shouldCall()) textWatcher_.onTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (shouldCall()) textWatcher_.afterTextChanged(s);
            }
        };
    }

    /**
     * converts a {@link android.text.TextWatcher} into a {@link PreviewTextWatcher}
     * @param textWatcher the {@link android.text.TextWatcher} to convert
     * @return a new {@link PreviewTextWatcher}
     */
    public PreviewTextWatcher convertToPreviewTextWatcher(android.text.TextWatcher textWatcher) {
        return new PreviewTextWatcher() {
            final android.text.TextWatcher textWatcher_ = textWatcher;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (shouldCall()) textWatcher_.beforeTextChanged(s, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (shouldCall()) textWatcher_.onTextChanged(s, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (shouldCall()) textWatcher_.afterTextChanged(s);
            }
        };
    }

    public void setOnPreviewListener(OnPreviewListener mOnPreviewListener) {
        this.mOnPreviewListener = mOnPreviewListener;
    }

    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        inRefresh = true;
        if (focused) {
            setText(realText);
        } else needsRefresh = true;
        mOnPreviewListener.onPreview(focused);
        inRefresh = false;
    }

    private void setText(String text) {
        textView.setText(text);
    }


    public class OnPreviewListener {
        /**
         * @param active if the preview is active or not
         */
        void onPreview(boolean active) {
            // do nothing
        }

        @CallSuper
        void refreshBegin() {
            if (!isFocused()) {
                realText = getText().toString();
            } else {
                // the IME can cause a layout sometimes
                // which will erase what the user has typed
//                setText(realText);
            }
        }

        /**
         * process your preview string here
         * @param realText a copy of the current real text
         * @param preview a copy of the current preview text
         * @return a string to become the preview
         */
        String refreshProcess(String realText, String preview) {
            return "A";
        }

        @CallSuper
        void refreshEnd() {
            if (!isFocused()) {
                isSettingPreview = true;
                setText(preview);
                isSettingPreview = false;
            }
            needsRefresh = false;
        }

        public void onLayout(boolean changed, int left, int top, int right, int bottom) {
            // do nothing
        }
    }

    private CharSequence getText() {
        return textView.getText();
    }

    private boolean isFocused() {
        return textView.isFocused();
    }

    private OnPreviewListener mOnPreviewListener = new OnPreviewListener();

}
