package com.niloortega.millionairemind.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.niloortega.millionairemind.ai.GeminiClient;
import com.niloortega.millionairemind.dto.ChatRequest;
import com.niloortega.millionairemind.dto.ChatResponse;
import com.niloortega.millionairemind.entity.ConversationEntity;
import com.niloortega.millionairemind.entity.MessageEntity;
import com.niloortega.millionairemind.entity.MessageRole;
import com.niloortega.millionairemind.repository.BookChunkRepository;
import com.niloortega.millionairemind.repository.BookChunkSearchResult;
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
	private BookChunkRepository bookChunkRepository;

	@Mock
	private GeminiClient geminiClient;

	@Mock
	private ConversationRepository conversationRepository;

	@Mock
	private MessageRepository messageRepository;

	private ChatService chatService;

	@BeforeEach
	void setUp() {
		chatService = new ChatService(geminiClient, bookChunkRepository, conversationRepository, messageRepository, CLOCK);
		when(conversationRepository.save(any(ConversationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(messageRepository.save(any(MessageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void createsConversationAndStoresUserAndAssistantMessages() {
		ChatRequest request = new ChatRequest(null, "  How should I think about assets?  ", null);
		when(bookChunkRepository.searchByText("How should I think about assets?", 5))
				.thenReturn(List.of(searchResult(
						"Jewels of the Millionaire Mind",
						8,
						"Before you decide, pause and ask: Does this move me closer to who I want to become? Will I be proud of this tomorrow? Am I choosing comfort or growth?",
						0.75)));
		when(geminiClient.generateAnswer(eq("How should I think about assets?"), any()))
				.thenReturn(Optional.of("Choose the path that aligns with who you want to become, not just what feels comfortable today."));

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
		assertThat(savedMessages.get(1).getContent())
				.isEqualTo("Choose the path that aligns with who you want to become, not just what feels comfortable today.");
		assertThat(response.message()).isEqualTo(savedMessages.get(1).getContent());
		assertThat(response.sources()).hasSize(1);
		assertThat(response.sources().get(0).title()).isEqualTo("Jewels of the Millionaire Mind - chunk 8");
		assertThat(response.createdAt()).isEqualTo(NOW);
	}

	@Test
	void appendsMessagesToExistingConversation() {
		UUID conversationId = UUID.fromString("8c71ca7b-edb3-4792-91d4-1de3a9968475");
		ConversationEntity existingConversation =
				new ConversationEntity(conversationId, "Original question", Instant.parse("2026-07-14T00:00:00Z"));
		when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(existingConversation));
		when(bookChunkRepository.searchByText("Continue", 5)).thenReturn(List.of());
		when(bookChunkRepository.findFirstChunks(5)).thenReturn(List.of());

		ChatResponse response = chatService.reply(new ChatRequest(conversationId, "Continue", null));

		assertThat(response.conversationId()).isEqualTo(conversationId);
		assertThat(existingConversation.getUpdatedAt()).isEqualTo(NOW);
		verify(conversationRepository).save(existingConversation);
		verify(messageRepository, times(2)).save(any(MessageEntity.class));
	}

	@Test
	void usesLocalChunkReplyWhenGeminiDoesNotReturnAnswer() {
		when(bookChunkRepository.findDecisionGuidanceChunks(5))
				.thenReturn(List.of(searchResult(
						"Jewels of the Millionaire Mind",
						8,
						"Before you decide, pause and ask: Does this move me closer to who I want to become? Will I be proud of this tomorrow? Am I choosing comfort or growth?",
						0.75)));
		when(bookChunkRepository.searchByText("How can I know I selected the best choice?", 5))
				.thenReturn(List.of());
		when(geminiClient.generateAnswer(eq("How can I know I selected the best choice?"), any()))
				.thenReturn(Optional.empty());

		ChatResponse response = chatService.reply(new ChatRequest(null, "How can I know I selected the best choice?", null));

		assertThat(response.message()).contains("Based on the saved Jewel #1 content");
		assertThat(response.message()).contains("does this move me closer to who I want to become");
		assertThat(response.sources()).hasSize(1);
	}

	@Test
	void usesLocalChunkReplyWhenGeminiClaimsFoundGuidanceIsInsufficient() {
		when(bookChunkRepository.findDecisionGuidanceChunks(5))
				.thenReturn(List.of(searchResult(
						"Jewels of the Millionaire Mind",
						8,
						"Before you decide, pause and ask: Does this move me closer to who I want to become? Will I be proud of this tomorrow? Am I choosing comfort or growth?",
						1.0)));
		when(bookChunkRepository.searchByText("How can I know that I select the best choice?", 5))
				.thenReturn(List.of());
		when(geminiClient.generateAnswer(eq("How can I know that I select the best choice?"), any()))
				.thenReturn(Optional.of("The provided text does not offer a specific step-by-step guide. I do not have enough book content yet."));

		ChatResponse response = chatService.reply(new ChatRequest(null, "How can I know that I select the best choice?", null));

		assertThat(response.message()).contains("Based on the saved Jewel #1 content");
		assertThat(response.message()).contains("will I be proud of this tomorrow?");
		assertThat(response.sources()).hasSize(1);
	}

	@Test
	void returnsNoDataReplyWhenNoChunksExist() {
		when(bookChunkRepository.findDecisionGuidanceChunks(5)).thenReturn(List.of());
		when(bookChunkRepository.searchByText("What should I choose?", 5)).thenReturn(List.of());
		when(bookChunkRepository.findFirstChunks(5)).thenReturn(List.of());

		ChatResponse response = chatService.reply(new ChatRequest(null, "What should I choose?", null));

		assertThat(response.message()).isEqualTo(NO_DATA_REPLY);
		assertThat(response.sources()).isEmpty();
	}

	private BookChunkSearchResult searchResult(String title, Integer chunkIndex, String content, Double score) {
		return new BookChunkSearchResult() {
			@Override
			public String getBookTitle() {
				return title;
			}

			@Override
			public Integer getChunkIndex() {
				return chunkIndex;
			}

			@Override
			public String getContent() {
				return content;
			}

			@Override
			public Double getScore() {
				return score;
			}
		};
	}
}
