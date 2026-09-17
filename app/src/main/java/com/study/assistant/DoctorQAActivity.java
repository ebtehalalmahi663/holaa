package com.study.assistant;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class DoctorQAActivity extends AppCompatActivity {

    private long courseId;
    private String courseName;
    private DatabaseHelper dbHelper;

    private LinearLayout doctorsContainer, qaContainer;

    private List<Long> doctorIds = new ArrayList<>();
    private List<String> doctorNames = new ArrayList<>();
    private List<String> doctorEmails = new ArrayList<>();
    private List<String> doctorPhones = new ArrayList<>();

    private long pendingAnswerQuestionId = -1;
    private String pendingAudioUri = null;
    private ActivityResultLauncher<String[]> audioPickerLauncher;
    private TextView pendingAttachedFileNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_qa);

        courseId = getIntent().getLongExtra("course_id", -1);
        courseName = getIntent().getStringExtra("course_name");
        setTitle(courseName);

        dbHelper = new DatabaseHelper(this);
        doctorsContainer = findViewById(R.id.doctorsContainer);
        qaContainer = findViewById(R.id.qaContainer);

        audioPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                    pendingAudioUri = uri.toString();
                    if (pendingAttachedFileNameView != null) {
                        pendingAttachedFileNameView.setText("تم إرفاق: " + uri.getLastPathSegment());
                    }
                }
        );

        findViewById(R.id.btnAddDoctor).setOnClickListener(v -> showAddDoctorDialog());
        findViewById(R.id.btnAskQuestion).setOnClickListener(v -> showAskQuestionDialog());

        refreshDoctors();
        refreshQuestions();
    }

    private void showAddDoctorDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null);
        EditText etName = dialogView.findViewById(R.id.etDoctorName);
        EditText etEmail = dialogView.findViewById(R.id.etDoctorEmail);
        EditText etPhone = dialogView.findViewById(R.id.etDoctorPhone);

        new AlertDialog.Builder(this)
                .setTitle("إضافة دكتور")
                .setView(dialogView)
                .setPositiveButton("إضافة", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "أدخل اسم الدكتور", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dbHelper.insertDoctor(courseId, name, email, phone);
                    refreshDoctors();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void refreshDoctors() {
        doctorsContainer.removeAllViews();
        doctorIds.clear();
        doctorNames.clear();
        doctorEmails.clear();
        doctorPhones.clear();

        Cursor cursor = dbHelper.getDoctors(courseId);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_NAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_EMAIL));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DOC_PHONE));

            doctorIds.add(id);
            doctorNames.add(name);
            doctorEmails.add(email);
            doctorPhones.add(phone);

            View row = LayoutInflater.from(this).inflate(R.layout.item_doctor, doctorsContainer, false);
            TextView tvName = row.findViewById(R.id.tvDoctorName);
            TextView tvContact = row.findViewById(R.id.tvDoctorContact);
            Button btnDelete = row.findViewById(R.id.btnDeleteDoctor);

            tvName.setText(name);
            String contact = "";
            if (email != null && !email.isEmpty()) contact += "📧 " + email + "  ";
            if (phone != null && !phone.isEmpty()) contact += "📱 " + phone;
            tvContact.setText(contact);

            btnDelete.setOnClickListener(v -> {
                dbHelper.deleteDoctor(id);
                refreshDoctors();
            });

            doctorsContainer.addView(row);
        }
        cursor.close();

        if (doctorIds.isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("لا يوجد دكاترة مضافين لهذا المقرر بعد");
            tvEmpty.setPadding(20, 10, 20, 20);
            tvEmpty.setTextColor(getResources().getColor(R.color.text_dark));
            doctorsContainer.addView(tvEmpty);
        }
    }

    private void showAskQuestionDialog() {
        if (doctorIds.isEmpty()) {
            Toast.makeText(this, "أضف بيانات دكتور أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> lectureNames = new ArrayList<>();
        lectureNames.add("عام / غير محدد");
        Cursor lecCursor = dbHelper.getLectureFiles(courseId);
        while (lecCursor.moveToNext()) {
            lectureNames.add(lecCursor.getString(lecCursor.getColumnIndexOrThrow(DatabaseHelper.COL_FILE_NAME)));
        }
        lecCursor.close();

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ask_question, null);
        Spinner spinnerDoctor = dialogView.findViewById(R.id.spinnerDoctor);
        Spinner spinnerLecture = dialogView.findViewById(R.id.spinnerLecture);
        EditText etQuestion = dialogView.findViewById(R.id.etQuestionText);
        Button btnEmail = dialogView.findViewById(R.id.btnSendEmail);
        Button btnWhatsapp = dialogView.findViewById(R.id.btnSendWhatsapp);

        spinnerDoctor.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, doctorNames));
        spinnerLecture.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, lectureNames));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("اسأل الدكتور")
                .setView(dialogView)
                .setNegativeButton("إلغاء", null)
                .create();

        btnEmail.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            if (question.isEmpty()) {
                Toast.makeText(this, "اكتب سؤالك أولاً", Toast.LENGTH_SHORT).show();
                return;
            }
            int docIndex = spinnerDoctor.getSelectedItemPosition();
            String email = doctorEmails.get(docIndex);
            if (email == null || email.isEmpty()) {
                Toast.makeText(this, "هذا الدكتور ليس له بريد إلكتروني مسجل", Toast.LENGTH_SHORT).show();
                return;
            }
            String lecture = spinnerLecture.getSelectedItem().toString();
            sendViaEmail(email, question, lecture);
            dialog.dismiss();
        });

        btnWhatsapp.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            if (question.isEmpty()) {
                Toast.makeText(this, "اكتب سؤالك أولاً", Toast.LENGTH_SHORT).show();
                return;
            }
            int docIndex = spinnerDoctor.getSelectedItemPosition();
            String phone = doctorPhones.get(docIndex);
            if (phone == null || phone.isEmpty()) {
                Toast.makeText(this, "هذا الدكتور ليس له رقم واتساب مسجل", Toast.LENGTH_SHORT).show();
                return;
            }
            String lecture = spinnerLecture.getSelectedItem().toString();
            sendViaWhatsapp(phone, question, lecture);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void sendViaEmail(String email, String question, String lecture) {
        String subject = "سؤال بخصوص مقرر: " + courseName;
        String body = "استفسار عن محاضرة: " + lecture + "\n\n" + question;

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(intent);
            dbHelper.insertQuestion(courseId, lecture, question, "email");
            refreshQuestions();
        } catch (Exception e) {
            Toast.makeText(this, "لا يوجد تطبيق بريد مثبت على جهازك", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendViaWhatsapp(String phone, String question, String lecture) {
        String cleanedPhone = phone.replaceAll("[^0-9]", "");
        String message = "سؤال بخصوص مقرر: " + courseName + "\nمحاضرة: " + lecture + "\n\n" + question;
        String url = "https://wa.me/" + cleanedPhone + "?text=" + Uri.encode(message);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        try {
            startActivity(intent);
            dbHelper.insertQuestion(courseId, lecture, question, "whatsapp");
            refreshQuestions();
        } catch (Exception e) {
            Toast.makeText(this, "تعذر فتح واتساب", Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshQuestions() {
        qaContainer.removeAllViews();
        Cursor cursor = dbHelper.getQuestions(courseId);

        if (cursor.getCount() == 0) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("لم يتم طرح أي أسئلة بعد");
            tvEmpty.setPadding(20, 10, 20, 20);
            tvEmpty.setTextColor(getResources().getColor(R.color.text_dark));
            qaContainer.addView(tvEmpty);
        }

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_Q_ID));
            String lecture = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_Q_LECTURE));
            String question = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_Q_TEXT));
            String answerText = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_Q_ANSWER_TEXT));
            String answerAudio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_Q_ANSWER_AUDIO));

            View row = LayoutInflater.from(this).inflate(R.layout.item_qa_entry, qaContainer, false);
            TextView tvLecture = row.findViewById(R.id.tvQaLecture);
            TextView tvQuestion = row.findViewById(R.id.tvQaQuestion);
            TextView tvAnswerText = row.findViewById(R.id.tvQaAnswerText);
            Button btnPlayAudio = row.findViewById(R.id.btnPlayAnswerAudio);
            Button btnAddAnswer = row.findViewById(R.id.btnAddAnswer);

            tvLecture.setText("📖 " + lecture);
            tvQuestion.setText("❓ " + question);

            if (answerText != null && !answerText.isEmpty()) {
                tvAnswerText.setText("💬 " + answerText);
                tvAnswerText.setVisibility(View.VISIBLE);
            }

            if (answerAudio != null && !answerAudio.isEmpty()) {
                btnPlayAudio.setVisibility(View.VISIBLE);
                btnPlayAudio.setOnClickListener(v -> openAudio(Uri.parse(answerAudio)));
            }

            btnAddAnswer.setText((answerText != null && !answerText.isEmpty()) || (answerAudio != null && !answerAudio.isEmpty())
                    ? "✍ تعديل الرد" : "✍ أضف رد الدكتور");
            btnAddAnswer.setOnClickListener(v -> showAddAnswerDialog(id));

            qaContainer.addView(row);
        }
        cursor.close();
    }

    private void showAddAnswerDialog(long questionId) {
        pendingAnswerQuestionId = questionId;
        pendingAudioUri = null;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_answer, null);
        EditText etAnswer = dialogView.findViewById(R.id.etAnswerText);
        Button btnAttach = dialogView.findViewById(R.id.btnAttachAudio);
        TextView tvAttached = dialogView.findViewById(R.id.tvAttachedFileName);
        pendingAttachedFileNameView = tvAttached;

        btnAttach.setOnClickListener(v -> audioPickerLauncher.launch(new String[]{"audio/*"}));

        new AlertDialog.Builder(this)
                .setTitle("إضافة رد الدكتور")
                .setView(dialogView)
                .setPositiveButton("حفظ", (d, w) -> {
                    String answerText = etAnswer.getText().toString().trim();
                    dbHelper.updateAnswer(pendingAnswerQuestionId, answerText, pendingAudioUri);
                    refreshQuestions();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void openAudio(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "لا يوجد تطبيق لتشغيل هذا الملف الصوتي", Toast.LENGTH_SHORT).show();
        }
    }
}