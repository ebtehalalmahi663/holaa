package com.study.assistant;

import java.util.ArrayList;
import java.util.List;

public class Question {
    public String questionText;
    public List<String> options = new ArrayList<>();
    public int correctIndex;

    public Question(String questionText, List<String> options, int correctIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctIndex = correctIndex;
    }
}