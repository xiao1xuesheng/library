package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.BookAccessConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookAccessConfigRepository extends JpaRepository<BookAccessConfig, Long> {

    Optional<BookAccessConfig> findByBookId(Long bookId);
}