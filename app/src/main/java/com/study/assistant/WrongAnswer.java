package com.study.assistant;

import java.util.List;

public class WrongAnswer {
    public String questionText;
    public List<String> options;
    public int correctIndex;
    public int selectedIndex;

    public WrongAnswer(String questionText, List<String> options, int correctIndex, int selectedIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctIndex = correctIndex;
        this.selectedIndex = selectedIndex;
    }
}