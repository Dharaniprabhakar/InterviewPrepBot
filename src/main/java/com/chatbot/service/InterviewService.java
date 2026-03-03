package com.chatbot.service;

import com.chatbot.groq.GroqClient;
import com.chatbot.model.EvaluationResult;
import com.chatbot.model.QAPair;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final GroqClient groqClient;
    private final ObjectMapper objectMapper;

    public String generateQuestion(String category, String difficulty, String company, List<QAPair> previousPairs) {
        StringBuilder prompt = new StringBuilder();

        boolean isGeneral = company == null || company.isBlank() || company.equalsIgnoreCase("General");

        if (isGeneral) {
            prompt.append("You are an experienced technical interviewer conducting a ")
                  .append(difficulty).append(" level ").append(category).append(" interview.\n\n");
        } else {
            prompt.append("You are an experienced technical interviewer at ").append(company)
                  .append(" conducting a ").append(difficulty).append(" level ").append(category).append(" interview.\n\n");
        }

        if (!previousPairs.isEmpty()) {
            prompt.append("Questions already asked (DO NOT repeat these):\n");
            for (QAPair pair : previousPairs) {
                prompt.append("- ").append(pair.getQuestion()).append("\n");
            }
            prompt.append("\n");
        }

        if (isGeneral) {
            prompt.append("Generate ONE new, unique ").append(category)
                  .append(" interview question appropriate for ").append(difficulty)
                  .append(" level. Return ONLY the question text, no numbering, no extra explanation.");
        } else {
            prompt.append("Generate ONE new, unique ").append(category)
                  .append(" interview question that ").append(company)
                  .append(" is known to ask in ").append(difficulty).append(" level interviews.")
                  .append(" Return ONLY the question text, no numbering, no extra explanation.");
        }

        return groqClient.generateText(prompt.toString());
    }

    public EvaluationResult evaluateAnswer(String category, String difficulty, String question, String answer) {
        String candidateAnswer = (answer == null || answer.isBlank()) ? "(No answer provided)" : answer;

        String prompt = """
                You are an experienced technical interviewer evaluating a candidate's answer.

                Category: %s
                Difficulty: %s
                Question: %s
                Candidate's Answer: %s

                Evaluate the answer and return a JSON object with exactly these three fields:
                {
                  "score": <integer from 0 to 10>,
                  "feedback": "<2-3 sentences about what was good and what was missing>",
                  "modelAnswer": "<a clear, concise ideal answer in 3-5 sentences>"
                }
                """.formatted(category, difficulty, question, candidateAnswer);

        String jsonResponse = groqClient.generateJson(prompt);

        try {
            return objectMapper.readValue(jsonResponse, EvaluationResult.class);
        } catch (Exception e) {
            log.error("Failed to parse Groq evaluation response: {}", jsonResponse, e);
            return new EvaluationResult(0, "Could not evaluate the answer. Please try again.", "");
        }
    }
}
