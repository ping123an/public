package com.health.service;

import com.health.dto.AvatarDTO;
import com.health.dto.LoginDTO;
import com.health.dto.PasswordUpdateDTO;
import com.health.dto.RegisterDTO;
import com.health.dto.UserProfileDTO;
import com.health.entity.User;
import com.health.vo.LoginVO;
import com.health.vo.UserVO;

/**
 * 用户业务逻辑层
 */
public interface UserService {

    /** 用户注册 */
    void register(RegisterDTO dto);

    /** 用户登录，返回 Token 与用户信息 */
    LoginVO login(LoginDTO dto);

    /** 根据 ID 查询用户 */
    User getById(Long id);

    /** 查询用户公开信息（VO） */
    UserVO getUserVO(Long id);

    /** 修改密码 */
    void updatePassword(Long userId, PasswordUpdateDTO dto);

    /** 修改个人资料（昵称、邮箱），返回最新用户信息 */
    UserVO updateProfile(Long userId, UserProfileDTO dto);

    /** 设置头像，返回最新用户信息 */
    UserVO updateAvatar(Long userId, AvatarDTO dto);

    /** 清除头像，返回最新用户信息 */
    UserVO clearAvatar(Long userId);
}
