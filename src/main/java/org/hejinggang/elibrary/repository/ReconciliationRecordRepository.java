package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.ReconciliationRecord;
import org.hejinggang.elibrary.common.enums.HandleStatus;
import org.hejinggang.elibrary.common.enums.MismatchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconciliationRecordRepository extends JpaRepository<ReconciliationRecord, Long> {

    List<ReconciliationRecord> findByReportId(Long reportId);

    List<ReconciliationRecord> findByReportIdAndHandleStatus(Long reportId, HandleStatus handleStatus);

    List<ReconciliationRecord> findByMismatchType(MismatchType mismatchType);

    List<ReconciliationRecord> findByHandleStatus(HandleStatus handleStatus);

    @Query("SELECT r FROM ReconciliationRecord r WHERE r.localOrderNo = :localOrderNo OR r.platformOrderNo = :platformOrderNo")
    List<ReconciliationRecord> findByOrderNo(@Param("localOrderNo") String localOrderNo, 
                                              @Param("platformOrderNo") String platformOrderNo);

    long countByReportIdAndMismatchType(Long reportId, MismatchType mismatchType);
}