package smallville7123.vstmanager.core.Views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import smallville7123.vstmanager.core.HandlerThread;

import static android.view.PixelCopy.ERROR_DESTINATION_INVALID;
import static android.view.PixelCopy.ERROR_SOURCE_INVALID;
import static android.view.PixelCopy.ERROR_SOURCE_NO_DATA;
import static android.view.PixelCopy.ERROR_TIMEOUT;
import static android.view.PixelCopy.ERROR_UNKNOWN;
import static android.view.PixelCopy.SUCCESS;

/**
 * composites a viewGroup to a bitmap
 */
public class ViewCompositor {
    private static final String TAG = "ViewCompositor";

    // render back to front

    private static void compositeInternalViewGroup(ViewGroup view, Canvas canvas) {
        int childCount = view.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = view.getChildAt(i);
            ViewHierarchy viewHierarchy = new ViewHierarchy();
            viewHierarchy.analyze(child);
            ArrayList<ViewHierarchy> sorted = viewHierarchy.sortByDepth();
            for (ViewHierarchy hierarchy : sorted) compositeInternalView(hierarchy, canvas);
        }
    }

    private static void compositeInternalView(ViewHierarchy viewHierarchy, Canvas canvas) {
        float x = viewHierarchy.x;
        float y = viewHierarchy.y;
        int w = viewHierarchy.view.getWidth();
        int h = viewHierarchy.view.getHeight();
        compositeInternalView(viewHierarchy.view, x, y, w, h, canvas);
    }

    private static void compositeInternalView(View view, Canvas canvas) {
        float x = view.getX();
        float y = view.getY();
        int w = view.getWidth();
        int h = view.getHeight();
        compositeInternalView(view, x, y, w, h, canvas);
    }

    private static void compositeInternalView(View view, float x, float y, int w, int h, Canvas canvas) {
        if (view != null) {
            if (w <= 0 || h <= 0) return;
            if (view instanceof TextureView) {
                Bitmap bitmap = ((TextureView) view).getBitmap();
                canvas.drawBitmap(bitmap, x, y, null);
                bitmap.recycle();
            } else {
                Bitmap bitmap = Bitmap.createBitmap(
                        w,
                        h,
                        Bitmap.Config.ARGB_8888
                );
                if (view instanceof SurfaceView) {
                    AtomicBoolean completed = new AtomicBoolean(false);
                    HandlerThread handle = new HandlerThread("Handler");
                    Handler handler = handle.getThreadHandler();
                    while(true) {
                        PixelCopy.request(((SurfaceView) view).getHolder().getSurface(), null, bitmap, copyResult -> {
                            if (completed.get()) return;
                            if (copyResult == PixelCopy.SUCCESS) {
                                canvas.drawBitmap(bitmap, x, y, null);
                                bitmap.recycle();
                                completed.set(true);
                            } else {
                                Log.d(TAG, "compositeInternalView: pixel copy failed: " + pixelCopyResultToString(copyResult));
                            }
                        }, handler);
                        handle.waitForCurrentPost();
                        if (completed.get()) break;
                    }
                } else {
                    Canvas childCanvas = new Canvas(bitmap);
                    view.draw(childCanvas);
                    view.onDrawForeground(childCanvas);
                    canvas.drawBitmap(bitmap, x, y, null);
                    bitmap.recycle();
                }
            }
        }
    }

    private static String pixelCopyResultToString(int copyResult) {
        switch (copyResult) {
            case ERROR_DESTINATION_INVALID:
                return "ERROR_DESTINATION_INVALID";
            case ERROR_SOURCE_INVALID:
                return "ERROR_SOURCE_INVALID";
            case ERROR_SOURCE_NO_DATA:
                return "ERROR_SOURCE_NO_DATA";
            case ERROR_TIMEOUT:
                return "ERROR_TIMEOUT";
            case ERROR_UNKNOWN:
                return "ERROR_UNKNOWN";
            case SUCCESS:
                return "SUCCESS";
            default:
                return "UNKNOWN";
        }
    }


    public static Bitmap composite(View view) {
        if (view == null) return null;
        int w = view.getWidth();
        int h = view.getHeight();
        if (w == 0) w = 2;
        if (h == 0) h = 2;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (view instanceof ViewGroup) compositeInternalViewGroup((ViewGroup) view, canvas);
        else compositeInternalView(view, canvas);
        return bitmap;
    }
}
