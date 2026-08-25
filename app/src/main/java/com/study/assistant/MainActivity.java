package com.study.assistant;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "study_assistant_prefs";
    public static final String KEY_LAST_USERNAME = "last_username";

    private EditText etUsername;
    private TextView tvWelcomeBack;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        etUsername = findViewById(R.id.etUsername);
        tvWelcomeBack = findViewById(R.id.tvWelcomeBack);
        Button btnLogin = findViewById(R.id.btnLogin);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String lastUsername = prefs.getString(KEY_LAST_USERNAME, null);
        if (lastUsername != null && !lastUsername.isEmpty()) {
            tvWelcomeBack.setText("آخر دخول باسم: " + lastUsername);
            tvWelcomeBack.setVisibility(TextView.VISIBLE);
        }

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();

            if (username.isEmpty()) {
                Toast.makeText(this, "من فضلك أدخل اسم المستخدم", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.ensureUserExists(username);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_LAST_USERNAME, username);
            editor.putString("active_username", username);
            editor.apply();

            checkResumeProgress(username);
        });
    }

    private void checkResumeProgress(String username) {
        Cursor cursor = dbHelper.getUserProgress(username);
        String college = null, department = null, semester = null, course = null;

        if (cursor.moveToFirst()) {
            college = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COLLEGE));
            department = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DEPARTMENT));
            semester = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SEMESTER));
            course = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COURSE));
        }
        cursor.close();

        if (college != null && !college.isEmpty()) {
            String resumeMsg = college + " ← " + department + " ← " + semester;
            if (course != null && !course.isEmpty()) {
                resumeMsg += " ← " + course;
            }

            final String fCollege = college, fDepartment = department, fSemester = semester;
            final String fUsername = username;

            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_resume, null);
            TextView tvResumePath = dialogView.findViewById(R.id.tvResumePath);
            Button btnContinue = dialogView.findViewById(R.id.btnResumeContinue);
            Button btnNew = dialogView.findViewById(R.id.btnResumeNew);

            tvResumePath.setText(resumeMsg);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            btnContinue.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, CourseWorkspaceActivity.class);
                intent.putExtra("username", fUsername);
                intent.putExtra("college", fCollege);
                intent.putExtra("department", fDepartment);
                intent.putExtra("semester", fSemester);
                startActivity(intent);
            });

            btnNew.setOnClickListener(v -> {
                dialog.dismiss();
                Intent intent = new Intent(MainActivity.this, SelectionActivity.class);
                intent.putExtra("username", fUsername);
                startActivity(intent);
            });

            dialog.show();
        } else {
            Intent intent = new Intent(MainActivity.this, SelectionActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        }
    }
}