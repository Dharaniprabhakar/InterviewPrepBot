package com.chatbot.service;

import com.chatbot.groq.GroqClient;
import com.chatbot.model.EvaluationResult;
import com.chatbot.model.QAPair;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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

    public List<String> generateSubtopics(String category) {
        String prompt = """
                Return a JSON object with a single key "subtopics" containing an array of 8 to 10
                key sub-topics for the "%s" technical interview category.
                Each sub-topic should be a short, specific label (1-4 words).
                Example: {"subtopics": ["Arrays", "Strings", "OOP", "Collections"]}
                """.formatted(category);

        String json = groqClient.generateJson(prompt);
        try {
            JsonNode node = objectMapper.readTree(json);
            return objectMapper.convertValue(node.get("subtopics"), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse subtopics response: {}", json, e);
            return List.of();
        }
    }

    public String generateQuestion(String category, String subtopic, String difficulty, String company, List<QAPair> previousPairs) {
        StringBuilder prompt = new StringBuilder();

        boolean isGeneral = company == null || company.isBlank() || company.equalsIgnoreCase("General");
        boolean hasSubtopic = subtopic != null && !subtopic.isBlank();

        String topic = hasSubtopic ? category + " - " + subtopic : category;

        if (isGeneral) {
            prompt.append("You are an experienced technical interviewer conducting a ")
                  .append(difficulty).append(" level ").append(topic).append(" interview.\n\n");
        } else {
            prompt.append("You are an experienced technical interviewer at ").append(company)
                  .append(" conducting a ").append(difficulty).append(" level ").append(topic).append(" interview.\n\n");
        }

        if (!previousPairs.isEmpty()) {
            prompt.append("Questions already asked (DO NOT repeat these):\n");
            for (QAPair pair : previousPairs) {
                prompt.append("- ").append(pair.getQuestion()).append("\n");
            }
            prompt.append("\n");
        }

        String questionTypes =
                "Vary the question type — choose one that has NOT been used in the previous questions above. " +
                "Rotate through these types across the interview:\n" +
                "  - Theory/Concept: Explain a concept or how something works internally\n" +
                "  - Scenario-based: A real-world situation the candidate must reason through\n" +
                "  - Code Writing: Write a function, class, or snippet from scratch\n" +
                "  - Input/Output: Given a code snippet, predict what the output will be\n" +
                "  - Debugging: Spot and fix a bug in a provided code snippet\n" +
                "  - Comparison: Differences, trade-offs, or when to choose X over Y\n" +
                "  - Best Practices: Design decisions, patterns, or do's and don'ts\n";

        if (isGeneral) {
            prompt.append("Generate ONE new, unique ").append(topic)
                  .append(" interview question appropriate for ").append(difficulty).append(" level.\n")
                  .append(questionTypes)
                  .append("Return ONLY the question text. No type label, no numbering, no extra explanation.");
        } else {
            prompt.append("Generate ONE new, unique ").append(topic)
                  .append(" interview question that ").append(company)
                  .append(" is known to ask in ").append(difficulty).append(" level interviews.\n")
                  .append(questionTypes)
                  .append("Return ONLY the question text. No type label, no numbering, no extra explanation.");
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
