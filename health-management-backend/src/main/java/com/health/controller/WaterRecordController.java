package com.health.controller;

import com.health.common.PageVO;
import com.health.common.Result;
import com.health.dto.RecordQueryDTO;
import com.health.dto.WaterGoalDTO;
import com.health.dto.WaterRecordDTO;
import com.health.dto.WaterStatsQueryDTO;
import com.health.service.WaterRecordService;
import com.health.vo.WaterRecordVO;
import com.health.vo.WaterStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 饮水记录接口。用户身份完全由 JWT 决定。
 */
@Tag(name = "饮水记录")
@RestController
@RequestMapping("/records/water")
public class WaterRecordController {

    private final WaterRecordService service;

    public WaterRecordController(WaterRecordService service) {
        this.service = service;
    }

    @Operation(summary = "新增饮水记录")
    @PostMapping
    public Result<WaterRecordVO> create(@Valid @RequestBody WaterRecordDTO dto) {
        return Result.ok(service.create(dto));
    }

    @Operation(summary = "修改饮水记录")
    @PutMapping("/{id}")
    public Result<WaterRecordVO> update(@PathVariable Long id, @Valid @RequestBody WaterRecordDTO dto) {
        return Result.ok(service.update(id, dto));
    }

    @Operation(summary = "删除饮水记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }

    @Operation(summary = "查看饮水记录详情")
    @GetMapping("/{id}")
    public Result<WaterRecordVO> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @Operation(summary = "分页查询饮水记录（支持日期区间）")
    @GetMapping
    public Result<PageVO<WaterRecordVO>> page(@Valid RecordQueryDTO query) {
        return Result.ok(service.page(query));
    }

    @Operation(summary = "设置每日饮水目标")
    @PutMapping("/goal")
    public Result<Integer> setGoal(@Valid @RequestBody WaterGoalDTO dto) {
        return Result.ok(service.setGoal(dto));
    }

    @Operation(summary = "饮水统计（每日明细 + 目标完成率 + 汇总）")
    @GetMapping("/stats")
    public Result<WaterStatsVO> stats(@Valid WaterStatsQueryDTO query) {
        return Result.ok(service.stats(query));
    }
}
