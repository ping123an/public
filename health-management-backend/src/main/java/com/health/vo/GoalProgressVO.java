package com.health.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 健康目标完成情况。
 */
@Data
public class GoalProgressVO {

    /** 目标体重 kg */
    private BigDecimal targetWeight;

    /** 当前体重 kg */
    private BigDecimal currentWeight;

    /** 距目标体重差值 kg（正=超出，负=未达） */
    private BigDecimal weightDiff;

    /** 体重目标是否达成 */
    private Boolean weightReached;

    /** 每日饮水目标 ml */
    private Integer waterGoal;

    /** 今日饮水量 ml */
    private Integer todayWater;

    /** 饮水目标完成率 % */
    private Integer waterRate;

    /** 饮水目标是否达成 */
    private Boolean waterReached;

    /** 每周运动目标（分钟） */
    private Integer weeklyExerciseGoal;

    /** 近7天运动总时长（分钟） */
    private Integer weekSportDuration;

    /** 运动目标完成率 % */
    private Integer sportRate;

    /** 运动目标是否达成 */
    private Boolean sportReached;
}
