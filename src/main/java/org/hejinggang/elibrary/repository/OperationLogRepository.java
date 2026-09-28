package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    Page<OperationLog> findByOperatorId(Long operatorId, Pageable pageable);

    Page<OperationLog> findByOperatorType(String operatorType, Pageable pageable);

    Page<OperationLog> findByOperationType(String operationType, Pageable pageable);
}