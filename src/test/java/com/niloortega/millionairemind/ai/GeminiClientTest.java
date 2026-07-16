package com.niloortega.millionairemind.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GeminiClientTest {

	@Test
	void extractsTextFromGenerateContentResponse() {
		RestClient.Builder restClientBuilder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
		GeminiClient geminiClient =
				new GeminiClient(restClientBuilder, "test-api-key", "gemini-2.0-flash", "https://gemini.test/v1beta");

		server.expect(requestTo("https://gemini.test/v1beta/models/gemini-2.0-flash:generateContent?key=test-api-key"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("""
						{
						  "candidates": [
						    {
						      "content": {
						        "parts": [
						          {
						            "text": "Choose the option that aligns with who you want to become."
						          }
						        ]
						      }
						    }
						  ]
						}
						""", MediaType.APPLICATION_JSON));

		assertThat(geminiClient.generateAnswer("How do I choose?", List.of("Book context")))
				.contains("Choose the option that aligns with who you want to become.");
		server.verify();
	}
}
