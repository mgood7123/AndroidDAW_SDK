package smallville7123.AndroidDAW.SDK.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GridView extends RecyclerView {
    private static final String TAG = "GridView";
    public boolean autoSizeRow = true;
    public boolean autoSizeColumn = true;
    public int rowSize;
    public int columnSize;
    public Context mContext;
    public GridViewAdapter adapter;
    OnClickListener onClickListener;
    int rowCount;
    int columnCount;
    public ArrayList<Pair<View, Object>> data;
    public static final int VERTICAL = RecyclerView.VERTICAL;
    public static final int HORIZONTAL = RecyclerView.HORIZONTAL;
    int mOrientation;


    ItemClickListener onItemClickListener;

    public interface ItemClickListener {
        void onClick(Object data);
    }

    public void setOnItemClick(ItemClickListener listener) {
        onItemClickListener = listener;
    }


    public GridView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public GridView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context);
        init(context);
    }

    public GridView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        data = new ArrayList<>();
        rowCount = 1;
        columnCount = 1;
        adapter = new GridViewAdapter(this);
        setOrientation(VERTICAL);
        setAdapter(adapter);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    public void setOrientation(int orientation) {
        if (mOrientation == orientation) return;
        mOrientation = orientation;
        adapter.manager.setOrientation(orientation);
        setRows(rowCount);
        setColumns(columnCount);
    }

    public interface ResizeUIRunnable {
        void run(int width, int Height, Pair<View, Object> data);
    }

    ResizeUIRunnable resizeUI = (width, height, data) -> {};

    public void setResizeUI(ResizeUIRunnable resizeUIRunnable) {
        resizeUI = resizeUIRunnable;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        onClickListener = l;
        super.setOnClickListener(l);
    }

    public void setRows(int count) {
        if (mOrientation == RecyclerView.VERTICAL) {
            rowCount = count;
        } else {
            columnCount = count;
            adapter.manager.setSpanCount(count);
        }
    }

    public void setColumns(int count) {
        if (mOrientation == RecyclerView.VERTICAL) {
            columnCount = count;
            adapter.manager.setSpanCount(count);
        } else {
            rowCount = count;
        }
    }

    public void clear() {
        // remove all items from this RecycleView so they can be garbage collected if needed
        data.clear();
        adapter.notifyDataSetChanged();
    }
}