package com.health.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 今日健康数据汇总。
 */
@Data
public class TodaySummaryVO {

    /** 今日最新体重 kg（无记录取最近一次体重） */
    private BigDecimal weight;

    /** 今日运动总时长（分钟） */
    private Integer sportDuration;

    /** 今日运动总热量（千卡） */
    private Integer sportCalories;

    /** 今日运动次数 */
    private Integer sportCount;

    /** 今日饮水总量 ml */
    private Integer waterAmount;

    /** 每日饮水目标 ml */
    private Integer waterGoal;

    /** 今日饮水完成率 % */
    private Integer waterCompletionRate;

    /** 今日饮水是否达标 */
    private Boolean waterReached;

    /** 今日是否有任意记录 */
    private Boolean hasRecord;
}
