package com.boetchain.bitcoinnode.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
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
}