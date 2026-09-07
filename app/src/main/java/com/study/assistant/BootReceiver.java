package com.study.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String username = prefs.getString("active_username", "");
        if (username.isEmpty()) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Cursor cursor = dbHelper.getReminders(username);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REM_ID));
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REM_COURSE));
            int hour = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REM_HOUR));
            int minute = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REM_MINUTE));

            AlarmScheduler.scheduleReminder(context, id, hour, minute, courseName);
        }
        cursor.close();
    }
}