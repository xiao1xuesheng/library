package org.hejinggang.elibrary.task;

import org.hejinggang.elibrary.common.enums.BorrowStatus;
import org.hejinggang.elibrary.model.entity.Account;
import org.hejinggang.elibrary.model.entity.BookAccessConfig;
import org.hejinggang.elibrary.model.entity.BorrowRecord;
import org.hejinggang.elibrary.repository.AccountRepository;
import org.hejinggang.elibrary.repository.BookAccessConfigRepository;
import org.hejinggang.elibrary.repository.BorrowRecordRepository;
import org.hejinggang.elibrary.service.BorrowService;
import org.hejinggang.elibrary.service.NotificationService;
import org.hejinggang.elibrary.util.DistributedLockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class BalanceCheckTask {

    private static final Logger log = LoggerFactory.getLogger(BalanceCheckTask.class);

    private static final String LOCK_KEY = "task:scan_insufficient_balance";
    private static final long LOCK_EXPIRE_SECONDS = 300;
    private static final long ALERT_SILENCE_HOURS = 24;

    private final BorrowRecordRepository borrowRecordRepository;
    private final AccountRepository accountRepository;
    private final BookAccessConfigRepository accessConfigRepository;
    private final BorrowService borrowService;
    private final NotificationService notificationService;
    private final DistributedLockUtil lockUtil;
    
    // 开发环境使用内存Map模拟Redis告警静默
    private final Map<String, Long> alertSilenceMap = new ConcurrentHashMap<>();

    public BalanceCheckTask(BorrowRecordRepository borrowRecordRepository,
                            AccountRepository accountRepository,
                            BookAccessConfigRepository accessConfigRepository,
                            BorrowService borrowService,
                            NotificationService notificationService,
                            DistributedLockUtil lockUtil) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.accountRepository = accountRepository;
        this.accessConfigRepository = accessConfigRepository;
        this.borrowService = borrowService;
        this.notificationService = notificationService;
        this.lockUtil = lockUtil;
    }

    @Scheduled(cron = "0 0/30 * * * ?")
    public void scanInsufficientBalance() {
        lockUtil.executeWithLock(LOCK_KEY, LOCK_EXPIRE_SECONDS, () -> {
            log.info("开始扫描余额不足用户...");
            List<Long> userIdsWithActiveBorrows = borrowRecordRepository.findDistinctUserIdsByStatus(BorrowStatus.ACTIVE);

            for (Long userId : userIdsWithActiveBorrows) {
                try {
                    Account account = accountRepository.findByUserId(userId).orElse(null);
                    if (account == null || account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                        List<BorrowRecord> activeBorrows = borrowRecordRepository.findByUserIdAndStatus(userId, BorrowStatus.ACTIVE);
                        if (!activeBorrows.isEmpty()) {
                            boolean hasPaidBorrow = false;
                            for (BorrowRecord record : activeBorrows) {
                                BookAccessConfig config = accessConfigRepository.findByBookId(record.getBookId()).orElse(null);
                                if (config != null && !config.getAccessType().endsWith("FREE")) {
                                    hasPaidBorrow = true;
                                    break;
                                }
                            }
                            if (hasPaidBorrow) {
                                forceReturnUserBooks(userId, activeBorrows);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("扫描余额不足用户失败: userId={}", userId, e);
                }
            }
            log.info("扫描余额不足用户完成");
        });
    }

    private void forceReturnUserBooks(Long userId, List<BorrowRecord> activeBorrows) {
        String alertKey = "alert:force_return:" + userId;
        Long expireTime = alertSilenceMap.get(alertKey);
        if (expireTime != null && expireTime > System.currentTimeMillis()) {
            log.info("告警静默中，跳过重复通知: userId={}", userId);
            return;
        }

        log.info("强制归还用户书籍: userId={}, 借阅数={}", userId, activeBorrows.size());
        for (BorrowRecord record : activeBorrows) {
            try {
                borrowService.returnBook(record.getId(), userId);
                log.info("强制归还成功: borrowId={}, bookId={}", record.getId(), record.getBookId());
            } catch (Exception e) {
                log.error("强制归还失败: borrowId={}", record.getId(), e);
            }
        }

        notificationService.sendForceReturnNotification(userId,
                "您的账户余额不足，系统已强制归还所有借阅书籍，请及时充值");

        // 设置告警静默时间（24小时）
        long silenceExpireAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(ALERT_SILENCE_HOURS);
        alertSilenceMap.put(alertKey, silenceExpireAt);
        log.info("设置告警静默: userId={}, 静默至={}", userId, silenceExpireAt);
    }
}