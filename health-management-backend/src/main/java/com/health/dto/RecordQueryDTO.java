package com.health.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 健康记录通用分页查询参数：页码、每页条数、可选日期区间。
 * 三类记录（体重/运动/饮水）共用。
 */
@Data
public class RecordQueryDTO {

    /** 当前页码，从 1 开始 */
    @Min(value = 1, message = "页码必须大于等于 1")
    private Integer current = 1;

    /** 每页条数，最大 100 */
    @Min(value = 1, message = "每页条数必须大于等于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Integer size = 10;

    /** 起始日期（含），格式 yyyy-MM-dd */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 结束日期（含），格式 yyyy-MM-dd */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
