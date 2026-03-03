package com.chatbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QAPair {
    private int questionNumber;
    private String question;
    private String userAnswer;
    private int score;
    private String feedback;
    private String modelAnswer;
}
