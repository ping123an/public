package com.health.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 某一天的饮水统计。
 */
@Data
public class WaterDailyStatsVO {

    /** 日期 */
    private LocalDate date;

    /** 当天饮水总量 ml */
    private Integer totalAmount;

    /** 当天记录次数 */
    private Integer count;

    /** 当天目标完成率 %（总量/目标*100，保留 1 位小数） */
    private Integer completionRate;

    /** 当天是否达标 */
    private Boolean reached;
}
