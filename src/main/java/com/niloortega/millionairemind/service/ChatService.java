package com.niloortega.millionairemind.service;

import com.niloortega.millionairemind.dto.ChatRequest;
import com.niloortega.millionairemind.dto.ChatResponse;
import com.niloortega.millionairemind.entity.ConversationEntity;
import com.niloortega.millionairemind.entity.MessageEntity;
import com.niloortega.millionairemind.entity.MessageRole;
import com.niloortega.millionairemind.repository.ConversationRepository;
import com.niloortega.millionairemind.repository.MessageRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

	private static final int MAX_TITLE_LENGTH = 80;
	private static final String PLACEHOLDER_REPLY =
			"The chat API is ready. RAG retrieval and Gemini generation will be connected next.";

	private final ConversationRepository conversationRepository;
	private final MessageRepository messageRepository;
	private final Clock clock;

	public ChatService(ConversationRepository conversationRepository, MessageRepository messageRepository) {
		this(conversationRepository, messageRepository, Clock.systemUTC());
	}

	ChatService(ConversationRepository conversationRepository, MessageRepository messageRepository, Clock clock) {
		this.conversationRepository = conversationRepository;
		this.messageRepository = messageRepository;
		this.clock = clock;
	}

	@Transactional
	public ChatResponse reply(ChatRequest request) {
		Instant createdAt = Instant.now(clock);
		String prompt = request.prompt();
		ConversationEntity conversation = resolveConversation(request.conversationId(), prompt, createdAt);

		messageRepository.save(new MessageEntity(conversation, MessageRole.USER, prompt, createdAt));
		MessageEntity assistantMessage =
				messageRepository.save(new MessageEntity(conversation, MessageRole.ASSISTANT, PLACEHOLDER_REPLY, createdAt));

		return new ChatResponse(
				conversation.getId(),
				"ASSISTANT",
				assistantMessage.getContent(),
				List.of(),
				assistantMessage.getCreatedAt());
	}

	private ConversationEntity resolveConversation(UUID conversationId, String prompt, Instant now) {
		if (conversationId == null) {
			return conversationRepository.save(new ConversationEntity(null, titleFrom(prompt), now));
		}

		return conversationRepository.findById(conversationId)
				.map(conversation -> {
					conversation.setUpdatedAt(now);
					return conversationRepository.save(conversation);
				})
				.orElseGet(() -> conversationRepository.save(new ConversationEntity(conversationId, titleFrom(prompt), now)));
	}

	private String titleFrom(String prompt) {
		String normalized = prompt.replaceAll("\\s+", " ").trim();
		if (normalized.length() <= MAX_TITLE_LENGTH) {
			return normalized;
		}

		return normalized.substring(0, MAX_TITLE_LENGTH);
	}
}
