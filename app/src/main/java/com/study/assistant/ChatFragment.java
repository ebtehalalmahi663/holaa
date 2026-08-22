package com.study.assistant;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ChatFragment extends Fragment {

    private LinearLayout chatContainer;
    private ScrollView chatScroll;
    private EditText etMessage;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        chatContainer = view.findViewById(R.id.chatContainer);
        chatScroll = view.findViewById(R.id.chatScroll);
        etMessage = view.findViewById(R.id.etMessage);
        Button btnSend = view.findViewById(R.id.btnSend);

        addMessage("مرحبًا! أنا مساعدك الدراسي، اسألني عن أي حاجة في مقرراتك.", false);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (text.isEmpty()) return;

            addMessage(text, true);
            etMessage.setText("");

            TextView loadingView = addMessage("...جاري الكتابة", false);

            GeminiHelper.sendPrompt(requireContext(), text, new GeminiHelper.Callback() {
                @Override
                public void onSuccess(String responseText) {
                    loadingView.setText(responseText);
                }

                @Override
                public void onError(String errorMessage) {
                    loadingView.setText(errorMessage);
                }
            });
        });

        return view;
    }

    private TextView addMessage(String text, boolean isUser) {
        TextView tv = new TextView(requireContext());
        tv.setText(text);
        tv.setPadding(28, 18, 28, 18);
        tv.setTextColor(isUser ? getResources().getColor(android.R.color.white) : getResources().getColor(R.color.text_dark));
        tv.setBackgroundResource(isUser ? R.drawable.bg_chat_bubble_user : R.drawable.bg_chat_bubble_ai);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 8, 16, 8);
        params.gravity = isUser ? Gravity.END : Gravity.START;
        tv.setLayoutParams(params);

        chatContainer.addView(tv);
        chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
        return tv;
    }