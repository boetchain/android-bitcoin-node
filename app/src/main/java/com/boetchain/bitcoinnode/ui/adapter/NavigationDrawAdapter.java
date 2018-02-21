package com.boetchain.bitcoinnode.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.boetchain.bitcoinnode.R;

/**
 * Created by Ross Badenhorst.
 */
public class NavigationDrawAdapter extends BaseAdapter {

    private Context context;
    private String[] menuItems;

    public NavigationDrawAdapter(Context context, String[] menuItems) {
        this.context = context;
        this.menuItems = menuItems;
    }

    @Override
    public int getCount() {
        return menuItems.length;
    }

    @Override
    public String getItem(int i) {
        return menuItems[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        String menuItem = menuItems[i];

        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = View.inflate(context, R.layout.drawer_list_item, null);
            holder.drawer_list_item_iv = view.findViewById(R.id.drawer_list_item_iv);
            holder.drawer_list_item_tv = view.findViewById(R.id.drawer_list_item_tv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();

        }

        holder.drawer_list_item_iv.setImageResource(getMenuItemImage(menuItem));
        holder.drawer_list_item_tv.setText(menuItem);

        return view;
    }

    private int getMenuItemImage(String menuItem) {

        if (menuItem.equalsIgnoreCase(context.getString(R.string.activity_main_drawer_item_map))) {
            return R.mipmap.planetearth;
        }

        return R.mipmap.shaka;
    }

    public static class ViewHolder {
        public ImageView drawer_list_item_iv;
        public TextView drawer_list_item_tv;
    }
}
