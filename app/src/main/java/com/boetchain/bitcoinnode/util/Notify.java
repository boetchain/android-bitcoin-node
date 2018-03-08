package com.boetchain.bitcoinnode.util;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.Gravity;
import android.widget.Toast;

import com.boetchain.bitcoinnode.R;
import com.boetchain.bitcoinnode.ui.activity.MainActivity;

/**
 * Created by Tyler on 2015-11-05.
 */
public class Notify {

	public static final int NOTIF_STICKY_PEER_ID = 1951920195;

	public static final String NOTIF_CHANNEL_ID = "boetchain_channel_id";

    public static void toast(Context context, int resId, int duration) {

        toast(context, context.getResources().getString(resId), duration);
    }

    public static void toast(Context context, String msg, int duration) {

        Toast toast = Toast.makeText(context, msg, duration);
        toast.setGravity(Gravity.TOP, 0, 200);
        toast.show();
    }

    public static void toastBottom(Context context, int resId, int duration) {

        toastBottom(context, context.getResources().getString(resId), duration);
    }

    public static void toastBottom(Context context, String msg, int duration) {

        Toast toast = Toast.makeText(context, msg, duration);
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.show();
    }

	public static void notificationStickyPeerService(Context context) {

		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent viewPendingIntent =
				PendingIntent.getActivity(context, NOTIF_STICKY_PEER_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		int color;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			color = context.getColor(R.color.colorPrimary);
		} else {
			color = context.getResources().getColor(R.color.colorPrimary);
		}

		NotificationCompat.Builder notificationBuilder =
				new NotificationCompat.Builder(context, NOTIF_CHANNEL_ID)
						.setSmallIcon(R.drawable.ic_stat_notif_icon)
						.setContentTitle(context.getString(R.string.notification_sticky_peer_service_title))
						.setContentText(context.getString(R.string.notification_sticky_peer_service_tag))
						.setAutoCancel(false)
						.setColor(color)
						.setContentIntent(viewPendingIntent);

		Notification notification;

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
			notification = notificationBuilder.build();
		} else {
			notification = notificationBuilder.getNotification();
		}

		notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

		// Get an instance of the NotificationManager service
		NotificationManagerCompat notificationManager =
				NotificationManagerCompat.from(context);

		// Build the notification and issues it with notification manager.
		notificationManager.notify(NOTIF_STICKY_PEER_ID, notification);
	}

	public static void notificationStickyPeerServiceCancel(Context context) {

		// Get an instance of the NotificationManager service
		NotificationManagerCompat notificationManager =
				NotificationManagerCompat.from(context);

		// Build the notification and issues it with notification manager.
		notificationManager.cancel(NOTIF_STICKY_PEER_ID);
	}

	public static AlertDialog yesCancelDialog(Context context, String title, int strId,
	                                          DialogInterface.OnClickListener yes, DialogInterface.OnClickListener cancel) {

		return yesCancelDialog(context, title, context.getResources().getString(strId), yes, cancel);
	}

	public static AlertDialog yesCancelDialog(Context context, String title, String str,
	                                          DialogInterface.OnClickListener yes, DialogInterface.OnClickListener cancel) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(str);
		if (!title.isEmpty()) {
			builder.setTitle(title);
		}
		builder.setPositiveButton(android.R.string.yes, yes);

		if (cancel == null) {
			cancel = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			};
		}
		builder.setNegativeButton(android.R.string.cancel, cancel);

		return builder.show();
	}

	public static AlertDialog alertDialog(Context context, String title, int msgId) {

		return alertDialog(context, title, context.getResources().getString(msgId), null);
	}

	public static AlertDialog alertDialog(Context context, String title, String msg) {

		return alertDialog(context, title, msg, null);
	}

	public static AlertDialog alertDialog(Context context, String title, String msg,
	                                      DialogInterface.OnClickListener okListener) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(msg);
		if (!title.isEmpty()) {
			builder.setTitle(title);
		}
		builder.setPositiveButton(android.R.string.ok, okListener);

		return builder.show();
	}

	public static AlertDialog askLocationDialog(final Context context) {
		return Notify.yesCancelDialog(context, "", R.string.error_no_location, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				dialog.dismiss();
			}
		}, null);
	}
}