package com.study.assistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

public class SelectionActivity extends AppCompatActivity {

    private GridLayout collegesGrid;
    private LinearLayout detailsSection;
    private Spinner spinnerDepartment, spinnerSemester;
    private String username;
    private String selectedCollege = null;
    private View selectedCardView = null;
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

        collegesGrid = findViewById(R.id.collegesGrid);
        detailsSection = findViewById(R.id.detailsSection);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        Button btnContinue = findViewById(R.id.btnContinue);

        buildCollegeCards();

        btnContinue.setOnClickListener(v -> {
            if (selectedCollege == null || spinnerDepartment.getSelectedItem() == null
                    || spinnerSemester.getSelectedItem() == null) {
                return;
            }

            String department = spinnerDepartment.getSelectedItem().toString();
            String semester = spinnerSemester.getSelectedItem().toString();

            dbHelper.updateUserProgress(username, selectedCollege, department, semester, "", "", 0);

            Intent intent = new Intent(SelectionActivity.this, CourseWorkspaceActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("college", selectedCollege);
            intent.putExtra("department", department);
            intent.putExtra("semester", semester);
            startActivity(intent);
        });
    }

    private void buildCollegeCards() {
        List<String> colleges = Constants.getColleges();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cardWidth = (screenWidth - dp(20 * 2) - dp(12)) / 2;

        for (int i = 0; i < colleges.size(); i++) {
            String collegeName = colleges.get(i);
            Constants.CollegeInfo info = Constants.COLLEGES.get(collegeName);

            View card = LayoutInflater.from(this).inflate(R.layout.item_college_card, collegesGrid, false);
            FrameLayout iconCircle = card.findViewById(R.id.iconCircle);
            ImageView ivIcon = card.findViewById(R.id.ivCollegeIcon);
            TextView tvName = card.findViewById(R.id.tvCollegeName);

            ivIcon.setImageResource(info.iconRes);
            tvName.setText(collegeName);
            iconCircle.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, info.colorRes)));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cardWidth;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 2, 1f);
            params.rowSpec = GridLayout.spec(i / 2);
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            card.setLayoutParams(params);

            card.setOnClickListener(v -> selectCollege(collegeName, card));

            collegesGrid.addView(card);
        }
    }

    private void selectCollege(String collegeName, View cardView) {
        if (selectedCardView != null) {
            selectedCardView.setBackgroundResource(R.drawable.bg_card_rounded);
        }
        cardView.setBackgroundResource(R.drawable.bg_card_selected);
        selectedCardView = cardView;
        selectedCollege = collegeName;

        List<String> departments = Constants.getDepartments(collegeName);
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, departments);
        spinnerDepartment.setAdapter(deptAdapter);

        List<String> semesters = Constants.getSemesters(collegeName);
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, semesters);
        spinnerSemester.setAdapter(semesterAdapter);

        detailsSection.setVisibility(View.VISIBLE);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}