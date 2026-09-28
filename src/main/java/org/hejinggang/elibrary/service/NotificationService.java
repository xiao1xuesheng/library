package org.hejinggang.elibrary.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendBalanceAlert(Long userId, String message) {
        log.info("发送余额不足告警: userId={}, message={}", userId, message);
    }

    public void sendVipExpiryReminder(Long userId, String message) {
        log.info("发送VIP到期提醒: userId={}, message={}", userId, message);
    }

    public void sendForceReturnNotification(Long userId, String message) {
        log.info("发送强制归还通知: userId={}, message={}", userId, message);
    }

    public void sendSystemAlert(String level, String message) {
        log.info("发送系统告警: level={}, message={}", level, message);
    }
}