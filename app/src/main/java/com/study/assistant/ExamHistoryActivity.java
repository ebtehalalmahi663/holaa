package com.study.assistant;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExamHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_history);
        setTitle("سجل النتائج");

        String username = getIntent().getStringExtra("username");
        String college = getIntent().getStringExtra("college");
        String department = getIntent().getStringExtra("department");
        String semester = getIntent().getStringExtra("semester");

        LinearLayout historyContainer = findViewById(R.id.historyContainer);
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        Cursor cursor = dbHelper.getExamHistory(username, college, department, semester);

        if (cursor.getCount() == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("لسه ما عملت أي امتحان في التخصص ده");
            tvEmpty.setPadding(40, 20, 40, 20);
            tvEmpty.setTextColor(getResources().getColor(R.color.text_dark));
            historyContainer.addView(tvEmpty);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy - h:mm a", new Locale("ar"));

        while (cursor.moveToNext()) {
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EH_COURSE));
            int correct = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EH_CORRECT));
            int total = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EH_TOTAL));
            double percentage = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EH_PERCENTAGE));
            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EH_TIMESTAMP));

            View row = LayoutInflater.from(this).inflate(R.layout.item_exam_history, historyContainer, false);
            TextView tvCourse = row.findViewById(R.id.tvHistoryCourse);
            TextView tvDate = row.findViewById(R.id.tvHistoryDate);
            TextView tvScore = row.findViewById(R.id.tvHistoryScore);
            TextView tvPercentage = row.findViewById(R.id.tvHistoryPercentage);
            FrameLayout circle = row.findViewById(R.id.percentageCircle);

            tvCourse.setText(courseName);
            tvDate.setText(dateFormat.format(timestamp));
            tvScore.setText("أجبت صح على " + correct + " من " + total);
            tvPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));

            boolean passed = percentage >= 60;
            int color = ContextCompat.getColor(this, passed ? R.color.college_economics : R.color.college_medicine);
            circle.setBackgroundTintList(ColorStateList.valueOf(color));

            historyContainer.addView(row);
        }
        cursor.close();
    }
}