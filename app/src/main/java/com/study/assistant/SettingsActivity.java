package com.study.assistant;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("الإعدادات");

        EditText etApiKey = findViewById(R.id.etApiKey);
        Button btnSave = findViewById(R.id.btnSaveKey);

        etApiKey.setText(GeminiHelper.getApiKey(this));

        btnSave.setOnClickListener(v -> {
            String key = etApiKey.getText().toString().trim();
            GeminiHelper.saveApiKey(this, key);
            Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}