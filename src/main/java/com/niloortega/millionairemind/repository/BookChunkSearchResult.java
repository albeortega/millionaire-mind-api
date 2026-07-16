package com.niloortega.millionairemind.repository;

public interface BookChunkSearchResult {

	String getBookTitle();

	Integer getChunkIndex();

	String getContent();

	Double getScore();
}
