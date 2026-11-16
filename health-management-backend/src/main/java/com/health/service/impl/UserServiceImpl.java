package com.health.service.impl;

import com.health.dto.AvatarDTO;
import com.health.dto.LoginDTO;
import com.health.dto.PasswordUpdateDTO;
import com.health.dto.RegisterDTO;
import com.health.dto.UserProfileDTO;
import com.health.entity.User;
import com.health.exception.BusinessException;
import com.health.mapper.UserMapper;
import com.health.service.UserService;
import com.health.utils.JwtUtil;
import com.health.vo.LoginVO;
import com.health.vo.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务逻辑实现：注册、登录、密码管理
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(RegisterDTO dto) {
        // 账号唯一性校验
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException(400, "账号已存在，请更换账号");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        // 密码 BCrypt 加密存储，绝不保存明文
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null && !dto.getNickname().isBlank()
                ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "账号或密码错误");
        }
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generateToken(user.getId()));
        vo.setUserInfo(toUserVO(user));
        return vo;
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    @Override
    public UserVO getUserVO(Long id) {
        return toUserVO(getById(id));
    }

    @Override
    public void updatePassword(Long userId, PasswordUpdateDTO dto) {
        User user = getById(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public UserVO updateProfile(Long userId, UserProfileDTO dto) {
        User user = getById(userId);
        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.setNickname(dto.getNickname().trim());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail().trim().isEmpty() ? null : dto.getEmail().trim());
        }
        userMapper.updateById(user);
        return toUserVO(getById(userId));
    }

    @Override
    public UserVO updateAvatar(Long userId, AvatarDTO dto) {
        User user = getById(userId);
        user.setAvatar(dto.getAvatar());
        userMapper.updateById(user);
        return toUserVO(getById(userId));
    }

    @Override
    public UserVO clearAvatar(Long userId) {
        // updateById 默认忽略 null 字段，需用 UpdateWrapper 显式置空
        userMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getAvatar, null));
        return toUserVO(getById(userId));
    }

    /** 实体转 VO，剔除密码字段 */
    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        return vo;
    }
}
