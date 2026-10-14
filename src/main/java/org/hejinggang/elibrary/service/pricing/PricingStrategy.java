package org.hejinggang.elibrary.service.pricing;

import org.hejinggang.elibrary.model.entity.BookAccessConfig;
import org.hejinggang.elibrary.model.entity.User;

import java.math.BigDecimal;

/**
 * 借阅定价策略接口
 * 每种访问类型对应一个实现，新增访问类型只需新增实现类并注册到 Spring 容器
 */
public interface PricingStrategy {

    /**
     * 获取策略支持的访问类型
     * @return accessType 枚举值，如 "PUBLIC_FREE"、"VIP_FREE"、"PAID"
     */
    String getSupportedAccessType();

    /**
     * 计算日借阅成本
     * @param config 书籍访问配置
     * @param user   借阅用户
     * @return 日成本（元/天）
     */
    BigDecimal calculateDailyCost(BookAccessConfig config, User user);

    /**
     * 计算续借天数
     * @param config 书籍访问配置
     * @param user   借阅用户
     * @return 续借天数
     */
    int calculateRenewDays(BookAccessConfig config, User user);
}