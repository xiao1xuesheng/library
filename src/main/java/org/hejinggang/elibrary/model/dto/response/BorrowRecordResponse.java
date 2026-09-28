package org.hejinggang.elibrary.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BorrowRecordResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String coverUrl;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status;
    private Long remainingDays;
    private Integer renewCount;
    private java.math.BigDecimal totalCost;
}