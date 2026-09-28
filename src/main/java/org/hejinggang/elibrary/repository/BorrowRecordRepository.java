package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.BorrowRecord;
import org.hejinggang.elibrary.common.enums.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByUserIdAndStatus(Long userId, BorrowStatus status);

    long countByUserIdAndStatus(Long userId, BorrowStatus status);

    Optional<BorrowRecord> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, BorrowStatus status);

    Optional<BorrowRecord> findByIdAndUserId(Long id, Long userId);

    List<BorrowRecord> findByStatusAndDueDateBefore(BorrowStatus status, LocalDateTime dueDate);

    @Query("SELECT DISTINCT br.userId FROM BorrowRecord br WHERE br.status = :status")
    List<Long> findDistinctUserIdsByStatus(@Param("status") BorrowStatus status);

    long countByBookIdAndBorrowDateAfter(Long bookId, java.time.LocalDateTime borrowDateAfter);

    long countByBorrowDateAfter(java.time.LocalDateTime borrowDateAfter);
}