package com.chatbot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InterviewSession {
    private String category;
    private String difficulty;
    private String company;
    private List<QAPair> completedPairs = new ArrayList<>();
    private String currentQuestion;
    private int currentQuestionNumber = 1;

    public static final int MAX_QUESTIONS = 10;

    public boolean isCompleted() {
        return currentQuestionNumber > MAX_QUESTIONS;
    }
}
