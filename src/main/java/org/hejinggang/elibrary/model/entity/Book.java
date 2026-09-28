package org.hejinggang.elibrary.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hejinggang.elibrary.common.enums.BookStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(nullable = false, length = 128)
    private String author;

    @Column(nullable = false, unique = true, length = 32)
    private String isbn;

    @Column(length = 64)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "page_count", nullable = false)
    private Integer pageCount = 0;

    @Column(name = "cover_url", length = 512)
    private String coverUrl;

    @Column(name = "content_url", length = 512)
    private String contentUrl;

    @Column(name = "preview_url", length = 512)
    private String previewUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status = BookStatus.ON_SHELF;

    private Double rating = 0.0;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "borrow_count", nullable = false)
    private Integer borrowCount = 0;

    @Column(name = "hot_score", nullable = false)
    private Integer hotScore = 0;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}