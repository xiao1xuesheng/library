package org.hejinggang.elibrary.service.pricing;

import org.hejinggang.elibrary.model.entity.BookAccessConfig;
import org.hejinggang.elibrary.model.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 公开免费策略：所有用户免费借阅
 */
@Component
public class PublicFreePricingStrategy implements PricingStrategy {

    @Override
    public String getSupportedAccessType() {
        return "PUBLIC_FREE";
    }

    @Override
    public BigDecimal calculateDailyCost(BookAccessConfig config, User user) {
        return BigDecimal.ZERO;
    }

    @Override
    public int calculateRenewDays(BookAccessConfig config, User user) {
        return config.getRenewDaysFree();
    }
}