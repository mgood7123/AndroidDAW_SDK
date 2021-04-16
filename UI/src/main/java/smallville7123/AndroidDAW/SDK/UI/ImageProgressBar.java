package smallville7123.AndroidDAW.SDK.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import smallville7123.AndroidDAW.SDK.UI.R;

@SuppressLint("AppCompatCustomView")
public class ImageProgressBar extends View {
    private static final String TAG = "ImageProgressBar";

    private Context mContext;
    private int mProgress;
    private int mProgressMax;
    private Drawable mProgressBackground;
    private int mProgressBackgroundResource;
    int mOrientation;

    public void setProgress(int mProgress) {
        this.mProgress = mProgress;
        invalidate();
    }

    public void setProgressMax(int mProgressMax) {
        this.mProgressMax = mProgressMax;
        invalidate();
    }

    static class Orientation {
        static final int vertical = 0;
        static final int horizontal = 1;
        static final int vertical_flipped = 2;
        static final int horizontal_flipped = 3;
    }

    /**
     * draws the background onto the canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (mProgressBackground != null) {
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();
            mProgressBackground.setBounds(0, 0, canvasWidth, canvasHeight);
            switch (mOrientation) {
                case Orientation.vertical:
                    // 75% = bottom is 75% drawn, top is 25% empty
                    canvas.clipOutRect(0, 0, canvasWidth, canvasHeight - (canvasHeight * mProgress / mProgressMax));
                    break;
                case Orientation.horizontal:
                    // 75% = left is 75% drawn, right is 25% empty
                    canvas.clipOutRect(canvasWidth * mProgress / mProgressMax, 0, canvasWidth, canvasHeight);
                    break;
                case Orientation.vertical_flipped:
                    // 75% = bottom is 25% empty, top is 75% drawn
                    canvas.clipOutRect(0, canvasHeight * mProgress / mProgressMax, canvasWidth, canvasHeight);
                    break;
                case Orientation.horizontal_flipped:
                    // 75% = left is 25% empty, right is 75% drawn
                    canvas.clipOutRect(0, 0, canvasWidth - (canvasWidth * mProgress / mProgressMax), canvasHeight);
                    break;
                default:
                    Log.w(TAG, "unknown orientation: " + mOrientation);
                    return;
            }
            mProgressBackground.draw(canvas);
        }
    }

    public ImageProgressBar(Context context) {
        super(context);
        init(context, null);
    }

    public ImageProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ImageProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ImageProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        if (attrs != null) {
            TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ImageProgressBar, 0, 0);
            mProgressBackground = attributes.getDrawable(R.styleable.ImageProgressBar_progress_background);
            mProgress = attributes.getInt(R.styleable.ImageProgressBar_progress_value, 100);
            mProgressMax = attributes.getInt(R.styleable.ImageProgressBar_progress_max, 100);
            mOrientation = attributes.getInt(R.styleable.ImageProgressBar_progress_orientation, Orientation.vertical);
            attributes.recycle();
        } else {
            mProgress = 100;
            mProgress = 100;
            mOrientation = Orientation.vertical;
        }
    }

    /**
     * Sets the progress background color for this view.
     * @param color the color of the progress background
     */
    public void setProgressBackgroundColor(@ColorInt int color) {
        if (mProgressBackground instanceof ColorDrawable) {
            ((ColorDrawable) mProgressBackground.mutate()).setColor(color);
            mProgressBackgroundResource = 0;
        } else {
            setProgressBackground(new ColorDrawable(color));
        }
    }

    /**
     * Set the progress background to a given resource. The resource should refer to
     * a Drawable object or 0 to remove the progress background.
     * @param resid The identifier of the resource.
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    public void setProgressBackgroundResource(@DrawableRes int resid) {
        if (resid != 0 && resid == mProgressBackgroundResource) {
            return;
        }

        Drawable d = null;
        if (resid != 0) {
            d = mContext.getDrawable(resid);
        }
        setProgressBackground(d);

        mProgressBackgroundResource = resid;
    }

    /**
     * Set the progress background to a given Drawable,
     * or remove the progress background.
     * @param ProgressBackground The Drawable to use as the progress background,
     *        or null to remove the progress background
     */
    public void setProgressBackground(Drawable ProgressBackground) {
        //noinspection deprecation
        setProgressBackgroundDrawable(ProgressBackground);
    }

    /**
     * @deprecated use {@link #setProgressBackground(Drawable)} instead
     */
    @Deprecated
    public void setProgressBackgroundDrawable(Drawable ProgressBackground) {
        mProgressBackground = ProgressBackground;
        invalidate();
    }

    /**
     * Gets the progress background drawable
     *
     * @return The drawable used as the progress background for this view, if any.
     *
     * @see #setProgressBackground(Drawable)
     *
     */
    public Drawable getProgressBackground() {
        return mProgressBackground;
    }
}
