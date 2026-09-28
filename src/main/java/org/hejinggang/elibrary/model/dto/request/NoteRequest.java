package org.hejinggang.elibrary.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoteRequest {
    @NotNull(message = "userId cannot be null")
    private Long userId;

    @NotNull(message = "page cannot be null")
    private Integer page;

    @NotBlank(message = "content cannot be empty")
    private String content;
}