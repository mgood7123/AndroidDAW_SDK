package smallville7123.AndroidDAW.SDK.UI;

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class GridViewAdapter extends RecyclerView.Adapter<GridViewAdapter.ViewHolder> {
    private static final String TAG = "GridViewAdapter";
    public GridLayoutManager manager;
    GridView gridview;

    public GridViewAdapter(GridView gridview) {
        this.gridview = gridview;
        setManager(gridview);
    }

    public void setManager(GridView gridview) {
        manager = new GridLayoutManager(
                gridview.mContext,            // context
                1
        ) {
            @Override
            protected boolean isLayoutRTL() {
                return false;
            }
        };
        gridview.setLayoutManager(manager);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        FrameLayout root;

        public ViewHolder(FrameLayout itemView) {
            super(itemView);
            root = itemView;
        }

        public void adjustDimensions(Pair<View, Object> viewObjectPair) {
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
            int width = gridview.getWidth();
            int height = gridview.getHeight();
            if (manager.getOrientation() == RecyclerView.VERTICAL) {
                if (gridview.autoSizeColumn) {
                    p.width = width;
                } else {
                    p.width = gridview.columnSize;
                }
                if (gridview.autoSizeRow) {
                    p.height = Math.round(height / gridview.rowCount);
                } else {
                    p.height = gridview.rowSize;
                }
            } else {
                if (gridview.autoSizeRow) {
                    p.width = Math.round(width / gridview.rowCount);
                } else {
                    p.width = gridview.rowSize;
                }
                if (gridview.autoSizeColumn) {
                    p.height = height;
                } else {
                    p.height = gridview.columnSize;
                }
            }
            root.setLayoutParams(p);
            if (viewObjectPair != null) gridview.resizeUI.run(p.width, p.height, viewObjectPair);
        }

        public void setItem(Pair<View, Object> position) {
            root.setVisibility(View.VISIBLE);
            root.addView(position.first);
        }

        public void setEmptyItem() {
            root.setVisibility(View.INVISIBLE);
            root.removeAllViews();
        }

        public void setOnClickListener() {
            if (gridview.onItemClickListener != null) {
                root.setOnClickListener(v -> {
                    if (gridview.onClickListener != null) {
                        gridview.onClickListener.onClick(v);
                    }
                    gridview.onItemClickListener.onClick(null);
                });
            }
        }

        public void setEmptyOnClickListener() {
            if (gridview.onClickListener != null) {
                root.setOnClickListener(v -> {
                    gridview.onClickListener.onClick(v);
                });
            } else {
                root.setOnClickListener(null);
                root.setClickable(false);
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.root.removeAllViews();
        super.onViewRecycled(holder);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new FrameLayout(gridview.mContext));
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setEmptyOnClickListener();
        if (position < gridview.data.size()) {
            Pair<View, Object> viewObjectPair = gridview.data.get(position);
            holder.setItem(viewObjectPair);
            holder.adjustDimensions(viewObjectPair);
            holder.setOnClickListener();
        } else {
            holder.setEmptyItem();
            holder.adjustDimensions(null);
        }
    }

    @Override
    public int getItemCount() {
        int itemSize = gridview.data.size();
        return Math.max(gridview.columnCount*gridview.rowCount, itemSize + (itemSize % (manager.getOrientation() == RecyclerView.VERTICAL ? gridview.columnCount : gridview.rowCount)));
    }
}