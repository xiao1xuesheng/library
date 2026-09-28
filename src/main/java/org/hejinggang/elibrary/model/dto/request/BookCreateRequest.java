package org.hejinggang.elibrary.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookCreateRequest {
    @NotBlank(message = "title cannot be empty")
    private String title;

    @NotBlank(message = "author cannot be empty")
    private String author;

    @NotBlank(message = "isbn cannot be empty")
    private String isbn;

    private String category;
    private String description;

    @NotNull(message = "pageCount cannot be null")
    private Integer pageCount;

    private String coverUrl;
    private String contentUrl;
    private String previewUrl;
    private LocalDate publishedDate;
}