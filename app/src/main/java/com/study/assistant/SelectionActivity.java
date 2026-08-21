package com.study.assistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SelectionActivity extends AppCompatActivity {

    private Spinner spinnerCollege, spinnerDepartment, spinnerSemester;
    private String username;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        dbHelper = new DatabaseHelper(this);
        username = getIntent().getStringExtra("username");
        if (username == null) {
            SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
            username = prefs.getString("active_username", "");
        }

        spinnerCollege = findViewById(R.id.spinnerCollege);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        Button btnContinue = findViewById(R.id.btnContinue);

        List<String> colleges = Constants.getColleges();
        ArrayAdapter<String> collegeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, colleges);
        spinnerCollege.setAdapter(collegeAdapter);

        spinnerCollege.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedCollege = colleges.get(position);

                List<String> departments = Constants.getDepartments(selectedCollege);
                ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(SelectionActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, departments);
                spinnerDepartment.setAdapter(deptAdapter);

                List<String> semesters = Constants.getSemesters(selectedCollege);
                ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(SelectionActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, semesters);
                spinnerSemester.setAdapter(semesterAdapter);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnContinue.setOnClickListener(v -> {
            if (spinnerCollege.getSelectedItem() == null || spinnerDepartment.getSelectedItem() == null
                    || spinnerSemester.getSelectedItem() == null) {
                return;
            }

            String college = spinnerCollege.getSelectedItem().toString();
            String department = spinnerDepartment.getSelectedItem().toString();
            String semester = spinnerSemester.getSelectedItem().toString();

            dbHelper.updateUserProgress(username, college, department, semester, "", "", 0);

            Intent intent = new Intent(SelectionActivity.this, CourseWorkspaceActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("college", college);
            intent.putExtra("department", department);
            intent.putExtra("semester", semester);
            startActivity(intent);
        });
    }
}