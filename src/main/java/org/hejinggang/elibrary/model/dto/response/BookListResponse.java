package org.hejinggang.elibrary.model.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookListResponse {
    private Long total;
    private Integer page;
    private Integer size;
    private List<BookItem> items;

    @Data
    public static class BookItem {
        private Long id;
        private String title;
        private String author;
        private String category;
        private String coverUrl;
        private Double rating;
        private Integer reviewCount;
        private LocalDate publishedDate;
    }
}