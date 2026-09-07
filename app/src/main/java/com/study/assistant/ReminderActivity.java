package com.study.assistant;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {

    private String username;
    private DatabaseHelper dbHelper;
    private LinearLayout remindersContainer;

    private int pickedHour = -1, pickedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        setTitle("التذكيرات");

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        username = prefs.getString("active_username", "");

        remindersContainer = findViewById(R.id.remindersContainer);
        Button btnAdd = findViewById(R.id.btnAddReminder);

        requestNotificationPermissionIfNeeded();

        btnAdd.setOnClickListener(v -> showAddReminderDialog());

        refreshReminders();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                }).launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void refreshReminders() {
        remindersContainer.removeAllViews();
        Cursor cursor = dbHelper.getReminders(username);

        if (cursor.getCount() == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("لا توجد تذكيرات مضافة بعد");
            tvEmpty.setPadding(40, 20, 40, 20);
            tvEmpty.setTextColor(getResources().getColor(R.color.text_dark));
            remindersContainer.addView(tvEmpty);
        }

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REM_ID));
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REM_COURSE));
            int hour = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REM_HOUR));
            int minute = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REM_MINUTE));

            View row = LayoutInflater.from(this).inflate(R.layout.item_reminder, remindersContainer, false);
            TextView tvCourse = row.findViewById(R.id.tvReminderCourse);
            TextView tvTime = row.findViewById(R.id.tvReminderTime);
            Button btnDelete = row.findViewById(R.id.btnDeleteReminder);

            tvCourse.setText(courseName);
            tvTime.setText(String.format(Locale.getDefault(), "يوميًا الساعة %02d:%02d", hour, minute));

            btnDelete.setOnClickListener(v -> {
                AlarmScheduler.cancelReminder(this, id);
                dbHelper.deleteReminder(id);
                refreshReminders();
            });

            remindersContainer.addView(row);
        }
        cursor.close();
    }

    private void showAddReminderDialog() {
        List<String> courseNames = new ArrayList<>();
        Cursor cursor = dbHelper.getAllCourseNames(username);
        while (cursor.moveToNext()) {
            courseNames.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COURSE_NAME)));
        }
        cursor.close();

        if (courseNames.isEmpty()) {
            Toast.makeText(this, "أضف مقرر أولاً من تاب المقررات", Toast.LENGTH_SHORT).show();
            return;
        }

        pickedHour = -1;
        pickedMinute = -1;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null);
        Spinner spinnerCourse = dialogView.findViewById(R.id.spinnerReminderCourse);
        Button btnPickTime = dialogView.findViewById(R.id.btnPickTime);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, courseNames);
        spinnerCourse.setAdapter(adapter);

        btnPickTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                pickedHour = hourOfDay;
                pickedMinute = minute;
                btnPickTime.setText(String.format(Locale.getDefault(), "الوقت: %02d:%02d", hourOfDay, minute));
            }, 18, 0, true);
            timePicker.show();
        });

        new AlertDialog.Builder(this)
                .setTitle("إضافة تذكير")
                .setView(dialogView)
                .setPositiveButton("حفظ", (dialog, which) -> {
                    if (pickedHour == -1) {
                        Toast.makeText(this, "اختر الوقت أولاً", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String selectedCourse = spinnerCourse.getSelectedItem().toString();
                    long id = dbHelper.insertReminder(username, selectedCourse, pickedHour, pickedMinute);
                    AlarmScheduler.scheduleReminder(this, (int) id, pickedHour, pickedMinute, selectedCourse);
                    refreshReminders();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}