package com.taobao.android.ski.hud;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Yell {

	public static Yell get(final Context context) {
		if (sInstance == null)
			sInstance = new Yell(context.getApplicationContext());
		return sInstance;
	}

	@SuppressWarnings("deprecation")
	public void showNotification(final String tag, final String title, final String info) {
		final Notification notification = new Notification(android.R.drawable.stat_notify_error, info, System.currentTimeMillis());
		notification.setLatestEventInfo(mContext, title, info,
				PendingIntent.getBroadcast(mContext, 0, new Intent(), 0));
		mNotificationManager.notify(tag, info.hashCode(), notification);
	}

	private Yell(final Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private final Context mContext;
	private final NotificationManager mNotificationManager;

	private static Yell sInstance;
}
