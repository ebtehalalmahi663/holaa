package com.study.assistant;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    public static List<WrongAnswer> pendingWrongAnswers;
    private String courseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setTitle("تقرير النتيجة");

        courseName = getIntent().getStringExtra("course_name");
        String username = getIntent().getStringExtra("username");
        String college = getIntent().getStringExtra("college");
        String department = getIntent().getStringExtra("department");
        String semester = getIntent().getStringExtra("semester");
        int correctCount = getIntent().getIntExtra("correct_count", 0);
        int totalCount = getIntent().getIntExtra("total_count", 0);
        double percentage = getIntent().getDoubleExtra("percentage", 0);

        TextView tvTitle = findViewById(R.id.tvResultTitle);
        TextView tvPercentage = findViewById(R.id.tvPercentage);
        TextView tvDetails = findViewById(R.id.tvScoreDetails);
        TextView tvFeedback = findViewById(R.id.tvAiFeedback);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView tvWrongTitle = findViewById(R.id.tvWrongAnswersTitle);
        LinearLayout wrongContainer = findViewById(R.id.wrongAnswersContainer);

        boolean passed = percentage >= 60;
        tvTitle.setText(passed ? "🎉 مبروك، ناجح" : "📘 يحتاج مراجعة إضافية");
        tvTitle.setTextColor(getResources().getColor(passed ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));

        tvPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
        tvDetails.setText("أجبت بشكل صحيح على " + correctCount + " من أصل " + totalCount + " سؤال في مقرر " + courseName);

        new DatabaseHelper(this).insertExamResult(username, college, department, semester, courseName, correctCount, totalCount, percentage);

        String prompt = "طالب جامعي أدى امتحان في مقرر \"" + courseName + "\" وحصل على نسبة " +
                String.format(Locale.getDefault(), "%.0f", percentage) + "% (" + correctCount + " من " + totalCount + "). " +
                "اكتب له تحليلًا موجزًا (لا يتجاوز 5 أسطر) عن مستواه، ونصائح عملية للمذاكرة، واقترح خطة مراجعة قصيرة تحضّره لامتحان مثالي في نفس المقرر. اكتب بالعربية بأسلوب مشجّع.";

        progressBar.setVisibility(View.VISIBLE);
        tvFeedback.setText("");

        GeminiHelper.sendPrompt(this, prompt, new GeminiHelper.Callback() {
            @Override
            public void onSuccess(String responseText) {
                progressBar.setVisibility(View.GONE);
                tvFeedback.setText(responseText);
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                tvFeedback.setText(errorMessage);
            }
        });

        if (pendingWrongAnswers != null && !pendingWrongAnswers.isEmpty()) {
            tvWrongTitle.setVisibility(View.VISIBLE);
            buildWrongAnswersList(wrongContainer, pendingWrongAnswers);
        }
    }

    private void buildWrongAnswersList(LinearLayout container, List<WrongAnswer> wrongAnswers) {
        for (WrongAnswer wa : wrongAnswers) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_wrong_answer, container, false);
            TextView tvQuestion = row.findViewById(R.id.tvWrongQuestion);
            TextView tvYourAnswer = row.findViewById(R.id.tvWrongYourAnswer);
            TextView tvCorrectAnswer = row.findViewById(R.id.tvWrongCorrectAnswer);
            Button btnExplain = row.findViewById(R.id.btnExplainWrong);

            tvQuestion.setText(wa.questionText);

            String yourAnswerText = (wa.selectedIndex == -1 || wa.selectedIndex >= wa.options.size())
                    ? "❌ لم تُجب على هذا السؤال"
                    : "❌ إجابتك: " + wa.options.get(wa.selectedIndex);
            tvYourAnswer.setText(yourAnswerText);

            String correctAnswerText = "✅ الإجابة الصحيحة: " + wa.options.get(wa.correctIndex);
            tvCorrectAnswer.setText(correctAnswerText);

            btnExplain.setOnClickListener(v -> showExplanationDialog(wa));

            container.addView(row);
        }
    }

    private void showExplanationDialog(WrongAnswer wa) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_summary, null);
        TextView tvFileName = dialogView.findViewById(R.id.tvSummaryFileName);
        TextView tvContent = dialogView.findViewById(R.id.tvSummaryContent);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBarSummary);
        Button btnClose = dialogView.findViewById(R.id.btnCloseSummary);

        tvFileName.setText(courseName);
        tvContent.setText("");
        progressBar.setVisibility(View.VISIBLE);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        String prompt = "أنت مدرس جامعي في مقرر \"" + courseName + "\". السؤال: \"" + wa.questionText + "\". " +
                "الخيارات: " + String.join(" | ", wa.options) + ". " +
                "الإجابة الصحيحة هي: \"" + wa.options.get(wa.correctIndex) + "\". " +
                (wa.selectedIndex != -1 && wa.selectedIndex < wa.options.size()
                        ? "الطالب اختار: \"" + wa.options.get(wa.selectedIndex) + "\" وهي إجابة خاطئة. "
                        : "الطالب لم يجب على السؤال. ") +
                "اشرح للطالب بأسلوب مبسط وواضح ليه الإجابة الصحيحة هي دي، ولو كان اختيار الطالب خطأ وضّح ليه هو غلط بالتحديد. اكتب بالعربية في حدود 4-5 أسطر.";

        GeminiHelper.sendPrompt(this, prompt, new GeminiHelper.Callback() {
            @Override
            public void onSuccess(String responseText) {
                progressBar.setVisibility(View.GONE);
                tvContent.setText(responseText);
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                tvContent.setText(errorMessage);
            }
        });
    }
}