package smallville7123.AndroidDAW.SDK.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import smallville7123.AndroidDAW.SDK.UI.R;

public class WindowsContextMenuAdapter extends BaseAdapter {
    private Context mContext;
    private List<WindowsContextMenuItem> mDataSource = new ArrayList<>();
    private LayoutInflater layoutInflater;

    WindowsContextMenuAdapter(Context context, List<WindowsContextMenuItem> dataSource) {
        mContext = context;
        mDataSource = dataSource;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public WindowsContextMenuItem getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        WindowsContextMenuItem item = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();
            int res;
            if (item.subMenu == null) {
                res = R.layout.windows_context_menu_item;
            } else {
                res = R.layout.windows_context_menu_sub_menu;
            }
            convertView = layoutInflater.inflate(res,
                    null
            );
            holder.title = convertView.findViewById(R.id.text_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // bind data
        holder.title.setText(item.title);
        return convertView;
    }

    public class ViewHolder {
        private TextView title;
    }
}
