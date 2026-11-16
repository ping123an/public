package com.health.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 某一天的运动统计。
 */
@Data
public class SportDailyStatsVO {

    /** 日期 */
    private LocalDate date;

    /** 当天运动总时长（分钟） */
    private Integer totalDuration;

    /** 当天消耗总热量（千卡） */
    private Integer totalCalories;

    /** 当天运动记录条数 */
    private Integer count;

    /** 当天涉及的运动类型（去重） */
    private List<String> sportTypes;
}
