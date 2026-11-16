package com.health.controller;

import com.health.common.PageVO;
import com.health.common.Result;
import com.health.dto.RecordQueryDTO;
import com.health.dto.WeightRecordDTO;
import com.health.dto.WeightTrendQueryDTO;
import com.health.service.WeightRecordService;
import com.health.vo.WeightRecordVO;
import com.health.vo.WeightTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 体重记录接口。用户身份完全由 JWT 决定，URL 中不接收 userId。
 */
@Tag(name = "体重记录")
@RestController
@RequestMapping("/records/weight")
public class WeightRecordController {

    private final WeightRecordService service;

    public WeightRecordController(WeightRecordService service) {
        this.service = service;
    }

    @Operation(summary = "新增体重记录")
    @PostMapping
    public Result<WeightRecordVO> create(@Valid @RequestBody WeightRecordDTO dto) {
        return Result.ok(service.create(dto));
    }

    @Operation(summary = "修改体重记录")
    @PutMapping("/{id}")
    public Result<WeightRecordVO> update(@PathVariable Long id, @Valid @RequestBody WeightRecordDTO dto) {
        return Result.ok(service.update(id, dto));
    }

    @Operation(summary = "删除体重记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }

    @Operation(summary = "查看体重记录详情")
    @GetMapping("/{id}")
    public Result<WeightRecordVO> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @Operation(summary = "分页查询体重记录（支持日期区间）")
    @GetMapping
    public Result<PageVO<WeightRecordVO>> page(@Valid RecordQueryDTO query) {
        return Result.ok(service.page(query));
    }

    @Operation(summary = "体重趋势分析（按日期聚合 + 变化统计）")
    @GetMapping("/trend")
    public Result<WeightTrendVO> trend(@Valid WeightTrendQueryDTO query) {
        return Result.ok(service.trend(query));
    }
}
