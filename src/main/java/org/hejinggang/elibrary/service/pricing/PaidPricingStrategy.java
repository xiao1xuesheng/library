package org.hejinggang.elibrary.service.pricing;

import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.model.entity.BookAccessConfig;
import org.hejinggang.elibrary.model.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 付费策略：普通用户原价，VIP享受折扣
 */
@Component
public class PaidPricingStrategy implements PricingStrategy {

    @Override
    public String getSupportedAccessType() {
        return "PAID";
    }

    @Override
    public BigDecimal calculateDailyCost(BookAccessConfig config, User user) {
        BigDecimal basePrice = config.getDailyPrice();
        if (user.getUserType() == UserType.VIP) {
            return basePrice.multiply(config.getVipDiscount());
        }
        return basePrice;
    }

    @Override
    public int calculateRenewDays(BookAccessConfig config, User user) {
        if (user.getUserType() == UserType.VIP) {
            return config.getRenewDaysVip();
        }
        return config.getRenewDaysFree();
    }
}