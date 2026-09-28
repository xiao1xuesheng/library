package org.hejinggang.elibrary.model.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookDetailResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private String description;
    private Integer pageCount;
    private String coverUrl;
    private Double rating;
    private Integer reviewCount;
    private LocalDate publishedDate;
    private String userPermission;
}