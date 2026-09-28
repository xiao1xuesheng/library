package org.hejinggang.elibrary.task;

import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.model.entity.User;
import org.hejinggang.elibrary.repository.UserRepository;
import org.hejinggang.elibrary.service.NotificationService;
import org.hejinggang.elibrary.util.DistributedLockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class VipTask {

    private static final Logger log = LoggerFactory.getLogger(VipTask.class);

    private static final String LOCK_KEY_EXPIRED = "task:scan_expired_vip";
    private static final String LOCK_KEY_REMINDER = "task:send_vip_reminder";
    private static final long LOCK_EXPIRE_SECONDS = 300;

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final DistributedLockUtil lockUtil;

    public VipTask(UserRepository userRepository, 
                   NotificationService notificationService,
                   DistributedLockUtil lockUtil) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.lockUtil = lockUtil;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scanExpiredVip() {
        lockUtil.executeWithLock(LOCK_KEY_EXPIRED, LOCK_EXPIRE_SECONDS, () -> {
            log.info("开始扫描到期VIP用户...");
            List<User> expiredVips = userRepository.findByUserTypeAndVipExpireAtBefore(
                    UserType.VIP, LocalDateTime.now());

            for (User user : expiredVips) {
                try {
                    user.setUserType(UserType.NORMAL);
                    userRepository.save(user);
                    log.info("VIP到期降级: userId={}, username={}", user.getId(), user.getUsername());
                } catch (Exception e) {
                    log.error("VIP到期降级失败: userId={}", user.getId(), e);
                }
            }
            log.info("扫描到期VIP用户完成，处理{}条", expiredVips.size());
        });
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void sendVipExpiryReminders() {
        lockUtil.executeWithLock(LOCK_KEY_REMINDER, LOCK_EXPIRE_SECONDS, () -> {
            log.info("开始发送VIP到期提醒...");
            LocalDateTime sevenDaysLater = LocalDateTime.now().plusDays(7);
            List<User> expiringVips = userRepository.findByUserTypeAndVipExpireAtBetween(
                    UserType.VIP, LocalDateTime.now(), sevenDaysLater);

            for (User user : expiringVips) {
                try {
                    notificationService.sendVipExpiryReminder(user.getId(),
                            "您的VIP将在" + user.getVipExpireAt() + "到期，请及时续期");
                } catch (Exception e) {
                    log.error("发送VIP到期提醒失败: userId={}", user.getId(), e);
                }
            }
            log.info("发送VIP到期提醒完成，处理{}条", expiringVips.size());
        });
    }
}