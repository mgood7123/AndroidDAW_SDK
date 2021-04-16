package smallville7123.AndroidDAW.SDK.UI;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.widget.MultiAutoCompleteTextView;

public class StringTokenizer implements MultiAutoCompleteTextView.Tokenizer {
    private static final String TAG = "StringTokenizer";
    private String stringToSplitBy;
    private String stringToAppendAfterCompletion;

    public StringTokenizer(char stringToSplitBy) {
        this(String.valueOf(stringToSplitBy));
    }

    public StringTokenizer(char stringToSplitBy, char stringToAppendAfterCompletion) {
        this(String.valueOf(stringToSplitBy), String.valueOf(stringToAppendAfterCompletion));
    }

    public StringTokenizer(String stringToSplitBy) {
        this(stringToSplitBy, null);
    }

    public StringTokenizer(String stringToSplitBy, String stringToAppendAfterCompletion) {
        this.stringToSplitBy = stringToSplitBy;
        this.stringToAppendAfterCompletion = stringToAppendAfterCompletion;
    }

    public StringTokenizer(String stringToSplitBy, char stringToAppendAfterCompletion) {
        this(stringToSplitBy, String.valueOf(stringToAppendAfterCompletion));
    }

    public StringTokenizer(char stringToSplitBy, String stringToAppendAfterCompletion) {
        this(String.valueOf(stringToSplitBy), stringToAppendAfterCompletion);
    }

    public int findTokenStart(CharSequence text, int cursor) {
        int index = text.toString().lastIndexOf(stringToSplitBy, cursor);
        index = index == -1 ? 0 : index + 1;
        Log.i(TAG, "start index = [" + index + "]");
        return index;
    }

    public int findTokenEnd(CharSequence text, int cursor) {
        int index = text.toString().indexOf(stringToSplitBy, cursor);
        index = index == -1 ? 0 : index;
        Log.i(TAG, "end index = [" + index + "]");
        return index;
    }

    public CharSequence terminateToken(CharSequence text) {
        String r = text + stringToAppendAfterCompletion;
        if (text instanceof Spanned) {
            SpannableString sp = new SpannableString(r);
            TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                    Object.class, sp, 0);
            return sp;
        } else {
            return r;
        }
    }
}
