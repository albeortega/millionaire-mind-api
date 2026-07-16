package com.niloortega.millionairemind.controller;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.niloortega.millionairemind.config.CorsConfig;
import com.niloortega.millionairemind.dto.ChatRequest;
import com.niloortega.millionairemind.dto.ChatResponse;
import com.niloortega.millionairemind.service.ChatService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatController.class)
@Import(CorsConfig.class)
@ActiveProfiles("test")
class ChatControllerTest {

	private static final String ASSISTANT_REPLY =
			"I do not have enough book content loaded yet to answer that from Jewels of the Millionaire Mind.";
	private static final UUID DEFAULT_CONVERSATION_ID = UUID.fromString("f3b18c5c-c9cc-4791-a55b-3975c67c5b85");
	private static final Instant CREATED_AT = Instant.parse("2026-07-15T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ChatService chatService;

	@BeforeEach
	void setUp() {
		when(chatService.reply(any(ChatRequest.class))).thenAnswer(invocation -> {
			ChatRequest request = invocation.getArgument(0);
			UUID conversationId = request.conversationId() == null ? DEFAULT_CONVERSATION_ID : request.conversationId();
			return new ChatResponse(conversationId, "ASSISTANT", ASSISTANT_REPLY, List.of(), CREATED_AT);
		});
	}

	@Test
	void returnsChatResponseForNewConversation() throws Exception {
		mockMvc.perform(post("/api/chat")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "message": "What is the first millionaire mind principle?"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.conversationId", notNullValue()))
				.andExpect(jsonPath("$.role").value("ASSISTANT"))
				.andExpect(jsonPath("$.message").value(ASSISTANT_REPLY))
				.andExpect(jsonPath("$.sources", is(empty())))
				.andExpect(jsonPath("$.createdAt", notNullValue()));
	}

	@Test
	void keepsProvidedConversationId() throws Exception {
		String conversationId = "8c71ca7b-edb3-4792-91d4-1de3a9968475";

		mockMvc.perform(post("/api/chat")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "conversationId": "%s",
								  "message": "Continue"
								}
								""".formatted(conversationId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.conversationId").value(conversationId));
	}

	@Test
	void acceptsFrontendMessagesPayload() throws Exception {
		mockMvc.perform(post("/api/chat")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "messages": [
								    {
								      "role": "user",
								      "content": "How should I think about assets?"
								    }
								  ]
								}
				"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ASSISTANT"))
				.andExpect(jsonPath("$.message").value(ASSISTANT_REPLY));
	}

	@Test
	void rejectsMessagesPayloadWithoutUserContent() throws Exception {
		mockMvc.perform(post("/api/chat")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "messages": [
								    {
								      "role": "assistant",
								      "content": "Previous reply"
								    }
								  ]
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsBlankMessage() throws Exception {
		mockMvc.perform(post("/api/chat")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "message": ""
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void allowsCorsPreflightForChatEndpoint() throws Exception {
		mockMvc.perform(options("/api/chat")
						.header(HttpHeaders.ORIGIN, "https://millionaire-mind.vercel.app")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://millionaire-mind.vercel.app"))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,PATCH,DELETE,OPTIONS"));
	}
}
