package org.hejinggang.elibrary.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 借阅配置
 * 优先使用Nacos配置，若Nacos不可用则使用本地配置兜底
 */
@Component
public class BorrowConfig {

    private static final Logger log = LoggerFactory.getLogger(BorrowConfig.class);

    private final NacosConfigManager nacosConfigManager;

    public BorrowConfig(NacosConfigManager nacosConfigManager) {
        this.nacosConfigManager = nacosConfigManager;
    }

    public int getMaxLimitFree() {
        return nacosConfigManager.getIntConfig("elibrary.borrow.max-limit-free", 3);
    }

    public int getMaxLimitVip() {
        return nacosConfigManager.getIntConfig("elibrary.borrow.max-limit-vip", 10);
    }

    public int getDurationFree() {
        return nacosConfigManager.getIntConfig("elibrary.borrow.duration-free", 7);
    }

    public int getDurationVip() {
        return nacosConfigManager.getIntConfig("elibrary.borrow.duration-vip", 30);
    }

    public BigDecimal getDailyPriceDefault() {
        return nacosConfigManager.getBigDecimalConfig("elibrary.borrow.daily-price-default", "0.00");
    }

    public BigDecimal getVipDiscountDefault() {
        return nacosConfigManager.getBigDecimalConfig("elibrary.borrow.vip-discount-default", "1.00");
    }

    public int getPreviewPages() {
        return nacosConfigManager.getIntConfig("elibrary.borrow.preview-pages", 10);
    }

    public double getPreviewPercent() {
        String value = nacosConfigManager.getConfig("elibrary.borrow.preview-percent", "0.05");
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.05;
        }
    }
}