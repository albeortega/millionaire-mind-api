package com.niloortega.millionairemind.service;

import com.niloortega.millionairemind.dto.ChatRequest;
import com.niloortega.millionairemind.dto.ChatResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

	private final Clock clock;

	public ChatService() {
		this(Clock.systemUTC());
	}

	ChatService(Clock clock) {
		this.clock = clock;
	}

	public ChatResponse reply(ChatRequest request) {
		UUID conversationId = request.conversationId() == null ? UUID.randomUUID() : request.conversationId();
		Instant createdAt = Instant.now(clock);

		return new ChatResponse(
				conversationId,
				"ASSISTANT",
				"The chat API is ready. RAG retrieval and Gemini generation will be connected next.",
				List.of(),
				createdAt);
	}
}
