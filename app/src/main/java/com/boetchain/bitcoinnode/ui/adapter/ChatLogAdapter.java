package com.boetchain.bitcoinnode.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.model.ChatLog;

import java.util.List;

/**
 * Created by Tyler Hogarth on 2018/02/08.
 */

public class ChatLogAdapter extends BaseAdapter {

    private Context context;
    private List<ChatLog> logs;

    public ChatLogAdapter(Context context, List<ChatLog> logs) {
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

        ChatLog log = logs.get(i);

        ViewHolder holder;
        if (view == null) {

            view = getChatView(log.type);
            holder = getChatViewHolder(log.type, view);
            view.setTag(holder);

        } else {

            if (view.getTag() instanceof ViewHolderIn && log.type == ChatLog.TYPE_IN) {

                holder = (ViewHolderIn) view.getTag();
            } else if (view.getTag() instanceof ViewHolderOut && log.type == ChatLog.TYPE_OUT) {

                holder = (ViewHolderOut) view.getTag();
            } else if (view.getTag() instanceof ViewHolderNeutral && log.type == ChatLog.TYPE_NEUTRAL) {

                holder = (ViewHolderNeutral) view.getTag();
            } else {

                //The TAG and TYPE don't match so we create a new view and holder
                view = getChatView(log.type);
                holder = getChatViewHolder(log.type, view);
                view.setTag(holder);
            }
        }

        switch (log.type) {

            case ChatLog.TYPE_IN:
                ViewHolderIn holderIn = (ViewHolderIn) holder;
                holderIn.textTv.setText(log.text);
                holderIn.commandTv.setText(log.command);
                holderIn.timeTv.setText(log.time);
                break;

            case ChatLog.TYPE_OUT:
                ViewHolderOut holderOut = (ViewHolderOut) holder;
                holderOut.textTv.setText(log.text);
                holderOut.commandTv.setText(log.command);
                holderOut.timeTv.setText(log.time);
                break;

            case ChatLog.TYPE_NEUTRAL:
                ViewHolderNeutral holderNeutral = (ViewHolderNeutral) holder;
                holderNeutral.textTv.setText(log.text);
                holderNeutral.timeTv.setText(log.time);
                break;
        }

        return view;
    }

    private View getChatView(int type) {

        switch (type) {
            case ChatLog.TYPE_IN:
                return View.inflate(context, R.layout.listitem_chat_log_in, null);

            case ChatLog.TYPE_OUT:
                return View.inflate(context, R.layout.listitem_chat_log_out, null);

            default:
                return View.inflate(context, R.layout.listitem_chat_log_neutral, null);
        }
    }

    private ViewHolder getChatViewHolder(int type, View view) {

        ViewHolder holder;
        switch (type) {
            case ChatLog.TYPE_IN:
                ViewHolderIn holderIn = new ViewHolderIn();
                holderIn.textTv = (TextView) view.findViewById(R.id.listitem_chat_log_in_tv);
                holderIn.commandTv = (TextView) view.findViewById(R.id.listitem_chat_log_in_command_tv);
                holderIn.timeTv = (TextView) view.findViewById(R.id.listitem_chat_log_in_time_tv);
                holder = holderIn;
                break;

            case ChatLog.TYPE_OUT:
                ViewHolderOut holderOut = new ViewHolderOut();
                holderOut.textTv = (TextView) view.findViewById(R.id.listitem_chat_log_out_tv);
                holderOut.commandTv = (TextView) view.findViewById(R.id.listitem_chat_log_out_command_tv);
                holderOut.timeTv = (TextView) view.findViewById(R.id.listitem_chat_log_out_time_tv);
                holder = holderOut;
                break;

            default:
                ViewHolderNeutral holderNeu = new ViewHolderNeutral();
                holderNeu.textTv = (TextView) view.findViewById(R.id.listitem_chat_log_neutral_tv);
                holderNeu.timeTv = (TextView) view.findViewById(R.id.listitem_chat_log_neutral_time_tv);
                holder = holderNeu;
        }

        return holder;
    }

    public static class ViewHolder {

    }

    public static class ViewHolderIn extends ViewHolder {

        TextView textTv;
        TextView commandTv;
        TextView timeTv;
    }

    public static class ViewHolderOut extends ViewHolder {

        TextView textTv;
        TextView commandTv;
        TextView timeTv;
    }

    public static class ViewHolderNeutral extends ViewHolder {

        TextView textTv;
        TextView timeTv;
    }
}
