package com.niloortega.millionairemind.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "books")
public class BookEntity {

	@Id
	private UUID id;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(nullable = false, length = 255)
	private String author;

	@Column(name = "source_filename", length = 512)
	private String sourceFilename;

	@Column(name = "content_sha256", length = 64)
	private String contentSha256;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected BookEntity() {
	}

	public UUID getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	public String getSourceFilename() {
		return sourceFilename;
	}

	public String getContentSha256() {
		return contentSha256;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
