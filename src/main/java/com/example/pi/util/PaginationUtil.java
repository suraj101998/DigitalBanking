package com.example.pi.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.pi.dto.response.PaginationResponse;

public class PaginationUtil {

	private PaginationUtil() {
		// Utility class - no instantiation
	}

	/**
	 * Converts Spring Data Page to custom PaginationResponse
	 */
	public static <T> PaginationResponse<T> buildPaginationResponse(Page<T> page) {
		return new PaginationResponse<>(
			page.getContent(),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.isFirst(),
			page.isLast()
		);
	}

	/**
	 * Builds pagination response from list and metadata
	 */
	public static <T> PaginationResponse<T> buildPaginationResponse(
			java.util.List<T> content, Pageable pageable, long totalElements) {
		int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
		boolean isFirst = pageable.getPageNumber() == 0;
		boolean isLast = totalElements > 0 && pageable.getPageNumber() == totalPages - 1;

		return new PaginationResponse<>(
			content,
			pageable.getPageNumber(),
			pageable.getPageSize(),
			totalElements,
			totalPages,
			isFirst,
			isLast
		);
	}

	/**
	 * Calculates query offset from page number and size
	 */
	public static int calculateOffset(int pageNumber, int pageSize) {
		return pageNumber * pageSize;
	}

	/**
	 * Returns empty pagination response for error scenarios
	 */
	public static <T> PaginationResponse<T> emptyResponse() {
		return new PaginationResponse<>(
			java.util.Collections.emptyList(),
			0,
			0,
			0,
			0,
			true,
			true
		);
	}
}
