package com.health.controller;

import com.health.common.Result;
import com.health.dto.OverviewQueryDTO;
import com.health.service.StatisticsService;
import com.health.vo.StatisticsOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计中心聚合接口。用户身份完全由 JWT 决定。
 */
@Tag(name = "统计中心")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService service;

    public StatisticsController(StatisticsService service) {
        this.service = service;
    }

    @Operation(summary = "健康数据聚合概览（今日/近7天/近30天/目标完成/图表数据）")
    @GetMapping("/overview")
    public Result<StatisticsOverviewVO> overview(OverviewQueryDTO query) {
        return Result.ok(service.overview(query));
    }
}
