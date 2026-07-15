package com.niloortega.millionairemind.dto;

import jakarta.validation.constraints.AssertTrue;
import java.util.List;
import java.util.UUID;

public record ChatRequest(
		UUID conversationId,
		String message,
		List<ChatMessage> messages) {

	public String prompt() {
		if (message != null && !message.isBlank()) {
			return message.trim();
		}

		if (messages == null) {
			return null;
		}

		for (int i = messages.size() - 1; i >= 0; i--) {
			ChatMessage chatMessage = messages.get(i);
			if (chatMessage != null && "user".equalsIgnoreCase(chatMessage.role()) && chatMessage.content() != null
					&& !chatMessage.content().isBlank()) {
				return chatMessage.content().trim();
			}
		}

		return null;
	}

	@AssertTrue(message = "message is required")
	public boolean hasPrompt() {
		return prompt() != null;
	}

	@AssertTrue(message = "message must be 4000 characters or fewer")
	public boolean isPromptWithinLimit() {
		String prompt = prompt();
		return prompt == null || prompt.length() <= 4000;
	}
}
