package com.health.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人资料修改请求参数（昵称、邮箱）
 */
@Data
public class UserProfileDTO {

    @Size(max = 32, message = "昵称长度不能超过 32 位")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过 128 位")
    private String email;
}
