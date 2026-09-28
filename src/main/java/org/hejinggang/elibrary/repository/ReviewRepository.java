package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByBookIdAndIsDeletedFalse(Long bookId, Pageable pageable);

    Page<Review> findByIsDeletedFalse(Pageable pageable);

    long countByBookIdAndIsDeletedFalse(Long bookId);
}