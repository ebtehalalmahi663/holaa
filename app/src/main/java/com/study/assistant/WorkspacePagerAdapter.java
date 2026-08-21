package com.study.assistant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class WorkspacePagerAdapter extends FragmentStateAdapter {

    private final String username, college, department, semester;

    public WorkspacePagerAdapter(@NonNull FragmentActivity activity,
                                  String username, String college, String department, String semester) {
        super(activity);
        this.username = username;
        this.college = college;
        this.department = department;
        this.semester = semester;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return LecturesFragment.newInstance(username, college, department, semester);
            case 1:
                return ChatFragment.newInstance();
            case 2:
                QuizFragment quizFragment = QuizFragment.newInstance();
                Bundle args = new Bundle();
                args.putString("username", username);
                args.putString("college", college);
                args.putString("department", department);
                args.putString("semester", semester);
                quizFragment.setArguments(args);
                return quizFragment;
            default:
                return LecturesFragment.newInstance(username, college, department, semester);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}