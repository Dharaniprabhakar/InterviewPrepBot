package com.chatbot.groq;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroqRequest {

    private String model;
    private List<Message> messages;

    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseFormat {
        private String type;
    }

    public static GroqRequest of(String model, String prompt) {
        return new GroqRequest(model, List.of(new Message("user", prompt)), null);
    }

    public static GroqRequest ofJson(String model, String prompt) {
        return new GroqRequest(model, List.of(new Message("user", prompt)), new ResponseFormat("json_object"));
    }
}
