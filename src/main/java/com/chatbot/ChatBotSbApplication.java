package com.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.chatbot.groq.GroqProperties;

@SpringBootApplication
@EnableConfigurationProperties(GroqProperties.class)
public class ChatBotSbApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatBotSbApplication.class, args);
	}

}
