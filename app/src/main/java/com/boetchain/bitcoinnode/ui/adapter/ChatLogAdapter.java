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
                ((ViewHolderIn) holder).text.setText(log.text);
                break;

            case ChatLog.TYPE_OUT:
                ((ViewHolderOut) holder).text.setText(log.text);
                break;

            case ChatLog.TYPE_NEUTRAL:
                ((ViewHolderNeutral) holder).text.setText(log.text);
                break;
        }

        /*
        switch (log.type) {

            case ChatLog.TI:
                holder.text.setTextColor(colorInfo);
                break;

            case ChatLog.TW:
                holder.text.setTextColor(colorWarning);
                break;

            case ChatLog.TD:
                holder.text.setTextColor(colorDebug);
                break;

            case ChatLog.TE:
                holder.text.setTextColor(colorError);
                break;

            case ChatLog.TV:
                holder.text.setTextColor(colorVerbose);
                break;

            default:
                holder.text.setTextColor(colorInfo);
        }
         */

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
                holderIn.text = (TextView) view.findViewById(R.id.listitem_chat_log_in_tv);
                holder = holderIn;
                break;

            case ChatLog.TYPE_OUT:
                ViewHolderOut holderOut = new ViewHolderOut();
                holderOut.text = (TextView) view.findViewById(R.id.listitem_chat_log_out_tv);
                holder = holderOut;
                break;

            default:
                ViewHolderNeutral holderNeu = new ViewHolderNeutral();
                holderNeu.text = (TextView) view.findViewById(R.id.listitem_chat_log_neutral_tv);
                holder = holderNeu;
        }

        return holder;
    }

    public static class ViewHolder {

    }

    public static class ViewHolderIn extends ViewHolder {

        TextView text;
    }

    public static class ViewHolderOut extends ViewHolder {

        TextView text;
    }

    public static class ViewHolderNeutral extends ViewHolder {

        TextView text;
    }
}
