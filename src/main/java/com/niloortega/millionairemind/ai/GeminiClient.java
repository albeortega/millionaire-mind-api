package com.niloortega.millionairemind.ai;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class GeminiClient {

	private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);

	private final RestClient restClient;
	private final String apiKey;
	private final String model;
	private final String apiUrl;

	public GeminiClient(
			RestClient.Builder restClientBuilder,
			@Value("${app.gemini.api-key:}") String apiKey,
			@Value("${app.gemini.model:gemini-3.1-flash-lite}") String model,
			@Value("${app.gemini.api-url:https://generativelanguage.googleapis.com/v1beta}") String apiUrl) {
		this.restClient = restClientBuilder.build();
		this.apiKey = apiKey;
		this.model = model;
		this.apiUrl = apiUrl;
	}

	public Optional<String> generateAnswer(String question, List<String> contextChunks) {
		if (apiKey == null || apiKey.isBlank()) {
			return Optional.empty();
		}

		try {
			JsonNode response = restClient.post()
					.uri("%s/models/%s:generateContent?key=%s".formatted(apiUrl, model, apiKey))
					.body(requestBody(question, contextChunks))
					.retrieve()
					.body(JsonNode.class);

			return extractText(response);
		} catch (RestClientException exception) {
			logger.warn("Gemini request failed; falling back to local response", exception);
			return Optional.empty();
		}
	}

	private Map<String, Object> requestBody(String question, List<String> contextChunks) {
		return Map.of(
				"systemInstruction", Map.of(
						"parts", List.of(Map.of(
								"text", """
										You answer questions for the Millionaire Mind app.
										Use only the provided Jewels of the Millionaire Mind context.
										If the context is not enough, say you do not have enough book content yet.
										Be warm, practical, concise, and do not invent quotes or facts.
										"""))),
				"contents", List.of(Map.of(
						"role", "user",
						"parts", List.of(Map.of(
								"text", """
										Book context:
										%s

										User question:
										%s
										""".formatted(String.join("\n\n---\n\n", contextChunks), question))))),
				"generationConfig", Map.of("temperature", 0.4));
	}

	private Optional<String> extractText(JsonNode response) {
		if (response == null) {
			return Optional.empty();
		}

		JsonNode parts = response.path("candidates").path(0).path("content").path("parts");
		if (parts.isArray()) {
			StringBuilder text = new StringBuilder();
			for (JsonNode part : parts) {
				String partText = part.path("text").asText("");
				if (!partText.isBlank()) {
					text.append(partText);
				}
			}
			if (!text.isEmpty()) {
				return Optional.of(text.toString().trim());
			}
		}

		return Optional.empty();
	}
}
