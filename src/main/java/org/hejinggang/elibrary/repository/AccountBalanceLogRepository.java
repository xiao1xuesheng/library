package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.AccountBalanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountBalanceLogRepository extends JpaRepository<AccountBalanceLog, Long> {

    Page<AccountBalanceLog> findByUserId(Long userId, Pageable pageable);

    Page<AccountBalanceLog> findByUserIdAndLogType(Long userId, String logType, Pageable pageable);
}