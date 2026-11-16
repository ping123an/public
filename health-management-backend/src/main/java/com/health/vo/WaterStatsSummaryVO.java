package com.health.vo;

import lombok.Data;

/**
 * 饮水统计区间汇总。
 */
@Data
public class WaterStatsSummaryVO {

    /** 区间饮水总量 ml */
    private Integer totalAmount;

    /** 有记录的天数 */
    private Integer activeDays;

    /** 日均饮水量 ml（总量/有记录天数，保留 0 位） */
    private Integer avgAmount;

    /** 达标天数（当天总量 >= 目标） */
    private Integer reachedDays;

    /** 当前每日目标 ml */
    private Integer dailyGoal;
}
