package smallville7123.AndroidDAW.SDK.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * An AutoCompleteTextView that correctly focuses <br><br><br>
 * Does not being up completions if not focused<br><br>
 * Clears focus when IME is dismissed<br><br>
 * provides dismissSoftKeyboard method <br><br>
 */
@SuppressLint("AppCompatCustomView")
public class FocusAutoCompleteTextView extends AutoCompleteTextView {

    public FocusAutoCompleteTextView(Context context) {
        super(context);
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public FocusAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public FocusAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @SuppressWarnings("unused")
    public FocusAutoCompleteTextView(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public final InputMethodManager inputMethodManager;

    // clear focus on keyboard back
    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_BACK) clearFocus();
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public void clearFocus() {
        dismissDropDown();
        super.clearFocus();
    }

    @Override
    public void showDropDown() {
        if (isFocused()) super.showDropDown();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) super.showDropDown();
    }

    public void dismissSoftKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        clearFocus();
    }
}
