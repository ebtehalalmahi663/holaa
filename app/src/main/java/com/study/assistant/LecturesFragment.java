package com.study.assistant;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LecturesFragment extends Fragment {

    private String username, college, department, semester;
    private DatabaseHelper dbHelper;
    private LinearLayout coursesContainer;
    private long pendingCourseIdForUpload = -1;

    private ActivityResultLauncher<String[]> filePickerLauncher;

    public static LecturesFragment newInstance(String username, String college, String department, String semester) {
        LecturesFragment fragment = new LecturesFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
        args.putString("college", college);
        args.putString("department", department);
        args.putString("semester", semester);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString("username");
            college = getArguments().getString("college");
            department = getArguments().getString("department");
            semester = getArguments().getString("semester");
        }
        dbHelper = new DatabaseHelper(requireContext());

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris == null || uris.isEmpty() || pendingCourseIdForUpload == -1) return;
                    for (Uri uri : uris) {
                        try {
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {}

                        String fileName = getFileNameFromUri(uri);
                        dbHelper.insertLectureFile(pendingCourseIdForUpload, fileName, uri.toString());
                    }
                    refreshCourses();
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lectures, container, false);
        coursesContainer = view.findViewById(R.id.coursesContainer);
        Button btnAddCourse = view.findViewById(R.id.btnAddCourse);

        btnAddCourse.setOnClickListener(v -> showAddCourseDialog());

        refreshCourses();
        return view;
    }

    private void showAddCourseDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_course, null);
        EditText etName = dialogView.findViewById(R.id.etCourseName);
        EditText etHours = dialogView.findViewById(R.id.etCourseHours);
        EditText etLabLang = dialogView.findViewById(R.id.etLabLanguage);

        new AlertDialog.Builder(requireContext())
                .setTitle("إضافة مقرر")
                .setView(dialogView)
                .setPositiveButton("إضافة", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String hoursStr = etHours.getText().toString().trim();
                    String labLang = etLabLang.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "أدخل اسم المقرر", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int hours = hoursStr.isEmpty() ? 0 : Integer.parseInt(hoursStr);
                    dbHelper.insertCourse(username, college, department, semester, name, hours, labLang);
                    refreshCourses();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void refreshCourses() {
        coursesContainer.removeAllViews();
        Cursor cursor = dbHelper.getCourses(username, college, department, semester);

        while (cursor.moveToNext()) {
            long courseId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COURSE_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COURSE_NAME));
            int hours = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COURSE_HOURS));
            String labLang = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COURSE_LAB_LANG));

            View courseView = LayoutInflater.from(requireContext()).inflate(R.layout.item_course, coursesContainer, false);
            TextView tvName = courseView.findViewById(R.id.tvCourseName);
            TextView tvDetails = courseView.findViewById(R.id.tvCourseDetails);
            Button btnAddFiles = courseView.findViewById(R.id.btnAddFiles);
            Button btnAskDoctor = courseView.findViewById(R.id.btnAskDoctor);
            LinearLayout filesContainer = courseView.findViewById(R.id.filesContainer);

            tvName.setText(name);
            String details = "عدد الساعات: " + hours;
            if (labLang != null && !labLang.isEmpty()) {
                details += " | لغة المعمل: " + labLang;
            }
            tvDetails.setText(details);

            final String courseNameFinal = name;

            btnAddFiles.setOnClickListener(v -> {
                pendingCourseIdForUpload = courseId;
                dbHelper.updateUserProgress(username, college, department, semester, courseNameFinal, "", 0);
                filePickerLauncher.launch(new String[]{"*/*"});
            });

            btnAskDoctor.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), DoctorQAActivity.class);
                intent.putExtra("course_id", courseId);
                intent.putExtra("course_name", courseNameFinal);
                startActivity(intent);
            });

            loadFilesForCourse(courseId, filesContainer, courseNameFinal);

            coursesContainer.addView(courseView);
        }
        cursor.close();
    }

    private void loadFilesForCourse(long courseId, LinearLayout filesContainer, String courseName) {
        filesContainer.removeAllViews();
        Cursor cursor = dbHelper.getLectureFiles(courseId);

        while (cursor.moveToNext()) {
            String fileName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FILE_NAME));
            String fileUriStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FILE_URI));

            View fileRow = LayoutInflater.from(requireContext()).inflate(R.layout.item_file_row, filesContainer, false);
            TextView tvFile = fileRow.findViewById(R.id.tvFileName);
            Button btnSummarize = fileRow.findViewById(R.id.btnSummarize);

            tvFile.setText("📄 " + fileName);
            tvFile.setOnClickListener(v -> openFile(Uri.parse(fileUriStr)));
            btnSummarize.setOnClickListener(v -> showSummaryDialog(fileName, courseName));

            filesContainer.addView(fileRow);
        }
        cursor.close();
    }

    private void showSummaryDialog(String fileName, String courseName) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_summary, null);
        TextView tvFileName = dialogView.findViewById(R.id.tvSummaryFileName);
        TextView tvContent = dialogView.findViewById(R.id.tvSummaryContent);
        android.widget.ProgressBar progressBar = dialogView.findViewById(R.id.progressBarSummary);
        Button btnClose = dialogView.findViewById(R.id.btnCloseSummary);

        tvFileName.setText(courseName + " ← " + fileName);
        tvContent.setText("");
        progressBar.setVisibility(View.VISIBLE);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        String prompt = "أنت مساعد دراسي لطالب جامعي. المقرر: \"" + courseName +
                "\". اسم ملف المحاضرة: \"" + fileName + "\". " +
                "بناءً على اسم المحاضرة والمقرر، اكتب ملخصًا تعليميًا مفيدًا لأهم النقاط المتوقع أن تتناولها محاضرة بهذا العنوان في هذا التخصص. " +
                "اكتب الملخص على شكل نقاط قصيرة وواضحة بالعربية، بحد أقصى 8 نقاط. " +
                "لو اسم الملف غير واضح المعنى (مثل lec1 أو ملف بدون عنوان دال)، وضّح للطالب إن الاسم غير كافٍ لتحديد محتوى دقيق واطلب منه إعادة تسمية الملف باسم موضوع المحاضرة للحصول على ملخص أدق.";

        GeminiHelper.sendPrompt(requireContext(), prompt, new GeminiHelper.Callback() {
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

    private void openFile(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "لا يوجد تطبيق لفتح هذا الملف", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = "ملف";
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1 && cursor.moveToFirst()) {
                result = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return result;
    }
}