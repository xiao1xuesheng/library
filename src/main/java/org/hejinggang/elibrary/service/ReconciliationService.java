package org.hejinggang.elibrary.service;

import org.hejinggang.elibrary.common.enums.*;
import org.hejinggang.elibrary.model.entity.*;
import org.hejinggang.elibrary.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 对账服务
 * 负责比对本地订单与支付平台记录，生成对账报告和对账记录
 */
@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final OrderRepository orderRepository;
    private final PlatformBillRecordRepository platformBillRecordRepository;
    private final ReconciliationRecordRepository reconciliationRecordRepository;
    private final ReconciliationReportRepository reconciliationReportRepository;

    public ReconciliationService(OrderRepository orderRepository,
                                 PlatformBillRecordRepository platformBillRecordRepository,
                                 ReconciliationRecordRepository reconciliationRecordRepository,
                                 ReconciliationReportRepository reconciliationReportRepository) {
        this.orderRepository = orderRepository;
        this.platformBillRecordRepository = platformBillRecordRepository;
        this.reconciliationRecordRepository = reconciliationRecordRepository;
        this.reconciliationReportRepository = reconciliationReportRepository;
    }

    /**
     * 执行对账
     * @param billDate 对账日期
     * @return 对账报告ID
     */
    @Transactional
    public Long reconcile(LocalDate billDate) {
        log.info("开始对账: date={}", billDate);

        // 检查是否已经对账
        if (reconciliationReportRepository.existsByReportDate(billDate)) {
            log.warn("该日期已对账: date={}", billDate);
            return reconciliationReportRepository.findByReportDate(billDate).get().getId();
        }

        // 创建对账报告
        ReconciliationReport report = new ReconciliationReport();
        report.setReportDate(billDate);
        report = reconciliationReportRepository.save(report);

        List<Order> localOrders = orderRepository.findByPayTimeBetween(
                billDate.atStartOfDay(), billDate.plusDays(1).atStartOfDay());
        List<PlatformBillRecord> platformRecords = platformBillRecordRepository.findByBillDate(billDate);

        int localCount = localOrders.size();
        int platformCount = platformRecords.size();
        int matchedCount = 0;
        int mismatchCount = 0;
        BigDecimal mismatchAmount = BigDecimal.ZERO;
        List<String> mismatchTypes = new ArrayList<>();

        for (Order localOrder : localOrders) {
            if (localOrder.getPayOrderNo() == null) {
                continue;
            }

            PlatformBillRecord platformRecord = platformBillRecordRepository
                    .findByPlatformOrderNo(localOrder.getPayOrderNo());

            if (platformRecord == null) {
                mismatchCount++;
                mismatchAmount = mismatchAmount.add(localOrder.getAmount());
                if (!mismatchTypes.contains(MismatchType.LOCAL_EXTRA.name())) {
                    mismatchTypes.add(MismatchType.LOCAL_EXTRA.name());
                }

                ReconciliationRecord record = new ReconciliationRecord();
                record.setReportId(report.getId());
                record.setLocalOrderNo(localOrder.getOrderNo());
                record.setPayChannel(PayChannel.valueOf(localOrder.getPayChannel().toUpperCase()));
                record.setLocalAmount(localOrder.getAmount());
                record.setLocalStatus(localOrder.getStatus());
                record.setLocalPaidAt(localOrder.getPayTime());
                record.setMismatchType(MismatchType.LOCAL_EXTRA);
                reconciliationRecordRepository.save(record);
            } else {
                if (localOrder.getAmount().compareTo(platformRecord.getAmount()) != 0) {
                    mismatchCount++;
                    mismatchAmount = mismatchAmount.add(
                            localOrder.getAmount().subtract(platformRecord.getAmount()).abs());
                    if (!mismatchTypes.contains(MismatchType.AMOUNT_MISMATCH.name())) {
                        mismatchTypes.add(MismatchType.AMOUNT_MISMATCH.name());
                    }

                    ReconciliationRecord record = new ReconciliationRecord();
                    record.setReportId(report.getId());
                    record.setLocalOrderNo(localOrder.getOrderNo());
                    record.setPlatformOrderNo(platformRecord.getPlatformOrderNo());
                    record.setPayChannel(platformRecord.getPayChannel());
                    record.setLocalAmount(localOrder.getAmount());
                    record.setPlatformAmount(platformRecord.getAmount());
                    record.setLocalStatus(localOrder.getStatus());
                    record.setPlatformStatus(platformRecord.getStatus());
                    record.setLocalPaidAt(localOrder.getPayTime());
                    record.setPlatformPaidAt(platformRecord.getPaidAt());
                    record.setMismatchType(MismatchType.AMOUNT_MISMATCH);
                    reconciliationRecordRepository.save(record);
                } else {
                    matchedCount++;
                }
            }
        }

        for (PlatformBillRecord platformRecord : platformRecords) {
            Order localOrder = orderRepository.findByPayOrderNo(platformRecord.getPlatformOrderNo())
                    .orElse(null);

            if (localOrder == null) {
                mismatchCount++;
                mismatchAmount = mismatchAmount.add(platformRecord.getAmount());
                if (!mismatchTypes.contains(MismatchType.PLATFORM_EXTRA.name())) {
                    mismatchTypes.add(MismatchType.PLATFORM_EXTRA.name());
                }

                ReconciliationRecord record = new ReconciliationRecord();
                record.setReportId(report.getId());
                record.setPlatformOrderNo(platformRecord.getPlatformOrderNo());
                record.setPayChannel(platformRecord.getPayChannel());
                record.setPlatformAmount(platformRecord.getAmount());
                record.setPlatformStatus(platformRecord.getStatus());
                record.setPlatformPaidAt(platformRecord.getPaidAt());
                record.setMismatchType(MismatchType.PLATFORM_EXTRA);
                reconciliationRecordRepository.save(record);
            }
        }

        // 更新对账报告
        report.setLocalOrderCount(localCount);
        report.setPlatformOrderCount(platformCount);
        report.setMatchedCount(matchedCount);
        report.setMismatchCount(mismatchCount);
        report.setMismatchAmount(mismatchAmount);
        report.setMismatchTypes(String.join(",", mismatchTypes));
        report.setStatus(mismatchCount > 0 ? "MISMATCH" : "RESOLVED");
        reconciliationReportRepository.save(report);

        log.info("对账完成: date={}, reportId={}, local={}, platform={}, matched={}, mismatch={}, amount={}",
                billDate, report.getId(), localCount, platformCount, matchedCount, mismatchCount, mismatchAmount);

        return report.getId();
    }
}