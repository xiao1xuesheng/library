package org.hejinggang.elibrary.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_reading_history")
public class ReadingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "page", nullable = false)
    private Integer page = 1;

    @Column(name = "chapter", length = 128)
    private String chapter;

    @Column(name = "scroll_position")
    private Integer scrollPosition;

    @Column(name = "read_duration", nullable = false)
    private Integer readDuration = 0;

    @Column(name = "total_read_duration", nullable = false)
    private Integer totalReadDuration = 0;

    @Column(name = "device_type", length = 32)
    private String deviceType;

    @Column(name = "user_agent", length = 256)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}