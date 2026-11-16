package com.health.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 运动统计区间汇总。
 */
@Data
public class SportStatsSummaryVO {

    /** 总运动时长（分钟） */
    private Integer totalDuration;

    /** 总消耗热量（千卡） */
    private Integer totalCalories;

    /** 运动记录总条数 */
    private Integer totalCount;

    /** 有运动的天数 */
    private Integer activeDays;

    /** 平均每日运动时长（分钟，保留 1 位小数） */
    private BigDecimal avgDuration;

    /** 最常进行的运动类型（出现次数最多） */
    private String topSportType;
}
