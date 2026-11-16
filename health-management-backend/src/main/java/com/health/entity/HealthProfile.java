package com.health.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康档案实体，对应 health_profile 表
 */
@Data
@TableName("health_profile")
public class HealthProfile {

    private Long id;

    /** 用户 ID（始终来自 JWT 上下文，禁止前端传入） */
    @TableField("user_id")
    private Long userId;

    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;

    /** 出生日期 */
    private LocalDate birthday;

    /** 身高 cm */
    private BigDecimal height;

    /** 目标体重 kg */
    @TableField("target_weight")
    private BigDecimal targetWeight;

    /** 每日饮水目标 ml */
    @TableField("daily_water_goal")
    private Integer dailyWaterGoal;

    /** 每周运动目标 分钟 */
    @TableField("weekly_exercise_goal")
    private Integer weeklyExerciseGoal;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
