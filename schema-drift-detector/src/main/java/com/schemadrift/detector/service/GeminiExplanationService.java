package com.schemadrift.detector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Thin layer that turns a rule-classified breaking change into a plain
 * English explanation. Deliberately kept separate from DiffEngineService:
 * the AI EXPLAINS, it never DECIDES severity. That separation is the
 * difference between "an AI wrapper" and "a real diff engine with an AI
 * assist" - keep it that way when you talk about this project.
 */
@Service
public class GeminiExplanationService {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiExplanationService(@Value("${app.gemini.api-key}") String apiKey,
                                     @Value("${app.gemini.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String explain(String fieldName, String changeType, String oldValue, String newValue) {
        String prompt = String.format("""
                A database column changed. Explain in 2-3 plain-English sentences
                what could break downstream, and one suggestion for how to check
                or fix it. Be concrete, no filler.

                Field: %s
                Change type: %s
                Old: %s
                New: %s
                """, fieldName, changeType, oldValue, newValue);

        try {
            String url = String.format("/v1beta/models/%s:generateContent?key=%s", model, apiKey);

            Map<String, Object> body = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{ Map.of("text", prompt) })
                    }
            );

            String response = webClient.post()
                    .uri(url)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

        } catch (Exception e) {
            // Never let an AI/network failure break the core diff result -
            // fall back to the rule-based reason already stored.
            return null;
        }
    }
}
