package net.orgizm.imgshr;

import android.app.NotificationManager;
import android.content.Context;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

class ProgressNotificationUpdate implements Runnable {
	Context context;
	int status;

	NotificationManager nManager;
	NotificationCompat.Builder nBuilder;

	final int NOTIFICATION_ID = 0;

	ProgressNotificationUpdate(Context context, int status) {
		this.context = context;
		this.status = status;

		nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nBuilder = new NotificationCompat.Builder(context);
	}

	public void run() {
		nBuilder.setContentText("%: " + status).setProgress(100, status, false);
		nManager.notify(NOTIFICATION_ID, nBuilder.build());
	}
}

