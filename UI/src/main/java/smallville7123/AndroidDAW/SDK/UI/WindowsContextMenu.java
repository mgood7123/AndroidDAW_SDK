package smallville7123.AndroidDAW.SDK.UI;

import android.content.Context;
import android.view.View;
import android.widget.ListPopupWindow;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WindowsContextMenu {
    private Context mContext;
    private WindowsContextMenu parent;
    private ListPopupWindow listPopupWindow;
    private WindowsContextMenuAdapter listPopupWindowAdapter;
    private List<WindowsContextMenuItem> sampleData;
    private View anchor;

    private void setup() {
        sampleData = new ArrayList<>();
        listPopupWindow = new ListPopupWindow(mContext);
        listPopupWindowAdapter = new WindowsContextMenuAdapter(mContext, sampleData);
        listPopupWindow.setWidth(600);
        listPopupWindow.setModal(true);
        listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            WindowsContextMenuItem item = listPopupWindowAdapter.getItem(position);
            if (item.subMenu == null) {
                listPopupWindow.dismiss();
                if (item.isHeader) {
                    if (item.parent != null) {
                        item.parent.show();
                    }
                }
            } else {
                listPopupWindow.dismiss();
                item.subMenu.show();
            }
        });
        listPopupWindow.setAdapter(listPopupWindowAdapter);
    }

    public WindowsContextMenu(Context context) {
        mContext = context;
        parent = null;
        setup();
    }

    public WindowsContextMenu(Context context, WindowsContextMenu parent) {
        mContext = context;
        this.parent = parent;
        setup();
    }

    public ListPopupWindow getListPopupWindow() {
        return listPopupWindow;
    }

    public WindowsContextMenuItem addHeader(String title) {
        WindowsContextMenuItem item = new WindowsContextMenuItem(title, parent, null, true);
        sampleData.add(item);
        listPopupWindowAdapter.notifyDataSetChanged();
        return item;
    }

    public WindowsContextMenuItem addItem(String title) {
        WindowsContextMenuItem item = new WindowsContextMenuItem(title, parent, null, false);
        sampleData.add(item);
        listPopupWindowAdapter.notifyDataSetChanged();
        return item;
    }

    public WindowsContextMenuItem addSubMenu(String title) {
        WindowsContextMenu subMenu = new WindowsContextMenu(mContext, this);
        subMenu.addHeader(title);
        subMenu.setAnchorView(anchor);

        WindowsContextMenuItem item = new WindowsContextMenuItem(title, parent, subMenu, false);
        sampleData.add(item);
        listPopupWindowAdapter.notifyDataSetChanged();
        return item;
    }

    public void setAnchorView(@Nullable View anchor) {
        this.anchor = anchor;
        for (WindowsContextMenuItem item : sampleData) {
            if (item.subMenu != null) {
                item.subMenu.listPopupWindow.setAnchorView(anchor);
            }
        }
        listPopupWindow.setAnchorView(anchor);
    }

    public void show() {
        listPopupWindow.show();
    }

    public void setOffsetX(int offset) {
        listPopupWindow.setHorizontalOffset(offset);
    }

    public void setOffsetY(int offset) {
        listPopupWindow.setVerticalOffset(offset);
    }
}
