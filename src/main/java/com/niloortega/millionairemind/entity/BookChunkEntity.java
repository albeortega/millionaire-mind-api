package com.niloortega.millionairemind.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "book_chunks")
public class BookChunkEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "book_id", nullable = false)
	private BookEntity book;

	@Column(name = "chunk_index", nullable = false)
	private Integer chunkIndex;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(name = "token_count")
	private Integer tokenCount;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected BookChunkEntity() {
	}

	public UUID getId() {
		return id;
	}

	public BookEntity getBook() {
		return book;
	}

	public Integer getChunkIndex() {
		return chunkIndex;
	}

	public String getContent() {
		return content;
	}

	public Integer getTokenCount() {
		return tokenCount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
