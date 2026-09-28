package org.hejinggang.elibrary.common.enums;

/**
 * 支付渠道枚举
 */
public enum PayChannel {
    WECHAT("微信支付"),
    ALIPAY("支付宝"),
    UNIONPAY("银联支付");

    private final String description;

    PayChannel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}