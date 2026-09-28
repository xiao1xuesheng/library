package org.hejinggang.elibrary.service;

import lombok.Data;

@Data
public class ReadingProgressResponse {
    private Long userId;
    private Long bookId;
    private Integer page;
    private String chapter;
    private Integer scrollPosition;
    private java.time.LocalDateTime updatedAt;
}