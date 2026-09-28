package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.Book;
import org.hejinggang.elibrary.common.enums.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE b.status = :status " +
           "AND (:keyword IS NULL OR b.title LIKE %:keyword%) " +
           "AND (:author IS NULL OR b.author LIKE %:author%) " +
           "AND (:category IS NULL OR b.category = :category)")
    Page<Book> findByConditions(
            @Param("keyword") String keyword,
            @Param("author") String author,
            @Param("category") String category,
            @Param("status") BookStatus status,
            Pageable pageable);

    @Query("SELECT b FROM Book b WHERE (:status IS NULL OR b.status = :status) " +
           "AND (:keyword IS NULL OR b.title LIKE %:keyword%) " +
           "AND (:author IS NULL OR b.author LIKE %:author%) " +
           "AND (:category IS NULL OR b.category = :category)")
    Page<Book> findByConditionsForBos(
            @Param("keyword") String keyword,
            @Param("author") String author,
            @Param("category") String category,
            @Param("status") BookStatus status,
            Pageable pageable);

    Optional<Book> findByIdAndStatus(Long id, BookStatus status);
}