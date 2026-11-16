package com.health.vo;

import lombok.Data;

/**
 * 登录响应对象：JWT Token + 用户信息
 */
@Data
public class LoginVO {

    /** 认证 Token（使用时需加 Bearer 前缀） */
    private String token;

    /** 用户信息 */
    private UserVO userInfo;
}
