package com.niloortega.millionairemind.repository;

import com.niloortega.millionairemind.entity.BookChunkEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookChunkRepository extends JpaRepository<BookChunkEntity, UUID> {

	@Query(value = """
			SELECT b.title AS bookTitle,
			       bc.chunk_index AS chunkIndex,
			       bc.content AS content,
			       ts_rank_cd(to_tsvector('english', bc.content), websearch_to_tsquery('english', :query)) AS score
			FROM book_chunks bc
			JOIN books b ON b.id = bc.book_id
			WHERE websearch_to_tsquery('english', :query) @@ to_tsvector('english', bc.content)
			ORDER BY score DESC, bc.chunk_index ASC
			LIMIT :limit
			""", nativeQuery = true)
	List<BookChunkSearchResult> searchByText(@Param("query") String query, @Param("limit") int limit);

	@Query(value = """
			SELECT b.title AS bookTitle,
			       bc.chunk_index AS chunkIndex,
			       bc.content AS content,
			       0.0 AS score
			FROM book_chunks bc
			JOIN books b ON b.id = bc.book_id
			ORDER BY bc.chunk_index ASC
			LIMIT :limit
			""", nativeQuery = true)
	List<BookChunkSearchResult> findFirstChunks(@Param("limit") int limit);
}
