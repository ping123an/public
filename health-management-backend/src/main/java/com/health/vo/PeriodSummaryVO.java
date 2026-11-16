package com.health.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 某个时间周期（近7天/近30天）的健康数据汇总。
 */
@Data
public class PeriodSummaryVO {

    /** 周期标签，如"近7天" */
    private String label;

    /** 天数 */
    private Integer days;

    /** 周期内体重变化 kg（最新 - 最早） */
    private BigDecimal weightChange;

    /** 周期内最新体重 kg */
    private BigDecimal latestWeight;

    /** 周期内运动总时长（分钟） */
    private Integer sportDuration;

    /** 周期内运动总热量（千卡） */
    private Integer sportCalories;

    /** 周期内运动次数 */
    private Integer sportCount;

    /** 周期内饮水总量 ml */
    private Integer waterAmount;

    /** 饮水达标天数 */
    private Integer waterReachedDays;

    /** 有任意记录的天数 */
    private Integer activeDays;
}
