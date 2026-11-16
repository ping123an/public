package com.health.controller;

import com.health.common.PageVO;
import com.health.common.Result;
import com.health.dto.RecordQueryDTO;
import com.health.dto.SportRecordDTO;
import com.health.dto.SportStatsQueryDTO;
import com.health.service.SportRecordService;
import com.health.vo.SportRecordVO;
import com.health.vo.SportStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 运动记录接口。用户身份完全由 JWT 决定。
 */
@Tag(name = "运动记录")
@RestController
@RequestMapping("/records/sport")
public class SportRecordController {

    private final SportRecordService service;

    public SportRecordController(SportRecordService service) {
        this.service = service;
    }

    @Operation(summary = "新增运动记录")
    @PostMapping
    public Result<SportRecordVO> create(@Valid @RequestBody SportRecordDTO dto) {
        return Result.ok(service.create(dto));
    }

    @Operation(summary = "修改运动记录")
    @PutMapping("/{id}")
    public Result<SportRecordVO> update(@PathVariable Long id, @Valid @RequestBody SportRecordDTO dto) {
        return Result.ok(service.update(id, dto));
    }

    @Operation(summary = "删除运动记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }

    @Operation(summary = "查看运动记录详情")
    @GetMapping("/{id}")
    public Result<SportRecordVO> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @Operation(summary = "分页查询运动记录（支持日期区间）")
    @GetMapping
    public Result<PageVO<SportRecordVO>> page(@Valid RecordQueryDTO query) {
        return Result.ok(service.page(query));
    }

    @Operation(summary = "运动统计（每日明细 + 区间汇总）")
    @GetMapping("/stats")
    public Result<SportStatsVO> stats(@Valid SportStatsQueryDTO query) {
        return Result.ok(service.stats(query));
    }
}
