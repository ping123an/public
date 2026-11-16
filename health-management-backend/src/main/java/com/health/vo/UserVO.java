package com.health.vo;

import lombok.Data;

/**
 * 用户信息响应对象（不含密码等敏感字段）
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String avatar;
}
