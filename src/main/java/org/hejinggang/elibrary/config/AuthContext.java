package org.hejinggang.elibrary.config;

import org.slf4j.MDC;

public class AuthContext {

    public static Long getCurrentUserId() {
        String userId = MDC.get("userId");
        if (userId == null || userId.isEmpty()) {
            throw new SecurityException("未获取到用户身份，请先鉴权");
        }
        return Long.parseLong(userId);
    }

    public static String getCurrentUserType() {
        return MDC.get("userType");
    }

    public static Long getCurrentAdminId() {
        String adminId = MDC.get("adminId");
        if (adminId == null || adminId.isEmpty()) {
            throw new SecurityException("未获取到管理员身份，请先鉴权");
        }
        return Long.parseLong(adminId);
    }

    public static String getCurrentAdminRole() {
        return MDC.get("adminRole");
    }

    public static String getTraceId() {
        return MDC.get("traceId");
    }
}