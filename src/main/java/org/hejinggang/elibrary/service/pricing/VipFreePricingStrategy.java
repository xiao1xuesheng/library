package org.hejinggang.elibrary.service.pricing;

import org.hejinggang.elibrary.common.enums.UserType;
import org.hejinggang.elibrary.model.entity.BookAccessConfig;
import org.hejinggang.elibrary.model.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * VIP免费策略：VIP用户免费，普通用户按原价
 */
@Component
public class VipFreePricingStrategy implements PricingStrategy {

    @Override
    public String getSupportedAccessType() {
        return "VIP_FREE";
    }

    @Override
    public BigDecimal calculateDailyCost(BookAccessConfig config, User user) {
        if (user.getUserType() == UserType.VIP) {
            return BigDecimal.ZERO;
        }
        return config.getDailyPrice();
    }

    @Override
    public int calculateRenewDays(BookAccessConfig config, User user) {
        if (user.getUserType() == UserType.VIP) {
            return config.getRenewDaysVip();
        }
        return config.getRenewDaysFree();
    }
}