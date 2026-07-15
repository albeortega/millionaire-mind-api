package com.niloortega.millionairemind.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatResponse(
		UUID conversationId,
		String role,
		String message,
		List<ChatSource> sources,
		Instant createdAt) {
}
