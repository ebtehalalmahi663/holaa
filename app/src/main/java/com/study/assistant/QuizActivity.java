package com.study.assistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    public static List<Question> pendingQuestions;

    private LinearLayout questionsContainer;
    private List<Question> questions;
    private RadioGroup[] radioGroups;
    private String courseName;
    private String username, college, department, semester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        setTitle("الامتحان");

        courseName = getIntent().getStringExtra("course_name");
        username = getIntent().getStringExtra("username");
        college = getIntent().getStringExtra("college");
        department = getIntent().getStringExtra("department");
        semester = getIntent().getStringExtra("semester");
        questions = pendingQuestions;

        if (questions == null || questions.isEmpty()) {
            Toast.makeText(this, "حدث خطأ في تحميل الأسئلة", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        questionsContainer = findViewById(R.id.questionsContainer);
        Button btnSubmit = findViewById(R.id.btnSubmitExam);

        radioGroups = new RadioGroup[questions.size()];

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            View qView = LayoutInflater.from(this).inflate(R.layout.item_quiz_question, questionsContainer, false);
            TextView tvQuestion = qView.findViewById(R.id.tvQuestionText);
            RadioGroup radioGroup = qView.findViewById(R.id.radioGroupOptions);

            tvQuestion.setText((i + 1) + ". " + q.questionText);

            for (int j = 0; j < q.options.size(); j++) {
                RadioButton rb = new RadioButton(this);
                rb.setText(q.options.get(j));
                rb.setId(j);
                radioGroup.addView(rb);
            }

            radioGroups[i] = radioGroup;
            questionsContainer.addView(qView);
        }

        btnSubmit.setOnClickListener(v -> submitExam());
    }

    private void submitExam() {
        int correctCount = 0;
        int answeredCount = 0;
        ArrayList<WrongAnswer> wrongAnswers = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            int selectedId = radioGroups[i].getCheckedRadioButtonId();
            Question q = questions.get(i);

            if (selectedId != -1) {
                answeredCount++;
                if (selectedId == q.correctIndex) {
                    correctCount++;
                } else {
                    wrongAnswers.add(new WrongAnswer(q.questionText, q.options, q.correctIndex, selectedId));
                }
            } else {
                wrongAnswers.add(new WrongAnswer(q.questionText, q.options, q.correctIndex, -1));
            }
        }

        int total = questions.size();
        double percentage = total == 0 ? 0 : (correctCount * 100.0 / total);

        ReportActivity.pendingWrongAnswers = wrongAnswers;

        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("course_name", courseName);
        intent.putExtra("username", username);
        intent.putExtra("college", college);
        intent.putExtra("department", department);
        intent.putExtra("semester", semester);
        intent.putExtra("correct_count", correctCount);
        intent.putExtra("total_count", total);
        intent.putExtra("answered_count", answeredCount);
        intent.putExtra("percentage", percentage);
        startActivity(intent);
        finish();
    }
}