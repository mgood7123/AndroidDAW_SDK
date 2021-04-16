package smallville7123.AndroidDAW.SDK.UI;

import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.widget.TextView;

public class TextViewResizeUtils extends TextViewPreviewUtils {

    // Our ellipsis string
    private static final String mEllipsis = "...";

    final TextView textView;

    TextViewResizeUtils(TextView textView) {
        super(textView);
        this.textView = textView;
    }

    public void setResizeModeFromAttributes(int r) {
        switch (r) {
            case -1:
            case 0:
                resizeMode = ResizeMode.width;
                break;
            case 1:
                resizeMode = ResizeMode.height;
                break;
            case 2:
                resizeMode = ResizeMode.heightWithEllipsis;
                break;
            case 3:
                resizeMode = ResizeMode.ellipsis;
                break;
            case 4:
                resizeMode = ResizeMode.none;
                break;
        }
    }

    enum ResizeMode {
        width,
        height,
        heightWithEllipsis,
        ellipsis,
        none
    }

    ResizeMode resizeMode;

    public ResizeMode switchToResizeMode(ResizeMode mode) {
        ResizeMode old = resizeMode;
        resizeMode = mode;
        return old;
    }

    public ResizeMode getResizeMode() {
        return resizeMode;
    }


    final public OnPreviewListener SLASETListener = new OnPreviewListener() {
        boolean isEllipsis = false;
        boolean isEllipsisWithHeight = false;
        int w = 0;
        int h = 0;

        @Override
        void onPreview(boolean active) {
            if (active) {
                if (resizeMode == ResizeMode.heightWithEllipsis) {
                    isEllipsisWithHeight = true;
                    resizeMode = ResizeMode.height;
                } else if (resizeMode == ResizeMode.ellipsis) {
                    isEllipsis = true;
                    resizeMode = ResizeMode.none;
                }
            } else {
                if (isEllipsisWithHeight) {
                    resizeMode = ResizeMode.heightWithEllipsis;
                    isEllipsisWithHeight = false;
                } else if (isEllipsis) {
                    resizeMode = ResizeMode.ellipsis;
                    isEllipsis = false;
                }
            }
        }

        @Override
        public void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            w = (right - left) - getCompoundPaddingLeft() - getCompoundPaddingRight();
            h = (bottom - top) - getCompoundPaddingBottom() - getCompoundPaddingTop();
        }

        @Override
        String refreshProcess(String realText, String preview_) {
            if (realText.isEmpty()) return "";
            TextPaint textPaint = getPaint();

            // Store the current text size
            float oldTextSize = textPaint.getTextSize();

            boolean measureElipsis = false;

            switch (resizeMode) {
                case width:
                    // resize to width and height

                    int size = 1;
                    int sizep = 1;

                    while (true) {
                        // see if text will fit
                        textPaint.setTextSize(size);
                        int textWidth = getTextWidth(realText, textPaint);
                        int textHeight = getTextHeight(realText, textPaint, w, size);

                        // ensure text is not larger than height
                        if (textHeight == h || textWidth == w) {
                            break;
                        }
                        if (textHeight > h || textWidth > w) {
                            size = sizep;
                            break;
                        }

                        // the text does not fit, increase size and try again
                        sizep = size++;
                    }

                    textPaint.setTextSize(oldTextSize);
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
                    return realText;
                case height:
                    // resize to height

                    textPaint.setTextSize(oldTextSize);
                    ResizeData textDataMain = new ResizeData();
                    while (true) {
                        // see if text will fit
                        textDataMain.process(w, h, realText, textPaint, false);
                        if (textDataMain.resizable) {
                            // the text itself fits, exit loop
                            if (textDataMain.textWidth <= w) break;
                        }
                        // the text does not fit, trim text and try again
                        realText = realText.substring(0, realText.length() - 1);
                    }

                    textPaint.setTextSize(oldTextSize);
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataMain.textSize);
                    break;
                case heightWithEllipsis:
                    // resize to height, add ellipsis

                    // if only the ellipse fits, then exit early
                    boolean onlyRoomForEllipsis = false;
                    // if the ellipse cannot fit, then exit early
                    boolean noSpace = false;

                    String newEllipsis = mEllipsis;
                    ResizeData textDataEllipsis = new ResizeData();
                    while (true) {
                        // see if ellipsis will fit
                        textDataEllipsis.process(w, h, newEllipsis, textPaint, false);
                        if (textDataEllipsis.resizable) {
                            // the ellipsis itself fits, exit loop
                            if (textDataEllipsis.textWidth <= w) break;

                            // either the ellipsis fits partially or there is no room for it to fit,
                            // exit early
                            onlyRoomForEllipsis = true;
                            newEllipsis = newEllipsis.substring(0, newEllipsis.length() - 1);
                            if (newEllipsis.length() == 0) {
                                // the ellipsis is unable to fit
                                noSpace = true;
                                break;
                            }
                        } else {
                            // the ellipsis was unable to fit
                            noSpace = true;
                            break;
                        }
                    }

                    textPaint.setTextSize(oldTextSize);

                    if (noSpace) return "";

                    if (onlyRoomForEllipsis) {
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataEllipsis.textSize);
                        return newEllipsis;
                    }

                    // there is at least room for ellipsis

