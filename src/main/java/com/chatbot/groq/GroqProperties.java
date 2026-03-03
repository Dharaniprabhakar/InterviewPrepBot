package com.chatbot.groq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "groq.api")
public record GroqProperties(String key, String model) {

    public GroqProperties {
        if (model == null || model.isBlank()) {
            model = "llama-3.3-70b-versatile";
        }
    }
}
