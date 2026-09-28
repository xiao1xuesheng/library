package org.hejinggang.elibrary.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类（开发环境Mock实现）
 * 用于解决分布式环境下定时任务重复执行的问题
 * 开发环境使用内存Map模拟，生产环境应使用Redis
 */
@Component
public class DistributedLockUtil {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockUtil.class);

    private static final String LOCK_PREFIX = "distributed:lock:";
    private static final String LOCK_VALUE = "LOCKED";

    // 开发环境使用内存Map模拟Redis
    private final ConcurrentHashMap<String, Long> locks = new ConcurrentHashMap<>();

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的key
     * @param expireTime 锁的过期时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long expireTime) {
        String fullKey = LOCK_PREFIX + lockKey;
        long expireAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireTime);
        
        Long existingExpire = locks.putIfAbsent(fullKey, expireAt);
        if (existingExpire == null) {
            log.debug("获取分布式锁成功: {}", fullKey);
            return true;
        }
        
        // 检查锁是否已过期
        if (existingExpire < System.currentTimeMillis()) {
            locks.remove(fullKey, existingExpire);
            Long newExpire = locks.putIfAbsent(fullKey, expireAt);
            if (newExpire == null) {
                log.debug("获取分布式锁成功（锁已过期）: {}", fullKey);
                return true;
            }
        }
        
        log.info("未获取到分布式锁: {}", fullKey);
        return false;
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁的key
     */
    public void releaseLock(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;
        locks.remove(fullKey);
        log.debug("释放分布式锁: {}", fullKey);
    }

    /**
     * 执行带分布式锁的任务
     * @param lockKey 锁的key
     * @param expireTime 锁的过期时间（秒）
     * @param task 任务逻辑
     * @return 是否执行成功（获取到锁并执行）
     */
    public boolean executeWithLock(String lockKey, long expireTime, Runnable task) {
        if (tryLock(lockKey, expireTime)) {
            try {
                task.run();
                return true;
            } finally {
                releaseLock(lockKey);
            }
        } else {
            log.info("未获取到分布式锁，跳过执行: {}", lockKey);
            return false;
        }
    }
}