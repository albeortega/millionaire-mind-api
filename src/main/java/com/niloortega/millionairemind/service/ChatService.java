package com.niloortega.millionairemind.service;

import com.niloortega.millionairemind.ai.GeminiClient;
import com.niloortega.millionairemind.dto.ChatRequest;
import com.niloortega.millionairemind.dto.ChatResponse;
import com.niloortega.millionairemind.dto.ChatSource;
import com.niloortega.millionairemind.entity.ConversationEntity;
import com.niloortega.millionairemind.entity.MessageEntity;
import com.niloortega.millionairemind.entity.MessageRole;
import com.niloortega.millionairemind.repository.BookChunkRepository;
import com.niloortega.millionairemind.repository.BookChunkSearchResult;
import com.niloortega.millionairemind.repository.ConversationRepository;
import com.niloortega.millionairemind.repository.MessageRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

	private static final int MAX_TITLE_LENGTH = 80;
	private static final int MAX_SOURCES = 5;
	private static final String NO_DATA_REPLY =
			"I do not have enough book content loaded yet to answer that from Jewels of the Millionaire Mind.";

	private final GeminiClient geminiClient;
	private final BookChunkRepository bookChunkRepository;
	private final ConversationRepository conversationRepository;
	private final MessageRepository messageRepository;
	private final Clock clock;

	@Autowired
	public ChatService(
			GeminiClient geminiClient,
			BookChunkRepository bookChunkRepository,
			ConversationRepository conversationRepository,
			MessageRepository messageRepository) {
		this(geminiClient, bookChunkRepository, conversationRepository, messageRepository, Clock.systemUTC());
	}

	ChatService(
			GeminiClient geminiClient,
			BookChunkRepository bookChunkRepository,
			ConversationRepository conversationRepository,
			MessageRepository messageRepository,
			Clock clock) {
		this.geminiClient = geminiClient;
		this.bookChunkRepository = bookChunkRepository;
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
		List<BookChunkSearchResult> matchingChunks = searchChunks(prompt);
		String reply = generateReply(prompt, matchingChunks);
		List<ChatSource> sources = matchingChunks.stream()
				.map(chunk -> new ChatSource(
						chunk.getBookTitle() + " - chunk " + chunk.getChunkIndex(),
						excerpt(chunk.getContent()),
						chunk.getScore()))
				.toList();
		MessageEntity assistantMessage =
				messageRepository.save(new MessageEntity(conversation, MessageRole.ASSISTANT, reply, createdAt));

		return new ChatResponse(
				conversation.getId(),
				"ASSISTANT",
				assistantMessage.getContent(),
				sources,
				assistantMessage.getCreatedAt());
	}

	private List<BookChunkSearchResult> searchChunks(String prompt) {
		Map<Integer, BookChunkSearchResult> mergedResults = new LinkedHashMap<>();
		if (isDecisionQuestion(prompt)) {
			for (BookChunkSearchResult chunk : bookChunkRepository.findDecisionGuidanceChunks(MAX_SOURCES)) {
				mergedResults.put(chunk.getChunkIndex(), chunk);
			}
		}

		List<BookChunkSearchResult> matches = bookChunkRepository.searchByText(prompt, MAX_SOURCES);
		if (!matches.isEmpty()) {
			for (BookChunkSearchResult chunk : matches) {
				mergedResults.putIfAbsent(chunk.getChunkIndex(), chunk);
			}
			return mergedResults.values().stream().limit(MAX_SOURCES).toList();
		}

		if (!mergedResults.isEmpty()) {
			return mergedResults.values().stream().limit(MAX_SOURCES).toList();
		}

		return bookChunkRepository.findFirstChunks(MAX_SOURCES);
	}

	private boolean isDecisionQuestion(String prompt) {
		String normalized = prompt.toLowerCase(Locale.ROOT);
		return normalized.contains("choice")
				|| normalized.contains("choose")
				|| normalized.contains("decide")
				|| normalized.contains("decision")
				|| normalized.contains("select")
				|| normalized.contains("best");
	}

	private String generateReply(String prompt, List<BookChunkSearchResult> chunks) {
		if (chunks.isEmpty()) {
			return NO_DATA_REPLY;
		}

		List<String> contextChunks = chunks.stream()
				.map(BookChunkSearchResult::getContent)
				.toList();

		String localReply = composeReply(chunks);
		return geminiClient.generateAnswer(prompt, contextChunks)
				.filter(this::hasUsefulGeminiAnswer)
				.orElse(localReply);
	}

	private boolean hasUsefulGeminiAnswer(String answer) {
		String normalized = answer.toLowerCase(Locale.ROOT);
		return !normalized.contains("do not have enough book content")
				&& !normalized.contains("don't have enough book content")
				&& !normalized.contains("does not offer a specific")
				&& !normalized.contains("doesn't offer a specific");
	}

	private String composeReply(List<BookChunkSearchResult> chunks) {
		if (chunks.isEmpty()) {
			return NO_DATA_REPLY;
		}

		List<String> guidance = new ArrayList<>();
		for (BookChunkSearchResult chunk : chunks) {
			addGuidanceFromChunk(guidance, chunk.getContent());
		}

		if (guidance.isEmpty()) {
			guidance.add("Pause before deciding and ask what outcome you truly want.");
			guidance.add("Choose the option that brings you closer to who you want to become.");
			guidance.add("Notice whether you are choosing comfort or growth.");
		}

		return "Based on the saved Jewel #1 content, the best choice is not the easiest one; it is the one aligned "
				+ "with who you want to become. " + String.join(" ", guidance);
	}

	private void addGuidanceFromChunk(List<String> guidance, String content) {
		String lowerContent = content.toLowerCase();
		if (lowerContent.contains("does this move me closer") && guidance.size() < 3) {
			guidance.add("Ask: does this move me closer to who I want to become?");
		}
		if (lowerContent.contains("will i be proud") && guidance.size() < 3) {
			guidance.add("Ask: will I be proud of this tomorrow?");
		}
		if (lowerContent.contains("comfort or growth") && guidance.size() < 3) {
			guidance.add("Ask: am I choosing comfort or growth?");
		}
		if (lowerContent.contains("future self") && guidance.size() < 3) {
			guidance.add("Ask what your future self would choose today.");
		}
		if (lowerContent.contains("values") && guidance.size() < 3) {
			guidance.add("Let your values guide you more than outside pressure.");
		}
		if (lowerContent.contains("regret") && guidance.size() < 3) {
			guidance.add("Remember that loneliness or discomfort can be cheaper than regret.");
		}
	}

	private String excerpt(String content) {
		String normalized = content.replaceAll("\\s+", " ").trim();
		if (normalized.length() <= 240) {
			return normalized;
		}

		return normalized.substring(0, 240).trim() + "...";
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
