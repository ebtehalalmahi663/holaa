package com.study.assistant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SelectionActivity extends AppCompatActivity {

    private Spinner spinnerCollege, spinnerDepartment, spinnerSemester;
    private String username;
    private DatabaseHelper dbHelper;
    private List<String> colleges;

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

        colleges = Constants.getColleges();

        CollegeSpinnerAdapter collegeAdapter = new CollegeSpinnerAdapter(this, colleges);
        spinnerCollege.setAdapter(collegeAdapter);

        spinnerCollege.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedCollege = colleges.get(position);

                List<String> departments = Constants.getDepartments(selectedCollege);
                DepartmentSpinnerAdapter deptAdapter = new DepartmentSpinnerAdapter(SelectionActivity.this, departments);
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

    // أداپتر مخصص لقائمة الكليات - بيعرض أيقونة الكلية ولونها جنب الاسم
    private static class CollegeSpinnerAdapter extends ArrayAdapter<String> {
        CollegeSpinnerAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return buildRow(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return buildRow(position, convertView, parent);
        }

        private View buildRow(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spinner_college, parent, false);
            }
            String collegeName = getItem(position);
            Constants.CollegeInfo info = Constants.COLLEGES.get(collegeName);

            TextView tvText = convertView.findViewById(R.id.tvText);
            ImageView ivIcon = convertView.findViewById(R.id.ivIcon);
            View iconCircle = convertView.findViewById(R.id.iconCircle);

            tvText.setText(collegeName);
            if (info != null) {
                ivIcon.setImageResource(info.iconRes);
                iconCircle.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                androidx.core.content.ContextCompat.getColor(getContext(), info.colorRes)));
            }
            return convertView;
        }
    }

    // أداپتر مخصص لقائمة الأقسام - بيعرض أيقونة عامة جنب اسم القسم
    private static class DepartmentSpinnerAdapter extends ArrayAdapter<String> {
        DepartmentSpinnerAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return buildRow(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return buildRow(position, convertView, parent);
        }

        private View buildRow(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spinner_department, parent, false);
            }
            TextView tvText = convertView.findViewById(R.id.tvText);
            tvText.setText(getItem(position));
            return convertView;
        }
    }
}