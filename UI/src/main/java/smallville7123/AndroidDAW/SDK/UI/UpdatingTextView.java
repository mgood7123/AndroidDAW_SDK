package smallville7123.AndroidDAW.SDK.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class UpdatingTextView extends androidx.appcompat.widget.AppCompatTextView {

    public UpdatingTextView(@NonNull Context context) {
        super(context);
    }

    public UpdatingTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UpdatingTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    ArrayList<Runnable> initialDrawQueue = new ArrayList<>();
    boolean initialDrawQueueHasBeenExecuted = false;
    ArrayList<Runnable> drawQueue = new ArrayList<>();

    public void addOnFirstDrawAction(Runnable action) {
        initialDrawQueue.add(action);
    }

    public void addOnDrawAction(Runnable action) {
        drawQueue.add(action);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (initialDrawQueueHasBeenExecuted) {
            for (Runnable action : drawQueue) action.run();
        } else {
            if (!initialDrawQueue.isEmpty()) {
                for (Runnable action : initialDrawQueue) action.run();
                initialDrawQueueHasBeenExecuted = true;
            }
        }
    }
}
