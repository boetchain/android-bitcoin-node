package com.boetchain.bitcoinnode.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.LogItem;

import java.util.List;

/**
 * Created by Tyler Hogarth on 2018/02/08.
 */

public class LogAdapter extends BaseAdapter {

    private Context context;
    private List<LogItem> logs;

    private int colorInfo;
    private int colorWarning;
    private int colorDebug;
    private int colorError;
    private int colorVerbose;

    public LogAdapter(Context context, List<LogItem> logs) {
        this.context = context;
        this.logs = logs;

        colorInfo = context.getResources().getColor(R.color.log_info);
        colorWarning = context.getResources().getColor(R.color.log_warning);
        colorDebug = context.getResources().getColor(R.color.log_debug);
        colorError = context.getResources().getColor(R.color.log_error);
        colorVerbose = context.getResources().getColor(R.color.log_verbose);
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

        switch (log.type) {

            case LogItem.TI:
                holder.text.setTextColor(colorInfo);
                break;

            case LogItem.TW:
                holder.text.setTextColor(colorWarning);
                break;

            case LogItem.TD:
                holder.text.setTextColor(colorDebug);
                break;

            case LogItem.TE:
                holder.text.setTextColor(colorError);
                break;

            case LogItem.TV:
                holder.text.setTextColor(colorVerbose);
                break;

            default:
                holder.text.setTextColor(colorInfo);
        }

        return view;
    }

    public static class ViewHolder {

        TextView text;
    }
}
