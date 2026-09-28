package org.hejinggang.elibrary.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequest {
    @NotNull(message = "userId cannot be null")
    private Long userId;

    @NotNull(message = "bookId cannot be null")
    private Long bookId;

    private String requestId;
}