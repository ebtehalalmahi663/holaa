package com.study.assistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiHelper {

    private static final String MODEL_NAME = "gemini-3.5-flash-lite";
    private static final String PREFS_NAME = "study_assistant_prefs";
    private static final String KEY_API_KEY = "gemini_api_key";

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void onSuccess(String responseText);
        void onError(String errorMessage);
    }

    public static String getApiKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_API_KEY, "AQ.Ab8RN6JJ4F2EJkPGcKX63675alaVR-60qUU9Ygpa5nAn7MRCXg");
    }

    public static void saveApiKey(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_API_KEY, key).apply();
    }

    public static void sendPrompt(Context context, String prompt, Callback callback) {
        String apiKey = getApiKey(context);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            callback.onError("لم يتم إدخال مفتاح Gemini API. روح للإعدادات وأضفه.");
            return;
        }

        executor.execute(() -> {
            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/"
                        + MODEL_NAME + ":generateContent?key=" + apiKey);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);

                JSONObject part = new JSONObject();
                part.put("text", prompt);
                JSONArray parts = new JSONArray().put(part);
                JSONObject content = new JSONObject();
                content.put("parts", parts);
                JSONObject body = new JSONObject();
                body.put("contents", new JSONArray().put(content));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes("UTF-8"));
                }

                int code = conn.getResponseCode();
                java.io.InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
                String responseBody = scanner.hasNext() ? scanner.next() : "";
                scanner.close();

                if (code < 200 || code >= 300) {
                    postError(callback, "خطأ من الخادم (" + code + "): " + responseBody);
                    return;
                }

                JSONObject json = new JSONObject(responseBody);
                String text = json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                postSuccess(callback, text);

            } catch (Exception e) {
                postError(callback, "حدث خطأ: " + e.getMessage());
            }
        });
    }

    private static void postSuccess(Callback callback, String text) {
        mainHandler.post(() -> callback.onSuccess(text));
    }

    private static void postError(Callback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }
}