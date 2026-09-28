package org.hejinggang.elibrary.task;

import org.hejinggang.elibrary.common.enums.BorrowStatus;
import org.hejinggang.elibrary.model.entity.BorrowRecord;
import org.hejinggang.elibrary.repository.BorrowRecordRepository;
import org.hejinggang.elibrary.service.BorrowService;
import org.hejinggang.elibrary.service.NotificationService;
import org.hejinggang.elibrary.util.DistributedLockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BorrowTask {

    private static final Logger log = LoggerFactory.getLogger(BorrowTask.class);

    private static final String LOCK_KEY = "task:scan_expired_borrows";
    private static final long LOCK_EXPIRE_SECONDS = 300;

    private final BorrowRecordRepository borrowRecordRepository;
    private final BorrowService borrowService;
    private final NotificationService notificationService;
    private final DistributedLockUtil lockUtil;

    public BorrowTask(BorrowRecordRepository borrowRecordRepository,
                      BorrowService borrowService,
                      NotificationService notificationService,
                      DistributedLockUtil lockUtil) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.borrowService = borrowService;
        this.notificationService = notificationService;
        this.lockUtil = lockUtil;
    }

    @Scheduled(cron = "0 0/30 * * * ?")
    public void scanExpiredBorrows() {
        lockUtil.executeWithLock(LOCK_KEY, LOCK_EXPIRE_SECONDS, () -> {
            log.info("开始扫描到期借阅记录...");
            List<BorrowRecord> expiredRecords = borrowRecordRepository.findByStatusAndDueDateBefore(
                    BorrowStatus.ACTIVE, LocalDateTime.now());

            for (BorrowRecord record : expiredRecords) {
                try {
                    borrowService.returnBook(record.getId(), record.getUserId());
                    log.info("自动归还到期借阅: borrowId={}, userId={}, bookId={}",
                            record.getId(), record.getUserId(), record.getBookId());
                } catch (Exception e) {
                    log.error("自动归还到期借阅失败: borrowId={}", record.getId(), e);
                }
            }
            log.info("扫描到期借阅记录完成，处理{}条", expiredRecords.size());
        });
    }
}