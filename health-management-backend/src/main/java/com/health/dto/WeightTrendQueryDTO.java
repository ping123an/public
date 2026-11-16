package com.health.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 体重趋势查询参数。
 * days 与 startDate/endDate 二选一：days 优先（近 N 天），否则用自定义日期区间，都不传则查全部。
 */
@Data
public class WeightTrendQueryDTO {

    /** 近 N 天：7/30/90；传 0 或不传结合日期区间使用 */
    private Integer days;

    /** 起始日期（含） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 结束日期（含） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
