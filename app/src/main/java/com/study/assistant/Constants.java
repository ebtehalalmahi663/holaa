package com.study.assistant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Constants {

    public static class CollegeInfo {
        public List<String> departments;
        public int semesterCount;
        public int iconRes;
        public int colorRes;

        public CollegeInfo(List<String> departments, int semesterCount, int iconRes, int colorRes) {
            this.departments = departments;
            this.semesterCount = semesterCount;
            this.iconRes = iconRes;
            this.colorRes = colorRes;
        }
    }

    public static final Map<String, CollegeInfo> COLLEGES = new LinkedHashMap<>();

    static {
        COLLEGES.put("كلية علوم الحاسوب وتقانة المعلومات", new CollegeInfo(
                listOf("تقانة المعلومات", "نظم المعلومات", "علوم الحاسوب"), 10,
                R.drawable.ic_college_computer, R.color.college_cs));

        COLLEGES.put("كلية الطب", new CollegeInfo(
                listOf("طب بيطري", "تمريض", "مختبرات", "طب بشري"), 10,
                R.drawable.ic_college_medicine, R.color.college_medicine));

        COLLEGES.put("كلية الاقتصاد", new CollegeInfo(
                listOf("اقتصاد", "محاسبة", "نظم معلومات", "إدارة"), 8,
                R.drawable.ic_college_economics, R.color.college_economics));

        COLLEGES.put("كلية القانون", new CollegeInfo(
                listOf("القانون"), 8,
                R.drawable.ic_college_law, R.color.college_law));

        COLLEGES.put("كلية التربية", new CollegeInfo(
                listOf("لغة عربية", "لغة إنجليزية", "فيزياء ورياضيات", "كيمياء وأحياء", "علم نفس", "تربية خاصة"), 8,
                R.drawable.ic_college_education, R.color.college_education));
    }

    private static List<String> listOf(String... items) {
        List<String> list = new ArrayList<>();
        for (String i : items) list.add(i);
        return list;
    }

    public static List<String> getColleges() {
        return new ArrayList<>(COLLEGES.keySet());
    }

    public static List<String> getDepartments(String college) {
        CollegeInfo info = COLLEGES.get(college);
        return info != null ? info.departments : new ArrayList<>();
    }

    public static List<String> getSemesters(String college) {
        CollegeInfo info = COLLEGES.get(college);
        int count = info != null ? info.semesterCount : 8;
        List<String> semesters = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            semesters.add("السمستر " + i);
        }
        return semesters;
    }
}