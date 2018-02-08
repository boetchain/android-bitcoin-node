package com.boetcoin.bitcoinnode.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.boetcoin.bitcoinnode.R;
import com.boetcoin.bitcoinnode.model.LogItem;

import java.util.List;

/**
 * Created by Tyler Hogarth on 2018/02/08.
 */

public class LogAdapter extends BaseAdapter {

    private Context context;
    private List<LogItem> logs;

    public LogAdapter(Context context, List<LogItem> logs) {
        this.context = context;
        this.logs = logs;
    }

    @Override
    public int getCount() {
        return logs.size();
    }

    @Override
    public Object getItem(int i) {
        return logs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LogItem log = logs.get(i);

        ViewHolder holder;
        if (view == null) {

            holder = new ViewHolder();
            view = View.inflate(context, R.layout.listitem_log, null);
            view.setTag(holder);

            holder.text = (TextView) view.findViewById(R.id.listitem_log_text_tv);

        } else {

            holder = (ViewHolder) view.getTag();
        }

        holder.text.setText(log.text);

        return view;
    }

    public static class ViewHolder {

        TextView text;
    }
}
