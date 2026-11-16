package com.health.common;

import com.health.exception.BusinessException;

/**
 * 当前登录用户上下文，基于 ThreadLocal。
 * JWT 拦截器在校验通过后写入 userId，业务层从这里获取，
 * 从根本上避免通过前端传入 userId 决定数据归属。
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        CURRENT_USER.set(userId);
    }

    /**
     * 获取当前登录用户 ID
     *
     * @throws BusinessException 未登录或上下文缺失时抛出 401
     */
    public static Long getUserId() {
        Long userId = CURRENT_USER.get();
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return userId;
    }

    /** 仅用于判断是否存在，不抛异常 */
    public static Long getUserIdOrNull() {
        return CURRENT_USER.get();
    }

    /** 请求结束后清理，防止内存泄漏 */
    public static void clear() {
        CURRENT_USER.remove();
    }
}
