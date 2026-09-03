package com.study.assistant;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuizFragment extends Fragment {

    private String username, college, department, semester;
    private DatabaseHelper dbHelper;

    private Spinner spinnerCourse, spinnerQuestionType;
    private LinearLayout lecturesCheckContainer;
    private EditText etNumQuestions;
    private ProgressBar progressBar;

    private List<Long> courseIds = new ArrayList<>();
    private List<String> courseNames = new ArrayList<>();

    public static QuizFragment newInstance() {
        return new QuizFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);
        dbHelper = new DatabaseHelper(requireContext());

        if (getArguments() != null) {
            username = getArguments().getString("username");
            college = getArguments().getString("college");
            department = getArguments().getString("department");
            semester = getArguments().getString("semester");
        }

        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        spinnerQuestionType = view.findViewById(R.id.spinnerQuestionType);
        lecturesCheckContainer = view.findViewById(R.id.lecturesCheckContainer);
        etNumQuestions = view.findViewById(R.id.etNumQuestions);
        progressBar = view.findViewById(R.id.progressBar);
        Button btnGenerate = view.findViewById(R.id.btnGenerateExam);
        Button btnViewHistory = view.findViewById(R.id.btnViewHistory);

        btnViewHistory.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireContext(), ExamHistoryActivity.class);
            intent.putExtra("username", username);
            intent.putExtra("college", college);
            intent.putExtra("department", department);
            intent.putExtra("semester", semester);
            startActivity(intent);
        });

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"اختيار من متعدد", "صح أو خطأ", "مزيج"});
        spinnerQuestionType.setAdapter(typeAdapter);

        loadCourses();

        spinnerCourse.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int position, long id) {
                if (position < courseIds.size()) {
                    loadLectureCheckboxes(courseIds.get(position));
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnGenerate.setOnClickListener(v -> generateExam());

        return view;
    }

    private void loadCourses() {
        courseIds.clear();
        courseNames.clear();
        Cursor cursor = dbHelper.getCourses(username, college, department, semester);
        while (cursor.moveToNext()) {
            courseIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COURSE_ID)));
            courseNames.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COURSE_NAME)));
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, courseNames);
        spinnerCourse.setAdapter(adapter);

        if (!courseIds.isEmpty()) {
            loadLectureCheckboxes(courseIds.get(0));
        }
    }

    private void loadLectureCheckboxes(long courseId) {
        lecturesCheckContainer.removeAllViews();
        Cursor cursor = dbHelper.getLectureFiles(courseId);
        while (cursor.moveToNext()) {
            String fileName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FILE_NAME));
            CheckBox cb = new CheckBox(requireContext());
            cb.setText(fileName);
            cb.setChecked(true);
            lecturesCheckContainer.addView(cb);
        }
        cursor.close();

        if (lecturesCheckContainer.getChildCount() == 0) {
            android.widget.TextView tv = new android.widget.TextView(requireContext());
            tv.setText("لا توجد ملفات محاضرات مضافة لهذا المقرر بعد");
            lecturesCheckContainer.addView(tv);
        }
    }

    private void generateExam() {
        if (courseNames.isEmpty()) {
            Toast.makeText(requireContext(), "أضف مقرر ومحاضرات أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        String courseName = spinnerCourse.getSelectedItem().toString();
        String questionType = spinnerQuestionType.getSelectedItem().toString();
        String numStr = etNumQuestions.getText().toString().trim();
        int numQuestions = numStr.isEmpty() ? 10 : Integer.parseInt(numStr);

        List<String> selectedLectures = new ArrayList<>();
        for (int i = 0; i < lecturesCheckContainer.getChildCount(); i++) {
            View child = lecturesCheckContainer.getChildAt(i);
            if (child instanceof CheckBox && ((CheckBox) child).isChecked()) {
                selectedLectures.add(((CheckBox) child).getText().toString());
            }
        }

        String lecturesText = selectedLectures.isEmpty() ? "كل محاضرات المقرر" : String.join("، ", selectedLectures);

        String prompt = "أنت معلم جامعي. كوّن امتحان بصيغة JSON فقط بدون أي نص إضافي ولا علامات ماركداون. " +
                "المقرر: " + courseName + ". المحاضرات المشمولة: " + lecturesText + ". " +
                "عدد الأسئلة: " + numQuestions + ". نوع الأسئلة: " + questionType + ". " +
                "الصيغة المطلوبة بالضبط: " +
                "{\"questions\":[{\"question\":\"نص السؤال\",\"options\":[\"خيار1\",\"خيار2\",\"خيار3\",\"خيار4\"],\"correct_index\":0}]}. " +
                "لأسئلة صح/خطأ اجعل options مصفوفة من عنصرين فقط: [\"صح\",\"خطأ\"]. " +
                "اكتب الأسئلة بمستوى مناسب لطالب جامعي في هذا المقرر بناءً على عناوين المحاضرات المذكورة.";

        progressBar.setVisibility(View.VISIBLE);

        GeminiHelper.sendPrompt(requireContext(), prompt, new GeminiHelper.Callback() {
            @Override
            public void onSuccess(String responseText) {
                progressBar.setVisibility(View.GONE);
                try {
                    String cleaned = responseText.trim();
                    if (cleaned.startsWith("```")) {
                        cleaned = cleaned.replaceAll("```json", "").replaceAll("```", "").trim();
                    }
                    JSONObject json = new JSONObject(cleaned);
                    JSONArray questionsArray = json.getJSONArray("questions");

                    ArrayList<Question> questions = new ArrayList<>();
                    for (int i = 0; i < questionsArray.length(); i++) {
                        JSONObject q = questionsArray.getJSONObject(i);
                        List<String> options = new ArrayList<>();
                        JSONArray optArray = q.getJSONArray("options");
                        for (int j = 0; j < optArray.length(); j++) {
                            options.add(optArray.getString(j));
                        }
                        questions.add(new Question(q.getString("question"), options, q.getInt("correct_index")));
                    }

                    android.content.Intent intent = new android.content.Intent(requireContext(), QuizActivity.class);
                    intent.putExtra("course_name", courseName);
                    QuizActivity.pendingQuestions = questions;
                    startActivity(intent);

                } catch (Exception e) {
                    Toast.makeText(requireContext(), "تعذّر تكوين الامتحان، حاول مرة أخرى", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}