package com.study.assistant;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String courseName = intent.getStringExtra("course_name");
        int reminderId = intent.getIntExtra("reminder_id", 0);

        NotificationHelper.createChannel(context);

        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, reminderId, openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("حان وقت مذاكرتك 📚")
                .setContentText("موعد مذاكرة مقرر: " + courseName)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("موعد مذاكرة مقرر: " + courseName))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(reminderId, builder.build());
        } catch (SecurityException ignored) {
            // المستخدم لم يمنح إذن الإشعارات
        }
    }
}