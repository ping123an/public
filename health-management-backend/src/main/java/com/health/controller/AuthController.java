package com.health.controller;

import com.health.common.Result;
import com.health.common.UserContext;
import com.health.dto.AvatarDTO;
import com.health.dto.LoginDTO;
import com.health.dto.PasswordUpdateDTO;
import com.health.dto.RegisterDTO;
import com.health.dto.UserProfileDTO;
import com.health.service.UserService;
import com.health.vo.LoginVO;
import com.health.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 身份认证与个人账户接口
 */
@Tag(name = "认证与账户")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.ok();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserVO> currentUser() {
        return Result.ok(userService.getUserVO(UserContext.getUserId()));
    }

    @Operation(summary = "修改个人资料（昵称、邮箱）")
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody UserProfileDTO dto) {
        return Result.ok(userService.updateProfile(UserContext.getUserId(), dto));
    }

    @Operation(summary = "设置头像（base64 DataURL）")
    @PutMapping("/avatar")
    public Result<UserVO> updateAvatar(@Valid @RequestBody AvatarDTO dto) {
        return Result.ok(userService.updateAvatar(UserContext.getUserId(), dto));
    }

    @Operation(summary = "清除头像")
    @DeleteMapping("/avatar")
    public Result<UserVO> clearAvatar() {
        return Result.ok(userService.clearAvatar(UserContext.getUserId()));
    }

    @Operation(summary = "修改登录密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        userService.updatePassword(UserContext.getUserId(), dto);
        return Result.ok();
    }

    @Operation(summary = "退出登录（JWT 无状态，由前端清除 Token）")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT 无状态，服务端无需操作；前端清除本地 Token 即可
        return Result.ok();
    }
}