                    // see if the text by itself will fit first
                    ResizeData textDataText = new ResizeData();
                    textDataText.process(w, h, realText, textPaint, false);
                    if (textDataText.resizable) {
                        // the text itself fits, exit loop
                        if (textDataText.textWidth <= w) {
                            textPaint.setTextSize(oldTextSize);
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataText.textSize);
                            return realText;
                        }
                    }

                    // text does not fit
                    textPaint.setTextSize(oldTextSize);
                    ResizeData textDataMain__ = new ResizeData();

                    String result;
                    while (true) {
                        // see if text and ellipsis will fit
                        result = realText + mEllipsis;
                        textDataMain__.process(w, h, result, textPaint, false);
                        if (textDataMain__.resizable) {
                            // the text + ellipsis itself fits, exit loop
                            if (textDataMain__.textWidth <= w) break;
                        }
                        // the text + ellipsis does not fit, trim text and try again
                        realText = realText.substring(0, realText.length() - 1);
                    }

                    textPaint.setTextSize(oldTextSize);
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, textDataMain__.textSize);

                    return result;
                case ellipsis:
                    // if only the ellipse fits, then exit early
                    boolean onlyRoomForEllipsis_ = false;
                    // if the ellipse cannot fit, then exit early
                    boolean noSpace_ = false;

                    String newEllipsis_ = mEllipsis;
                    while (true) {
                        // if the ellipsis itself fits, exit loop
                        if ((int) textPaint.measureText(newEllipsis_) <= w) break;

                        // either the ellipsis fits partially or there is no room for it to fit,
                        // exit early
                        onlyRoomForEllipsis_ = true;
                        newEllipsis_ = newEllipsis_.substring(0, newEllipsis_.length() - 1);
                        if (newEllipsis_.length() == 0) {
                            // the ellipsis is unable to fit
                            noSpace_ = true;
                            break;
                        }
                    }

                    if (noSpace_) return "";

                    if (onlyRoomForEllipsis_) {
                        return newEllipsis_;
                    }

                    // there is at least room for ellipsis

                    // see if the text by itself will fit first
                    if ((int) textPaint.measureText(realText) <= w) {
                        // the text itself fits, return it
                        return realText;
                    }

                    // text does not fit
                    String result_;
                    while (true) {
                        // see if text and ellipsis will fit
                        result_ = realText + mEllipsis;

                        // if the text + ellipsis itself fits, exit loop
                        if ((int) textPaint.measureText(result_) <= w) break;

                        // the text + ellipsis does not fit, trim text and try again
                        realText = realText.substring(0, realText.length() - 1);
                    }
                    return result_;
                case none:
                    return realText;
            }
            return realText;
        }
    };

    private void setTextSize(int complexUnitPx, float textSize) {
        textView.setTextSize(complexUnitPx, textSize);
    }

    private int getCompoundPaddingLeft() {
        return textView.getCompoundPaddingLeft();
    }
    private int getCompoundPaddingRight() {
        return textView.getCompoundPaddingRight();
    }
    private int getCompoundPaddingTop() {
        return textView.getCompoundPaddingTop();
    }
    private int getCompoundPaddingBottom() {
        return textView.getCompoundPaddingBottom();
    }

    private TextPaint getPaint() {
        return textView.getPaint();
    }

    float resizeTextInternal(int width, int height, CharSequence text, TextPaint textPaint, float oldTextSize) {
        float targetTextSize = oldTextSize;

        // Get the current text height
        int textHeight = getTextHeight(text, textPaint, width, oldTextSize);

        // if we are larger than our text view, incrementally try smaller sizes until we fit
        while (textHeight > height) {
            textHeight = getTextHeight(text, textPaint, width, --targetTextSize);
        }
        // if we are smaller than our text view, incrementally try larger sizes until we fit
        while (textHeight < height) {
            textHeight = getTextHeight(text, textPaint, width, ++targetTextSize);
        }

        return targetTextSize;
    }

    /**
     * tests to resize the text size with specified width and height
     * @param width
     * @param height
     */
    public Float resize(int width, int height, CharSequence text, TextPaint textPaint) {
        // Do not resize if the view does not have dimensions or there is no text
        if(text == null || text.length() == 0 || height <= 0 || width <= 0) {
            return null;
        }
        return resizeTextInternal(width, height, text, textPaint, textPaint.getTextSize());
    }

    class ResizeData {
        boolean resizable;
        StaticLayout layout;
        float textWidth;
        float originalTextSize;
        float textSize;
        public void process(int width, int height, CharSequence text, TextPaint textPaint, boolean includepad) {
            Float ts = resize(width, height, text, textPaint);
            if (ts == null) {
                resizable = false;
            } else {
                resizable = true;
                textSize = ts;
                originalTextSize = textPaint.getTextSize();
                textPaint.setTextSize(textSize);
                layout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, getLineSpacingMultiplier(), getLineSpacingExtra(), includepad);
                textWidth = textPaint.measureText(text.toString());
                textPaint.setTextSize(originalTextSize);
            }
        }
    }

    public int getTextWidth(CharSequence text, TextPaint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int width = bounds.left + bounds.width();
        return width;
    }

    public int getTextHeight(CharSequence text, TextPaint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int height = bounds.bottom + bounds.height();
        return height;
    }

    // Set the text size of the text paint object and use a static layout to render text off screen before measuring
    private int getTextHeight(CharSequence source, TextPaint paint, int width, float textSize) {
        paint.setTextSize(textSize);
        StaticLayout layout = new StaticLayout(source, paint, width, Layout.Alignment.ALIGN_NORMAL, getLineSpacingMultiplier(), getLineSpacingExtra(), true);
        return layout.getHeight();
    }

    private float getLineSpacingExtra() {
        return textView.getLineSpacingExtra();
    }

    private float getLineSpacingMultiplier() {
        return textView.getLineSpacingMultiplier();
    }
}
