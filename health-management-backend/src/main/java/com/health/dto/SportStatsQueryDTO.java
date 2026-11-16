package com.health.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 运动统计查询参数。days 优先，其次自定义日期区间，都不传则查全部。
 */
@Data
public class SportStatsQueryDTO {

    /** 近 N 天：7/30/90 */
    private Integer days;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
