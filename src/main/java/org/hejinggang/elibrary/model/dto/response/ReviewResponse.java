package org.hejinggang.elibrary.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private Integer pageReference;
    private Integer rating;
    private LocalDateTime createdAt;
}