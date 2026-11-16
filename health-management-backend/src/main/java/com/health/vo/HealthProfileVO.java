package com.health.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康档案响应对象
 */
@Data
public class HealthProfileVO {

    private Long id;
    private Integer gender;
    private LocalDate birthday;
    private BigDecimal height;
    /** 目标体重 kg */
    private BigDecimal targetWeight;
    /** 每日饮水目标 ml */
    private Integer dailyWaterGoal;
    /** 每周运动目标 分钟 */
    private Integer weeklyExerciseGoal;
    private LocalDateTime updateTime;
}
