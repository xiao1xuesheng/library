package org.hejinggang.elibrary.common.enums;

/**
 * 对账差异类型枚举
 */
public enum MismatchType {
    LOCAL_EXTRA("本地有，支付平台无"),
    PLATFORM_EXTRA("本地无，支付平台有"),
    AMOUNT_MISMATCH("金额不一致"),
    STATUS_MISMATCH("状态不一致"),
    TIME_MISMATCH("支付时间差异过大");

    private final String description;

    MismatchType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}