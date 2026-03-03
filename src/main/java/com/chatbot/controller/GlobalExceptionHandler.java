package com.chatbot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Silently return 404 for missing static resources (e.g. favicon.ico)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public String handleQuotaExceeded(Model model) {
        model.addAttribute("errorTitle", "API Quota Exceeded");
        model.addAttribute("errorMessage",
                "The Gemini API free tier limit has been reached. Please wait a minute and try again, or check your API key quota at aistudio.google.com.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Unexpected error", ex);
        model.addAttribute("errorTitle", "Something went wrong");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please go back and try again.");
        return "error";
    }
}
