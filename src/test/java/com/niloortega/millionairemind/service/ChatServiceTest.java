package com.niloortega.millionairemind.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.niloortega.millionairemind.dto.ChatRequest;
import com.niloortega.millionairemind.dto.ChatResponse;
import com.niloortega.millionairemind.entity.ConversationEntity;
import com.niloortega.millionairemind.entity.MessageEntity;
import com.niloortega.millionairemind.entity.MessageRole;
import com.niloortega.millionairemind.repository.ConversationRepository;
import com.niloortega.millionairemind.repository.MessageRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	private static final Instant NOW = Instant.parse("2026-07-15T00:00:00Z");
	private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
	private static final String NO_DATA_REPLY =
			"I do not have enough book content loaded yet to answer that from Jewels of the Millionaire Mind.";

	@Mock
	private ConversationRepository conversationRepository;

	@Mock
	private MessageRepository messageRepository;

	private ChatService chatService;

	@BeforeEach
	void setUp() {
		chatService = new ChatService(conversationRepository, messageRepository, CLOCK);
		when(conversationRepository.save(any(ConversationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(messageRepository.save(any(MessageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void createsConversationAndStoresUserAndAssistantMessages() {
		ChatRequest request = new ChatRequest(null, "  How should I think about assets?  ", null);

		ChatResponse response = chatService.reply(request);

		ArgumentCaptor<ConversationEntity> conversationCaptor = ArgumentCaptor.forClass(ConversationEntity.class);
		verify(conversationRepository).save(conversationCaptor.capture());
		ConversationEntity conversation = conversationCaptor.getValue();

		assertThat(response.conversationId()).isEqualTo(conversation.getId());
		assertThat(conversation.getTitle()).isEqualTo("How should I think about assets?");
		assertThat(conversation.getCreatedAt()).isEqualTo(NOW);
		assertThat(conversation.getUpdatedAt()).isEqualTo(NOW);

		ArgumentCaptor<MessageEntity> messageCaptor = ArgumentCaptor.forClass(MessageEntity.class);
		verify(messageRepository, times(2)).save(messageCaptor.capture());
		List<MessageEntity> savedMessages = messageCaptor.getAllValues();

		assertThat(savedMessages.get(0).getConversation()).isEqualTo(conversation);
		assertThat(savedMessages.get(0).getRole()).isEqualTo(MessageRole.USER);
		assertThat(savedMessages.get(0).getContent()).isEqualTo("How should I think about assets?");
		assertThat(savedMessages.get(0).getCreatedAt()).isEqualTo(NOW);
		assertThat(savedMessages.get(1).getConversation()).isEqualTo(conversation);
		assertThat(savedMessages.get(1).getRole()).isEqualTo(MessageRole.ASSISTANT);
		assertThat(savedMessages.get(1).getContent()).isEqualTo(NO_DATA_REPLY);
		assertThat(response.message()).isEqualTo(savedMessages.get(1).getContent());
		assertThat(response.createdAt()).isEqualTo(NOW);
	}

	@Test
	void appendsMessagesToExistingConversation() {
		UUID conversationId = UUID.fromString("8c71ca7b-edb3-4792-91d4-1de3a9968475");
		ConversationEntity existingConversation =
				new ConversationEntity(conversationId, "Original question", Instant.parse("2026-07-14T00:00:00Z"));
		when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(existingConversation));

		ChatResponse response = chatService.reply(new ChatRequest(conversationId, "Continue", null));

		assertThat(response.conversationId()).isEqualTo(conversationId);
		assertThat(existingConversation.getUpdatedAt()).isEqualTo(NOW);
		verify(conversationRepository).save(existingConversation);
		verify(messageRepository, times(2)).save(any(MessageEntity.class));
	}
}
