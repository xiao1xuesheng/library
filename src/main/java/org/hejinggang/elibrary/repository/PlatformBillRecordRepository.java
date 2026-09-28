package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.PlatformBillRecord;
import org.hejinggang.elibrary.common.enums.PayChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlatformBillRecordRepository extends JpaRepository<PlatformBillRecord, Long> {

    List<PlatformBillRecord> findByUploadId(String uploadId);

    List<PlatformBillRecord> findByPayChannelAndBillDate(PayChannel payChannel, LocalDate billDate);

    List<PlatformBillRecord> findByBillDate(LocalDate billDate);

    PlatformBillRecord findByPlatformOrderNo(String platformOrderNo);

    PlatformBillRecord findByLocalOrderNo(String localOrderNo);

    long countByBillDate(LocalDate billDate);
}