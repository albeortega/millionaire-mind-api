package com.niloortega.millionairemind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ChatRequest(
		UUID conversationId,
		@NotBlank(message = "message is required") @Size(max = 4000, message = "message must be 4000 characters or fewer") String message) {
}
