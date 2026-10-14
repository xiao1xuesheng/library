package org.hejinggang.elibrary.service.pricing;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 定价策略工厂
 * 通过 Spring 自动注入所有 PricingStrategy 实现，按 accessType 构建路由表
 */
@Component
public class PricingStrategyFactory {

    private final Map<String, PricingStrategy> strategyMap;

    public PricingStrategyFactory(List<PricingStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        PricingStrategy::getSupportedAccessType,
                        Function.identity()
                ));
    }

    /**
     * 根据访问类型获取对应的定价策略
     * @param accessType 访问类型（如 "PUBLIC_FREE"、"VIP_FREE"、"PAID"）
     * @return 对应的定价策略实现
     * @throws IllegalArgumentException 如果找不到支持的策略
     */
    public PricingStrategy getStrategy(String accessType) {
        PricingStrategy strategy = strategyMap.get(accessType);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的访问类型: " + accessType);
        }
        return strategy;
    }
}