package com.study.assistant;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setTitle("تقرير النتيجة");

        String courseName = getIntent().getStringExtra("course_name");
        int correctCount = getIntent().getIntExtra("correct_count", 0);
        int totalCount = getIntent().getIntExtra("total_count", 0);
        double percentage = getIntent().getDoubleExtra("percentage", 0);

        TextView tvTitle = findViewById(R.id.tvResultTitle);
        TextView tvPercentage = findViewById(R.id.tvPercentage);
        TextView tvDetails = findViewById(R.id.tvScoreDetails);
        TextView tvFeedback = findViewById(R.id.tvAiFeedback);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        boolean passed = percentage >= 60;
        tvTitle.setText(passed ? "🎉 مبروك، ناجح" : "📘 يحتاج مراجعة إضافية");
        tvTitle.setTextColor(getResources().getColor(passed ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));

        tvPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
        tvDetails.setText("أجبت بشكل صحيح على " + correctCount + " من أصل " + totalCount + " سؤال في مقرر " + courseName);

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
    }
}