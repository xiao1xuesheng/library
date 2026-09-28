package org.hejinggang.elibrary.common.enums;

/**
 * 对账处理状态枚举
 */
public enum HandleStatus {
    PENDING("待处理"),
    RESOLVED("已解决"),
    IGNORED("已忽略");

    private final String description;

    HandleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}