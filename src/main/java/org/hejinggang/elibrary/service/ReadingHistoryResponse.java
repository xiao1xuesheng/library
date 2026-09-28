package org.hejinggang.elibrary.service;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingHistoryResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookCoverUrl;
    private Integer lastPage;
    private String lastChapter;
    private Integer totalReadDuration;
    private LocalDateTime lastReadAt;
    private Double progress;
}