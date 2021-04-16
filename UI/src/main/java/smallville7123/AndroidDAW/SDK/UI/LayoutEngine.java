package smallville7123.AndroidDAW.SDK.UI;

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class LayoutEngine {

    private static final String TAG = "LayoutEngine";

    static int MODE_WIDTH = 0;
    static int MODE_HEIGHT = 1;

    int mode = MODE_HEIGHT;
    int maxWidth = 0;
    int maxHeight = 0;

    int totalWidth;
    int totalHeight;
    public int remainingWidth;
    public int remainingHeight;

    ArrayList<LayoutEngine> regionList = new ArrayList<>();
    ArrayList<Pair<View, Integer>> viewList = new ArrayList<>();

    public LayoutEngine newWidthRegion(int width) {
        LayoutEngine region = new LayoutEngine();
        region.mode = MODE_WIDTH;
        region.maxWidth = width;
        region.remainingWidth = width;
        regionList.add(region);
        return region;
    }

    public LayoutEngine newHeightRegion(int height) {
        LayoutEngine region = new LayoutEngine();
        region.mode = MODE_HEIGHT;
        region.maxHeight = height;
        region.remainingHeight = height;
        regionList.add(region);
        return region;
    }

    public int height(View view, int height) {
        if (mode == MODE_WIDTH) {
            throw new RuntimeException("attempting to set height in a width region");
        }
//        if (remainingHeight - height < 0) height = 0;
        totalHeight += height;
        remainingHeight -= height;
        viewList.add(new Pair<>(view, height));
        return remainingHeight;
    }

    public int width(View view, int width) {
        if (mode == MODE_HEIGHT) {
            throw new RuntimeException("attempting to set width in a height region");
        }
//        if (remainingWidth - width < 0) width = 0;
        totalWidth += width;
        remainingWidth -= width;
        viewList.add(new Pair<>(view, width));
        return remainingWidth;
    }

    public void execute() {
        for (Pair<View, Integer> viewIntegerPair : viewList) {
            ViewGroup.LayoutParams layoutParams = viewIntegerPair.first.getLayoutParams();
            if (mode == MODE_HEIGHT) layoutParams.height = viewIntegerPair.second;
            else if (mode == MODE_WIDTH) layoutParams.width = viewIntegerPair.second;
            viewIntegerPair.first.setLayoutParams(layoutParams);
        }
        for (LayoutEngine layoutEngine : regionList) {
            layoutEngine.execute();
        }
    }
}
