package com.study.assistant;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CourseWorkspaceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_workspace);

        String username = getIntent().getStringExtra("username");
        String college = getIntent().getStringExtra("college");
        String department = getIntent().getStringExtra("department");
        String semester = getIntent().getStringExtra("semester");

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        WorkspacePagerAdapter adapter = new WorkspacePagerAdapter(this, username, college, department, semester);
        viewPager.setAdapter(adapter);

        String[] tabTitles = {"المقررات", "الشات الذكي", "مقياس المذاكرة"};
        int[] tabIcons = {R.drawable.ic_tab_courses, R.drawable.ic_tab_chat, R.drawable.ic_tab_quiz};

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
            tab.setIcon(ContextCompat.getDrawable(this, tabIcons[position]));
        }).attach();

        findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }
}