package com.niloortega.millionairemind.controller;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.niloortega.millionairemind.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatController.class)
@Import(ChatService.class)
@ActiveProfiles("test")
class ChatControllerTest {

	@Autowired
	private MockMvc mockMvc;

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
				.andExpect(jsonPath("$.message").value("The chat API is ready. RAG retrieval and Gemini generation will be connected next."))
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
}
