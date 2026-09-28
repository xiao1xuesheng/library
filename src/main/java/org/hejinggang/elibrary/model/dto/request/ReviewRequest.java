package org.hejinggang.elibrary.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull(message = "userId cannot be null")
    private Long userId;

    @NotBlank(message = "content cannot be empty")
    private String content;

    private Integer pageReference;
    private Integer rating;
    private String requestId;
}