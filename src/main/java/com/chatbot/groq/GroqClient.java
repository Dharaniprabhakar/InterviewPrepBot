package com.chatbot.groq;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GroqClient {

    private final RestClient restClient;
    private final GroqProperties properties;

    public GroqClient(RestClient.Builder builder, GroqProperties properties) {
        this.restClient = builder
                .baseUrl("https://api.groq.com")
                .defaultHeader("Authorization", "Bearer " + properties.key())
                .build();
        this.properties = properties;
    }

    public String generateText(String prompt) {
        GroqResponse response = restClient.post()
                .uri("/openai/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(GroqRequest.of(properties.model(), prompt))
                .retrieve()
                .body(GroqResponse.class);
        return response != null ? response.getText().trim() : "";
    }

    public String generateJson(String prompt) {
        GroqResponse response = restClient.post()
                .uri("/openai/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(GroqRequest.ofJson(properties.model(), prompt))
                .retrieve()
                .body(GroqResponse.class);
        return response != null ? response.getText().trim() : "{}";
    }
}
