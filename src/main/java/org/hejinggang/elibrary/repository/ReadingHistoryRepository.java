package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.ReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {

    List<ReadingHistory> findByUserIdAndBookIdOrderByCreatedAtDesc(Long userId, Long bookId);

    Optional<ReadingHistory> findFirstByUserIdAndBookIdOrderByCreatedAtDesc(Long userId, Long bookId);

    @Query("SELECT rh FROM ReadingHistory rh WHERE rh.id IN " +
           "(SELECT MAX(rh2.id) FROM ReadingHistory rh2 WHERE rh2.userId = :userId GROUP BY rh2.bookId)")
    Page<ReadingHistory> findLatestByUserId(@Param("userId") Long userId, Pageable pageable);

    List<ReadingHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}