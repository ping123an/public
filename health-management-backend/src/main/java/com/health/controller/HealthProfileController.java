package com.health.controller;

import com.health.common.Result;
import com.health.dto.HealthProfileDTO;
import com.health.service.HealthProfileService;
import com.health.vo.HealthProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 健康档案接口。用户身份完全由 JWT 决定， URL 中不接收 userId。
 */
@Tag(name = "健康档案")
@RestController
@RequestMapping("/profile")
public class HealthProfileController {

    private final HealthProfileService profileService;

    public HealthProfileController(HealthProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "获取当前用户健康档案")
    @GetMapping
    public Result<HealthProfileVO> get() {
        return Result.ok(profileService.getCurrentProfile());
    }

    @Operation(summary = "保存当前用户健康档案（新增或更新）")
    @PutMapping
    public Result<HealthProfileVO> save(@Valid @RequestBody HealthProfileDTO dto) {
        return Result.ok(profileService.save(dto));
    }
}
